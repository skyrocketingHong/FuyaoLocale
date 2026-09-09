#!/usr/bin/env python3
"""Import a fixed Android resource closure; original bitmaps remain byte-identical."""
from __future__ import annotations

import argparse
import base64
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import re
import tarfile
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_REFERENCE = re.compile(r"@(?:android:)?(drawable|color|dimen|integer|bool|anim|interpolator)/([a-zA-Z0-9_]+)")
SAFE_NAME = re.compile(r"[a-z][a-z0-9_]*\Z")


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def xml(data: bytes) -> ET.Element:
    # ASVS 1.5.2: imported reference XML never resolves DTDs or external entities.
    if b"<!DOCTYPE" in data.upper() or b"<!ENTITY" in data.upper():
        raise ValueError("DTD/entity declarations are not accepted")
    return ET.fromstring(data)


def archive_files(archive: Path, spec: dict) -> dict[str, bytes]:
    if digest(archive.read_bytes()) != spec["archiveSha256"]:
        raise ValueError("Archive checksum differs from the pinned source")
    result = {}

    def accept(name: str, size: int, read):
        # ASVS 5.3.2/5.3.3: archive names cannot control paths outside the import root.
        path = PurePosixPath(name)
        if path.is_absolute() or ".." in path.parts or size > 10_000_000:
            return
        source_prefix = spec.get("resourcePrefix", "")
        if not name.startswith(source_prefix):
            return
        rel = name.removeprefix(source_prefix)
        if rel and len(PurePosixPath(rel).parts) == 2:
            result[rel] = read()

    if archive.suffix == ".zip":
        with zipfile.ZipFile(archive) as z:
            for member in z.infolist():
                if not member.is_dir():
                    accept(member.filename, member.file_size, lambda m=member: z.read(m))
    else:
        with tarfile.open(archive) as t:
            for member in t:
                if member.isfile():
                    accept(member.name, member.size, lambda m=member: t.extractfile(m).read())
    return result


def import_resources(era: str, archive_dir: Path, download: bool) -> dict:
    docs = ROOT / "docs/android-themes" / era
    spec = json.loads((docs / "import-spec.json").read_text())
    prefix = spec["prefix"]
    if not SAFE_NAME.fullmatch(prefix):
        raise ValueError("Invalid resource prefix")
    archive = archive_dir / spec["archiveName"]
    if not archive.exists():
        if not download:
            raise FileNotFoundError(f"Missing {archive}; use --download to fetch the pinned archive")
        url = spec["archiveUrl"]
        # ASVS 12.2.1: fixed official HTTPS origins, with standard certificate checks.
        if not url.startswith(("https://android.googlesource.com/platform/", "https://dl.google.com/android/repository/")):
            raise ValueError("Unsupported source origin")
        archive.parent.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(url, timeout=60) as response:
            data = response.read(120_000_001)
        if len(data) > 120_000_000 or digest(data) != spec["archiveSha256"]:
            raise ValueError("Downloaded archive size/checksum mismatch")
        archive.write_bytes(data)
    source = archive_files(archive, spec)
    files_by_resource = {}
    values_by_resource = {}
    for path, data in source.items():
        folder, name = path.split("/")
        kind = folder.split("-")[0]
        if kind in {"drawable", "color", "anim", "interpolator"}:
            stem = name.split(".")[0]
            files_by_resource.setdefault((kind, stem), []).append((path, data))
        elif kind == "values" and name.endswith(".xml"):
            for element in xml(data):
                # public.xml reserves framework IDs but does not define values.
                # Never let a declaration replace the value from colors/dimens.
                if element.tag not in {"color", "drawable", "dimen", "integer", "bool", "item"}:
                    continue
                item_kind = element.get("type", element.tag)
                item_name = element.get("name")
                if item_name:
                    values_by_resource.setdefault((item_kind, item_name), []).append((path, data, element))
    pending = [tuple(item.split("/", 1)) for item in spec["roots"]]
    visited = set()
    imports = {}
    value_elements = {}
    value_sources = {}
    while pending:
        key = pending.pop()
        if key in visited:
            continue
        visited.add(key)
        if key in files_by_resource:
            for path, data in files_by_resource[key]:
                imports[path] = data
                if path.endswith(".xml"):
                    xml(data)
                    pending.extend(RESOURCE_REFERENCE.findall(data.decode()))
        elif key in values_by_resource:
            for path, data, element in values_by_resource[key]:
                folder = path.split("/")[0]
                raw = ET.tostring(element, encoding="unicode")
                value_elements.setdefault(folder, {})[key] = raw
                value_sources[path] = data
                pending.extend(RESOURCE_REFERENCE.findall(raw))
        else:
            raise ValueError(f"Unresolved source resource: {key[0]}/{key[1]}")
    rows = []

    def rewrite(text: str) -> str:
        output = RESOURCE_REFERENCE.sub(lambda match: f"@{match[1]}/{prefix}{match[2]}", text)
        if spec.get("qualifyFrameworkAttributes"):
            private_attrs = spec.get("privateAttributes", [])
            if any(not re.fullmatch(r"[a-zA-Z][a-zA-Z0-9_]*", name) for name in private_attrs):
                raise ValueError("Invalid private attribute name")
            output = re.sub(r"\?attr/([a-zA-Z0-9_]+)",
                            lambda m: f"?attr/{prefix}{m[1]}" if m[1] in private_attrs else f"?android:attr/{m[1]}", output)
        if spec.get("prefixStateIds"):
            # Animated selectors use local on/off identities; layer IDs retain
            # their public framework identity for level-driven progress.
            output = re.sub(r"@\+?id/(on|off)\b", lambda m: "@+id/" + prefix + m[1], output)
            output = re.sub(r"@id/(background|progress|secondaryProgress)\b", r"@android:id/\1", output)
        return output

    for path, data in sorted(imports.items()):
        folder, name = path.split("/")
        target = ROOT / "app/src/main/res" / folder / (prefix + name)
        output = rewrite(data.decode()).encode() if name.endswith(".xml") else data
        adaptation = None
        if path in spec.get("sharedSelectorStateIds", []):
            element = xml(output)
            if element.tag != "animated-selector":
                raise ValueError("Shared state IDs are valid only for animated selectors")
            identities = [item.get("{http://schemas.android.com/apk/res/android}id") for item in element.findall("item")]
            identities = [identity for identity in identities if identity]
            if len(identities) == len(set(identities)):
                raise ValueError("No shared selector IDs found; suppression is not justified")
            # The AOSP switch deliberately gives disabled-off and normal-off the
            # same transition identity. Lint's findViewById rule is for layouts.
            output = output.decode().replace("<animated-selector ", '<animated-selector xmlns:tools="http://schemas.android.com/tools" tools:ignore="DuplicateIds" ', 1).encode()
        if path in spec.get("levelDrivenRotations", []):
            # AnimatedRotateDrawable's frame attributes are private in the public SDK.
            # Keep the original pixels/pivot and drive discrete RotateDrawable levels
            # from Compose; preserve the source frame cadence in the manifest.
            element = xml(output)
            if element.tag != "animated-rotate":
                raise ValueError("Rotation adaptation requires an animated-rotate root")
            android = "{http://schemas.android.com/apk/res/android}"
            adaptation = {"type": "level-driven rotation", "frames": int(element.attrib[android + "framesCount"]),
                          "frameDurationMillis": int(element.attrib[android + "frameDuration"])}
            content = output.decode().replace("<animated-rotate", '<rotate android:fromDegrees="0" android:toDegrees="360"')
            content = re.sub(r'\s+android:(framesCount|frameDuration)="[^\"]+"', "", content)
            content = content.replace("</animated-rotate>", "</rotate>")
            output = content.encode()
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(output)
        rows.append({"source": spec["sourcePathPrefix"] + path, "target": str(target.relative_to(ROOT)),
                     "sourceSha256": digest(data), "sha256": digest(output), "size": len(output),
                     "density": folder.removeprefix("drawable-"), "license": spec["license"],
                     "change": "resource references prefixed" if name.endswith(".xml") else "prefix rename only; bytes unchanged"})
        if adaptation:
            rows[-1]["adaptation"] = adaptation
            rows[-1]["change"] += "; public RotateDrawable with source-timed discrete levels"
        if name.endswith(".xml") and spec.get("qualifyFrameworkAttributes"):
            rows[-1]["change"] += "; framework attributes qualified; listed private attributes and selector state IDs scoped to the era"
        if path in spec.get("sharedSelectorStateIds", []):
            rows[-1]["change"] += "; local DuplicateIds lint exception for original shared off-state transition identity"
    for folder, elements in sorted(value_elements.items()):
        target = ROOT / "app/src/main/res" / folder / (prefix + "dependencies.xml")
        contents = []
        for (kind, name), raw in sorted(elements.items()):
            element = xml(rewrite(raw).encode())
            element.set("name", prefix + name)
            contents.append(ET.tostring(element, encoding="unicode"))
        output = ('<?xml version="1.0" encoding="utf-8"?>\n<!-- AOSP, Apache-2.0. See docs/android-themes/' + era + '/asset-manifest.json. -->\n<resources>\n' + "\n".join(contents) + '\n</resources>\n').encode()
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(output)
        rows.append({"sources": sorted(spec["sourcePathPrefix"] + p for p in value_sources if p.startswith(folder + "/")),
                     "target": str(target.relative_to(ROOT)), "sha256": digest(output), "size": len(output),
                     "license": spec["license"], "change": "selected dependency values; names and references prefixed"})
    for extra in spec.get("extraFiles", []):
        name = PurePosixPath(extra["source"]).name
        cached = archive_dir / era / "fonts" / name
        if "archivePath" in extra:
            member = PurePosixPath(extra["archivePath"])
            expected_parent = PurePosixPath(spec["resourcePrefix"]).parent / "fonts"
            if member.parent != expected_parent or member.name != name or member.is_absolute() or ".." in member.parts:
                raise ValueError("Invalid SDK font member")
            with zipfile.ZipFile(archive) as package:
                info = package.getinfo(str(member))
                if info.file_size > 2_000_000:
                    raise ValueError("SDK font exceeds the import limit")
                data = package.read(info)
        elif not cached.exists():
            if not download:
                raise FileNotFoundError(f"Missing {cached}; use --download to fetch the fixed font")
            if not extra["url"].startswith("https://android.googlesource.com/platform/"):
                raise ValueError("Unsupported font origin")
            with urllib.request.urlopen(extra["url"], timeout=60) as response:
                data = base64.b64decode(response.read(2_000_000), validate=True)
        else:
            data = cached.read_bytes()
        if digest(data) != extra["sha256"]:
            raise ValueError("Font checksum mismatch")
        target = ROOT / extra["target"]
        if target.parent != ROOT / "app/src/main/res/font" or not target.name.startswith(prefix):
            raise ValueError("Invalid font destination")
        cached.parent.mkdir(parents=True, exist_ok=True)
        cached.write_bytes(data)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        rows.append({**extra, "sourceSha256": digest(data), "size": len(data)})

    # Keep source evidence outside runtime resources; no application code depends on plans/.
    reference_rows = []
    reference_paths = set(value_sources) | {p for p in source if p.startswith("values") and p.endswith(("/themes.xml", "/styles.xml", "/dimens.xml", "/colors.xml", "/config.xml", "/themes_material.xml", "/styles_material.xml"))}
    reference_paths.update(spec.get("referenceFiles", []))
    reference_paths.update(spec.get("levelDrivenRotations", []))
    reference_paths.update(spec.get("sharedSelectorStateIds", []))
    for path in sorted(reference_paths):
        target = docs / "reference" / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(source[path])
        reference_rows.append({"source": spec["sourcePathPrefix"] + path, "target": str(target.relative_to(ROOT)), "sha256": digest(source[path])})
    manifest = {"source": {k: spec[k] for k in ("version", "archiveUrl", "archiveSha256", "license")},
                "files": rows, "references": reference_rows}
    (docs / "asset-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    return {"era": era, "resources": len(visited), "files": len(rows), "sourceReferences": len(reference_rows)}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--era", required=True, choices=("eclair", "froyo", "gingerbread", "honeycomb", "kitkat", "lollipop"))
    parser.add_argument("--archive-dir", type=Path, required=True)
    parser.add_argument("--download", action="store_true")
    args = parser.parse_args()
    print(json.dumps(import_resources(args.era, args.archive_dir, args.download), ensure_ascii=False))

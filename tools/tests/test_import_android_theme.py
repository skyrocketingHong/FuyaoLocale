"""Offline regression tests for the real resource-import boundary."""
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import tarfile
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("theme_importer", Path(__file__).resolve().parents[1] / "import_android_theme.py")
importer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(importer)


class ResourceImportTest(unittest.TestCase):
    def archive(self, root, files):
        archive = root / "source.tar.gz"
        with tarfile.open(archive, "w:gz") as output:
            for path, data in files.items():
                entry = tarfile.TarInfo(path)
                entry.size = len(data)
                output.addfile(entry, io.BytesIO(data))
        return archive

    def test_public_id_cannot_shadow_the_actual_color_value(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            archive = self.archive(root, {
                "values/colors.xml": b'<resources><color name="transparent">#00000000</color></resources>',
                "values/public.xml": b'<resources><public type="color" name="transparent" id="0x0106000d"/></resources>',
                "drawable/background.xml": b'<shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="@android:color/transparent"/></shape>',
            })
            docs = root / "docs/android-themes/kitkat"
            docs.mkdir(parents=True)
            (docs / "import-spec.json").write_text(json.dumps({
                "prefix": "kitkat_", "version": "fixture", "license": "Apache-2.0",
                "archiveName": archive.name, "archiveUrl": "https://android.googlesource.com/platform/fixture",
                "archiveSha256": hashlib.sha256(archive.read_bytes()).hexdigest(),
                "sourcePathPrefix": "core/res/res/", "roots": ["drawable/background"],
            }))
            with patch.object(importer, "ROOT", root):
                importer.import_resources("kitkat", root, False)
            values = (root / "app/src/main/res/values/kitkat_dependencies.xml").read_bytes()
            color = importer.xml(values).find("color")
            self.assertEqual("kitkat_transparent", color.attrib["name"])
            self.assertEqual("#00000000", color.text)
            self.assertNotIn(b"<public", values)

    def test_external_entities_are_rejected_before_parsing(self):
        with self.assertRaisesRegex(ValueError, "DTD/entity"):
            importer.xml(b'<!DOCTYPE resources [<!ENTITY ext SYSTEM "file:///private/file">]><resources>&ext;</resources>')

    def test_private_rotation_attributes_keep_source_cadence_and_original_evidence(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            original = b'<animated-rotate xmlns:android="http://schemas.android.com/apk/res/android" android:pivotX="50%" android:pivotY="50%" android:framesCount="12" android:frameDuration="100"/>'
            archive = self.archive(root, {"drawable/spinner.xml": original})
            docs = root / "docs/android-themes/eclair"
            docs.mkdir(parents=True)
            (docs / "import-spec.json").write_text(json.dumps({
                "prefix": "eclair_", "version": "fixture", "license": "Apache-2.0",
                "archiveName": archive.name, "archiveUrl": "https://android.googlesource.com/platform/fixture",
                "archiveSha256": hashlib.sha256(archive.read_bytes()).hexdigest(),
                "sourcePathPrefix": "core/res/res/", "roots": ["drawable/spinner"],
                "levelDrivenRotations": ["drawable/spinner.xml"],
            }))
            with patch.object(importer, "ROOT", root):
                importer.import_resources("eclair", root, False)
            output = importer.xml((root / "app/src/main/res/drawable/eclair_spinner.xml").read_bytes())
            self.assertEqual("rotate", output.tag)
            self.assertNotIn("{http://schemas.android.com/apk/res/android}framesCount", output.attrib)
            manifest = json.loads((docs / "asset-manifest.json").read_text())
            self.assertEqual({"type": "level-driven rotation", "frames": 12, "frameDurationMillis": 100}, manifest["files"][0]["adaptation"])
            self.assertEqual(original, (docs / "reference/drawable/spinner.xml").read_bytes())

    def test_archive_traversal_entries_never_enter_the_resource_index(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            archive = self.archive(root, {
                "../drawable/escape.xml": b"invalid",
                "/drawable/absolute.xml": b"invalid",
                "drawable/safe.xml": b"<shape/>",
            })
            files = importer.archive_files(archive, {"archiveSha256": hashlib.sha256(archive.read_bytes()).hexdigest()})
            self.assertEqual({"drawable/safe.xml": b"<shape/>"}, files)

    def test_framework_attributes_and_private_selector_ids_keep_distinct_namespaces(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            original = b'<animated-selector xmlns:android="http://schemas.android.com/apk/res/android"><item android:id="@+id/on"><shape android:tint="?attr/colorControlNormal" android:color="?attr/colorSwitchThumbNormal"/></item><transition android:fromId="@id/off" android:toId="@id/on"/></animated-selector>'
            archive = self.archive(root, {"drawable/switch.xml": original})
            docs = root / "docs/android-themes/lollipop"
            docs.mkdir(parents=True)
            (docs / "import-spec.json").write_text(json.dumps({
                "prefix": "lollipop_", "version": "fixture", "license": "Apache-2.0",
                "archiveName": archive.name, "archiveUrl": "https://android.googlesource.com/platform/fixture",
                "archiveSha256": hashlib.sha256(archive.read_bytes()).hexdigest(),
                "sourcePathPrefix": "core/res/res/", "roots": ["drawable/switch"],
                "qualifyFrameworkAttributes": True, "prefixStateIds": True,
                "privateAttributes": ["colorSwitchThumbNormal"],
            }))
            with patch.object(importer, "ROOT", root):
                importer.import_resources("lollipop", root, False)
            output = (root / "app/src/main/res/drawable/lollipop_switch.xml").read_text()
            self.assertIn("?android:attr/colorControlNormal", output)
            self.assertIn("?attr/lollipop_colorSwitchThumbNormal", output)
            self.assertNotIn("?android:attr/colorSwitchThumbNormal", output)
            self.assertIn("@+id/lollipop_off", output)
            self.assertIn("@+id/lollipop_on", output)

    def test_shared_disabled_and_normal_off_state_retains_original_transition_identity(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            original = b'<animated-selector xmlns:android="http://schemas.android.com/apk/res/android"><item android:state_enabled="false" android:id="@+id/off"/><item android:id="@+id/off"/></animated-selector>'
            archive = self.archive(root, {"drawable/switch.xml": original})
            docs = root / "docs/android-themes/lollipop"
            docs.mkdir(parents=True)
            (docs / "import-spec.json").write_text(json.dumps({
                "prefix": "lollipop_", "version": "fixture", "license": "Apache-2.0",
                "archiveName": archive.name, "archiveUrl": "https://android.googlesource.com/platform/fixture",
                "archiveSha256": hashlib.sha256(archive.read_bytes()).hexdigest(),
                "sourcePathPrefix": "core/res/res/", "roots": ["drawable/switch"],
                "prefixStateIds": True, "sharedSelectorStateIds": ["drawable/switch.xml"],
            }))
            with patch.object(importer, "ROOT", root):
                importer.import_resources("lollipop", root, False)
            output = importer.xml((root / "app/src/main/res/drawable/lollipop_switch.xml").read_bytes())
            self.assertEqual("DuplicateIds", output.get("{http://schemas.android.com/tools}ignore"))
            self.assertEqual(["@+id/lollipop_off"] * 2, [e.get("{http://schemas.android.com/apk/res/android}id") for e in output.findall("item")])
            self.assertEqual(original, (docs / "reference/drawable/switch.xml").read_bytes())


if __name__ == "__main__":
    unittest.main()

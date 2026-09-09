#!/usr/bin/env python3
"""Check the UI/renderer boundary without a device or a visual test runner."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/ing/fuyaoskyrocket/applocale/ui"


def main():
    errors = []
    for path in UI.rglob("*.kt"):
        relative = path.relative_to(UI)
        source = path.read_text()
        if relative.parts[0] == "designsystem":
            if re.search(r"import .*\.(?:main|appinfo|configurations|systemlanguages)\..*ViewModel", source):
                errors.append(f"{relative}: renderer owns a feature model")
            continue
        source = re.sub(r"/\*.*?\*/|//[^\n]*", "", source, flags=re.S)
        if re.search(r"AppUiTheme\.(?:style|components|policy\.(?:controls|navigation|picker|continuousLists|toggleUsesCheckbox|usesContextualActions))\b", source):
            errors.append(f"{relative}: feature decides theme presentation")
        if re.search(r"import (?:androidx\.compose\.material[3]?\.(?!(?:icons|adaptive)\.)|top\.yukonga\.miuix\.kmp\.(?:basic|theme|layout)\.)", source):
            errors.append(f"{relative}: feature imports native renderer controls")
    if errors:
        raise SystemExit("\n".join(errors))
    print("PASS: feature pages delegate theme presentation; renderers do not own feature models")


if __name__ == "__main__":
    main()

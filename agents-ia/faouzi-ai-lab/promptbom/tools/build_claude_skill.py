#!/usr/bin/env python3
"""Build a Claude-compatible PromptBOM Skill ZIP with exactly one SKILL.md."""
from __future__ import annotations
import argparse, shutil, tempfile, zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
WRAPPER = '#!/usr/bin/env python3\nfrom pathlib import Path\nimport sys\nHERE = Path(__file__).resolve().parent\nsys.path.insert(0, str(HERE / "lib"))\nfrom promptbom.cli import main\nif __name__ == "__main__":\n    raise SystemExit(main())\n'

def build(output: Path) -> Path:
    with tempfile.TemporaryDirectory() as td:
        stage = Path(td) / "promptbom"
        (stage / "scripts" / "lib").mkdir(parents=True)
        shutil.copy2(ROOT / "claude-skill" / "SKILL.md", stage / "SKILL.md")
        shutil.copytree(ROOT / "claude-skill" / "fixtures", stage / "fixtures")
        shutil.copy2(ROOT / "claude-skill" / "README.md", stage / "README.md")
        shutil.copytree(ROOT / "promptbom", stage / "scripts" / "lib" / "promptbom", ignore=shutil.ignore_patterns("__pycache__", "*.pyc"))
        (stage / "scripts" / "promptbom.py").write_text(WRAPPER, encoding="utf-8")
        output.parent.mkdir(parents=True, exist_ok=True)
        if output.exists(): output.unlink()
        with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as z:
            for p in stage.rglob("*"):
                if p.is_file(): z.write(p, p.relative_to(stage.parent))
    with zipfile.ZipFile(output) as z:
        skills=[n for n in z.namelist() if n.endswith("SKILL.md")]
        if skills != ["promptbom/SKILL.md"]:
            raise RuntimeError("Expected exactly promptbom/SKILL.md; found %r" % skills)
    return output

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument("--output", default=str(ROOT / "dist" / "promptbom-claude-skill-v0.2.1.zip"))
    args=ap.parse_args()
    print(build(Path(args.output).resolve()))
    return 0
if __name__ == "__main__": raise SystemExit(main())

import json
import tempfile
import unittest
from datetime import datetime, timezone
from pathlib import Path

from promptbom.cli import main
from promptbom.core import diff_against_lock, make_lock, scan
from promptbom.report import html_report, markdown_report
from promptbom.validate import validate_bom

FIXED_NOW = datetime(2026, 10, 10, tzinfo=timezone.utc)


class PromptBOMTests(unittest.TestCase):
    def project(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        (root / "AGENTS.md").write_text("Must keep tests green.\nNever send external messages.\n", encoding="utf-8")
        (root / "CLAUDE.md").write_text("Always keep tests green.\n", encoding="utf-8")
        return temp, root

    def test_scan_and_ecosystem(self):
        temp, root = self.project()
        try:
            bom = scan(root, now=FIXED_NOW)
            self.assertEqual(2, bom["summary"]["total"])
            ecosystems = {i["path"]: i["ecosystem"] for i in bom["items"]}
            self.assertEqual("cross-tool", ecosystems["AGENTS.md"])
            self.assertEqual("claude", ecosystems["CLAUDE.md"])
            self.assertEqual([], validate_bom(bom))
        finally:
            temp.cleanup()

    def test_lock_drift(self):
        temp, root = self.project()
        try:
            bom = scan(root, now=FIXED_NOW)
            lock = make_lock(bom)
            (root / "AGENTS.md").write_text("Must keep tests green.\nMust write docs.\n", encoding="utf-8")
            current = scan(root, now=FIXED_NOW)
            diff = diff_against_lock(lock, current)
            self.assertEqual(["AGENTS.md"], [i["path"] for i in diff["changed"]])
        finally:
            temp.cleanup()

    def test_secret_refuses_lock(self):
        temp, root = self.project()
        try:
            (root / "MEMORY.md").write_text('api_key = "sk-abcdefghijklmnopqrstuvwxyz123456"\n', encoding="utf-8")
            self.assertEqual(2, main(["lock", str(root)]))
        finally:
            temp.cleanup()

    def test_custom_rule(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        try:
            (root / "assistant").mkdir()
            (root / "assistant" / "guide.md").write_text("Must be concise.\n", encoding="utf-8")
            config = {"rules": [{"pattern": "assistant/**/*.md", "kind": "policy", "authority": "policy", "mutability": "versioned"}]}
            (root / "promptbom.config.json").write_text(json.dumps(config), encoding="utf-8")
            bom = scan(root, now=FIXED_NOW)
            self.assertEqual("policy", bom["items"][0]["kind"])
        finally:
            temp.cleanup()

    def test_conflict_heuristic(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        try:
            (root / "AGENTS.md").write_text("Never send external messages.\n", encoding="utf-8")
            (root / "MEMORY.md").write_text("Always send external messages.\n", encoding="utf-8")
            bom = scan(root, now=FIXED_NOW)
            self.assertEqual(1, bom["summary"]["possible_conflicts"])
            c = bom["conflicts"][0]
            self.assertEqual("AGENTS.md", c["stronger"]["path"])
        finally:
            temp.cleanup()

    def test_conflict_permission_vs_approval_fixture(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        try:
            (root / "CLAUDE.md").write_text("Never publish or deploy without explicit user approval.\n", encoding="utf-8")
            (root / "AGENTS.md").write_text("You may deploy automatically after tests pass.\n", encoding="utf-8")
            bom = scan(root, now=FIXED_NOW)
            self.assertGreaterEqual(bom["summary"]["possible_conflicts"], 1)
            conflict = bom["conflicts"][0]
            self.assertIn("deploy", conflict.get("shared_actions", []))
            self.assertEqual({"CLAUDE.md", "AGENTS.md"}, {conflict["stronger"]["path"], conflict["weaker"]["path"]})
        finally:
            temp.cleanup()

    def test_scan_no_write_is_truly_read_only(self):
        temp, root = self.project()
        try:
            before = sorted(p.relative_to(root).as_posix() for p in root.rglob("*") if p.is_file())
            self.assertEqual(0, main(["scan", str(root), "--no-write"]))
            after = sorted(p.relative_to(root).as_posix() for p in root.rglob("*") if p.is_file())
            self.assertEqual(before, after)
            self.assertFalse((root / "promptbom.json").exists())
        finally:
            temp.cleanup()

    def test_reports(self):
        temp, root = self.project()
        try:
            bom = scan(root, now=FIXED_NOW)
            md = markdown_report(bom)
            html = html_report(bom)
            self.assertIn("# PromptBOM Report", md)
            self.assertIn("<!doctype html>", html)
            self.assertIn("AGENTS.md", html)
        finally:
            temp.cleanup()

    def test_cli_scan_validate_report_and_verify(self):
        temp, root = self.project()
        try:
            self.assertEqual(0, main(["scan", str(root)]))
            self.assertEqual(0, main(["validate", str(root / "promptbom.json")]))
            self.assertEqual(0, main(["lock", str(root)]))
            self.assertEqual(0, main(["verify", str(root)]))
            self.assertEqual(0, main(["report", str(root), "--format", "html"]))
            self.assertTrue((root / "promptbom-report.html").exists())
        finally:
            temp.cleanup()

    def test_symlink_is_skipped(self):
        temp, root = self.project()
        external = tempfile.TemporaryDirectory()
        try:
            target = Path(external.name) / "MEMORY.md"
            target.write_text("Must not leak.\n", encoding="utf-8")
            link = root / "MEMORY.md"
            try:
                link.symlink_to(target)
            except (OSError, NotImplementedError):
                self.skipTest("symlinks unavailable")
            bom = scan(root, now=FIXED_NOW)
            self.assertNotIn("MEMORY.md", [i["path"] for i in bom["items"]])
        finally:
            temp.cleanup(); external.cleanup()


if __name__ == "__main__":
    unittest.main()

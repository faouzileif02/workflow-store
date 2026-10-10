"""PromptBOM command-line interface."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, Optional

from . import __version__
from .core import BOM_FILE, CONFIG_FILES, LOCK_FILE, diff_against_lock, diff_boms, dump, has_diff, load, make_lock, scan
from .report import html_report, markdown_report
from .validate import validate_bom


def _config_path(args: argparse.Namespace) -> Optional[Path]:
    value = getattr(args, "config", None)
    return Path(value) if value else None


def _print_summary(bom: Dict[str, Any]) -> None:
    s = bom["summary"]
    print("PromptBOM: %s items | root_hash=%s…" % (s["total"], s["root_hash"][:16]))
    for kind, n in s.get("by_kind", {}).items():
        print("  %-20s %s" % (kind, n))
    if s.get("with_secret_findings"):
        print("WARNING: %s item(s) contain possible secrets (locations only; values never stored)." % s["with_secret_findings"], file=sys.stderr)
    if s.get("possible_conflicts"):
        print("NOTICE: %s possible authority conflict(s) detected (heuristic)." % s["possible_conflicts"], file=sys.stderr)


def cmd_scan(args: argparse.Namespace) -> int:
    root = Path(args.path)
    bom = scan(root, config_path=_config_path(args))
    _print_summary(bom)
    if args.no_write:
        if args.json:
            print(json.dumps(bom, indent=2, ensure_ascii=False))
        else:
            print("Read-only scan: no files written.")
        return 0
    out = Path(args.output) if args.output else root / BOM_FILE
    dump(bom, out)
    if args.json:
        print(json.dumps(bom, indent=2, ensure_ascii=False))
    print("Wrote %s" % out)
    return 0


def cmd_show(args: argparse.Namespace) -> int:
    bom = scan(Path(args.path), config_path=_config_path(args))
    print("%-11s %-18s %-11s %-10s %5s  %-10s %s" % ("AUTH", "KIND", "ECOSYSTEM", "MUT", "AGE", "SHA", "PATH"))
    for item in bom["items"]:
        flag = " [!secret]" if item["secret_findings"] else ""
        print("%-11s %-18s %-11s %-10s %4sd  %-10s %s%s" % (
            item["authority"]["level"], item["kind"], item.get("ecosystem", "generic"), item["mutability"],
            item["freshness"]["age_days"], item["sha256"][:8], item["path"], flag))
    if bom.get("conflicts"):
        print("\nPossible authority conflicts: %s (run `promptbom report` for details)" % len(bom["conflicts"]))
    return 0


def cmd_lock(args: argparse.Namespace) -> int:
    root = Path(args.path)
    bom = scan(root, config_path=_config_path(args))
    if bom["summary"]["with_secret_findings"] and not args.allow_secrets:
        print("Refusing to lock: possible secrets found. Fix them or pass --allow-secrets.", file=sys.stderr)
        return 2
    if bom["summary"]["possible_conflicts"] and args.fail_on_conflicts:
        print("Refusing to lock: possible authority conflicts found. Review them or omit --fail-on-conflicts.", file=sys.stderr)
        return 3
    dump(bom, root / BOM_FILE)
    dump(make_lock(bom), root / LOCK_FILE)
    print("Locked %s items. root_hash=%s…" % (bom["summary"]["total"], bom["summary"]["root_hash"][:16]))
    return 0


def cmd_verify(args: argparse.Namespace) -> int:
    root = Path(args.path)
    lock_path = root / LOCK_FILE
    if not lock_path.exists():
        print("No %s in %s. Run `promptbom lock` first." % (LOCK_FILE, root), file=sys.stderr)
        return 2
    lock = load(lock_path)
    bom = scan(root, config_path=_config_path(args))
    d = diff_against_lock(lock, bom)
    if not has_diff(d):
        if args.fail_on_conflicts and bom["summary"]["possible_conflicts"]:
            print("CONFLICT: inventory is locked but possible authority conflicts exist.", file=sys.stderr)
            return 3
        print("OK: no drift. Everything the AI was told is unchanged.")
        return 0
    print("DRIFT detected:")
    for label in ("changed", "added", "removed"):
        for entry in d[label]:
            print("  %-8s %s" % (label.upper(), entry["path"]))
    return 1


def cmd_diff(args: argparse.Namespace) -> int:
    first = Path(args.first)
    if args.second:
        diff = diff_boms(load(first), load(Path(args.second)))
    elif first.is_dir():
        lock_path = first / LOCK_FILE
        if not lock_path.exists():
            print("No %s in %s." % (LOCK_FILE, first), file=sys.stderr)
            return 2
        diff = diff_against_lock(load(lock_path), scan(first, config_path=_config_path(args)))
    else:
        print("With one argument, diff expects a project directory. With two, pass old.json new.json.", file=sys.stderr)
        return 2
    if args.format == "json":
        print(json.dumps(diff, indent=2, ensure_ascii=False))
    else:
        if not has_diff(diff):
            print("No differences.")
        for label in ("changed", "added", "removed"):
            for entry in diff[label]:
                print("%-8s %s" % (label.upper(), entry["path"]))
    return 1 if has_diff(diff) and args.exit_code else 0


def cmd_report(args: argparse.Namespace) -> int:
    root = Path(args.path)
    bom = scan(root, config_path=_config_path(args))
    if args.format == "html":
        text = html_report(bom)
        default_name = "promptbom-report.html"
    else:
        text = markdown_report(bom)
        default_name = "promptbom-report.md"
    out = Path(args.output) if args.output else root / default_name
    out.write_text(text, encoding="utf-8")
    print("Wrote %s" % out)
    return 0


def cmd_validate(args: argparse.Namespace) -> int:
    target = Path(args.target)
    bom = scan(target, config_path=_config_path(args)) if target.is_dir() else load(target)
    errors = validate_bom(bom)
    if errors:
        print("INVALID PromptBOM:", file=sys.stderr)
        for error in errors:
            print("  - %s" % error, file=sys.stderr)
        return 1
    print("VALID: PromptBOM structure and invariants are consistent.")
    return 0


def cmd_init(args: argparse.Namespace) -> int:
    root = Path(args.path)
    root.mkdir(parents=True, exist_ok=True)
    out = root / "promptbom.config.json"
    if out.exists() and not args.force:
        print("Config already exists: %s (use --force to replace)" % out, file=sys.stderr)
        return 2
    example = {
        "rules": [],
        "ignore_dirs": ["vendor"],
        "ignore_patterns": ["docs/archive/**"],
        "max_bytes": 2000000,
        "stale_after_days": 180,
    }
    dump(example, out)
    print("Created %s" % out)
    return 0


def _add_config(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--config", help="path to promptbom config JSON (relative to project root unless absolute)")


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(prog="promptbom", description="SBOM for everything your AI was told. Local-first, no network, no LLM.")
    p.add_argument("--version", action="version", version="promptbom %s" % __version__)
    sub = p.add_subparsers(dest="command", required=True)

    s = sub.add_parser("scan", help="inventory prompts, skills, memory, policies and MCP configs")
    s.add_argument("path", nargs="?", default=".")
    group = s.add_mutually_exclusive_group()
    group.add_argument("-o", "--output")
    group.add_argument("--no-write", "--read-only", dest="no_write", action="store_true",
                       help="perform a true read-only scan; do not create promptbom.json")
    s.add_argument("--json", action="store_true", help="also print the complete PromptBOM JSON to stdout")
    _add_config(s); s.set_defaults(func=cmd_scan)

    s = sub.add_parser("show", help="print the live inventory as a table")
    s.add_argument("path", nargs="?", default=".")
    _add_config(s); s.set_defaults(func=cmd_show)

    s = sub.add_parser("lock", help="write promptbom.json and deterministic promptbom.lock")
    s.add_argument("path", nargs="?", default=".")
    s.add_argument("--allow-secrets", action="store_true")
    s.add_argument("--fail-on-conflicts", action="store_true")
    _add_config(s); s.set_defaults(func=cmd_lock)

    s = sub.add_parser("verify", help="exit 1 if anything drifted from promptbom.lock")
    s.add_argument("path", nargs="?", default=".")
    s.add_argument("--fail-on-conflicts", action="store_true")
    _add_config(s); s.set_defaults(func=cmd_verify)

    s = sub.add_parser("diff", help="compare current project to lock, or compare two PromptBOM JSON files")
    s.add_argument("first")
    s.add_argument("second", nargs="?")
    s.add_argument("--format", choices=("text", "json"), default="text")
    s.add_argument("--exit-code", action="store_true", help="return 1 when differences exist")
    _add_config(s); s.set_defaults(func=cmd_diff)

    s = sub.add_parser("report", help="generate a Markdown or standalone HTML report")
    s.add_argument("path", nargs="?", default=".")
    s.add_argument("--format", choices=("markdown", "html"), default="markdown")
    s.add_argument("-o", "--output")
    _add_config(s); s.set_defaults(func=cmd_report)

    s = sub.add_parser("validate", help="validate a live project or promptbom.json")
    s.add_argument("target", nargs="?", default=".")
    _add_config(s); s.set_defaults(func=cmd_validate)

    s = sub.add_parser("init", help="create promptbom.config.json")
    s.add_argument("path", nargs="?", default=".")
    s.add_argument("--force", action="store_true")
    s.set_defaults(func=cmd_init)
    return p


def main(argv: Optional[list] = None) -> int:
    try:
        args = build_parser().parse_args(argv)
        return int(args.func(args))
    except ValueError as exc:
        print("promptbom: %s" % exc, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())

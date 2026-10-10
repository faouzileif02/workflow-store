"""Core scanning, locking, diffing, secret detection and conflict heuristics.

PromptBOM is intentionally local-first and standard-library-only.
"""

from __future__ import annotations

import fnmatch
import hashlib
import json
import os
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple

from . import __version__

SCHEMA_VERSION = "0.2"
BOM_FILE = "promptbom.json"
LOCK_FILE = "promptbom.lock"
CONFIG_FILES = ("promptbom.config.json", ".promptbom.json")

# pattern, kind, authority rank (lower = stronger), mutability
DEFAULT_RULES: List[Tuple[str, str, int, str]] = [
    ("**/SYSTEM_PROMPT*", "system_prompt", 0, "versioned"),
    ("**/system_prompt*", "system_prompt", 0, "versioned"),
    ("**/system.prompt*", "system_prompt", 0, "versioned"),
    ("AGENTS.md", "developer_prompt", 1, "versioned"),
    ("**/AGENTS.md", "developer_prompt", 1, "versioned"),
    ("CLAUDE.md", "developer_prompt", 1, "versioned"),
    ("**/CLAUDE.md", "developer_prompt", 1, "versioned"),
    ("**/.claude/agents/**/*.md", "developer_prompt", 1, "versioned"),
    ("**/.claude/rules/**/*.md", "developer_prompt", 1, "versioned"),
    ("**/.claude/commands/**/*.md", "prompt", 1, "versioned"),
    (".cursorrules", "developer_prompt", 1, "versioned"),
    (".cursor/rules/**", "developer_prompt", 1, "versioned"),
    ("**/.cursor/rules/**", "developer_prompt", 1, "versioned"),
    (".cursor/commands/**/*.md", "prompt", 1, "versioned"),
    ("**/.cursor/commands/**/*.md", "prompt", 1, "versioned"),
    ("GEMINI.md", "developer_prompt", 1, "versioned"),
    ("**/GEMINI.md", "developer_prompt", 1, "versioned"),
    (".gemini/**/*.md", "developer_prompt", 1, "versioned"),
    ("**/.gemini/**/*.md", "developer_prompt", 1, "versioned"),
    (".github/copilot-instructions.md", "developer_prompt", 1, "versioned"),
    ("**/.github/copilot-instructions.md", "developer_prompt", 1, "versioned"),
    (".github/instructions/**/*.instructions.md", "developer_prompt", 1, "versioned"),
    ("**/.github/instructions/**/*.instructions.md", "developer_prompt", 1, "versioned"),
    (".github/prompts/**/*.prompt.md", "prompt", 1, "versioned"),
    ("**/.github/prompts/**/*.prompt.md", "prompt", 1, "versioned"),
    ("**/*.prompt", "prompt", 1, "versioned"),
    ("**/*.prompt.md", "prompt", 1, "versioned"),
    ("**/prompts/**/*.md", "prompt", 1, "versioned"),
    ("**/prompts/**/*.txt", "prompt", 1, "versioned"),
    ("POLICY.md", "policy", 2, "versioned"),
    ("**/POLICY.md", "policy", 2, "versioned"),
    ("**/policies/**/*.md", "policy", 2, "versioned"),
    ("SKILL.md", "skill", 3, "versioned"),
    ("**/SKILL.md", "skill", 3, "versioned"),
    (".mcp.json", "mcp_description", 4, "versioned"),
    ("**/.mcp.json", "mcp_description", 4, "versioned"),
    ("**/mcp.json", "mcp_description", 4, "versioned"),
    ("**/mcp_servers.json", "mcp_description", 4, "versioned"),
    (".cursor/mcp.json", "mcp_description", 4, "versioned"),
    ("**/.cursor/mcp.json", "mcp_description", 4, "versioned"),
    ("MEMORY.md", "memory", 5, "mutable"),
    ("**/MEMORY.md", "memory", 5, "mutable"),
    ("**/memory/**/*.md", "memory", 5, "mutable"),
    ("**/context/**/*.md", "retrieved_context", 6, "mutable"),
    ("**/retrieved/**", "retrieved_context", 6, "mutable"),
]

DEFAULT_IGNORED_DIRS = {
    ".git", "node_modules", "__pycache__", ".venv", "venv", "dist", "build",
    ".next", ".tox", ".mypy_cache", ".pytest_cache", "coverage", "htmlcov",
}

AUTHORITY_NAMES = {
    0: "system", 1: "developer", 2: "policy", 3: "skill",
    4: "tool_description", 5: "memory", 6: "retrieved",
}
AUTHORITY_RANKS = {v: k for k, v in AUTHORITY_NAMES.items()}

DEFAULT_MAX_BYTES = 2_000_000
DEFAULT_STALE_AFTER_DAYS = 180

SECRET_PATTERNS = [
    ("aws_access_key", re.compile(r"AKIA[0-9A-Z]{16}")),
    ("github_token", re.compile(r"gh[pousr]_[A-Za-z0-9]{30,}")),
    ("openai_style_key", re.compile(r"sk-[A-Za-z0-9_\-]{20,}")),
    ("slack_token", re.compile(r"xox[baprs]-[A-Za-z0-9\-]{10,}")),
    ("private_key", re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----")),
    ("generic_secret", re.compile(
        r"(?i)\b(api[_-]?key|secret|token|password)\b\s*[:=]\s*['\"][^'\"\s]{12,}['\"]")),
]

DIRECTIVE_RE = re.compile(
    r"(?i)^\s*(?:[-*]\s*)?"
    r"(?:(?:you|the\s+agent|agent|assistant)\s+)?"
    r"(must\s+not|do\s+not|don't|never|should\s+not|may\s+not|cannot|can't|"
    r"(?:is\s+)?not\s+(?:allowed|permitted|authorized)\s+to|"
    r"must|always|should|required\s+to|may|can|"
    r"(?:is\s+)?(?:allowed|permitted|authorized)\s+to)"
    r"\s+(.+?)\s*[.!]?\s*$"
)
NEGATIVE_DIRECTIVES = {
    "must not", "do not", "don't", "never", "should not", "may not",
    "cannot", "can't", "not allowed to", "not permitted to", "not authorized to",
    "is not allowed to", "is not permitted to", "is not authorized to",
}
STOPWORDS = {
    "a", "an", "the", "to", "of", "for", "and", "or", "any", "all", "this", "that",
    "your", "user", "users", "please", "only", "always", "never", "must", "should", "not",
    "you", "agent", "assistant", "may", "can", "cannot", "allowed", "permitted", "authorized",
}

ACTION_GROUPS = {
    "deploy": {"deploy", "publish", "release", "ship"},
    "send": {"send", "message", "email", "notify", "contact"},
    "delete": {"delete", "remove", "erase", "destroy"},
    "write": {"write", "modify", "edit", "change", "update"},
    "execute": {"execute", "run", "launch", "start"},
    "install": {"install", "download", "fetch"},
    "git-write": {"commit", "push", "merge"},
    "share-secret": {"reveal", "expose", "share", "leak"},
    "spend": {"pay", "purchase", "buy", "transfer", "spend"},
}

def _action_groups(tokens: set) -> set:
    return {name for name, words in ACTION_GROUPS.items() if tokens & words}


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _match(rel: str, pattern: str) -> bool:
    """Glob matching where **/ may represent zero or more directories."""
    candidates = {pattern}
    if pattern.startswith("**/"):
        candidates.add(pattern[3:])
    pending = list(candidates)
    while pending:
        candidate = pending.pop()
        if "/**/" in candidate:
            collapsed = candidate.replace("/**/", "/", 1)
            if collapsed not in candidates:
                candidates.add(collapsed)
                pending.append(collapsed)
    return any(fnmatch.fnmatchcase(rel, candidate) for candidate in candidates)


def _default_config() -> Dict[str, Any]:
    return {
        "rules": [],
        "ignore_dirs": [],
        "ignore_patterns": [],
        "max_bytes": DEFAULT_MAX_BYTES,
        "stale_after_days": DEFAULT_STALE_AFTER_DAYS,
    }


def load_config(root: Path, explicit: Optional[Path] = None) -> Tuple[Dict[str, Any], Optional[Path]]:
    config = _default_config()
    chosen: Optional[Path] = None
    if explicit is not None:
        chosen = explicit if explicit.is_absolute() else root / explicit
        if not chosen.exists():
            raise ValueError("Config file not found: %s" % chosen)
    else:
        for name in CONFIG_FILES:
            candidate = root / name
            if candidate.exists():
                chosen = candidate
                break
    if chosen is None:
        return config, None
    try:
        raw = json.loads(chosen.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError("Invalid PromptBOM config %s: %s" % (chosen, exc))
    if not isinstance(raw, dict):
        raise ValueError("PromptBOM config must be a JSON object")
    for key in ("rules", "ignore_dirs", "ignore_patterns"):
        if key in raw and not isinstance(raw[key], list):
            raise ValueError("Config field '%s' must be a list" % key)
    if "max_bytes" in raw and (not isinstance(raw["max_bytes"], int) or raw["max_bytes"] < 1):
        raise ValueError("Config field 'max_bytes' must be a positive integer")
    if "stale_after_days" in raw and (not isinstance(raw["stale_after_days"], int) or raw["stale_after_days"] < 0):
        raise ValueError("Config field 'stale_after_days' must be a non-negative integer")
    config.update({k: raw[k] for k in config if k in raw})
    return config, chosen


def _compiled_rules(config: Dict[str, Any]) -> List[Tuple[str, str, int, str]]:
    custom: List[Tuple[str, str, int, str]] = []
    allowed_kinds = {r[1] for r in DEFAULT_RULES}
    for idx, rule in enumerate(config.get("rules", [])):
        if not isinstance(rule, dict):
            raise ValueError("Custom rule #%d must be an object" % (idx + 1))
        pattern = rule.get("pattern")
        kind = rule.get("kind", "developer_prompt")
        authority = rule.get("authority", "developer")
        mutability = rule.get("mutability", "versioned")
        if not isinstance(pattern, str) or not pattern.strip():
            raise ValueError("Custom rule #%d requires a non-empty pattern" % (idx + 1))
        if kind not in allowed_kinds:
            raise ValueError("Custom rule #%d has unknown kind: %s" % (idx + 1, kind))
        if authority not in AUTHORITY_RANKS:
            raise ValueError("Custom rule #%d has unknown authority: %s" % (idx + 1, authority))
        if mutability not in ("versioned", "mutable"):
            raise ValueError("Custom rule #%d mutability must be versioned or mutable" % (idx + 1))
        custom.append((pattern, kind, AUTHORITY_RANKS[authority], mutability))
    return custom + DEFAULT_RULES


def classify(rel: str, rules: Sequence[Tuple[str, str, int, str]]) -> Optional[Tuple[str, int, str]]:
    for pattern, kind, rank, mutability in rules:
        if _match(rel, pattern):
            return kind, rank, mutability
    return None


def detect_ecosystem(rel: str) -> str:
    low = rel.lower()
    if low.endswith("claude.md") or "/.claude/" in "/" + low:
        return "claude"
    if low == "gemini.md" or low.endswith("/gemini.md") or "/.gemini/" in "/" + low:
        return "gemini"
    if low == ".cursorrules" or "/.cursor/" in "/" + low or low.startswith("cursor_rules/") or "/cursor_rules/" in "/" + low:
        return "cursor"
    if "copilot-instructions.md" in low or "/.github/instructions/" in "/" + low or "/.github/prompts/" in "/" + low or low.startswith("copilot_instructions/") or "/copilot_instructions/" in "/" + low:
        return "copilot"
    if low.endswith("agents.md"):
        return "cross-tool"
    if "mcp" in low or low.startswith("mcp_config/") or "/mcp_config/" in "/" + low:
        return "mcp"
    return "generic"


def walk(root: Path, ignored_dirs: Iterable[str]) -> Iterable[Tuple[Path, str]]:
    ignored = set(ignored_dirs)
    for dirpath, dirnames, filenames in os.walk(root, followlinks=False):
        dirnames[:] = sorted(d for d in dirnames if d not in ignored and not (Path(dirpath) / d).is_symlink())
        for name in sorted(filenames):
            full = Path(dirpath) / name
            if full.is_symlink():
                continue
            yield full, full.relative_to(root).as_posix()


def git_info(root: Path, rel: str) -> Optional[Dict[str, str]]:
    try:
        out = subprocess.run(
            ["git", "-C", str(root), "log", "-1", "--format=%H|%an|%cI", "--", rel],
            capture_output=True, text=True, timeout=5,
        )
        line = out.stdout.strip()
        if out.returncode == 0 and line:
            commit, author, date = line.split("|", 2)
            return {"commit": commit, "author": author, "date": date}
    except (OSError, subprocess.SubprocessError, ValueError):
        pass
    return None


def find_secrets(text: str) -> List[Dict[str, Any]]:
    findings: List[Dict[str, Any]] = []
    for name, rx in SECRET_PATTERNS:
        for match in rx.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            findings.append({"type": name, "line": line})
    return findings


def _directive_tokens(text: str) -> set:
    tokens = re.findall(r"[a-z0-9_]+", text.lower())
    return {t for t in tokens if t not in STOPWORDS and len(t) > 1}


def extract_directives(text: str, path: str, rank: int, level: str) -> List[Dict[str, Any]]:
    directives: List[Dict[str, Any]] = []
    for line_no, line in enumerate(text.splitlines(), 1):
        match = DIRECTIVE_RE.match(line)
        if not match:
            continue
        verb = re.sub(r"\s+", " ", match.group(1).lower())
        body = match.group(2).strip()
        tokens = sorted(_directive_tokens(body))
        if not tokens:
            continue
        directives.append({
            "path": path,
            "line": line_no,
            "authority": {"rank": rank, "level": level},
            "polarity": "negative" if verb in NEGATIVE_DIRECTIVES else "positive",
            "directive": (verb + " " + body)[:300],
            "tokens": tokens,
        })
    return directives


def detect_conflicts(directives: Sequence[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """Return conservative cross-file contradiction candidates.

    Combines token overlap with canonical action groups so common
    permission-vs-prohibition pairs are not missed merely because qualifiers differ.
    Results remain review signals, not semantic proof.
    """
    conflicts: List[Dict[str, Any]] = []
    seen = set()
    for idx, left in enumerate(directives):
        lt = set(left["tokens"])
        la = _action_groups(lt)
        for right in directives[idx + 1:]:
            if left["path"] == right["path"] or left["polarity"] == right["polarity"]:
                continue
            rt = set(right["tokens"])
            ra = _action_groups(rt)
            union = lt | rt
            shared = lt & rt
            shared_actions = la & ra
            similarity = (len(shared) / float(len(union))) if union else 0.0
            lexical_match = len(shared) >= 2 and similarity >= 0.35
            action_match = bool(shared_actions)
            if not (lexical_match or action_match):
                continue
            confidence = similarity
            if action_match:
                confidence = max(confidence, 0.72 if len(shared) >= 2 else 0.62)
            confidence = round(min(0.99, confidence), 2)
            stronger, weaker = (left, right)
            if right["authority"]["rank"] < left["authority"]["rank"]:
                stronger, weaker = right, left
            key = (stronger["path"], stronger["line"], weaker["path"], weaker["line"], tuple(sorted(shared_actions)))
            if key in seen:
                continue
            seen.add(key)
            conflicts.append({
                "type": "possible_authority_conflict",
                "confidence": confidence,
                "shared_terms": sorted(shared),
                "shared_actions": sorted(shared_actions),
                "stronger": {k: stronger[k] for k in ("path", "line", "authority", "polarity", "directive")},
                "weaker": {k: weaker[k] for k in ("path", "line", "authority", "polarity", "directive")},
            })
    conflicts.sort(key=lambda c: (-c["confidence"], c["stronger"]["path"], c["weaker"]["path"]))
    return conflicts


def scan(root: Path, now: Optional[datetime] = None, config_path: Optional[Path] = None) -> Dict[str, Any]:
    root = root.resolve()
    if not root.exists() or not root.is_dir():
        raise ValueError("Scan path must be an existing directory: %s" % root)
    now = now or datetime.now(timezone.utc)
    config, used_config = load_config(root, config_path)
    rules = _compiled_rules(config)
    ignored_dirs = set(DEFAULT_IGNORED_DIRS) | set(config.get("ignore_dirs", []))
    ignore_patterns = list(config.get("ignore_patterns", []))
    max_bytes = int(config.get("max_bytes", DEFAULT_MAX_BYTES))
    stale_after_days = int(config.get("stale_after_days", DEFAULT_STALE_AFTER_DAYS))

    items: List[Dict[str, Any]] = []
    all_directives: List[Dict[str, Any]] = []
    skipped = {"too_large": 0, "ignored_pattern": 0, "unreadable": 0}

    for full, rel in walk(root, ignored_dirs):
        if rel in (BOM_FILE, LOCK_FILE):
            continue
        if any(_match(rel, pattern) for pattern in ignore_patterns):
            skipped["ignored_pattern"] += 1
            continue
        cls = classify(rel, rules)
        if not cls:
            continue
        try:
            size = full.stat().st_size
            if size > max_bytes:
                skipped["too_large"] += 1
                continue
            data = full.read_bytes()
            stat = full.stat()
        except OSError:
            skipped["unreadable"] += 1
            continue
        kind, rank, mutability = cls
        text = data.decode("utf-8", errors="replace")
        mtime = datetime.fromtimestamp(stat.st_mtime, tz=timezone.utc)
        age_days = max(0, (now - mtime).days)
        prov = git_info(root, rel)
        level = AUTHORITY_NAMES[rank]
        directives = extract_directives(text, rel, rank, level)
        all_directives.extend(directives)
        items.append({
            "id": "pb:" + sha256_bytes(rel.encode("utf-8"))[:12],
            "path": rel,
            "kind": kind,
            "ecosystem": detect_ecosystem(rel),
            "sha256": sha256_bytes(data),
            "bytes": len(data),
            "lines": text.count("\n") + (0 if text.endswith("\n") or not text else 1),
            "authority": {"rank": rank, "level": level},
            "mutability": mutability,
            "freshness": {
                "modified": mtime.isoformat(timespec="seconds"),
                "age_days": age_days,
                "stale": age_days > stale_after_days,
            },
            "provenance": {"source": "git" if prov else "local-file", **(prov or {})},
            "secret_findings": find_secrets(text),
            "directives": len(directives),
        })

    items.sort(key=lambda i: (i["authority"]["rank"], i["path"]))
    conflicts = detect_conflicts(all_directives)
    return {
        "promptbom_version": SCHEMA_VERSION,
        "tool": "promptbom %s" % __version__,
        "generated_at": now.isoformat(timespec="seconds"),
        "root": root.name,
        "configuration": {
            "source": used_config.name if used_config else "defaults",
            "custom_rules": len(config.get("rules", [])),
            "max_bytes": max_bytes,
            "stale_after_days": stale_after_days,
        },
        "summary": summarize(items, conflicts),
        "conflicts": conflicts,
        "skipped": skipped,
        "items": items,
    }


def summarize(items: Sequence[Dict[str, Any]], conflicts: Sequence[Dict[str, Any]]) -> Dict[str, Any]:
    kinds: Dict[str, int] = {}
    ecosystems: Dict[str, int] = {}
    for item in items:
        kinds[item["kind"]] = kinds.get(item["kind"], 0) + 1
        ecosystems[item["ecosystem"]] = ecosystems.get(item["ecosystem"], 0) + 1
    return {
        "total": len(items),
        "by_kind": dict(sorted(kinds.items())),
        "by_ecosystem": dict(sorted(ecosystems.items())),
        "mutable": sum(1 for i in items if i["mutability"] == "mutable"),
        "stale": sum(1 for i in items if i["freshness"]["stale"]),
        "with_secret_findings": sum(1 for i in items if i["secret_findings"]),
        "possible_conflicts": len(conflicts),
        "root_hash": root_hash(items),
    }


def root_hash(items: Sequence[Dict[str, Any]]) -> str:
    lines = sorted("%s\t%s" % (i["path"], i["sha256"]) for i in items)
    return sha256_bytes("\n".join(lines).encode("utf-8"))


def make_lock(bom: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "promptbom_lock_version": SCHEMA_VERSION,
        "tool_version": __version__,
        "root_hash": bom["summary"]["root_hash"],
        "items": {
            i["path"]: {"sha256": i["sha256"], "kind": i["kind"], "ecosystem": i.get("ecosystem", "generic")}
            for i in sorted(bom["items"], key=lambda x: x["path"])
        },
    }


def diff_against_lock(lock: Dict[str, Any], bom: Dict[str, Any]) -> Dict[str, List[Any]]:
    locked = lock.get("items", {})
    current = {i["path"]: i for i in bom["items"]}
    added = sorted(set(current) - set(locked))
    removed = sorted(set(locked) - set(current))
    changed = sorted(p for p in set(current) & set(locked) if current[p]["sha256"] != locked[p].get("sha256"))
    return {
        "added": [{"path": p, "sha256": current[p]["sha256"], "kind": current[p]["kind"]} for p in added],
        "removed": [{"path": p, "sha256": locked[p].get("sha256"), "kind": locked[p].get("kind")} for p in removed],
        "changed": [{
            "path": p,
            "before": locked[p].get("sha256"),
            "after": current[p]["sha256"],
            "kind": current[p]["kind"],
        } for p in changed],
    }


def diff_boms(old: Dict[str, Any], new: Dict[str, Any]) -> Dict[str, List[Any]]:
    old_items = {i["path"]: i for i in old.get("items", [])}
    new_items = {i["path"]: i for i in new.get("items", [])}
    added = sorted(set(new_items) - set(old_items))
    removed = sorted(set(old_items) - set(new_items))
    changed = sorted(p for p in set(old_items) & set(new_items) if old_items[p].get("sha256") != new_items[p].get("sha256"))
    return {
        "added": [{"path": p, "sha256": new_items[p].get("sha256"), "kind": new_items[p].get("kind")} for p in added],
        "removed": [{"path": p, "sha256": old_items[p].get("sha256"), "kind": old_items[p].get("kind")} for p in removed],
        "changed": [{
            "path": p, "before": old_items[p].get("sha256"), "after": new_items[p].get("sha256"),
            "kind_before": old_items[p].get("kind"), "kind_after": new_items[p].get("kind"),
        } for p in changed],
    }


def dump(obj: Any, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def load(path: Path) -> Dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ValueError("Cannot read JSON %s: %s" % (path, exc))
    if not isinstance(value, dict):
        raise ValueError("JSON document must be an object: %s" % path)
    return value


def has_diff(diff: Dict[str, List[Any]]) -> bool:
    return any(bool(diff.get(k)) for k in ("added", "removed", "changed"))

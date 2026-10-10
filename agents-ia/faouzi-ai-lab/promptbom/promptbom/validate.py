"""Small built-in validator for PromptBOM output.

This is intentionally not a full JSON Schema engine; it validates the invariants
PromptBOM itself relies on without introducing a runtime dependency.
"""

import re
from typing import Any, Dict, List

HEX64 = re.compile(r"^[0-9a-f]{64}$")
ALLOWED_KINDS = {
    "system_prompt", "developer_prompt", "prompt", "policy", "skill",
    "mcp_description", "memory", "retrieved_context",
}
ALLOWED_MUTABILITY = {"versioned", "mutable"}


def validate_bom(bom: Dict[str, Any]) -> List[str]:
    errors: List[str] = []
    for key in ("promptbom_version", "tool", "generated_at", "root", "summary", "items"):
        if key not in bom:
            errors.append("missing top-level field: %s" % key)
    items = bom.get("items")
    if not isinstance(items, list):
        errors.append("items must be an array")
        return errors
    summary = bom.get("summary")
    if not isinstance(summary, dict):
        errors.append("summary must be an object")
    else:
        rh = summary.get("root_hash")
        if not isinstance(rh, str) or not HEX64.match(rh):
            errors.append("summary.root_hash must be a 64-character lowercase SHA-256")
        if summary.get("total") != len(items):
            errors.append("summary.total does not match number of items")
    seen = set()
    for idx, item in enumerate(items):
        prefix = "items[%d]" % idx
        if not isinstance(item, dict):
            errors.append("%s must be an object" % prefix)
            continue
        path = item.get("path")
        if not isinstance(path, str) or not path:
            errors.append("%s.path must be a non-empty string" % prefix)
        elif path in seen:
            errors.append("duplicate path: %s" % path)
        else:
            seen.add(path)
        if item.get("kind") not in ALLOWED_KINDS:
            errors.append("%s.kind is invalid" % prefix)
        digest = item.get("sha256")
        if not isinstance(digest, str) or not HEX64.match(digest):
            errors.append("%s.sha256 is invalid" % prefix)
        if item.get("mutability") not in ALLOWED_MUTABILITY:
            errors.append("%s.mutability is invalid" % prefix)
        authority = item.get("authority")
        if not isinstance(authority, dict) or not isinstance(authority.get("rank"), int):
            errors.append("%s.authority is invalid" % prefix)
    return errors

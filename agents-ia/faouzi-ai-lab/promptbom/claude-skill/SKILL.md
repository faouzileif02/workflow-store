---
name: promptbom
description: Audit AI-agent instruction surfaces for drift, possible secrets and conflicts using PromptBOM v0.2.1. Use for CLAUDE.md, AGENTS.md, Cursor/Copilot/Gemini rules, MCP descriptions, skills, policies, memory, lockfiles and PromptBOM reports.
---

# PromptBOM v0.2.1

Use the bundled local-first PromptBOM CLI. Never download dependencies or reveal secret values.

## Read-only audit first

```bash
python <skill-dir>/scripts/promptbom.py scan <target> --no-write
python <skill-dir>/scripts/promptbom.py validate <target>
python <skill-dir>/scripts/promptbom.py show <target>
```

Report component count, sources, authority levels, possible secret locations, possible conflicts and validation status. Do not create `promptbom.lock` unless explicitly requested.

## Safe bundled self-test

Audit `<skill-dir>/fixtures/test-project`. It intentionally contains a fake secret-like token and a deployment-rule conflict. Visible `cursor_rules`, `copilot_instructions` and `mcp_config` folders are used because some Claude Skill import paths omit hidden fixture directories.

Expected signals:
- several instruction components;
- one fake secret-like finding in `prompts/system.md`;
- a possible deployment conflict between `CLAUDE.md` and `AGENTS.md`;
- no file written by `scan --no-write`.

## Lock / verify

Only after explicit approval:

```bash
python <skill-dir>/scripts/promptbom.py lock <target>
python <skill-dir>/scripts/promptbom.py verify <target>
```

If lock is blocked by a possible secret, treat that as a successful safety control. Do not use `--allow-secrets` unless explicitly requested.

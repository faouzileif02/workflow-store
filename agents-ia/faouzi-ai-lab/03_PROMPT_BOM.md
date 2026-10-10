# PromptBOM

**Status:** 🟢 Hardened Functional MVP `v0.2.1`

> **SBOM for everything your AI was told.**

## Problem

AI-agent behavior can be influenced by system prompts, developer prompts, `AGENTS.md`, `CLAUDE.md`, Cursor rules, GitHub Copilot instructions, `GEMINI.md`, skills, MCP descriptions, memory, policies and retrieved context.

When behavior changes, teams often ask: **What changed?** The answer is usually scattered across files and runtime systems.

## Implemented solution

PromptBOM provides a local-first Python CLI that:

- discovers common instruction sources;
- fingerprints each source with SHA-256;
- records authority, mutability, freshness and provenance;
- tags ecosystems such as Claude, Cursor, Copilot, Gemini and MCP;
- detects possible secret locations without storing the secret value;
- creates a deterministic `promptbom.lock`;
- detects drift against the lock;
- supports true read-only scans with `--no-write` / `--read-only`;
- compares two BOM snapshots;
- generates Markdown and standalone HTML reports;
- supports custom scan rules;
- flags possible authority conflicts using action-aware conservative heuristics;
- builds a Claude-compatible Skill ZIP containing exactly one `SKILL.md`.

## CLI

```bash
promptbom scan . --no-write
promptbom show .
promptbom lock .
promptbom verify .
promptbom diff .
promptbom report . --format html
promptbom validate .
promptbom init .
```

## Example supply chain

```text
AGENTS.md
CLAUDE.md
Cursor rules
Copilot instructions
GEMINI.md
Skills
MCP descriptions
Memory
Policies
   ↓
PromptBOM
   ↓
promptbom.json / stdout-only read-only audit
   +
promptbom.lock
   ↓
Git / CI detects behavioral-surface drift
```

## Security properties

- local-first;
- no network calls in the core;
- no LLM calls;
- no API key required;
- symlinks skipped;
- secret values never copied into output;
- `lock` refuses possible secret findings unless explicitly overridden;
- `scan --no-write` has no PromptBOM file side effect.

## Conflict detection in v0.2.1

The detector now recognizes common permissions and prohibitions such as `may`, `can`, `allowed to`, `never` and `must not`. It also correlates high-signal action groups such as deploy/publish, send/message, delete/remove and write/modify.

Example now detected:

```text
CLAUDE.md: Never publish or deploy without explicit user approval.
AGENTS.md:  You may deploy automatically after tests pass.
```

This is reported as a **possible conflict** for human review. It remains a deterministic heuristic, not semantic proof.

## Claude Skill packaging

`tools/build_claude_skill.py` creates a self-contained Claude Skill ZIP. CI verifies that the archive contains exactly one `SKILL.md`. Test fixtures use visible folder names for Cursor, Copilot and MCP examples so they survive Claude import reliably.

## Current limitations

- conflict detection is heuristic, not full semantic contradiction analysis;
- regex secret detection is best-effort, not a replacement for a dedicated secret scanner;
- runtime prompt capture is not yet implemented;
- signed attestations are not yet implemented.

## Next milestones

- GitHub PR comment integration;
- CycloneDX-inspired export profile;
- signed lock / attestation mode;
- optional semantic-conflict plugin;
- MCP and n8n adapters.

➡️ **[Open the functional PromptBOM v0.2.1 project](promptbom/)**

> **SBOM tells you what your software contains. PromptBOM tells you what your AI was told.**

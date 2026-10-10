# PromptBOM

**Status:** 🟢 Functional MVP `v0.2.0`

> **SBOM for everything your AI was told.**

## Problem

AI-agent behavior can be influenced by system prompts, developer prompts, `AGENTS.md`, `CLAUDE.md`, Cursor rules, GitHub Copilot instructions, `GEMINI.md`, skills, MCP descriptions, memory, policies and retrieved context.

When behavior changes, teams often ask: **What changed?** The answer is usually scattered across files and runtime systems.

## Implemented solution

PromptBOM now provides a local-first Python CLI that:

- discovers common instruction sources;
- fingerprints each source with SHA-256;
- records authority, mutability, freshness and provenance;
- tags ecosystems such as Claude, Cursor, Copilot, Gemini and MCP;
- detects possible secret locations without storing the secret value;
- creates a deterministic `promptbom.lock`;
- detects drift against the lock;
- compares two BOM snapshots;
- generates Markdown and standalone HTML reports;
- supports custom scan rules;
- flags possible authority conflicts using a conservative heuristic.

## CLI

```bash
promptbom scan .
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
promptbom.json
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
- `lock` refuses possible secret findings unless explicitly overridden.

## Current limitations

- conflict detection is lexical/heuristic, not semantic proof;
- regex secret detection is best-effort, not a replacement for a dedicated secret scanner;
- runtime prompt capture is not yet implemented;
- signed attestations are not yet implemented.

## Next milestones

- GitHub PR comment integration;
- CycloneDX-inspired export profile;
- signed lock / attestation mode;
- optional semantic-conflict plugin;
- MCP and n8n adapters.

➡️ **[Open the functional PromptBOM v0.2.0 project](promptbom/)**

> **SBOM tells you what your software contains. PromptBOM tells you what your AI was told.**

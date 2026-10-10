# PromptBOM

**Working name:** `promptbom`

> Generate an SBOM-like inventory of everything an AI agent was told.

## Problem

Software can have a Software Bill of Materials. AI agents usually have no equivalent inventory for behavioral inputs such as system prompts, developer prompts, `AGENTS.md`, skills, MCP descriptions, workflow prompts, retrieved documents, long-term memory and organization policy.

When behavior changes, teams ask: **What changed?** The answer is scattered across files and runtime systems.

## Core idea

Generate a Prompt Bill of Materials.

```json
{
  "schema": "promptbom.v1",
  "agent": "support-agent",
  "components": [
    {
      "id": "system-main",
      "type": "system_prompt",
      "source": "prompts/system.md",
      "sha256": "49bf...",
      "authority": 100,
      "mutable": false
    }
  ]
}
```

## Key fields

- **Provenance** — where did the instruction originate?
- **Authority** — which source wins during conflict?
- **Freshness** — is it stale?
- **Mutability** — can it change at runtime?
- **Exposure** — was it visible to the model?
- **Sensitivity** — could it contain secrets or private data?

## CLI

```bash
promptbom scan .
promptbom build --format json
promptbom diff old.json new.json
promptbom graph
promptbom verify promptbom.lock
```

A `promptbom.lock` can make CI fail when a high-authority instruction changes without review.

## Future

Signature verification, prompt licenses, MCP snapshots, runtime capture, supply-chain scanning and GitHub PR comments.

> **SBOM tells you what your software contains. PromptBOM tells you what your AI was told.**
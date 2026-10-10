# Intent Lockfile

**Working name:** `intent-lock`

> Compile the user's non-negotiable constraints into a deterministic lockfile that the agent cannot silently reinterpret later.

## Problem

A task may start with clear restrictions such as: update documentation, do not modify source code, do not publish and spend $0. After many tool calls, summaries, subagents and context compression, the broad goal may remain while restrictions are diluted.

## Core idea

Compile critical constraints into `INTENT.lock`.

```yaml
version: 1
objective:
  id: docs-update
  statement: Update project documentation
must_not:
  - effect: EXECUTE
  - effect: PUBLISH
  - path: "src/**"
    effect: MODIFY
limits:
  spend_usd: 0
  external_messages: 0
```

## Enforcement

```text
Agent: write src/app.py
Intent Lock: DENY
Reason: protected path src/**
```

The lockfile lives outside conversational memory. A prompt injection cannot simply erase the deterministic runtime rule.

User-authorized changes are explicit amendments with an auditable diff.

```text
natural-language request
        ↓
candidate lock
        ↓
human confirmation
        ↓
INTENT.lock
```

> **Prompts can drift. Constraints should not.**
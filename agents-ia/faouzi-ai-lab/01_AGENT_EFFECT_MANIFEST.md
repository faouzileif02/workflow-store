# Agent Effect Manifest

**Working name:** `effect-manifest`

> Make AI agents declare what they expect to change before a tool call executes.

## Problem

Tool agents expose the request, but operators care about the effect: what data leaves the system, what files change, whether an action is reversible, and how large the blast radius is.

## Core idea

Transform important tool calls into a machine-readable Effect Manifest before execution.

```json
{
  "schema": "aem.v1",
  "intent": "send invoice reminder",
  "tool": "gmail.send",
  "predicted_effects": [{
    "type": "external_message",
    "target": "client@example.com",
    "visibility": "external",
    "reversible": false
  }]
}
```

Runtime:

```text
PLAN → POLICY CHECK → EXECUTE → OBSERVE → DIFF
```

After execution calculate **Effect Drift**: the difference between predicted and actual effects.

```text
Agent predicted: MODIFY 1 file
Observed:        MODIFY 27 files + DELETE 3 files
Result:          EFFECT_DRIFT_CRITICAL
```

## Effect classes

`READ` `CREATE` `MODIFY` `DELETE` `SEND` `PUBLISH` `EXECUTE` `SPEND` `GRANT_ACCESS` `REVOKE_ACCESS` `MOVE` `COPY`

## MVP

Support filesystem operations, GitHub writes, HTTP actions, email actions and SQL mutations.

```bash
effect-manifest inspect trace.json
effect-manifest plan tool-call.json
effect-manifest diff predicted.json actual.json
effect-manifest policy check manifest.json
```

## Why this is different

The central object is a **machine-readable side-effect contract for one action**, not only a permission rule, log or approval workflow.

## Future

MCP proxy, VS Code extension, GitHub Action, n8n node, OpenAI Agents SDK adapter and LangGraph adapter.

> **Your agent should know what it is about to change before it changes it.**
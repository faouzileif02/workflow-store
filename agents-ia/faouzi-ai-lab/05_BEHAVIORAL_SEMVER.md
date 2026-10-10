# Behavioral SemVer for AI Agents

**Working name:** `agent-semver`

> Automatically decide whether an agent update is PATCH, MINOR or MAJOR by measuring what the agent actually does.

## Problem

A tiny prompt edit can introduce a new tool call, external message, write action or refusal-policy change. A large code refactor may create no observable behavior change.

## Core idea

Version the behavioral contract by comparing outputs, tool trajectories, effect classes, approval requests, latency, cost, refusals and constraint violations.

### PATCH

No contract-visible behavior change: lower latency, lower cost or equivalent output.

### MINOR

Backward-compatible new capability: supported language, optional tool or additional output field.

### MAJOR

Breaking behavioral change: new external side effect, failed invariant, removed required tool or changed refusal behavior.

## Example

```text
agent-semver compare v1.4.2 HEAD

120 evaluation cases
116 behavior-compatible
3 additive changes
1 breaking effect change

before: READ
after:  READ + SEND

Recommended version: 2.0.0
```

## CI

```bash
agent-semver evaluate ./evals
agent-semver compare baseline.json current.json --fail-on-major
```

The differentiation is **automatic release classification from measured trajectories and effects**, not merely adding version metadata.

> **A one-line prompt change can be a breaking release. Measure it.**
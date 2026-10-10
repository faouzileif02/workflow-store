# ModelSwap Replay

**Working name:** `modelswap-replay`

> Measure the risk of replacing one LLM with another without re-running dangerous side effects.

## Problem

Teams switch models for cost, speed, privacy, outages or quality. Agent compatibility is not the same as chat quality: another model may choose different tools, ask for fewer approvals, attempt writes instead of reads or enter a loop.

Testing directly can repeat real side effects.

## Core idea

Record a safe trace and replay only decision points with a candidate model. External side-effect tools remain frozen and return recorded results.

```text
Recorded run
   ↓
LLM decision #1 → Candidate model
   ↓
recorded tool result
   ↓
LLM decision #2 → Candidate model
```

No email is resent. No file is deleted again. No payment is retried.

## Report

```text
Baseline: model-A
Candidate: model-B
Cases: 120
Same tool choice:       91%
Same effect class:      97%
Same approval behavior: 88%
New forbidden actions:   2
Loop regressions:        1
Substitution risk: HIGH
```

The sharper product angle is **provider/model migration risk assessment from frozen side-effect traces**.

> **Switch models without discovering behavioral incompatibility in production.**
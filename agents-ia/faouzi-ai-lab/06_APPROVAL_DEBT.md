# Approval Debt

**Working name:** `approval-debt`

> Measure where humans repeatedly approve the same safe agent actions and turn that friction into reviewable automation candidates.

## Problem

Human-in-the-loop is essential, but repetitive low-risk approvals create fatigue. Eventually operators may disable protections entirely.

## Core idea

```text
Approval Debt = repetitive approvals × operator time × interruption cost × repetition confidence
```

The tool never silently removes an approval. It produces narrow candidate policies for human review.

## Example

```text
github.read_file
approvals: 842
approved: 842
denied: 0
scope: repo=acme/docs, path=/docs/**
median decision time: 2.4 sec

Candidate:
allow github.read_file
where repo == "acme/docs"
and path matches "/docs/**"
and effect == READ
```

## Safety principle

Never infer `approved often = safe globally`. Infer only the smallest repeated scope. Strong candidates should have zero denials, stable resource scope, stable intent and reversible/read-only effects.

## CLI

```bash
approval-debt analyze approvals.jsonl
approval-debt candidates
approval-debt explain candidate_12
approval-debt export candidate_12 --format yaml
```

> **Human approval is a scarce resource. Measure where your agents waste it.**
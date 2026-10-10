# 🗺️ FAOUZI AI LAB Roadmap

This roadmap turns ideas into small, testable open-source tools.

## Stage model

```text
IDEA
  ↓
SPECIFICATION
  ↓
MVP
  ↓
BETA
  ↓
STABLE
```

A project should not be presented as production-ready until it has tests, documented limits and a reproducible setup.

---

## Priority 1 — PromptBOM

**Current stage:** 🟡 Specification

Goal: create an SBOM-like inventory of the instruction surface seen by an AI agent.

### MVP

- scan common instruction files;
- calculate SHA-256 hashes;
- output `promptbom.json`;
- generate `promptbom.lock`;
- compare two PromptBOM snapshots;
- flag high-authority changes.

### Later

- MCP tool-description inventory;
- GitHub Action;
- prompt provenance graph;
- signatures / attestations;
- runtime capture.

---

## Priority 2 — Intent Mutation Testing

**Current stage:** 🟡 Specification

### MVP

- define one canonical intent;
- generate deterministic mutations;
- support paraphrase fixtures and multilingual cases;
- compare tool/effect trajectories;
- report behavioral invariance score.

### Later

- LLM-powered mutation generation;
- voice-transcription variants;
- n8n adapter;
- CI integration.

---

## Priority 3 — Agent Effect Manifest

**Current stage:** 🟡 Specification

### MVP

- effect schema;
- predicted vs observed effects;
- filesystem simulator;
- effect-drift score;
- policy decision: allow / review / block.

### Later

- MCP proxy;
- GitHub adapter;
- email adapter;
- SQL mutation adapter.

---

## Priority 4 — Behavioral SemVer

**Current stage:** 🟡 Specification

### MVP

- behavior fingerprint format;
- baseline vs candidate comparison;
- PATCH / MINOR / MAJOR recommendation;
- CLI report.

---

## n8n track

### Active goals

- improve Multi-LLM Router reliability;
- add provider benchmarking;
- document reusable sub-workflows;
- normalize error handling;
- add test fixtures;
- separate demo workflows from production templates.

---

## Open-source quality track

Every promoted project should eventually include:

- `README.md`
- quick-start example
- `LICENSE` where appropriate
- tests
- security notes
- example data with no secrets
- clear maturity badge
- screenshots or diagrams
- changelog for stable releases

---

## Long-term architecture

```text
User Intent
    ↓
Intent Lockfile
    ↓
Purpose-Bound Capability Lease
    ↓
Agent Plan
    ↓
Effect Manifest
    ↓
Tool Execution
    ↓
Observed Effects
    ↓
Behavioral Evaluation
    ↓
PromptBOM / Audit Trail
```

The aim is not to build everything at once. Each layer should be useful independently.

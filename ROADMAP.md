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

**Current stage:** 🟢 Functional MVP `v0.2.0`

Implemented:

- scan common instruction files;
- SHA-256 inventory;
- `promptbom.json`;
- deterministic `promptbom.lock`;
- drift verification;
- snapshot diff;
- Markdown / HTML reports;
- built-in validation;
- custom config/rules;
- Claude / Cursor / Copilot / Gemini / MCP awareness;
- secret-location detection;
- heuristic authority-conflict signals;
- unit tests;
- GitHub Actions CI;
- Python 3.9+ compatibility.

### Next

- GitHub PR comment integration;
- CycloneDX-inspired export;
- signed lock / attestations;
- runtime instruction capture;
- MCP / n8n adapters;
- optional semantic conflict plugin.

➡️ [`agents-ia/faouzi-ai-lab/promptbom/`](agents-ia/faouzi-ai-lab/promptbom/)

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

Every promoted project should include:

- `README.md`
- quick-start example
- `LICENSE`
- tests
- security notes
- example data with no secrets
- clear maturity status
- screenshots or diagrams
- changelog when releases begin
- CI when executable code exists

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

Each layer should remain useful independently.

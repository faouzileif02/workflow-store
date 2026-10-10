<p align="center"><img src="assets/faouzi-ai-lab-banner.svg" alt="Faouzi AI Lab" width="100%"></p>

# FAOUZI AI LAB

**Novel AI-Agent Infrastructure for 2026** — an open-source research collection for safer, smarter and more auditable AI agents.

`AI Agents` · `Security` · `Evaluation` · `MCP` · `Developer Tools` · `n8n`

---

## Why this project exists

AI agents are moving from chat interfaces to systems that can read files, call APIs, modify repositories, send messages, trigger workflows and coordinate other agents.

The next challenge is not only making agents more capable. It is making their actions **predictable, auditable, measurable, portable and safer to automate**.

Faouzi AI Lab explores missing infrastructure layers around autonomous agents. The goal is not to publish another generic chatbot or wrapper. Each concept targets a precise engineering problem that appears when AI systems begin to act.

> **Build agents that do more — while making their behavior easier to understand, test and control.**

---

## The 8 concepts

| Project | Core idea | Why it matters |
|---|---|---|
| **PromptBOM** | SBOM-like inventory of everything an AI agent was told | Track prompts, policies, skills, memory and tool descriptions that influence behavior |
| **Agent Effect Manifest** | Predict side effects before a tool call, then compare with real effects | Detect unexpected blast radius |
| **Intent Mutation Testing** | Test one intent through many equivalent phrasings | Reveal brittle behavior caused by wording, language or transcription changes |
| **Behavioral SemVer** | Infer MAJOR / MINOR / PATCH from measured agent behavior | Treat prompt/model changes as compatibility changes |
| **Approval Debt** | Quantify repetitive human approvals | Reduce approval fatigue without silently weakening safety |
| **Intent Lockfile** | Compile user constraints into deterministic runtime rules | Prevent long-running agents from forgetting restrictions |
| **Purpose-Bound Capability Lease** | Permissions expire when the agent's purpose changes | Add the missing WHY dimension to temporary permissions |
| **ModelSwap Replay** | Replay decision points with another model while effects remain frozen | Measure migration risk without repeating dangerous actions |

---

## PromptBOM

> **SBOM tells you what your software contains. PromptBOM tells you what your AI was told.**

Modern agents may be influenced by system prompts, developer prompts, `AGENTS.md`, skills, MCP descriptions, retrieved documents, memories and organization policy. PromptBOM proposes a machine-readable inventory of provenance, hashes, authority, freshness, mutability, exposure and sensitivity.

```bash
promptbom scan .
promptbom build --format json
promptbom diff old.json new.json
promptbom verify promptbom.lock
```

➡️ [Complete specification](03_PROMPT_BOM.md)

---

## Agent Effect Manifest

> **Your agent should know what it is about to change before it changes it.**

```text
PLAN → EFFECT MANIFEST → POLICY CHECK → EXECUTE → OBSERVE → DIFF
```

```text
Agent predicted: MODIFY 1 file
Observed:        MODIFY 27 files + DELETE 3 files
Result:          EFFECT_DRIFT_CRITICAL
```

➡️ [Complete specification](01_AGENT_EFFECT_MANIFEST.md)

---

## Intent Mutation Testing

> **Your agent should obey the same intent even when the user says it differently.**

Test paraphrases, reordered constraints, multilingual requests, typos, voice-transcription noise and ambiguity. Measure behavioral invariance instead of only output-text similarity.

```text
same tools?
same effects?
same approval requirements?
same forbidden-action rate?
same completion criteria?
```

➡️ [Complete specification](04_INTENT_MUTATION_TESTING.md)

---

## Behavioral SemVer

> **A one-line prompt change can be a breaking release. Measure it.**

Compare tool trajectories, effect classes, approvals, refusals, cost and constraints before and after an update. A new irreversible effect can automatically trigger a MAJOR release recommendation.

➡️ [Complete specification](05_BEHAVIORAL_SEMVER.md)

---

## Approval Debt

> **Human approval is a scarce resource. Measure where your agents waste it.**

Repeated safe approvals are measured and converted into narrow policy candidates for human review — never silently removed.

➡️ [Complete specification](06_APPROVAL_DEBT.md)

---

## Intent Lockfile

> **Prompts can drift. Constraints should not.**

```yaml
must_not:
  - effect: PUBLISH
  - path: "src/**"
    effect: MODIFY
limits:
  spend_usd: 0
  external_messages: 0
```

Critical constraints live outside conversational memory and can be checked deterministically before execution.

➡️ [Complete specification](07_INTENT_LOCKFILE.md)

---

## Purpose-Bound Capability Lease

> **Permissions should expire when the purpose expires.**

Traditional permissions answer who, what, where and when. Purpose-bound leases add **why**. A lease granted to summarize an invoice is suspended if the active goal changes to contacting the customer.

➡️ [Complete specification](02_PURPOSE_BOUND_CAPABILITY_LEASE.md)

---

## ModelSwap Replay

> **Switch models without discovering behavioral incompatibility in production.**

Replay only LLM decision points while recorded tool outputs remain frozen. No email is resent, no file is deleted again and no payment is retried.

➡️ [Complete specification](08_MODELSWAP_REPLAY.md)

---

## Proposed safety stack

```text
USER REQUEST
     ↓
INTENT.lock
     ↓
Purpose-Bound Capability Lease
     ↓
Agent Plan
     ↓
Effect Manifest
     ↓
Policy Check
     ↓
Tool Execution
     ↓
Observed Effects
     ↓
Behavioral Evaluation
```

This separates probabilistic natural-language reasoning from deterministic runtime controls.

---

## Recommended build order

1. **PromptBOM** — zero-API-key CLI and lockfile.
2. **Intent Mutation Testing** — behavioral-invariance test runner.
3. **Agent Effect Manifest** — predicted vs observed effect contracts.
4. **Intent Lock + Purpose Lease + Effect Manifest** — integrated runtime safety stack.
5. **GitHub Actions / MCP / n8n adapters** — production integration layer.

---

## Design principles

- 30-second local demo.
- No paid API key for the basic demo when possible.
- Machine-readable outputs.
- Framework-neutral core.
- Adapters instead of lock-in.
- Explicit security boundaries.
- CLI first, UI later.
- Reproducible examples.

Target integrations include OpenAI Agents SDK, MCP, n8n, LangGraph, CrewAI, Ollama and GitHub Actions.

---

## Documents

| File | Description |
|---|---|
| [00_NOVELTY_MAP.md](00_NOVELTY_MAP.md) | Ranking, differentiation and launch order |
| [01_AGENT_EFFECT_MANIFEST.md](01_AGENT_EFFECT_MANIFEST.md) | Predicted vs observed side effects |
| [02_PURPOSE_BOUND_CAPABILITY_LEASE.md](02_PURPOSE_BOUND_CAPABILITY_LEASE.md) | Goal-bound temporary permissions |
| [03_PROMPT_BOM.md](03_PROMPT_BOM.md) | Instruction supply-chain inventory |
| [04_INTENT_MUTATION_TESTING.md](04_INTENT_MUTATION_TESTING.md) | Behavioral invariance across phrasing |
| [05_BEHAVIORAL_SEMVER.md](05_BEHAVIORAL_SEMVER.md) | Behavior-derived release compatibility |
| [06_APPROVAL_DEBT.md](06_APPROVAL_DEBT.md) | Human approval friction measurement |
| [07_INTENT_LOCKFILE.md](07_INTENT_LOCKFILE.md) | Deterministic user constraints |
| [08_MODELSWAP_REPLAY.md](08_MODELSWAP_REPLAY.md) | Frozen-effect model migration testing |
| [09_RESEARCH_NOTES.md](09_RESEARCH_NOTES.md) | Areas intentionally avoided or differentiated |

---

## Status

**Research / open-source MVP specifications.** These documents propose mechanisms and MVP directions. The AI open-source ecosystem moves rapidly, so novelty must be re-checked before each standalone repository launch.

## Contributing

Useful contributions include schemas, threat models, reference implementations, MCP adapters, n8n nodes, test corpora, multilingual mutation packs and anonymized production failure modes.

<p align="center"><strong>FAOUZI AI LAB</strong><br>From experimental ideas to safer agent infrastructure.</p>
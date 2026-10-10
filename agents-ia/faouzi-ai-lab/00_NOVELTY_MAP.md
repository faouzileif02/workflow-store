# Faouzi AI Lab — Novelty Map 2026

> A research-backed shortlist of AI-agent infrastructure ideas designed to be useful, buildable, and differentiated.

## Important note

It is impossible to prove that an idea has **never** been imagined anywhere. The objective is to avoid obvious clones, identify underserved engineering problems, define sharper angles and make every concept small enough for an open-source MVP.

## Ideas selected

| # | Project | One-line idea | Novelty confidence | MVP difficulty |
|---|---|---|---|---|
| 1 | Agent Effect Manifest | Declare expected side effects before execution, then compare expected vs actual | High | Medium |
| 2 | Purpose-Bound Capability Lease | Permissions expire when the agent's goal changes | High | Medium |
| 3 | PromptBOM | SBOM-like dependency graph of everything the model was told | High | Medium |
| 4 | Intent Mutation Testing | Fuzz user intent and verify behavioral invariance | High | Medium |
| 5 | Behavioral SemVer | Compute MAJOR/MINOR/PATCH from observed agent behavior | High | Medium |
| 6 | Approval Debt | Measure repeated human approvals and propose narrow automation candidates | Medium-High | Easy |
| 7 | Intent Lockfile | Compile non-negotiable user constraints into an enforceable lockfile | High | Easy-Medium |
| 8 | ModelSwap Replay | Replay decision points with another model while side effects stay frozen | Medium | Medium |

## Existing areas deliberately avoided

Do not market these generic categories as brand-new: time-travel debugging, chaos engineering, failure memory, tool-call deduplication, context pruning, portable agent state, generic least-privilege permissions and generic undo layers.

## Recommended launch order

1. **PromptBOM** — "SBOM, but for everything your AI agent was told."
2. **Intent Mutation Testing** — discover phrasings that change agent behavior.
3. **Agent Effect Manifest** — predict the blast radius before execution.
4. **Behavioral SemVer** — measure whether an agent update is breaking.

## Suggested namespace

```text
faouzi-ai-lab/
  promptbom
  intent-mutation
  effect-manifest
  behavioral-semver
  approval-debt
  intent-lock
  purpose-lease
  modelswap-replay
```

## Recommended topics

`ai-agents` `agent-infrastructure` `llm` `mcp` `agent-security` `agent-evaluation` `developer-tools` `open-source` `ai-engineering`

## Design rule

Every repository should support a 30-second demo and should work without a paid API key whenever possible.
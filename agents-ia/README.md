# 🤖 AI Agents — FAOUZI AI LAB

This section brings together two families of projects:

1. **AI-agent infrastructure** — safety, evaluation, permissions and auditability.
2. **Practical AI automations** — n8n workflows that connect LLMs to useful business tasks.

---

## 🧪 FAOUZI AI LAB — Agent Infrastructure

➡️ [Open FAOUZI AI LAB](faouzi-ai-lab/)

| Concept | Purpose |
|---|---|
| PromptBOM | Inventory everything an AI agent was told |
| Agent Effect Manifest | Predict and compare tool side effects |
| Intent Mutation Testing | Test whether behavior survives paraphrases and languages |
| Behavioral SemVer | Version releases from measured behavior |
| Approval Debt | Detect repetitive approval friction |
| Intent Lockfile | Preserve hard user constraints outside the prompt |
| Purpose-Bound Capability Lease | Bind permission to purpose, not only time |
| ModelSwap Replay | Evaluate model migration risk without replaying side effects |

Recommended first implementation: **PromptBOM**.

---

## ⚡ Practical AI Automations

### Multi-LLM Router

[Open project →](multi-llm-router/)

Routes requests between multiple LLM providers with fallback logic.

### AI Marketing & Sales Director

[Open project →](directeur-marketing-ventes/)

AI-assisted marketing, content and sales automation.

### AI Prospecting

[Open project →](prospection-ia/)

Prospect qualification and Gmail draft automation.

### SEO & Product Content

[Open project →](seo-fiches-produits/)

AI-assisted SEO and product-description workflow.

### Conversion & Customer Follow-up

[Open project →](conversion-relance-clients/)

Customer conversion and reactivation automation.

---

## Design philosophy

AI workflows should not be judged only by whether they produce text.

Where tools and external actions are involved, also evaluate:

```text
Intent preservation
Tool choice
Permission scope
Side effects
Approval behavior
Cost
Reliability
Auditability
```

➡️ [Back to repository hub](../README.md)

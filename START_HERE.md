# 🚀 Start Here — FAOUZI AI LAB

Welcome to the FAOUZI AI LAB workspace.

This repository is organized around three main paths:

## 🤖 I am interested in AI Agents

Start with **[FAOUZI AI LAB](agents-ia/faouzi-ai-lab/)**.

Recommended order:

1. [Novelty Map](agents-ia/faouzi-ai-lab/00_NOVELTY_MAP.md)
2. [PromptBOM](agents-ia/faouzi-ai-lab/03_PROMPT_BOM.md)
3. [Intent Mutation Testing](agents-ia/faouzi-ai-lab/04_INTENT_MUTATION_TESTING.md)
4. [Agent Effect Manifest](agents-ia/faouzi-ai-lab/01_AGENT_EFFECT_MANIFEST.md)
5. [Behavioral SemVer](agents-ia/faouzi-ai-lab/05_BEHAVIORAL_SEMVER.md)

These projects focus on agent infrastructure rather than chatbot UI.

---

## ⚡ I am interested in n8n

Open the **[n8n Workflow Catalog](N8N_CATALOG.md)**.

Good starting points:

- [Multi-LLM Router](agents-ia/multi-llm-router/)
- [AI Marketing & Sales Director](agents-ia/directeur-marketing-ventes/)
- [AI Prospecting](agents-ia/prospection-ia/)
- [Facebook Posts + Reels](marketing/facebook-posts-reels/)
- [Google Sheets Stock Alert](ecommerce/alerte-rupture-stock-google-sheets/)

Always connect credentials in your own n8n environment. Never commit secrets to GitHub.

---

## 🔓 I am interested in Open Source

Read:

- [Open Source Principles](OPEN_SOURCE.md)
- [Roadmap](ROADMAP.md)
- [Contribution Guide](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)

The aim is to move projects through clear maturity stages:

```text
IDEA → SPECIFICATION → MVP → BETA → STABLE
```

---

## ⭐ Recommended first project

### PromptBOM

**Idea:** an SBOM-like inventory of everything an AI agent was told.

Why start here?

- easy to explain;
- useful for agent auditability;
- first MVP can work locally;
- no paid LLM API is required for the core scanner;
- suitable for CLI + GitHub Actions later.

➡️ [Read the PromptBOM specification](agents-ia/faouzi-ai-lab/03_PROMPT_BOM.md)

---

## Repository map

```text
workflow-store/
├── agents-ia/              # AI agents and AI workflows
│   └── faouzi-ai-lab/      # Experimental agent infrastructure
├── business/               # Business automation
├── ecommerce/              # E-commerce workflows
├── marketing/              # Marketing automation
├── productivity/           # Productivity workflows
├── rh/                     # HR workflows
├── salon/                  # Service-business workflows
├── assets/                 # Visual identity and banners
├── START_HERE.md
├── N8N_CATALOG.md
├── ROADMAP.md
├── OPEN_SOURCE.md
├── CONTRIBUTING.md
└── SECURITY.md
```

**FAOUZI AI LAB — Build • Test • Automate • Share**

# 🔓 Open Source Principles

FAOUZI AI LAB publishes practical experiments and documentation with a simple rule:

> **Be useful, testable and honest about what works.**

## 1. Clear maturity

Every project should identify its stage:

- **Idea** — early concept;
- **Specification** — design is documented;
- **MVP** — minimal implementation exists;
- **Beta** — usable but still requires validation;
- **Stable** — tested and documented for repeatable use.

## 2. No fake production claims

A concept document is not a finished tool.

A workflow that has not been tested with real credentials should not be described as production-ready.

## 3. Security before convenience

Public examples must never contain:

- secrets;
- API keys;
- customer data;
- private webhook tokens;
- production credentials.

## 4. Small useful components

Prefer:

```text
small + understandable + reusable
```

over:

```text
huge + opaque + impossible to test
```

## 5. Local-first when possible

Core functionality should avoid requiring a paid model API when the problem can be solved deterministically.

Example: the first PromptBOM scanner should be able to inventory files and hashes without calling an LLM.

## 6. Reproducible examples

A strong project should eventually provide:

- a quick start;
- sample input;
- expected output;
- test fixtures;
- failure examples.

## 7. AI-agent projects need behavioral tests

Traditional unit tests are not enough for autonomous systems.

Where relevant, test:

- tool selection;
- side effects;
- forbidden actions;
- approval behavior;
- intent preservation;
- token/cost limits;
- model substitution.

## 8. Research claims stay modest

It is rarely possible to prove that an idea has never existed.

Use wording such as:

- "experimental approach";
- "underserved problem";
- "differentiated angle";
- "research prototype".

Avoid unsupported claims such as "world's first" unless they can actually be demonstrated.

---

**FAOUZI AI LAB — Build • Test • Automate • Share**

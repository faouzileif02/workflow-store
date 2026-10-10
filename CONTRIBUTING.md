# 🤝 Contributing to FAOUZI AI LAB

Thanks for your interest in improving the project.

## Good contribution types

- bug fixes;
- clearer documentation;
- test fixtures;
- safer defaults;
- n8n workflow improvements;
- MCP adapters;
- multilingual test cases;
- agent evaluation ideas;
- local-model compatibility;
- reproducible examples.

## Before proposing a change

1. Read the target project's README.
2. Check its maturity status.
3. Do not include real credentials or private data.
4. Keep changes focused and explain what problem they solve.
5. Include a test or example when practical.

## AI-generated contributions

AI-assisted code is welcome, but it should be reviewed before submission.

Please verify:

- APIs actually exist;
- dependencies are real;
- examples run;
- no secret values are embedded;
- security assumptions are documented.

## n8n workflow contributions

For n8n projects, document:

- trigger type;
- required credentials;
- external APIs;
- expected input;
- expected output;
- error behavior;
- test procedure.

Never export active credentials.

## AI-agent contributions

For agent infrastructure, document the behavioral contract where relevant:

```text
Input / Intent
      ↓
Allowed actions
      ↓
Forbidden actions
      ↓
Expected effects
      ↓
Evaluation criteria
```

## Pull request checklist

- [ ] The change has one clear purpose.
- [ ] No credentials or personal data are included.
- [ ] Documentation is updated.
- [ ] New behavior has an example or test.
- [ ] Limitations are stated.
- [ ] Links and file paths work.

## Style

Prefer simple Markdown, portable code and minimal dependencies.

**Useful beats complicated. Reproducible beats impressive-looking.**

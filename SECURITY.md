# 🔐 Security Policy

Security matters because this repository contains examples involving APIs, automation, AI agents and external services.

## Never commit secrets

Do not publish:

- API keys;
- OAuth tokens;
- passwords;
- n8n credentials;
- webhook secrets;
- session cookies;
- private customer data;
- production database credentials;
- private paid workflow packages.

Use environment variables, secret stores and n8n Credentials instead.

## If a secret is exposed

1. Revoke or rotate it immediately.
2. Do not assume deleting the file from the latest commit is enough.
3. Review Git history and external logs.
4. Replace the compromised credential everywhere it was used.

## Agent-specific safety

AI agents that can call tools should document:

- allowed tools;
- forbidden effects;
- approval requirements;
- data access scope;
- external side effects;
- cost/spend limits;
- rollback behavior when available.

## Workflow safety

Before enabling a workflow in production:

- test with non-production accounts where possible;
- validate webhook authentication;
- verify API rate limits;
- check retry behavior;
- prevent duplicate sends/orders/actions;
- verify error paths;
- review any destructive node.

## Reporting

If you discover a security issue, avoid publishing sensitive exploit details or real credentials in a public Issue.

Describe the affected project, the impact and a safe reproduction using dummy data.

---

**Default rule: no secret should ever be required inside a public repository file.**

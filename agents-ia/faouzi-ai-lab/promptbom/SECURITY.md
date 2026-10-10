# Security Policy

PromptBOM inventories files that may influence agent behavior. Some of those files can contain sensitive information.

## Security properties

- The default core makes no network requests.
- Symlinks are skipped to avoid unintentionally scanning outside the selected project tree.
- Secret detection stores only the finding type and line number — never the matched value.
- `promptbom lock` refuses to create a lock when possible secrets are detected unless the operator explicitly uses `--allow-secrets`.

## Limitations

PromptBOM is not a dedicated secret scanner and its regex checks can produce false positives and false negatives.

Authority-conflict detection is heuristic and must not be treated as a formal safety proof.

## Reporting a vulnerability

Do not publish real credentials, tokens, private prompts, customer data or exploit details in a public issue. Use a private disclosure channel offered by the repository owner/platform when available.

# Contributing to PromptBOM

Thanks for helping improve PromptBOM.

## Principles

- Keep the default core **local-first** and **network-free**.
- Keep runtime dependencies at zero unless a major design decision explicitly changes that.
- Prefer deterministic behavior over model-dependent behavior.
- Never add code that writes detected secret values to reports or logs.
- Every behavior change should include a test.

## Development

```bash
python -m pip install -e .
python -m unittest discover -s tests -v
python -m promptbom scan examples/demo
python -m promptbom validate examples/demo
```

## Pull requests

Please explain the problem, behavior before/after, tests added, and any security or compatibility impact. Small, testable PRs are preferred.

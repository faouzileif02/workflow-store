# Changelog

## 0.2.1 — 2026-10-10

### Fixed
- Conflict detection recognizes permission forms such as `you may`, `can`, and `allowed to`, and matches high-signal action groups so approval-vs-automatic deployment conflicts are not missed.
- Claude Skill packaging builds a ZIP with exactly one `SKILL.md` and a visible-folder fixture that survives Claude import.
- Added true read-only scanning with `scan --no-write` / `--read-only`; it does not create `promptbom.json`.

### Added
- Regression tests for the Claude-discovered deployment conflict and read-only scan behavior.
- Reproducible Claude Skill builder and package validation.

## 0.2.0 — 2026-10-10

### Added
- `diff`, `report`, `validate`, and `init` commands.
- Markdown and standalone HTML reports.
- Local JSON configuration with custom classification rules.
- Claude, Cursor, GitHub Copilot, Gemini, MCP and cross-tool ecosystem tagging.
- Heuristic authority-conflict detection.
- Staleness tracking and skipped-file counters.
- Built-in invariant validator and expanded JSON Schema.
- Symlink safety.
- Unit tests and GitHub Actions CI.

### Changed
- Python 3.9 compatibility fixed by removing 3.10-only union syntax.
- Lock format now records tool version and ecosystem.

## 0.1.0
- Initial local-first inventory, lock, verify, show and secret-location detection MVP.

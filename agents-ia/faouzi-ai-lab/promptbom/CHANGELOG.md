# Changelog

## 0.2.0 — 2026-10-10

### Added
- `diff`, `report`, `validate`, and `init` commands.
- Markdown and standalone HTML reports.
- Local JSON configuration with custom classification rules.
- Claude, Cursor, GitHub Copilot, Gemini, MCP and cross-tool ecosystem tagging.
- Heuristic authority-conflict detection.
- Staleness tracking and skipped-file counters.
- Built-in invariant validator.
- Symlink safety.
- Unit tests and GitHub Actions CI.

### Changed
- Python 3.9 compatibility fixed by avoiding 3.10-only union syntax.
- Lock format records tool version and ecosystem.

## 0.1.0
- Initial local-first inventory, lock, verify, show and secret-location detection MVP.

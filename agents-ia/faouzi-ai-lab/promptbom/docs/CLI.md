# PromptBOM CLI

## `scan`

```bash
promptbom scan .
promptbom scan . -o build/promptbom.json
promptbom scan . --no-write
promptbom scan . --read-only --json
```

By default `scan` preserves the v0.2 behavior and writes `promptbom.json`. Use `--no-write` (alias `--read-only`) for a true read-only audit. Add `--json` to emit the full BOM on stdout.

## `show`

```bash
promptbom show .
```

Prints authority, kind, ecosystem, mutability, age, hash and path.

## `lock`

```bash
promptbom lock .
promptbom lock . --fail-on-conflicts
```

Writes both `promptbom.json` and `promptbom.lock`.

By default it refuses to lock when built-in secret patterns match. `--allow-secrets` is an explicit override; it still never stores the secret value.

## `verify`

```bash
promptbom verify .
promptbom verify . --fail-on-conflicts
```

Returns exit `1` when a tracked instruction source was added, removed or changed.

## `diff`

Against the current lock:

```bash
promptbom diff .
promptbom diff . --format json
promptbom diff . --exit-code
```

Between snapshots:

```bash
promptbom diff old-promptbom.json new-promptbom.json
```

## `report`

```bash
promptbom report .
promptbom report . --format html
promptbom report . --format markdown -o audit.md
```

HTML is standalone and contains no remote assets.

## `validate`

```bash
promptbom validate .
promptbom validate promptbom.json
```

The built-in validator checks the invariants PromptBOM relies on. The full JSON Schema is also available in `schema/promptbom.schema.json` for external validators.

## `init`

```bash
promptbom init .
```

Creates `promptbom.config.json`.

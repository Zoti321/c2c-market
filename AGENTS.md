## Agent skills

### Issue tracker

Issues live in this repo's GitHub Issues (via `gh` CLI). See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context layout: root `CONTEXT.md` + `docs/adr/`. See `docs/agents/domain.md`.

### Git workflow

Feature Branch → PR → CI Green → Merge to `main`. Code changes go through PR; do not push directly to `main`. v2 work uses short-lived branches from `main` (e.g. `feature/v2.1-search`). See `CONTRIBUTING.md` and roadmap [#19](https://github.com/Zoti321/c2c-market/issues/19).

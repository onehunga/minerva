# Minerva Agent Guide

## Repository boundaries

- `frontend/` and `backend/` are independent applications; run commands from the respective directory.
- Keep implementation, models, and tests grouped by feature. Do not create horizontal root-level layers that mix unrelated features.
- Do not edit `/docs` unless explicitly requested. It is the manually finished user documentation and includes screenshots/layout work.
- Put temporary plans, investigation notes, and vibe-coding helper files in `.vibe/`, not `/docs` or source directories.
- Do not add or update E2E tests unless explicitly requested.

## Frontend architecture

- Feature code belongs in `frontend/src/feature/<feature>/`. Follow the existing split where applicable: `<feature>.model.ts`, `<feature>.api.ts`, `<feature>.repository.ts`, `composables/`, `components/`, optional store, colocated `__tests__/`, and `index.ts`.
- Keep transport calls in `<feature>.api.ts`. Wrap them behind the feature repository interface; repositories may unwrap or normalize API responses.
- Application/UI logic consumes the injected repository, normally through `use<Feature>Repository()` and a feature composable. Register production repositories in `src/main.ts` with Vue `provide`/`inject`.
- New unit tests must replace repositories through `global.provide`; do not mock Axios or real HTTP when the repository boundary covers the behavior. This boundary also permits gradual API replacement per feature.
- Models belong in the feature's `.model.ts`; do not duplicate response or domain types in components.
- Composables currently act as the use-case/orchestration layer. Add a separate use-case module only when reusable non-UI application logic warrants it; do not introduce use-case classes as boilerplate.
- Route views in `src/views/` stay thin. Put substantial feature UI and behavior in `src/feature/<feature>/components/`.
- `frontend/src/components/ui/` contains shadcn-vue generated primitives. Do not edit it unless explicitly requested; feature components belong in their feature folder and shell/layout components in `src/components/`.
- The shared Axios client in `src/api/client.ts` owns `/api`, authentication headers, refresh, and retry behavior. Do not reproduce this in feature APIs.
- Pinia is for shared mutable feature state, not a mandatory layer for every feature. In `main.ts`, install Pinia before session initialization.

## Frontend style and verification

- Use the `@/` alias for `frontend/src/`. TypeScript enables `noUncheckedIndexedAccess`; handle indexed values accordingly.
- CI uses Bun and the committed lockfile. Focused checks from `frontend/`:

```bash
bun run test:unit:ci -- src/feature/<feature>/__tests__/<file>.spec.ts
bun run test:unit:ci -- src/feature/<feature>/__tests__/<file>.spec.ts -t "test name"
bun run lint:oxlint -- src/feature/<feature>/<file>
bun run type-check
```

- `bun run lint` is not read-only: its ESLint phase runs with `--fix`. Use `lint:oxlint` for a non-fixing lint check.
- Full frontend CI order is `bun run format:check`, `bun run lint`, `bun run test:unit:ci`, `bun run build`.

## Backend architecture

- Backend features are Spring Modulith modules below `backend/src/main/java/de/fallstudie/minerva/backend/<feature>/`.
- A feature root is its cross-module API: service interfaces, commands/DTOs, enums, and events. Other modules must not import another feature's `internal` package.
- Implementations belong under `<feature>/internal/`: `web` for controllers and transport records, `service` for use cases/listeners, and `persistence` for JPA `*Model` entities and Spring Data `*Repository` interfaces.
- Keep controllers thin. Validation, transactions, repository coordination, and event publication belong in services.
- Prefer feature-root APIs for synchronous cross-module calls and Spring events for cross-feature side effects.
- Preserve naming used by the codebase: `*Request`, `*Response`, `*ListResponse`, persistence `*Model`, `*Repository`, and plural `*Tests`.
- Module boundaries are enforced by `BackendApplicationTests.verifyModules`; run it after changing package dependencies.
- Database changes require a new zero-padded Liquibase YAML file in `src/main/resources/db/changelog/changes/` and an ordered include in `db.changelog-master.yaml`. Production Hibernate validates rather than creates the schema.

## Backend style and verification

- Java formatting is owned by Spotless; do not hand-format around it. Focused checks from `backend/`:

```bash
./gradlew test --tests 'fully.qualified.TestClass'
./gradlew test --tests 'fully.qualified.TestClass.testMethod'
./gradlew test --tests 'de.fallstudie.minerva.backend.BackendApplicationTests.verifyModules'
./gradlew spotlessCheck
```

- Full backend CI order, with `SPRING_PROFILES_ACTIVE=test`, is `./gradlew spotlessCheck`, `./gradlew check`, `./gradlew bootJar`.
- Service tests normally instantiate the subject directly with Mockito dependencies. Use `@DataJpaTest` only for persistence behavior; tests run against H2 in PostgreSQL mode with Liquibase disabled.

## Test scope

- Add a small number of targeted tests for meaningful branches, transformations, permissions, failure handling, or regressions. Include one representative negative case when it protects real behavior.
- Do not add tests for trivial rendering, pass-through code, framework behavior, or simple getters/setters.
- Prefer one or two focused tests for a feature over broad permutations. Reuse existing feature fixtures and repository injection instead of building parallel test infrastructure.

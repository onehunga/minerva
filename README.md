# Minerva

Ticketsystem mit Vue 3 Frontend, Spring Boot Backend und PostgreSQL.

## Docker

Der Stack besteht aus drei Containern: `frontend` (nginx + Vue), `backend` (Spring Boot, Port 8080 intern), `postgres` (PostgreSQL 17). Nur nginx ist vom Host aus erreichbar — Webseite und API laufen über denselben Origin, CORS entfällt.

### Starten

```bash
cp .env.example .env
docker compose up --build
```

Die Anwendung ist unter <http://localhost:8081> erreichbar. Der Port kann in `.env` über `MINERVA_PORT` geändert werden.

Beim ersten Start wird der Benutzer `admin` mit dem Passwort aus `INITIAL_ADMIN_PASSWORD` angelegt. Liquibase richtet das Datenbankschema automatisch ein.

### Stoppen & Aufräumen

```bash
docker compose down              # Container stoppen, Daten behalten
docker compose down --volumes    # Container + Datenbank-Volume löschen
```

### Konfiguration (`.env`)

| Variable | Beschreibung |
|---|---|
| `MINERVA_PORT` | Host-Port (Default `8081`) |
| `POSTGRES_DB` | Datenbankname |
| `POSTGRES_USER` | Datenbankbenutzer |
| `POSTGRES_PASSWORD` | Datenbankpasswort |
| `JWT_SECRET` | Secret für JWT-Tokens (mind. 32 Zeichen) |
| `INITIAL_ADMIN_PASSWORD` | Initiales Admin-Passwort |

### Images einzeln bauen

```bash
docker build -t minerva-frontend ./frontend
docker build -t minerva-backend ./backend
```

Beide Dockerfiles trennen Build und Laufzeit (Multi-Stage).

---

## Benutzerdokumentation (mdBook)

Die Benutzerdokumentation liegt unter `docs/` und wird mit [mdBook](https://rust-lang.github.io/mdBook/) gebaut:

```bash
mdbook build docs      # Erzeugt statische HTML-Seiten in docs/book/
mdbook serve docs      # Live-Vorschau auf http://localhost:3000
```

mdBook ist nicht Teil des Docker-Stacks und muss lokal installiert werden (`cargo install mdbook` oder über den Paketmanager). Das gebaute Verzeichnis `docs/book/` wird im Docker-Container unter `/docs/` ausgeliefert.

---

## Entwicklung

### Voraussetzungen

- Java 25
- Bun (oder Node.js ≥22.12)
- PostgreSQL (für lokale Backend-Entwicklung)
- mdBook (optional, für Dokumentation)

### Erste Schritte

```bash
bun install          # Repository-Root: installiert lefthook (Git-Hooks)
cd frontend && bun install
cd backend && ./gradlew build
```

### Git-Hooks

Im Root `bun install` ausführen, damit lefthook die Hooks einrichtet:

| Hook | Prüfungen |
|---|---|
| `pre-commit` | Formatierung, Linting, Type-Check (Frontend) + Spotless-Check (Backend) |
| `pre-push` | Unit-Tests (Frontend) + `./gradlew :test` (Backend) |

Manuell ausführen:

```bash
bun run lefthook run pre-commit
bun run lefthook run pre-push
```

### Code-Stil

- **Einrückung:** Tabs (Frontend und Backend)
- **Zeilenlänge:** ca. 100 Zeichen
- **Formatierung automatisch:** Frontend mit `oxfmt`, Backend mit Spotless
- Keine manuelle Java-Formatierung — Spotless übernimmt das.
- API-DTOs: Backend-Records und Frontend-Typen gleich benennen.

```bash
cd frontend && bun run format        # Formatiert src/
cd frontend && bun run format:check  # Prüft Formatierung
cd backend && ./gradlew spotlessApply
cd backend && ./gradlew spotlessCheck
```

---

## Frontend (Vue 3 + Vite + TypeScript)

### Dev-Server

```bash
cd frontend && bun run dev
```

Vite startet auf `http://localhost:5173`, `/api` wird nach `http://localhost:8080` weitergeleitet. Dafür muss das Backend separat laufen (`./gradlew bootRun`).

### Architektur

Jedes Feature in `frontend/src/feature/<name>/` folgt dieser Struktur:

```
feature/
  user/
    user.api.ts            # API-Aufrufe (Axios)
    user.model.ts          # TypeScript-Typen
    user.repository.ts     # Datenzugriffsschicht
    user.store.ts          # Pinia-Store (wenn benötigt)
    user.session.ts        # Session-Management
    composables/           # Vue Composables (useUser, useUsers, …)
    components/            # Feature-Komponenten
    __tests__/             # Unit-Tests (*.spec.ts)
    index.ts               # Barrel-Export
```

Features: `user`, `project`, `ticket`, `activity`, `dashboard`, `notification`.

### Komponenten

- **`components/ui/`**: shadcn-vue Komponenten (autogeneriert, nicht manuell bearbeiten)
- **`components/`**: App-weite Komponenten (AppSidebar, Layouts)
- **`feature/<name>/components/`**: Feature-spezifische Komponenten

### Styling

Tailwind CSS für den Großteil des Stylings. `CenteredPageLayout` und andere Wrapper für Layout-Struktur. Keine dekorativen Stile, Animationen oder Branding — funktionales, minimalistisches UI.

### Views & Router

Views liegen in `frontend/src/views/`, der Router in `frontend/src/router/index.ts`. Routen sind teils verschachtelt (z. B. `/admin/*`). Navigation erfolgt primär über die AppSidebar. Auth-Guard prüft `requiresAuth` und `requiredRole`.

### API-Client

Eigener Axios-Client (`frontend/src/api/client.ts`) mit:
- Automatischem Authorization-Header (Access Token aus Memory)
- Token-Refresh bei 401 (Refresh Token aus `sessionStorage`)
- `baseURL: "/api"` — Vite-Proxy strippt `/api` im Dev-Modus, nginx im Docker-Stack

---

## Backend (Spring Boot + Java 25 + Gradle)

### Lokal starten

PostgreSQL lokal muss laufen (Datenbank `minerva-dev`, Benutzer `minerva-dev`, Passwort `dev`, siehe `application.yml`):

```bash
cd backend && ./gradlew bootRun
```

### Paketstruktur

```
backend/src/main/java/de/fallstudie/minerva/backend/
  BackendApplication.java
  auth/          # Authentifizierung, JWT, Refresh-Tokens, Security
  user/          # Benutzer, Rollen, Bootstrap
  project/       # Projekte, Mitgliedschaften, Rollen, Berechtigungen
  projectsetup/  # Projekt-Erstellungsprozess
  ticket/        # Tickets, Workflow-Konfiguration, Ticket-Events
  activity/      # Aktivitäts-Events und -Protokollierung
  statistics/    # Dashboard-Abfragen und Statistiken
  notification/  # Benachrichtigungen
  common/        # Exceptions, globaler Error Handler
```

Jedes Feature-Paket hat `internal/`-Unterpakete (`persistence/`, `service/`, `policy/`, `web/`). Öffentliche Contracts (Records, DTOs, Interfaces) liegen direkt im Feature-Paket.

### Events

Zwei Event-Kategorien:

- **Sync-Events** (`TicketEvent`, `ProjectDeletedEvent` etc.): Werden in derselben Transaktion verarbeitet (Aktivitäts-Tracking, Konsistenz). `@TransactionalEventListener` mit `BEFORE_COMMIT`.
- **Async-Events** (Notifications): Laufen in separatem Thread, unabhängig vom Haupt-Flow. `@Async` + eigener TaskExecutor.

### Datenbank

Schema-Migrationen über Liquibase (`db/changelog/`). `ddl-auto: validate` — Hibernate validiert nur, Liquibase führt Änderungen aus. Test-Profil verwendet H2 (In-Memory).

---

## Tests

### Frontend (Vitest)

```bash
cd frontend && bun run test:unit          # Watch-Modus
cd frontend && bun run test:unit:ci       # Einzeldurchlauf
cd frontend && bun run test:unit:ci -- src/path/to/file.spec.ts  # Fokussiert
cd frontend && bun run test:unit:ci -- -t "test name"            # Nach Name
```

### Backend (JUnit 5)

```bash
cd backend && ./gradlew test
cd backend && ./gradlew test --tests "de.fallstudie…Klassenname"
cd backend && ./gradlew test --tests "de.fallstudie…Klassenname.methodenname"
```

### End-to-End (Playwright + Docker)

```bash
cd e2e && bun run test           # Baut Stack, führt Tests aus, räumt auf
cd e2e && bun run test:ui        # Interaktiver Playwright-UI-Modus
```

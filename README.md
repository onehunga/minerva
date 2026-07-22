# Minerva mit Docker

Der lokale Stack besteht aus drei Containern:

- `frontend`: nginx liefert das gebaute Vue-Frontend aus und leitet `/api` weiter.
- `backend`: Spring Boot läuft intern auf Port 8080.
- `postgres`: PostgreSQL speichert seine Daten in einem Docker-Volume.

Nur nginx ist vom Host aus erreichbar. Dadurch kommen Webseite und API vom gleichen Origin und benötigen keine CORS-Konfiguration.

## Starten

```bash
cp .env.example .env
docker compose up --build
```

Anschließend ist Minerva unter <http://localhost:8081> erreichbar. Ein anderer Port kann in `.env` über `MINERVA_PORT` eingestellt werden.

Beim ersten Start legt das Backend den Benutzer `admin` mit dem in `INITIAL_ADMIN_PASSWORD` gesetzten Passwort an. Liquibase erstellt und aktualisiert das Datenbankschema.

## Stoppen

Container stoppen, Daten aber behalten:

```bash
docker compose down
```

Container und lokale Datenbank löschen:

```bash
docker compose down --volumes
```

## Images einzeln bauen

```bash
docker build -t minerva-frontend ./frontend
docker build -t minerva-backend ./backend
```

Beide Dockerfiles trennen Build und Laufzeit. Das finale Image enthält nur nginx mit den statischen Frontend-Dateien beziehungsweise Java mit dem Backend-JAR.

## Spätere Releases

Dieselben Dockerfiles können in GitHub Actions mit `docker/build-push-action` gebaut und beispielsweise als `ghcr.io/<owner>/minerva-frontend:<tag>` und `ghcr.io/<owner>/minerva-backend:<tag>` veröffentlicht werden. Zugangsdaten und `JWT_SECRET` gehören dabei in GitHub Secrets und nicht ins Repository.

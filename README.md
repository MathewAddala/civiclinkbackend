# CivicLink Backend (Spring Boot)

This backend is scaffolded to match the current frontend API calls.

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me` (Bearer token required)
- `GET /api/issues` (Bearer token required)
- `POST /api/issues` (Bearer token required)
- `GET /api/projects` (Bearer token required)

## Run in STS

1. Open STS -> **Import** -> **Existing Maven Projects**
2. Select the `backend` folder
3. Let STS download dependencies
4. Run `CiviclinkBackendApplication`

Server runs on `http://localhost:8080`.

## Frontend env

Create/update frontend `.env`:

`VITE_API_BASE_URL=http://localhost:8080/api`

## Notes

- Uses in-memory H2 DB for now.
- Seed data for one issue and one project is added at startup.
- Replace `app.jwt.secret` in `application.properties` before production.

# CollegeBuddy

> A campus‑only social app with **.edu email sign‑up & verification**, profiles, friend finder, 1:1 messaging, and a campus events calendar. Built with **Spring Boot 3** + **React 18**.

<p align="center">
  <img src="https://github.com/DevaanshMann/CollegeBuddy/blob/master/CollegeBuddy.png" alt="CollegeBuddy" width="720"/>
</p>


[![Java](https://img.shields.io/badge/Java-21-007396?logo=java)](#) [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=spring-boot\&logoColor=white)](#) [![React](https://img.shields.io/badge/React-18-61DAFB?logo=react\&logoColor=black)](#) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql\&logoColor=white)](#) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

---

## Features

* **.edu email sign‑up + verification** (campus‑only access)
* **Profile** creation & editing with visibility controls (Campus / Friends / Private)
* **Friend Finder** (ranking by same school, mutuals, major & grad year proximity)
* **1:1 messaging** (REST + WebSocket/STOMP for real‑time updates)
* **Campus Events** calendar with RSVP (Going / Interested)
* **Privacy & Safety**: every read/write scoped by `school_id` from JWT; invite/report/block scaffolding

---

## Tech Stack

**Backend**: Java 21, Spring Boot 3 (Web, Security, Data JPA, Validation, Mail, WebSocket), Flyway, JJWT, Lombok
**DB**: PostgreSQL 15+, Redis (optional for sessions/rate‑limit)
**Frontend**: React 18 + TypeScript + Vite, React Router, React Query, Tailwind, shadcn/ui, STOMP/SockJS, FullCalendar
**Infra/Dev**: Docker Compose (Postgres + MailHog), IntelliJ IDEA, GitHub Actions (CI), SonarLint (local), SonarQube/SonarCloud (optional)

---

## Monorepo Layout

```
collegebuddy/
├─ apps/
│  ├─ api/            # Spring Boot backend (Maven)
│  └─ web/            # React + Vite frontend
├─ infra/
│  └─ docker-compose.yml  # Postgres + MailHog for local dev
└─ README.md
```

---

## Quick Start (Local)

**Prereqs**

* JDK **21**
* Node 18+ and **pnpm** (or npm)
* Docker Desktop (or Docker Engine)
* IntelliJ IDEA (Ultimate recommended) with **Lombok** plugin enabled

**1) Start dev services**

```bash
cd infra
docker compose up -d   # starts postgres:15 and mailhog
```

**2) Run the API**

```bash
cd apps/api
./mvnw spring-boot:run
```

API runs at **[http://localhost:8080](http://localhost:8080)** and auto-applies Flyway migrations.

**3) Run the Web**

```bash
cd apps/web
cp .env.example .env    # sets VITE_API_BASE=http://localhost:8080
pnpm install            # or npm i
pnpm dev                # or npm run dev
```

Web app → **[http://localhost:5173](http://localhost:5173)**

**4) Try it**

* Sign up with `you@sample.edu`.
* In dev, open MailHog at **[http://localhost:8025](http://localhost:8025)** to view verification emails.
* Login and explore friends/messages/events.

---

## Configuration

### Backend: `apps/api/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/collegebuddy
    username: postgres
    password: postgres
  jpa:
    hibernate.ddl-auto: validate
  flyway.enabled: true
  mail:
    host: localhost   # MailHog in dev
    port: 1025
app:
  jwt:
    issuer: collegebuddy
    accessTtl: 900s
    refreshTtl: 7d
    secret: change-me-in-dev
```

### Frontend: `apps/web/.env.example`

```
VITE_API_BASE=http://localhost:8080
```

> **Note**: For production, set strong secrets and real SMTP (e.g., SES/SendGrid). Never commit real secrets.

---

## Database Schema (Flyway)

See `apps/api/src/main/resources/db/migration/V1__init.sql` for tables:

* `schools`, `users`, `profiles`
* `friendships`, `friend_requests`
* `conversations`, `messages`
* `events`, `event_attendees`
* `email_verification_tokens`

---

## API Overview (MVP)

```
POST   /auth/signup             # .edu only
POST   /auth/login              # returns { accessToken }
POST   /auth/verify?token=...
GET    /me                      # current profile
PATCH  /me                      # update profile

POST   /friends/requests/{userId}
POST   /friends/accept/{requestId}
GET    /friends/suggested

GET    /conversations           # list or open 1:1
POST   /conversations/{userId}
GET    /messages/{conversationId}
POST   /messages/{conversationId}

GET    /events?from=&to=
POST   /events
POST   /events/{id}/rsvp
```

WebSocket (STOMP): `ws://localhost:8080/ws`
Topic: `/topic/dm/{conversationId}`
Send to: `/app/dm/{conversationId}`

---

## Dev Tips

### Built‑in HTTP Client (IntelliJ)

Create `apps/api/http/collegebuddy.http` and paste sample requests:

```http
### Signup
POST http://localhost:8080/auth/signup
Content-Type: application/json

{ "email": "you@sample.edu", "password": "pass1234" }

### Login (saves token)
POST http://localhost:8080/auth/login
Content-Type: application/json

{ "email": "you@sample.edu", "password": "pass1234" }
> {% client.global.set('token', response.body.accessToken); %}

### Authorized
GET http://localhost:8080/me
Authorization: Bearer {{token}}
```

### Code Quality

* **SonarLint** in IDE for instant feedback
* (Optional) **SonarQube/SonarCloud** in CI with a Quality Gate
* **Spotless/Prettier/ESLint** for formatting (see web `package.json`)

---

## Security Notes

* Enforce `.edu` at signup; every read/write must be scoped by `school_id` from JWT claims
* Use **BCrypt** for password hashing
* Store **JWT secret** as an environment variable (strong ≥ 256‑bit key)
* Rate limit auth endpoints, especially `/auth/signup` and `/auth/login`
* Validate file uploads (avatars) and serve via signed URLs (S3/R2)

---

## Deployment

* **PaaS**: Render, Railway, or Fly.io (Docker)
* **Env**: `DATABASE_URL`, `APP_JWT_SECRET`, `SPRING_MAIL_*`, `CORS_ORIGINS`
* **DB**: run Flyway migrations on startup
* **Static**: build React → serve via CDN or static hosting

Example Dockerfile stubs are in `apps/api` and `apps/web` (TBD in repo).

---

## Contributing

1. Fork → branch: `feat/short-description`
2. Run `docker compose up -d` for dev DB
3. Add/adjust Flyway migrations instead of editing tables directly
4. Write small PRs with screenshots or HTTP Client snippets

Conventional commits (recommended): `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`

---

## Roadmap

* [ ] Email verification link + rate‑limited resend
* [ ] Friend Finder ranking (mutuals / major / grad year)
* [ ] Message read receipts & typing indicators
* [ ] Event images & ICS export
* [ ] Admin tools (moderation, reports)

---

## License

MIT © CollegeBuddy

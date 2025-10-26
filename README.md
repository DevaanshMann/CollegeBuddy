# CollegeBuddy

## Start
```
docker compose up --build
```

## Shutdown
```
docker compose down
```

## Rebuild Backend (Spring Boot)
```
docker compose build backend
docker compose up -d --no-deps backend
```


[//]: # (# CollegeBuddy)

[//]: # ()
[//]: # (> A campus‑only social app with **.edu email sign‑up & verification**, profiles, friend finder, 1:1 messaging, and a campus events calendar. Built with **Spring Boot 3** + **React 18**.)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <img src="https://github.com/DevaanshMann/CollegeBuddy/blob/master/CollegeBuddy.png" alt="CollegeBuddy" width="720"/>)

[//]: # (</p>)

[//]: # ()
[//]: # ()
[//]: # ([![Java]&#40;https://img.shields.io/badge/Java-21-007396?logo=java&#41;]&#40;#&#41; [![Spring Boot]&#40;https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=spring-boot\&logoColor=white&#41;]&#40;#&#41; [![React]&#40;https://img.shields.io/badge/React-18-61DAFB?logo=react\&logoColor=black&#41;]&#40;#&#41; [![PostgreSQL]&#40;https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql\&logoColor=white&#41;]&#40;#&#41; [![License: MIT]&#40;https://img.shields.io/badge/License-MIT-yellow.svg&#41;]&#40;./LICENSE&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Features)

[//]: # ()
[//]: # (* **.edu email sign‑up + verification** &#40;campus‑only access&#41;)

[//]: # (* **Profile** creation & editing with visibility controls &#40;Campus / Friends / Private&#41;)

[//]: # (* **Friend Finder** &#40;ranking by same school, mutuals, major & grad year proximity&#41;)

[//]: # (* **1:1 messaging** &#40;REST + WebSocket/STOMP for real‑time updates&#41;)

[//]: # (* **Campus Events** calendar with RSVP &#40;Going / Interested&#41;)

[//]: # (* **Privacy & Safety**: every read/write scoped by `school_id` from JWT; invite/report/block scaffolding)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Tech Stack)

[//]: # ()
[//]: # (**Backend**: Java 21, Spring Boot 3 &#40;Web, Security, Data JPA, Validation, Mail, WebSocket&#41;, Flyway, JJWT, Lombok)

[//]: # (**DB**: PostgreSQL 15+, Redis &#40;optional for sessions/rate‑limit&#41;)

[//]: # (**Frontend**: React 18 + TypeScript + Vite, React Router, React Query, Tailwind, shadcn/ui, STOMP/SockJS, FullCalendar)

[//]: # (**Infra/Dev**: Docker Compose &#40;Postgres + MailHog&#41;, IntelliJ IDEA, GitHub Actions &#40;CI&#41;, SonarLint &#40;local&#41;, SonarQube/SonarCloud &#40;optional&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Monorepo Layout)

[//]: # ()
[//]: # (```)

[//]: # (collegebuddy/)

[//]: # (├─ apps/)

[//]: # (│  ├─ api/            # Spring Boot backend &#40;Maven&#41;)

[//]: # (│  └─ web/            # React + Vite frontend)

[//]: # (├─ infra/)

[//]: # (│  └─ docker-compose.yml  # Postgres + MailHog for local dev)

[//]: # (└─ README.md)

[//]: # (```)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Quick Start &#40;Local&#41;)

[//]: # ()
[//]: # (**Prereqs**)

[//]: # ()
[//]: # (* JDK **21**)

[//]: # (* Node 18+ and **pnpm** &#40;or npm&#41;)

[//]: # (* Docker Desktop &#40;or Docker Engine&#41;)

[//]: # (* IntelliJ IDEA &#40;Ultimate recommended&#41; with **Lombok** plugin enabled)

[//]: # ()
[//]: # (**1&#41; Start dev services**)

[//]: # ()
[//]: # (```bash)

[//]: # (cd infra)

[//]: # (docker compose up -d   # starts postgres:15 and mailhog)

[//]: # (```)

[//]: # ()
[//]: # (**2&#41; Run the API**)

[//]: # ()
[//]: # (```bash)

[//]: # (cd apps/api)

[//]: # (./mvnw spring-boot:run)

[//]: # (```)

[//]: # ()
[//]: # (API runs at **[http://localhost:8080]&#40;http://localhost:8080&#41;** and auto-applies Flyway migrations.)

[//]: # ()
[//]: # (**3&#41; Run the Web**)

[//]: # ()
[//]: # (```bash)

[//]: # (cd apps/web)

[//]: # (cp .env.example .env    # sets VITE_API_BASE=http://localhost:8080)

[//]: # (pnpm install            # or npm i)

[//]: # (pnpm dev                # or npm run dev)

[//]: # (```)

[//]: # ()
[//]: # (Web app → **[http://localhost:5173]&#40;http://localhost:5173&#41;**)

[//]: # ()
[//]: # (**4&#41; Try it**)

[//]: # ()
[//]: # (* Sign up with `you@sample.edu`.)

[//]: # (* In dev, open MailHog at **[http://localhost:8025]&#40;http://localhost:8025&#41;** to view verification emails.)

[//]: # (* Login and explore friends/messages/events.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Configuration)

[//]: # ()
[//]: # (### Backend: `apps/api/src/main/resources/application.yml`)

[//]: # ()
[//]: # (```yaml)

[//]: # (spring:)

[//]: # (  datasource:)

[//]: # (    url: jdbc:postgresql://localhost:5432/collegebuddy)

[//]: # (    username: postgres)

[//]: # (    password: postgres)

[//]: # (  jpa:)

[//]: # (    hibernate.ddl-auto: validate)

[//]: # (  flyway.enabled: true)

[//]: # (  mail:)

[//]: # (    host: localhost   # MailHog in dev)

[//]: # (    port: 1025)

[//]: # (app:)

[//]: # (  jwt:)

[//]: # (    issuer: collegebuddy)

[//]: # (    accessTtl: 900s)

[//]: # (    refreshTtl: 7d)

[//]: # (    secret: change-me-in-dev)

[//]: # (```)

[//]: # ()
[//]: # (### Frontend: `apps/web/.env.example`)

[//]: # ()
[//]: # (```)

[//]: # (VITE_API_BASE=http://localhost:8080)

[//]: # (```)

[//]: # ()
[//]: # (> **Note**: For production, set strong secrets and real SMTP &#40;e.g., SES/SendGrid&#41;. Never commit real secrets.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Database Schema &#40;Flyway&#41;)

[//]: # ()
[//]: # (See `apps/api/src/main/resources/db/migration/V1__init.sql` for tables:)

[//]: # ()
[//]: # (* `schools`, `users`, `profiles`)

[//]: # (* `friendships`, `friend_requests`)

[//]: # (* `conversations`, `messages`)

[//]: # (* `events`, `event_attendees`)

[//]: # (* `email_verification_tokens`)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## API Overview &#40;MVP&#41;)

[//]: # ()
[//]: # (```)

[//]: # (POST   /auth/signup             # .edu only)

[//]: # (POST   /auth/login              # returns { accessToken })

[//]: # (POST   /auth/verify?token=...)

[//]: # (GET    /me                      # current profile)

[//]: # (PATCH  /me                      # update profile)

[//]: # ()
[//]: # (POST   /friends/requests/{userId})

[//]: # (POST   /friends/accept/{requestId})

[//]: # (GET    /friends/suggested)

[//]: # ()
[//]: # (GET    /conversations           # list or open 1:1)

[//]: # (POST   /conversations/{userId})

[//]: # (GET    /messages/{conversationId})

[//]: # (POST   /messages/{conversationId})

[//]: # ()
[//]: # (GET    /events?from=&to=)

[//]: # (POST   /events)

[//]: # (POST   /events/{id}/rsvp)

[//]: # (```)

[//]: # ()
[//]: # (WebSocket &#40;STOMP&#41;: `ws://localhost:8080/ws`)

[//]: # (Topic: `/topic/dm/{conversationId}`)

[//]: # (Send to: `/app/dm/{conversationId}`)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Dev Tips)

[//]: # ()
[//]: # (### Built‑in HTTP Client &#40;IntelliJ&#41;)

[//]: # ()
[//]: # (Create `apps/api/http/collegebuddy.http` and paste sample requests:)

[//]: # ()
[//]: # (```http)

[//]: # (### Signup)

[//]: # (POST http://localhost:8080/auth/signup)

[//]: # (Content-Type: application/json)

[//]: # ()
[//]: # ({ "email": "you@sample.edu", "password": "pass1234" })

[//]: # ()
[//]: # (### Login &#40;saves token&#41;)

[//]: # (POST http://localhost:8080/auth/login)

[//]: # (Content-Type: application/json)

[//]: # ()
[//]: # ({ "email": "you@sample.edu", "password": "pass1234" })

[//]: # (> {% client.global.set&#40;'token', response.body.accessToken&#41;; %})

[//]: # ()
[//]: # (### Authorized)

[//]: # (GET http://localhost:8080/me)

[//]: # (Authorization: Bearer {{token}})

[//]: # (```)

[//]: # ()
[//]: # (### Code Quality)

[//]: # ()
[//]: # (* **SonarLint** in IDE for instant feedback)

[//]: # (* &#40;Optional&#41; **SonarQube/SonarCloud** in CI with a Quality Gate)

[//]: # (* **Spotless/Prettier/ESLint** for formatting &#40;see web `package.json`&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Security Notes)

[//]: # ()
[//]: # (* Enforce `.edu` at signup; every read/write must be scoped by `school_id` from JWT claims)

[//]: # (* Use **BCrypt** for password hashing)

[//]: # (* Store **JWT secret** as an environment variable &#40;strong ≥ 256‑bit key&#41;)

[//]: # (* Rate limit auth endpoints, especially `/auth/signup` and `/auth/login`)

[//]: # (* Validate file uploads &#40;avatars&#41; and serve via signed URLs &#40;S3/R2&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Deployment)

[//]: # ()
[//]: # (* **PaaS**: Render, Railway, or Fly.io &#40;Docker&#41;)

[//]: # (* **Env**: `DATABASE_URL`, `APP_JWT_SECRET`, `SPRING_MAIL_*`, `CORS_ORIGINS`)

[//]: # (* **DB**: run Flyway migrations on startup)

[//]: # (* **Static**: build React → serve via CDN or static hosting)

[//]: # ()
[//]: # (Example Dockerfile stubs are in `apps/api` and `apps/web` &#40;TBD in repo&#41;.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Contributing)

[//]: # ()
[//]: # (1. Fork → branch: `feat/short-description`)

[//]: # (2. Run `docker compose up -d` for dev DB)

[//]: # (3. Add/adjust Flyway migrations instead of editing tables directly)

[//]: # (4. Write small PRs with screenshots or HTTP Client snippets)

[//]: # ()
[//]: # (Conventional commits &#40;recommended&#41;: `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Roadmap)

[//]: # ()
[//]: # (* [ ] Email verification link + rate‑limited resend)

[//]: # (* [ ] Friend Finder ranking &#40;mutuals / major / grad year&#41;)

[//]: # (* [ ] Message read receipts & typing indicators)

[//]: # (* [ ] Event images & ICS export)

[//]: # (* [ ] Admin tools &#40;moderation, reports&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## License)

[//]: # ()
[//]: # (MIT © CollegeBuddy)

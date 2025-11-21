# CollegeBuddy

A social networking platform for college students to connect, share, and communicate.

## Features
- User authentication and profile management
- Friend connections and search
- Real-time messaging with WebSocket
- Media upload capabilities
- Email verification

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.3.4, PostgreSQL 16, Maven
- **Frontend**: React 19, TypeScript, Vite

## Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL 16 (or Docker)

## Quick Start

### 1. Start Database
```bash
docker compose up db -d
```

### 2. Start Backend
```bash
cd apps/backend
mvn spring-boot:run
```
Backend runs on http://localhost:8081

### 3. Start Frontend
```bash
cd apps/frontend
npm install
npm run dev
```
Frontend runs on http://localhost:3000

## Configuration

**Backend**: `apps/backend/src/main/resources/application.yml`
- Database: `jdbc:postgresql://localhost:5432/collegebuddy`
- JWT secret and TTL settings

**Frontend**: `apps/frontend/.env`
- `VITE_API_URL=http://localhost:8081`

## Docker (Alternative)
```bash
# Start everything
docker compose up --build

# Stop everything
docker compose down
```

## Project Structure
```
apps/
├── backend/       # Spring Boot API
└── frontend/      # React app
```

## Common Commands

**Backend**:
```bash
mvn clean package    # Build
mvn test            # Run tests
```

**Frontend**:
```bash
npm run build       # Build for production
npm run lint        # Lint code
```

## License
CS5800 coursework project.

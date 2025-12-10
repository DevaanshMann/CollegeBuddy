# CollegeBuddy

A modern social networking platform designed for college students to connect, communicate, and build their campus community.

## Features

### Core Functionality
- **User Authentication**: Secure JWT-based authentication with email verification
- **Profile Management**: Customizable user profiles with avatars, bios, and campus information
- **Campus-Specific Networking**: Multi-tenant architecture supporting multiple college campuses
- **Connection System**: Send, accept, and manage connection requests
- **Real-time Messaging**: Direct messaging between connected users
- **User Search**: Search for students by name, major, or graduation year
- **Groups**: Create and join campus groups for clubs, classes, or interests
- **Group Chat**: Real-time group messaging within groups
- **User Blocking**: Block unwanted users with privacy controls
- **Account Management**: Delete account with data cleanup
- **Admin Dashboard**: Administrative tools for managing users and content

### Security Features
- Email verification for new accounts
- Password reset via email (SendGrid)
- JWT token authentication with configurable expiration
- Campus domain validation
- CORS protection
- Role-based access control (STUDENT/ADMIN)
- User blocking and privacy controls

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.3.4
- **Language**: Java 21
- **Database**: PostgreSQL 16 with Flyway migrations
- **Security**: Spring Security 6.3.2 with JWT
- **Email**: SendGrid SMTP integration
- **Build Tool**: Maven
- **Architecture**: Layered architecture with clear separation of concerns

### Frontend
- **Framework**: React 19
- **Language**: TypeScript 5.9
- **Build Tool**: Vite 7.2
- **Styling**: Tailwind CSS 4.1
- **Routing**: React Router 7.9
- **UI Components**: Lucide React icons, Framer Motion animations
- **HTTP Client**: Fetch API with custom wrapper

### Design Patterns
The project implements 27+ design patterns including:
- Repository, Service Layer, DTO, Factory, Strategy, Builder, Facade
- MVC, Dependency Injection, Singleton, Template Method
- Context API, Custom Hooks, Higher-Order Components
- See detailed analysis in project documentation

## Prerequisites

- **Java**: 21 or higher
- **Node.js**: 18 or higher
- **PostgreSQL**: 16 (or use Docker)
- **Maven**: 3.8+ (bundled with Maven Wrapper)
- **SendGrid API Key**: For email functionality (optional for development)

## Quick Start

### 1. Environment Setup

Create a `.env` file in `apps/backend/` based on `.env.example`:

```bash
cp apps/backend/.env.example apps/backend/.env
```

**Required environment variables:**

**For Development:**
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/collegebuddy
DB_USERNAME=collegebuddy
DB_PASSWORD=collegebuddy

# JWT (minimum 32 characters)
JWT_SECRET=your_secure_jwt_secret_here_minimum_32_characters
JWT_TTL_SECONDS=3600

# Email (use 'logging' for dev to avoid sending real emails)
EMAIL_STRATEGY=logging
EMAIL_FROM=noreply@collegebuddy.app

# Frontend
FRONTEND_URL=http://localhost:3000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:5174
```

**For Production (collegebuddy.app):**
```bash
# Database (Cloud SQL or managed PostgreSQL)
DB_URL=jdbc:postgresql://your-cloud-sql-host:5432/collegebuddy
DB_USERNAME=collegebuddy_prod
DB_PASSWORD=<strong-random-password>

# JWT (CRITICAL: Generate new secret for production)
JWT_SECRET=<generate-with-openssl-rand-base64-64>
JWT_TTL_SECONDS=3600

# Email (SendGrid SMTP)
EMAIL_STRATEGY=smtp
SENDGRID_API_KEY=SG.your_actual_sendgrid_api_key
EMAIL_FROM=noreply@collegebuddy.app

# Frontend (your production domain)
FRONTEND_URL=https://collegebuddy.app

# CORS (production domains only)
CORS_ALLOWED_ORIGINS=https://collegebuddy.app,https://www.collegebuddy.app
```

### 2. Start Database

**Using Docker (Recommended):**
```bash
docker compose up db -d
```

**Using Local PostgreSQL:**
```bash
psql -U postgres
CREATE DATABASE collegebuddy;
CREATE USER collegebuddy WITH PASSWORD 'collegebuddy';
GRANT ALL PRIVILEGES ON DATABASE collegebuddy TO collegebuddy;
```

### 3. Start Backend

```bash
cd apps/backend
mvn clean install
mvn spring-boot:run
```

Backend runs on **http://localhost:8081**

The application will:
- Auto-run Flyway database migrations
- Initialize the database schema
- Start the REST API server

### 4. Start Frontend

```bash
cd apps/frontend
npm install
npm run dev
```

Frontend runs on **http://localhost:3000**

## Docker Deployment (Full Stack)

Run the entire application stack with Docker Compose:

```bash
# Start all services
docker compose up --build

# Start in detached mode
docker compose up -d --build

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

**Services:**
- Frontend: http://localhost:3000
- Backend: http://localhost:8081
- PostgreSQL: localhost:5432

## Project Structure

```
CS5800 - CollegeBuddy/
├── apps/
│   ├── backend/                    # Spring Boot API
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/collegebuddy/
│   │   │   │   │   ├── account/    # Account management
│   │   │   │   │   ├── admin/      # Admin features
│   │   │   │   │   ├── auth/       # Authentication & authorization
│   │   │   │   │   ├── blocking/   # User blocking
│   │   │   │   │   ├── common/     # Shared utilities
│   │   │   │   │   ├── config/     # Spring configuration
│   │   │   │   │   ├── connection/ # Friend connections
│   │   │   │   │   ├── dto/        # Data Transfer Objects
│   │   │   │   │   ├── email/      # Email service
│   │   │   │   │   ├── groups/     # Group features
│   │   │   │   │   ├── media/      # File upload/storage
│   │   │   │   │   ├── messaging/  # Direct messaging
│   │   │   │   │   ├── profile/    # User profiles
│   │   │   │   │   ├── repo/       # JPA repositories
│   │   │   │   │   ├── search/     # User search
│   │   │   │   │   └── security/   # Security components
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-prod.yml
│   │   │   │       └── db/migration/  # Flyway SQL migrations
│   │   │   └── test/               # Unit & integration tests
│   │   ├── .env.example
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   └── frontend/                   # React Application
│       ├── src/
│       │   ├── api/                # API client & endpoints
│       │   ├── components/         # Reusable UI components
│       │   │   └── ui/             # Base UI components
│       │   ├── contexts/           # React Context (Auth, Theme)
│       │   ├── pages/              # Page components
│       │   │   ├── Auth/           # Login, Signup
│       │   │   ├── Profile/        # User profile
│       │   │   ├── Home/           # Home feed
│       │   │   ├── Search/         # User search
│       │   │   ├── Connections/    # Friend connections
│       │   │   ├── Chat/           # Messaging
│       │   │   ├── Groups/         # Group features
│       │   │   ├── Admin/          # Admin dashboard
│       │   │   └── Settings/       # Account settings
│       │   ├── types/              # TypeScript types
│       │   └── App.tsx             # Main app component
│       ├── package.json
│       └── vite.config.ts
│
├── docker-compose.yml
├── CB-features-updates.md          # Feature documentation
└── README.md
```

## Configuration

### Backend Configuration

**Main config:** `apps/backend/src/main/resources/application.yml`

Key settings:
- **Server Port**: 8081
- **Database**: PostgreSQL with HikariCP connection pooling
- **JPA**: Hibernate with validation mode (no auto-DDL)
- **Flyway**: Automatic database migrations
- **File Upload**: 5MB max file size
- **JWT**: Configurable secret and TTL
- **Email**: SendGrid SMTP with TLS
- **CORS**: Configurable allowed origins

**Production config:** `application-prod.yml`
- Optimized logging levels
- Health endpoint configuration
- Stricter security settings

### Frontend Configuration

Create `apps/frontend/.env`:

```bash
VITE_API_URL=http://localhost:8081
```

For production, update to your deployed backend URL.

## API Documentation

### Authentication Endpoints
- `POST /auth/signup` - Register new user
- `POST /auth/login` - User login
- `POST /auth/verify-email` - Verify email with token
- `GET /auth/me` - Get current user
- `POST /auth/request-password-reset` - Request password reset
- `POST /auth/reset-password` - Reset password with token

### User & Profile
- `GET /profile` - Get current user's profile
- `GET /profile/{userId}` - Get user profile by ID
- `POST /profile` - Update profile
- `POST /profile/avatar` - Upload profile picture

### Connections
- `POST /connections/request/{userId}` - Send connection request
- `POST /connections/respond/{requestId}` - Accept/reject request
- `GET /connections` - Get all connections
- `GET /connections/requests` - Get pending requests
- `DELETE /connections/{userId}` - Remove connection

### Messaging
- `POST /messages/send` - Send message
- `GET /messages/conversation/{userId}` - Get conversation
- `GET /messages/conversations` - Get all conversations
- `POST /messages/mark-read/{userId}` - Mark messages as read

### Search & Groups
- `GET /search?query=...` - Search users
- `GET /groups` - Get all groups
- `POST /groups` - Create group
- `POST /groups/{groupId}/join` - Join group

### Admin (ADMIN role required)
- `GET /admin/users` - List all users
- `GET /admin/stats` - Get platform statistics
- `POST /admin/users/{userId}/status` - Update user status

## Development

### Backend Commands

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Build JAR
mvn clean package

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Skip tests
mvn clean package -DskipTests
```

### Frontend Commands

```bash
# Install dependencies
npm install

# Development server with hot reload
npm run dev

# Type checking
npm run build  # Includes tsc -b

# Lint code
npm run lint

# Production build
npm run build

# Preview production build
npm run preview
```

### Database Migrations

Flyway migrations are located in `apps/backend/src/main/resources/db/migration/`

**Naming convention:** `V{version}__{description}.sql`

Example: `V001__create_users_table.sql`

Migrations run automatically on application startup.

## Testing

### Backend Tests

```bash
cd apps/backend
mvn test
```

Test categories:
- Unit tests for services and utilities
- Integration tests for API endpoints
- Repository tests for database operations

### Frontend Tests

Testing infrastructure is set up with Vitest and React Testing Library:

```bash
cd apps/frontend
npm test
```

## Security Considerations

### Before Deploying to Production

1. **Change all default secrets**
   - Generate secure JWT_SECRET (256+ bits)
   - Use strong database passwords
   - Rotate SendGrid API keys

2. **Update CORS configuration**
   - Restrict allowed origins to production domains
   - Never use `allowedOrigins("*")` in production

3. **Environment variables**
   - Use GCP Secret Manager or similar
   - Never commit `.env` files
   - Verify `.env` is in `.gitignore`

4. **Database security**
   - Use Cloud SQL with SSL/TLS
   - Enable automated backups
   - Restrict network access

5. **Enable HTTPS**
   - Cloud Run provides HTTPS automatically
   - Configure SSL certificates for custom domains

## Deployment

### Option 1: GCP Cloud Run (Recommended)

**Backend:**
```bash
cd apps/backend
gcloud run deploy collegebuddy-backend \
  --source . \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="DB_URL=..." \
  --set-secrets="JWT_SECRET=jwt-secret:latest"
```

**Frontend:**
```bash
cd apps/frontend
npm run build
gsutil -m cp -r dist/* gs://your-bucket/
```

**Database:**
```bash
gcloud sql instances create collegebuddy-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1
```

### Option 2: Docker Compose (Self-Hosted)

```bash
docker compose -f docker-compose.yml up -d
```

See `CB-features-updates.md` for comprehensive deployment guide.

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running: `docker compose ps`
- Verify database credentials in `.env`
- Check port 8081 is not in use: `lsof -i :8081`
- Review logs: `mvn spring-boot:run`

### Frontend can't connect to backend
- Verify `VITE_API_URL` in `.env`
- Check backend is running on port 8081
- Check browser console for CORS errors
- Verify backend CORS configuration

### Database connection failed
- Ensure PostgreSQL is running
- Check credentials in `application.yml`
- Verify database `collegebuddy` exists
- Check Flyway migrations completed

### Email not sending
- Verify `SENDGRID_API_KEY` is set
- Check SendGrid account is active
- Verify sender domain in SendGrid
- Set `EMAIL_STRATEGY=logging` for development

## Contributing

This is a CS5800 coursework project. For development:

1. Create feature branches from `master`
2. Follow existing code structure and patterns
3. Write tests for new features
4. Update documentation as needed
5. Ensure all tests pass before committing

## Architecture

- **Backend**: Layered architecture with Repository, Service, and Controller layers
- **Frontend**: Component-based architecture with Context API for state management
- **Database**: PostgreSQL with normalized schema and foreign key constraints
- **Authentication**: Stateless JWT-based authentication
- **File Storage**: Local filesystem (configurable to cloud storage via Strategy pattern)
- **Email**: Strategy pattern supporting SMTP and logging implementations

## Support

For issues or questions:
- Check existing documentation in `CB-features-updates.md`
- Review API endpoints and configuration
- Check logs for error messages

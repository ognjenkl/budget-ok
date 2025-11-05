# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Budget OK** is an ATDD (Acceptance Test-Driven Development) sandbox project demonstrating personal budget management using an "envelope" budgeting system. The project uses a monorepo structure with separate frontend and backend components.

- **Documentation**: https://ognjenkl.github.io/budget-ok/
- **Project Board**: https://github.com/users/ognjenkl/projects/2
- **License**: MIT

## Architecture

**Pattern**: Monorepo with Frontend/Backend separation

### Components:

1. **Backend (monolith/)**: Spring Boot 3.5.6 + Java 17
   - REST API at `/api/*` endpoints
   - Deployed as Docker image to GitHub Container Registry
   - Port: 8080

2. **Frontend (budget-ok-frontend-web/)**: React 18 + TypeScript + Vite
   - Single Page Application
   - Ant Design UI components
   - TanStack React Query for state management
   - Dev server port: 5173
   - Proxies `/api/*` to backend

3. **System Tests (system-test/)**: Java-based E2E & Smoke tests
   - Playwright for UI testing
   - Separate Maven module
   - Runs after deployment in CI/CD

### CI/CD Pipeline:

Automated GitHub Actions pipeline with 5 stages:
1. **Commit Stage**: Build backend, publish Docker image
2. **Acceptance Stage**: Deploy and run system tests (runs every 30 min)
3. **QA Stage**: QA environment deployment
4. **QA Sign-off**: Manual approval
5. **Production Stage**: Production deployment

## Build & Development Commands

### Backend (Spring Boot)

```bash
# Build backend
cd monolith
./mvnw clean package

# Run backend (port 8080)
./mvnw spring-boot:run

# Run specific test
./mvnw test -Dtest=YourTestClass#testMethod

# Build Docker image (for testing locally)
./mvnw spring-boot:build-image

# Build without running tests
./mvnw clean package -DskipTests
```

### Frontend (React/Vite)

```bash
cd budget-ok-frontend-web

# Install dependencies
npm install

# Development server (port 5173, proxies /api to localhost:8090)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Type check
npx tsc --noEmit
```

### System Tests

```bash
cd system-test

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ApiE2eTest

# Run specific test method
./mvnw test -Dtest=ApiE2eTest#shouldCreateEnvelope
```

## Key Architecture Concepts

### Frontend-Backend Communication:

- **Protocol**: REST API via HTTP
- **Vite Proxy**: In development, frontend's Vite config proxies `/api` requests to `http://localhost:8090` (allows independent server running)
- **API Structure**: REST endpoints following `/api/*` pattern
- **HTTP Client**: Axios with React Query for state management

### Deployment Model:

- **Containerization**: Backend packaged as Docker image (Alpine JDK 25 in system-test, JDK 17 in monolith)
- **Registry**: Docker images published to GitHub Container Registry (ghcr.io)
- **Configuration**: Spring Boot `application.yml` for backend configuration
- **Secrets**: GitHub Secrets for registry authentication

### Testing Strategy:

- **Unit Tests**: Located alongside production code
- **Acceptance Tests**: E2E tests with Playwright for UI validation
- **Smoke Tests**: Quick validation tests after deployment
- **ATDD Approach**: Tests drive development from acceptance level down

## Common Development Workflows

### Setting Up Local Development:

1. Backend and Frontend can run independently
2. Frontend dev server: `npm run dev` (from budget-ok-frontend-web/)
3. Backend: `./mvnw spring-boot:run` (from monolith/)
4. Frontend proxy will forward `/api` calls to backend

### Adding a New Feature:

1. Write acceptance test in system-test/ (E2E or API test)
2. Implement backend REST endpoint in monolith/
3. Update frontend React components in budget-ok-frontend-web/
4. Verify with `npm run lint` and `./mvnw test`

## Monolith - Budget OK Envelope Management

The monolith includes a budget envelope management system (combining backend + static frontend) with CRUD operations for envelopes and expense tracking.

### Quick Start

```bash
# Run the application
cd monolith
./mvnw spring-boot:run

# Access the envelope manager at http://localhost:8080/envelopes
```

### Architecture

**Backend Components:**
- Models: `monolith/src/main/java/.../models/{Envelope,Expense}.java`
- Repository: `monolith/src/main/java/.../repositories/{EnvelopeRepository,InMemoryEnvelopeRepository}.java`
- Service: `monolith/src/main/java/.../services/{EnvelopeService,EnvelopeServiceImpl}.java`
- API Controller: `monolith/src/main/java/.../controllers/api/EnvelopeApiController.java`
- Web Controller: `monolith/src/main/java/.../controllers/web/EnvelopeWebController.java`

**Frontend:**
- Static HTML: `monolith/src/main/resources/static/envelopes.html`
- Vanilla JavaScript with Fetch API (no frameworks)

### REST API Endpoints

```
GET    /api/envelopes           - List all envelopes
GET    /api/envelopes/{id}      - Get envelope by ID
POST   /api/envelopes           - Create envelope
PUT    /api/envelopes/{id}      - Update envelope
DELETE /api/envelopes/{id}      - Delete envelope
POST   /api/envelopes/{id}/expenses - Add expense to envelope
```

### Current Implementation Details

- **Storage**: In-memory (HashMap-based) - resets on application restart
- **Data Model**: Envelopes with name, budget, and list of expenses
- **Expenses**: Support WITHDRAW (spending) and DEPOSIT (refunds) transaction types
- **UI**: Responsive grid layout with modals for create/edit/add-expense

### For More Details

See `BUDGET_OK_IMPLEMENTATION.md` for:
- Detailed API request examples
- Design decisions and limitations
- Future enhancement roadmap
- Security considerations
- File locations and structure

### Debugging Envelope Feature

- **Backend**: Check logs in `monolith/` output, add logging to `EnvelopeServiceImpl`
- **Frontend**: Use browser DevTools (F12), check Network tab for API calls
- **API Testing**: Use curl or Postman with examples from `BUDGET_OK_IMPLEMENTATION.md`

### Debugging:

- **Frontend**: Use browser DevTools, React DevTools extension
- **Backend**: Add logging to Spring Boot components, use IDE debugger
- **API Integration**: Use system-test E2E tests to validate contract

## Database

- **Technology**: PostgreSQL
- **Configuration**: Specified in deployment (external host configuration in `application.yml`)
- **Note**: Local development may use in-memory or test database based on Spring Boot profile

## Important Files & Patterns

### Backend:
- **Entrypoint**: `monolith/src/main/java/com/ognjen/template/monolith/MonolithApplication.java`
- **Controllers**: `monolith/src/main/java/com/ognjen/template/monolith/controllers/api/` (REST endpoints)
- **Configuration**: `monolith/src/main/resources/application.yml`

### Frontend:
- **Entrypoint**: `budget-ok-frontend-web/src/main.tsx`
- **Main App**: `budget-ok-frontend-web/src/App.tsx`
- **API Clients**: `budget-ok-frontend-web/src/api/*.ts`
- **Vite Config**: `budget-ok-frontend-web/vite.config.ts`

### Tests:
- **E2E Tests**: `system-test/src/test/java/com/ognjen/template/systemtest/e2etests/`
- **Smoke Tests**: `system-test/src/test/java/com/ognjen/template/systemtest/smoketests/`

## Build System Notes

- **Backend**: Uses Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Frontend**: Uses npm (Node package manager)
- **Java Version**: 17 for monolith build, 25 for system-test execution
- **No Gradle**: Project uses Maven exclusively for Java builds

## Key Dependencies & Versions

### Backend:
- Spring Boot 3.5.6
- Java 17

### Frontend:
- React 18.2.0
- TypeScript 5.8.3
- Vite 7.1.7
- Ant Design 5.27.4
- Axios 1.12.2
- TanStack React Query 5.90.2
- ESLint 9.36.0

### Testing:
- Playwright 1.44.0 (E2E testing)

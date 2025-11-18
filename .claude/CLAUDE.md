# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project Overview

**Budget OK** is an ATDD (Acceptance Test-Driven Development) sandbox project demonstrating personal budget management using an "envelope" budgeting system.

- **Documentation**: https://ognjenkl.github.io/budget-ok/
- **Project Board**: https://github.com/users/ognjenkl/projects/2
- **License**: MIT

## Architecture

**Pattern**: Monorepo with Frontend/Backend separation

### Components:

1. **Backend** (Spring Boot 3.5.6 + Java 17)
   - REST API at `/api/*` on port 8080
   - PostgreSQL + Spring Data JDBC + Liquibase
   - Docker image in GitHub Container Registry

2. **Frontend** (React 18 + TypeScript + Vite)
   - SPA with Ant Design on port 5173
   - Proxies `/api/*` to backend
   - TanStack React Query for state management

3. **System Tests** (Java E2E/API tests)
   - HTTP client-based API testing (JUnit 5)
   - Playwright for UI testing
   - Runs after deployment

### CI/CD Pipeline:
1. **Commit**: Build backend, publish Docker image
2. **Acceptance**: Deploy and run system tests
3. **QA**: QA environment deployment + sign-off
4. **Production**: Production deployment

## Build & Development Commands

### Backend
```bash
cd backend
mvn clean package              # Build
mvn spring-boot:run           # Run (port 8080)
mvn test -Dtest=TestClass     # Run tests
mvn spring-boot:build-image   # Docker image
mvn clean package -DskipTests # Build without tests
```

### Frontend
```bash
cd budget-ok-frontend-web
npm install                # Install
npm run dev              # Dev server (port 5173)
npm run build            # Production build
npm run preview          # Preview build
npm run lint             # Lint code
npx tsc --noEmit        # Type check
```

### System Tests
```bash
cd system-test
mvn test                                    # All tests
mvn test -Dtest=EnvelopeCrudE2eTest        # Specific class
mvn test -Dtest=EnvelopeCrudE2eTest#testName # Specific method
```

## Communication & Deployment

- **Frontend-Backend**: REST API via HTTP (frontend Vite proxy forwards `/api` requests to backend:8080)
- **External APIs**: Bank OK integration via DummyJSON (configurable host in `application.yml`)
- **Containerization**: Docker images with JDK 17 (backend), JDK 25 (tests)
- **Configuration**: `backend/src/main/resources/application.yml` with environment variable overrides

## Envelope Management System

### REST API Endpoints

**Envelope Management:**
```
GET    /api/envelopes              - List all
GET    /api/envelopes/{id}         - Get by ID
POST   /api/envelopes              - Create
PUT    /api/envelopes/{id}         - Update
DELETE /api/envelopes/{id}         - Delete
POST   /api/envelopes/{id}/expenses - Add expense
POST   /api/envelopes/transfer     - Transfer between envelopes
```

**Bank OK (DummyJSON proxy):**
```
GET    /api/bankok/carts/{userId}  - Fetch cart
GET    /api/bankok/carts/id/{cartId} - Fetch by cart ID
```

### Core Implementation

**Data Model:**
- Envelopes: id, name, budget
- Expenses: id, amount, memo, transactionType (WITHDRAW/DEPOSIT), envelope_id (FK)
- Balance: Calculated as `budget - totalExpenses` via `@JsonProperty` getter

**Key Features:**
- Expense types: WITHDRAW (spending) and DEPOSIT (refunds)
- Balance calculation: On-the-fly via `getBalance()` method (no DB field needed)
- Bank OK integration: REST proxy using Spring `RestTemplate` to external DummyJSON API
- Transaction management: `@Transactional` annotations on write methods
- Security: Spring Security with public endpoints in `permitAll()` matchers

**Backend Structure:**
- Models: `application/{Envelope,Expense,BankOkCart,BankOkProduct}.java`
- DTOs: `application/{EnvelopeDto,ExpenseDto}.java`
- Repository: `infrastructure/EnvelopeRepository.java`
- Service: `application/{EnvelopeService,EnvelopeServiceImpl}.java`
- Controllers: `presentation/{EnvelopeController,BankOkApiController}.java`
- Database: `resources/db/changelog/xml/001-initial-schema.xml` (Liquibase)

### Development Workflow

1. Write acceptance test in `system-test/src/test/java/.../e2etests/`
2. Implement backend:
   - Add entity/DTO in `application/`
   - Create migration in `resources/db/changelog/xml/`
   - Implement service in `application/Service.java`
   - Add REST endpoint in `presentation/Controller.java`
3. Update frontend React components in `budget-ok-frontend-web/src/`
4. Run tests: `mvn clean package && mvn test`

## Common Issues & Fixes

### Repository methods returning null/empty in tests
**Cause**: `InMemoryEnvelopeRepository` methods not implemented
**Fix**: Implement `findById()` to search list, `findAll()` to return list, `deleteById()` to remove item:
```java
@Override
public List<Envelope> findAll() {
    return new ArrayList<>(envelopes);
}

@Override
public Envelope findById(Long id) {
    return envelopes.stream()
        .filter(env -> env.getId().equals(id))
        .findFirst()
        .orElse(null);
}

@Override
public void deleteById(Long id) {
    envelopes.removeIf(env -> env.getId().equals(id));
}
```

### GET endpoint returns 200 instead of 404 for missing resource
**Cause**: Controller method doesn't check if entity exists
**Fix**: Return `ResponseEntity` with 404 check:
```java
@GetMapping("/{id}")
public ResponseEntity<Envelope> getEnvelopeById(@PathVariable long id) {
    Envelope envelope = envelopeService.getById(id);
    if (envelope == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(envelope);
}
```

### POST endpoint returns 500: Field missing from database
**Cause**: Entity field added but migration not applied
**Fix**: Add Liquibase changeset in `001-initial-schema.xml`:
```xml
<changeSet id="3" author="system">
    <addColumn tableName="envelope_items">
        <column name="transaction_type" type="VARCHAR(50)"/>
    </addColumn>
</changeSet>
```
Also use `@Column("column_name")` on entity field. Rebuild and Liquibase runs pending migrations.

### "cannot execute UPDATE in a read-only transaction" error
**Cause**: Method missing `@Transactional` when class-level has `readOnly = true`
**Fix**: Add `@Transactional` to write methods:
```java
@Override
@Transactional  // Overrides class-level readOnly = true
public Envelope addExpense(long id, ExpenseDto expenseDto) { }
```

### Balance field missing from JSON response
**Cause**: Envelope lacks balance field serialization
**Fix**: Add calculated balance getter with `@JsonProperty`:
```java
@JsonProperty("balance")
public int getBalance() {
    if (expenses == null || expenses.isEmpty()) return budget;
    int balance = budget;
    for (Expense expense : expenses) {
        if ("WITHDRAW".equals(expense.getTransactionType())) {
            balance -= expense.getAmount();
        } else if ("DEPOSIT".equals(expense.getTransactionType())) {
            balance += expense.getAmount();
        }
    }
    return balance;
}
```
Benefits: No DB changes, handles WITHDRAW and DEPOSIT transactions, computed on-the-fly, automatically serialized.

## Key Files & Paths

**Backend:**
- Entrypoint: `backend/src/main/java/com/ognjen/budgetok/BudgetOkBackendApplication.java`
- Controllers: `backend/src/main/java/com/ognjen/budgetok/presentation/`
- Services: `backend/src/main/java/com/ognjen/budgetok/application/`
- Models: `backend/src/main/java/com/ognjen/budgetok/application/`
- Database: `backend/src/main/resources/db/changelog/xml/`
- Config: `backend/src/main/resources/application.yml`

**Frontend:**
- Entrypoint: `budget-ok-frontend-web/src/main.tsx`
- App: `budget-ok-frontend-web/src/App.tsx`
- API clients: `budget-ok-frontend-web/src/api/*.ts`
- Vite config: `budget-ok-frontend-web/vite.config.ts`

**Tests:**
- E2E tests: `system-test/src/test/java/.../e2etests/`
  - `EnvelopeCrudE2eTest`: CRUD operations
  - `ExpenseCrudE2eTest`: Expense operations (WITHDRAW/DEPOSIT)
  - `EnvelopeTransferE2eTest`: Transfers with balance validation
  - `BankOkEnvelopeIntegrationE2eTest`: Bank OK integration
  - `ApiE2eTest`: Legacy basic API test
- Smoke tests: `system-test/src/test/java/.../smoketests/`

## Build System & Dependencies

**Build:**
- Backend: Maven 3.x+ (Java 17 compilation, Spring Boot 3.5.6 runtime)
  - Output: `backend/target/backend-0.0.1-SNAPSHOT.jar`
- Frontend: npm (Node v20+)
- Database: PostgreSQL (local development)

**Backend Dependencies:**
- Spring Boot 3.5.6, Spring Data JDBC, PostgreSQL Driver
- Liquibase 4.4+, Lombok

**Frontend Dependencies:**
- React 18.2.0, TypeScript 5.8.3, Vite 7.1.7
- Ant Design 5.27.4, Axios 1.12.2, React Query 5.90.2
- ESLint 9.36.0

**Testing:**
- JUnit 5 (backend unit & E2E tests)
- Playwright 1.44.0 (UI testing)

## Recent Changes

### Monolith Removal (Latest)
- Deleted monolith directory - no longer maintaining dual implementation
- Architecture now: Backend (Spring Boot) + Frontend (React) + System Tests
- All envelope management features in backend with REST API

### Test Fixes
- Fixed `InMemoryEnvelopeRepository` implementations (findAll, findById, deleteById)
- Added 404 NOT_FOUND response for missing envelopes in getEnvelopeById()
- All 19 backend tests passing (ParamsTest, BudgetOkApplicationTests, EnvelopeComponentTest, EnvelopeServiceTest)

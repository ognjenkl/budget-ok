# Budget OK - Monolith Implementation

This document describes the budget management functionality added to the monolith using static HTML pages.

## Features Implemented

### 1. Envelope Management
- **Create Envelopes**: Create new budget envelopes with a name and budget amount
- **View Envelopes**: Display all envelopes with budget information, balance, and expense history
- **Edit Envelopes**: Update envelope name and budget amount
- **Delete Envelopes**: Remove envelopes from the system
- **Balance Calculation**: Automatic calculation of remaining balance based on expenses

### 2. Expense Tracking
- **Add Expenses**: Add transactions (withdrawals or deposits) to envelopes
- **Transaction Types**: Support for WITHDRAW (spending) and DEPOSIT (refunds/credits)
- **Expense History**: View recent expenses for each envelope
- **Automatic Calculation**: Real-time balance updates

### 3. Transfer Functionality
- **Transfer Between Envelopes**: Move amounts from one envelope to another
- **Balance Validation**: Ensures sufficient balance before transfer (business logic)
- **Atomic Operations**: Both source and target envelopes updated consistently
- **Transaction Records**: Creates WITHDRAW in source and DEPOSIT in target
- **Error Handling**: Returns appropriate HTTP status codes (400 for insufficient funds, 404 for not found)

### 4. Bank OK External System Integration
- **DummyJSON Integration**: Connects to DummyJSON API for cart/expense data
- **Cart Retrieval**: Fetch carts by user ID or cart ID
- **Configurable Host**: External API host configurable via `bankok.api.host` property
- **REST Proxy**: Exposes `/api/bankok/*` endpoints for cart data

## Architecture

### Backend Components

#### Domain Models
- **Envelope.java** (`monolith/src/main/java/.../models/Envelope.java`)
  - Represents a budget envelope
  - Properties: id, name, budget, expenses list
  - Methods: addExpense(), getBalance()

- **Expense.java** (`monolith/src/main/java/.../models/Expense.java`)
  - Represents a single transaction
  - Properties: id, envelopeId, amount, memo, transactionType, date
  - Transaction types: WITHDRAW, DEPOSIT

- **BankOkCart.java** (`monolith/src/main/java/.../models/BankOkCart.java`)
  - Maps DummyJSON cart structure from external API
  - Properties: id, userId, totalProducts, totalQuantity, total, discountedTotal, products
  - Used for integrating with Bank OK external system

- **BankOkProduct.java** (`monolith/src/main/java/.../models/BankOkProduct.java`)
  - Represents a product in a cart from Bank OK
  - Properties: id, title, price, quantity, total, discountedPrice
  - Maps to expense items for import into envelopes

#### Repository Layer
- **EnvelopeRepository.java** - Interface for data access operations
- **InMemoryEnvelopeRepository.java** - In-memory implementation using HashMap
  - Stores envelopes in memory (resets on application restart)
  - Supports CRUD operations with auto-incrementing IDs

#### Service Layer
- **EnvelopeService.java** - Business logic interface
  - Includes `transferAmount()` method for inter-envelope transfers
- **EnvelopeServiceImpl.java** - Service implementation
  - Handles envelope creation, updates, and deletion
  - Manages expense additions
  - Implements transfer logic with balance validation
  - Validates operations

#### Controller Layer
- **EnvelopeApiController.java** - REST API endpoints for envelope management
  - `GET /api/envelopes` - Get all envelopes
  - `GET /api/envelopes/{id}` - Get envelope by ID
  - `POST /api/envelopes` - Create new envelope
  - `PUT /api/envelopes/{id}` - Update envelope
  - `DELETE /api/envelopes/{id}` - Delete envelope
  - `POST /api/envelopes/{id}/expenses` - Add expense to envelope
  - `POST /api/envelopes/transfer` - Transfer between envelopes (with request/response DTOs)

- **BankOkApiController.java** - REST API endpoints for Bank OK integration
  - `GET /api/bankok/carts/{userId}` - Fetch cart by user ID
  - `GET /api/bankok/carts/id/{cartId}` - Fetch cart by cart ID

- **EnvelopeWebController.java** - Serves the static HTML page
  - `GET /envelopes` - Serves envelopes.html

### Frontend Components

#### Static HTML Page
- **envelopes.html** (`monolith/src/main/resources/static/envelopes.html`)
  - Responsive grid layout for envelope cards
  - Modal dialogs for creating/editing envelopes
  - Modal for adding expenses
  - Real-time UI updates
  - Error handling and user feedback

#### Vanilla JavaScript Features
- Fetch API for REST communication
- DOM manipulation for dynamic content
- Event handling for user interactions
- Form validation
- Message notifications
- HTML escaping for security

## Technical Stack

- **Backend**: Spring Boot 3.5.6, Java 17
- **Storage**: In-memory (HashMap-based)
- **Frontend**: Vanilla JavaScript (no frameworks)
- **UI**: Pure CSS with responsive design
- **API**: RESTful with JSON

## File Locations

```
monolith/
├── src/main/java/com/ognjen/template/monolith/
│   ├── models/
│   │   ├── Envelope.java
│   │   ├── Expense.java
│   │   ├── BankOkCart.java
│   │   └── BankOkProduct.java
│   ├── repositories/
│   │   ├── EnvelopeRepository.java
│   │   └── InMemoryEnvelopeRepository.java
│   ├── services/
│   │   ├── EnvelopeService.java
│   │   └── EnvelopeServiceImpl.java
│   └── controllers/
│       ├── api/
│       │   ├── EnvelopeApiController.java
│       │   └── BankOkApiController.java
│       └── web/
│           └── EnvelopeWebController.java
└── src/main/resources/static/
    ├── envelopes.html
    └── index.html (redirects to /envelopes)
```

## Running the Application

```bash
# Build the project
cd monolith
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Application will be available at http://localhost:8080
# Navigate to /envelopes for the budget manager
```

## API Request Examples

### Create Envelope
```bash
POST /api/envelopes
Content-Type: application/json

{
  "name": "Groceries",
  "budget": 200
}
```

### Add Expense
```bash
POST /api/envelopes/1/expenses
Content-Type: application/json

{
  "amount": 45,
  "memo": "Weekly groceries",
  "transactionType": "WITHDRAW"
}
```

### Get All Envelopes
```bash
GET /api/envelopes
```

### Update Envelope
```bash
PUT /api/envelopes/1
Content-Type: application/json

{
  "name": "Groceries & Food",
  "budget": 250
}
```

### Delete Envelope
```bash
DELETE /api/envelopes/1
```

### Transfer Between Envelopes
```bash
POST /api/envelopes/transfer
Content-Type: application/json

{
  "sourceEnvelopeId": 1,
  "targetEnvelopeId": 2,
  "amount": 200,
  "memo": "Transferring from Groceries to Entertainment"
}
```

### Fetch Bank OK Cart by User
```bash
GET /api/bankok/carts/1
```

### Fetch Bank OK Cart by ID
```bash
GET /api/bankok/carts/id/1
```

## Features & Limitations

### Features
✅ Full CRUD operations for envelopes
✅ Add expenses to envelopes
✅ Transfer amounts between envelopes with balance validation
✅ Bank OK external system integration (DummyJSON)
✅ Real-time balance calculation
✅ Transaction history per envelope
✅ Responsive UI design
✅ Form validation
✅ Error handling
✅ User-friendly interface
✅ 7 comprehensive E2E tests (transfer logic, Bank OK integration, error scenarios)

### Limitations
- Data is stored in-memory (resets on application restart)
- No database persistence
- Single user (no authentication)
- No multi-user support
- Limited transaction filtering/searching
- External API (Bank OK) connection timeout handling not implemented
- No rate limiting on API endpoints

## E2E Tests

### Test Files
Located at: `system-test/src/test/java/com/ognjen/template/systemtest/e2etests/`

### Test Suites

#### 1. BankOkApiE2eTest (2 tests)
- **getBankOkCart_shouldReturnCartWithProducts**: Verifies connection to DummyJSON external API
- **getBankOkCartById_shouldReturnSpecificCart**: Verifies fetching specific cart by ID
- **Coverage**: External system integration testing

#### 2. EnvelopeTransferE2eTest (3 tests)
- **transferAmount_shouldSucceed_whenSufficientBalance**: Happy path transfer validation
- **transferAmount_shouldFail_whenInsufficientBalance**: Business logic - insufficient funds protection
- **transferAmount_shouldFail_whenTargetEnvelopeNotFound**: Error handling - 404 not found
- **Coverage**: CRUD operations, business logic validation, error scenarios

#### 3. BankOkEnvelopeIntegrationE2eTest (2 tests)
- **syncExpensesFromBankOk_shouldCreateExpensesInEnvelope**: End-to-end Bank OK → Envelope workflow
- **multipleBankOkCartsCanBeImportedAsEnvelopes**: Multiple cart import integration
- **Coverage**: Integration testing, combined feature workflows

### Running Tests

```bash
# Run all E2E tests
cd system-test
./mvnw test

# Run specific test class
./mvnw test -Dtest=EnvelopeTransferE2eTest

# Run specific test method
./mvnw test -Dtest=EnvelopeTransferE2eTest#transferAmount_shouldSucceed_whenSufficientBalance
```

### Test Results
All 7 tests passing with no failures or errors.

## Future Enhancements

1. **Persistence**: Add database layer (PostgreSQL, H2)
2. **Authentication**: Add user authentication
3. **Search & Filter**: Add expense filtering by date, type
4. **Reports**: Generate budget reports and statistics
5. **Categories**: Support expense categories
6. **Goals**: Add savings goals per envelope
7. **Export**: Export data to CSV/PDF
8. **Mobile**: Optimize for mobile devices

## Security Considerations

- HTML escaping applied to user input in frontend
- Input validation on both frontend and backend
- CSRF protection (inherent in Spring Boot)
- No sensitive data in response bodies
- Error messages are generic (don't leak system info)

## Notes

- The implementation uses in-memory storage for simplicity
- Data persists only during the application session
- All IDs are auto-generated using AtomicLong
- Dates are stored as ISO format strings from LocalDateTime
- Transaction types are stored as uppercase strings (WITHDRAW, DEPOSIT)

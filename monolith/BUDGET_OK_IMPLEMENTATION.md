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

#### Repository Layer
- **EnvelopeRepository.java** - Interface for data access operations
- **InMemoryEnvelopeRepository.java** - In-memory implementation using HashMap
  - Stores envelopes in memory (resets on application restart)
  - Supports CRUD operations with auto-incrementing IDs

#### Service Layer
- **EnvelopeService.java** - Business logic interface
- **EnvelopeServiceImpl.java** - Service implementation
  - Handles envelope creation, updates, and deletion
  - Manages expense additions
  - Validates operations

#### Controller Layer
- **EnvelopeApiController.java** - REST API endpoints
  - `GET /api/envelopes` - Get all envelopes
  - `GET /api/envelopes/{id}` - Get envelope by ID
  - `POST /api/envelopes` - Create new envelope
  - `PUT /api/envelopes/{id}` - Update envelope
  - `DELETE /api/envelopes/{id}` - Delete envelope
  - `POST /api/envelopes/{id}/expenses` - Add expense to envelope

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
│   │   └── Expense.java
│   ├── repositories/
│   │   ├── EnvelopeRepository.java
│   │   └── InMemoryEnvelopeRepository.java
│   ├── services/
│   │   ├── EnvelopeService.java
│   │   └── EnvelopeServiceImpl.java
│   └── controllers/
│       ├── api/
│       │   └── EnvelopeApiController.java
│       └── web/
│           └── EnvelopeWebController.java
└── src/main/resources/static/
    ├── envelopes.html
    └── index.html (updated with link)
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

## Features & Limitations

### Features
✅ Full CRUD operations for envelopes
✅ Add expenses to envelopes
✅ Real-time balance calculation
✅ Transaction history per envelope
✅ Responsive UI design
✅ Form validation
✅ Error handling
✅ User-friendly interface

### Limitations
- Data is stored in-memory (resets on application restart)
- No database persistence
- Single user (no authentication)
- No multi-user support
- Limited transaction filtering/searching

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

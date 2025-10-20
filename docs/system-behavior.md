# System Behavior (Use Cases)

## Use Case Diagram

![Use Case Diagram](images/use-case-diagram.png)

Primary Actors:
- Client

Use Cases:
- Add envelope
- View envelopes
- Rename envelope
- Delete envelope
- Add expense to envelope
- Change expense
- Delete expense
- Transfere amount from one envelope to another

Secondary Actors:
- Bank OK

## Use Case Narrative: Add expense to envelope

### Use Case Name
Add expense to envelope

### Primary Actor
Client

### Goal
The client successfully adds an expense to envelope.

### Preconditions
- The client is registered and logged into the system.

### Main Success Scenario
1. The client enters expense name.
2. The client enters expense amount.
3. The client submits the expense.
4. The system validates the name length.
5. The system validates the amount is positive.
6. The system creates the expense.
7. The system provides an expense confirmation to the client.

### Extensions (Alternative Flows)
- 4a. Name length is too long:
    - The system notifies the client that name is too long.
- 5a. Non-positive amount:
    - The system notifies the client and suggests entering a valid amount.

### Postconditions
- The expense is created and stored in the system.
- The client receives an expense confirmation.

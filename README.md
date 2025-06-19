# System Name
Budget OK

# Contributors
[Ognjen](https://github.com/ognjen)

# Licence
MIT Licence

# Background Context
TDD Sandbox Project. The process was led and coached by [Valentina Jemuović](https://www.linkedin.com/in/valentinajemuovic) from [Optivem Journal](https://journal.optivem.com/)

# Use Cases:
#### UC1: Create and Manage Monthly Budgets

Users define a monthly budget by assigning budgeted amounts to envelopes representing spending categories.

#### UC2: Record Expenses by Envelope
Users log expenses under their respective envelopes throughout the month.

#### UC3: Monthly Budget Calculation
The system calculates total expenses and compares them with the allocated budget for each envelope at the end of the month.

#### UC4: Carry Over or Save Unused Funds 
At month-end, users can either transfer unused funds to the next month or move them into savings.

#### UC5: User Registration and Login
A user can register for an account and log in securely.

#### UC6: Manage Envelopes
Users can create, update, or delete budget envelopes.

#### UC7: Budget and Expense Entry
Users can enter and update their budgeted amounts and actual expenses.

#### UC8: View Monthly Budget Statistics
Users can see whether they stayed within budget or exceeded it, along with overage amounts.

#### UC9: Manage User Accounts
The administrator can create, deactivate, or manage user profiles.

#### UC10: Respond to User Reports
Admins can view and respond to user issues or feedback.

#### UC11: View Bank Integration Statistics
Admins can see how many users have linked their accounts with the bank.

#### UC12: Connect Bank Account to BudgetOK
Users can link their bank accounts to automatically reflect real-world transactions in the budget.

#### UC13: Reflect Bank Transactions in Budget
The application imports and categorizes bank transactions into corresponding envelopes.

#### UC14: Subscribe to Bank’s Public Expense API
The application consumes the bank’s public service that exposes account-level transaction data.

# External Systems 
BankOK

# System Architecture Style
Frontend + Monolithic Backend

Architecture Diagram
![image](https://github.com/user-attachments/assets/f8c0acc0-1839-49f4-ab26-2b7bcd4d7325)

# Tech stack
FE: React

BE: Spring Boot + PostgreSQL

# Repository Strategy
Multi Repo approach

# Branching Strategy
Trunk Based Development

# Deployment Model
On Premise

# Repositories 
[Frontend repository](https://github.com/ognjenkl/budget-ok-frontend)

[Backend repository](https://github.com/ognjenkl/budget-ok-backend)

# Project Board
[Board](https://github.com/users/ognjenkl/projects/2)

# Environments
UAT Environment

Production Environment

# Manual Deployment 
Deployment Procedure

# Manual Testing 
Manual Test Procedure



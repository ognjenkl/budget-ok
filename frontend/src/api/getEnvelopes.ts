import axios from 'axios';
import buildApiPrefix from './buildApiPrefix';

export type TransactionType = 'WITHDRAW' | 'DEPOSIT';

export interface Expense {
  id: string;
  amount: number;
  memo: string;
  description?: string;
  date: string;
  transactionType: TransactionType;
  envelopeId: string;
}

export class Envelope {
  constructor(
    public readonly id: string,
    public readonly name: string,
    public readonly budget: number,
    public readonly expenses: Expense[],
    public readonly createdAt: string,
    public readonly updatedAt: string
  ) {}

  addExpense(expense: Omit<Expense, 'id' | 'envelopeId' | 'date'>): Expense {
    const newExpense: Expense = {
      ...expense,
      id: crypto.randomUUID(),
      envelopeId: this.id,
      date: new Date().toISOString(),
    };

    // In a real DDD implementation, you would add domain validation here
    // For example, checking if the expense would exceed the budget

    this.expenses.push(newExpense);
    return newExpense;
  }

  // Helper method to calculate the current balance
  getBalance(): number {
    return this.expenses.reduce((balance, expense) => {
      return expense.transactionType === 'WITHDRAW' 
        ? balance - expense.amount 
        : balance + expense.amount;
    }, 0);
  }

  // Factory method to create an Envelope from raw data
  static fromRaw(data: Omit<Envelope, 'addExpense' | 'getBalance'>): Envelope {
    return new Envelope(
      data.id,
      data.name,
      data.budget,
      data.expenses,
      data.createdAt,
      data.updatedAt
    );
  }
}

export default async function getEnvelopes(includeExpenses: boolean = true): Promise<Envelope[]> {
  const response = await axios.get(`${buildApiPrefix()}/envelopes?includeExpenses=${includeExpenses}`);
  return response.data.map((envelopeData: Omit<Envelope, 'addExpense' | 'getBalance'>) => Envelope.fromRaw({
    ...envelopeData,
    expenses: envelopeData.expenses || []
  }));
}

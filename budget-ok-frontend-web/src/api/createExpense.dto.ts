import type { TransactionType } from './getEnvelopes';

export interface CreateExpenseDto {
  amount: number;
  memo: string;
  description?: string;
  transactionType: TransactionType;
  date?: string; // Optional date field if needed
}

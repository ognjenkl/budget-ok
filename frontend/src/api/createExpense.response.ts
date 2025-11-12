export interface CreateExpenseResponse {
  id: string;
  amount: number;
  description: string;
  transactionType: 'WITHDRAW' | 'DEPOSIT';
  date: string;
  envelopeId: string;
  createdAt: string;
  updatedAt: string;
}

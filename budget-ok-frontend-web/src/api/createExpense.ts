import buildApiPrefix from "./buildApiPrefix";
import type { CreateExpenseDto } from "./createExpense.dto";
import type { CreateExpenseResponse } from "./createExpense.response";
import axios from "axios";

const apiPrefix = buildApiPrefix();

export default async function createExpense(
  envelopeId: string,
  data: CreateExpenseDto
): Promise<CreateExpenseResponse> {
  const response = await axios.post(
    `${apiPrefix}/envelopes/${envelopeId}/expenses`,
    data,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );
  return response.data;
}

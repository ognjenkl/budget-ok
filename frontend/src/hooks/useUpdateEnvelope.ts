import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import type { Envelope } from '../api/getEnvelopes';

export type UpdateEnvelopeDto = {
  name: string;
  budget: number;
}

export default function useUpdateEnvelope(envelopeId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: UpdateEnvelopeDto) => {
      const response = await axios.patch<Envelope>(`/api/envelopes/${envelopeId}`, data, {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    },
    onSuccess: (updatedEnvelope) => {
      // Update the cache directly instead of invalidating
      queryClient.setQueryData<Envelope[]>(['envelopes'], (oldEnvelopes = []) => {
        return oldEnvelopes.map(envelope => 
          envelope.id === updatedEnvelope.id ? updatedEnvelope : envelope
        );
      });
    },
  });
}

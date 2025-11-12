import { useMutation, useQueryClient } from "@tanstack/react-query";
import { message } from "antd";
import { AxiosError } from "axios";
import createEnvelope from "../api/createEnvelope.ts";

export default function useCreateEnvelope() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createEnvelope,
    onSuccess: () => {
      // Invalidate the envelopes query to refetch the updated list
      queryClient.invalidateQueries({ queryKey: ['envelopes'] });
    },
    onError: (error: AxiosError) => {
      const { response } = error;
      if (!response) {
        message.error("Network error, please try again later!");
        return;
      }
      const { status } = response;
      if (status === 400) {
        message.error("Envelope with this name already exists!");
        return;
      }
      message.error("Failed to create envelope!");
    },
  });
}
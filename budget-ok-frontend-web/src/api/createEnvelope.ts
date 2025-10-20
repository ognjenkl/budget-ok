import buildApiPrefix from "./buildApiPrefix";
import type {CreateEnvelopeDto} from "./create.envelope.dto";
import type {CreateEnvelopeResponse} from "./create.envelope.response";
import axios from "axios";

const apiPrefix = buildApiPrefix();
const api = `${apiPrefix}/envelopes`;

export default async function createEnvelope(data: CreateEnvelopeDto): Promise<CreateEnvelopeResponse> {
  const response = await axios.post(api, data);
  return response.data;
}
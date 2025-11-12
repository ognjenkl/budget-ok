import axios from 'axios';
import buildApiPrefix from './buildApiPrefix';

export default async function deleteEnvelope(id: string): Promise<void> {
  await axios.delete(`${buildApiPrefix()}/envelopes/${id}`);
}

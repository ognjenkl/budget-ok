const fetchDirectly = import.meta.env.VITE_FETCH_API_DIRECTLY === "true";

export default function buildApiPrefix(): string {
  if (fetchDirectly) {
    return "/api";
  } else {
    const baseUrl = import.meta.env.VITE_API_URL;
    const apiPort = import.meta.env.VITE_API_PORT;

    if (!baseUrl || !apiPort) {
      throw new Error("VITE_API_URL or VITE_API_PORT is not defined in the environment variables")
    }

    return `${baseUrl}:${apiPort}/api`;
  }
}
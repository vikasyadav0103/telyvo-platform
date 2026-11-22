const configuredApiUrl = (import.meta.env.VITE_API_URL || '').trim();

export function apiUrl(path) {
  const base = configuredApiUrl.replace(/\/$/, '');
  return `${base}${path}`;
}

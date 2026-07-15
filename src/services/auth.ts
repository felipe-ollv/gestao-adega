type AuthPayload = {
  ps?: string;
  cs?: string;
  ts?: string;
  sub?: string;
  upn?: string;
  groups?: string[] | string;
  adegaUuid?: string;
  perfil?: string;
  nome?: string;
  exp?: number;
};

let authToken: string | null = null;

// ─── Token Memory ─────────────────────────────────────────────────────────────

export const getAuthToken = (): string | null => {
  return authToken;
};

export const saveAuthToken = (token: string): void => {
  authToken = token;
};

export const clearAuthToken = (): void => {
  authToken = null;
};

// ─── JWT Decode ───────────────────────────────────────────────────────────────

export const decodeJwt = (token: string): AuthPayload | null => {
  try {
    const [, payload] = token.split(".");
    if (!payload) return null;
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
    const decoded = atob(padded);
    return JSON.parse(decoded);
  } catch {
    return null;
  }
};

// ─── Auth Context ─────────────────────────────────────────────────────────────

export const getAuthContext = (): {
  profileUuid?: string;
  condominiumUuid?: string;
  profileType?: string;
} => {
  const token = getAuthToken();
  if (!token) return {};
  const payload = decodeJwt(token);
  if (!payload) return {};
  return {
    profileUuid: payload.sub || payload.ps,
    condominiumUuid: payload.adegaUuid || payload.cs,
    profileType: payload.perfil || payload.ts,
  };
};

// ─── Validações ───────────────────────────────────────────────────────────────

export const isJwtTokenValid = (token: string | null | undefined): boolean => {
  if (!token) return false;

  const payload = decodeJwt(token);
  if (!payload) return false;

  if (payload.exp) {
    const nowInSeconds = Math.floor(Date.now() / 1000);
    if (nowInSeconds >= payload.exp) {
      return false;
    }
  }

  return true;
};

export const isTokenValid = (): boolean => {
  const token = getAuthToken();

  if (!isJwtTokenValid(token)) {
    if (token) {
      clearAuthToken();
    }

    return false;
  }

  return true;
};

export const isSindico = (): boolean => {
  const { profileType } = getAuthContext();
  return profileType === "SINDICO" || profileType === "MANAGER" || profileType === "ADMIN";
};

export const isGestor = (): boolean => {
  const { profileType } = getAuthContext();
  return profileType === "GESTOR";
};

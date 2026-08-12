import { normalizeRole } from "../auth/access";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const TOKEN_KEY = "stockflow_token";
const USER_KEY = "stockflow_user";

export const getToken = () => localStorage.getItem(TOKEN_KEY);

export const getUser = () => {
  const value = localStorage.getItem(USER_KEY);
  if (!value) {
    return null;
  }

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
};

export const setAuth = (authResponse) => {
  const normalizedAuth = authResponse
    ? {
        ...authResponse,
        role: normalizeRole(authResponse.role),
      }
    : authResponse;

  if (normalizedAuth?.token) {
    localStorage.setItem(TOKEN_KEY, normalizedAuth.token);
  }
  localStorage.setItem(USER_KEY, JSON.stringify(normalizedAuth));
};

export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

const parseResponse = async (response) => {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

export async function request(path, options = {}) {
  const { auth = true, method = "GET", body } = options;
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (auth) {
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const data = await parseResponse(response);

  if (!response.ok) {
    const message =
      typeof data === "string"
        ? data
        : data?.message || data?.error || `Request failed (${response.status})`;
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  return data;
}

export async function login(email, password) {
  return request("/auth/login", {
    method: "POST",
    auth: false,
    body: { email, password },
  });
}

export async function signup(fullName, email, password) {
  return request("/auth/signup", {
    method: "POST",
    auth: false,
    body: { fullName, email, password },
  });
}

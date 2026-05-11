import { computed, shallowRef, type ComputedRef, type ShallowRef } from "vue";

export type TokenPair = {
	accessToken: string;
	refreshToken: string;
};

const authPaths: Set<string> = new Set(["/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh"]);

const refreshTokenStorage: Storage = sessionStorage;
const refreshTokenStorageKey: string = "minerva.refreshToken";

const tokens: ShallowRef<TokenPair | null> = shallowRef<TokenPair | null>(null);

export const isAuthenticated: ComputedRef<boolean> = computed((): boolean => tokens.value !== null);

export function setTokens(nextTokens: TokenPair): void {
	tokens.value = nextTokens;
	refreshTokenStorage.setItem(refreshTokenStorageKey, nextTokens.refreshToken);
}

/**
 * Clears the stored access and refresh tokens. This should be called when the user logs out or when token refresh fails.
 */
export function clearTokens(): void {
	tokens.value = null;
	refreshTokenStorage.removeItem(refreshTokenStorageKey);
}

export function isAuthPath(url: string | undefined): boolean {
	if (url === undefined) {
		return false;
	}

	const path: string = url.startsWith("/api/") ? url.slice(4) : url;

	return authPaths.has(path);
}

export function getAuthorizationHeader(): string | null {
	if (tokens.value === null) {
		return null;
	}

	return `Bearer ${tokens.value.accessToken}`;
}

let refreshPromise: Promise<TokenPair> | null = null;

export async function initializeAuth(): Promise<void> {
	const refreshToken: string | null = refreshTokenStorage.getItem(refreshTokenStorageKey);

	if (refreshToken === null) {
		return;
	}

	tokens.value = {
		accessToken: "",
		refreshToken,
	};

	try {
		await refreshTokens();
	} catch {
		clearTokens();
	}
}

export async function refreshTokens(): Promise<void> {
	if (tokens.value === null) {
		throw new Error("Cannot refresh without a refresh token.");
	}

	if (refreshPromise !== null) {
		await refreshPromise;
		return;
	}

	refreshPromise = fetch("/api/v1/auth/refresh", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({
			refreshToken: tokens.value.refreshToken,
		}),
	})
		.then(async (res: Response) => {
			if (!res.ok) {
				throw new Error("Token refresh failed.");
			}

			const nextTokens: TokenPair = (await res.json()) as TokenPair;
			setTokens(nextTokens);

			return nextTokens;
		})
		.catch((err: unknown) => {
			clearTokens();
			throw err;
		})
		.finally(() => {
			refreshPromise = null;
		});

	await refreshPromise;
}

import { computed, shallowRef, type ComputedRef, type ShallowRef } from "vue";

export type TokenPair = {
	accessToken: string;
	refreshToken: string;
};

const authPaths: Set<string> = new Set(["/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh"]);

const tokens: ShallowRef<TokenPair | null> = shallowRef<TokenPair | null>(null);

export const isAuthenticated: ComputedRef<boolean> = computed((): boolean => tokens.value !== null);

export function setTokens(nextTokens: TokenPair): void {
	tokens.value = nextTokens;
}

/**
 * Clears the stored access and refresh tokens. This should be called when the user logs out or when token refresh fails.
 */
export function clearTokens(): void {
	tokens.value = null;
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

export async function refreshTokens(): Promise<void> {
	if (tokens.value === null) {
		throw new Error("Cannot refresh without a refresh token.");
	}

	if (refreshPromise !== null) {
		await refreshPromise;
		return;
	}

	fetch("/api/v1/auth/refresh", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({
			refreshToken: tokens.value.refreshToken,
		}),
	})
		.then(async (res: Response) => {
			const tokens: TokenPair = (await res.json()) as TokenPair;
			setTokens(tokens);
		})
		.catch((err: unknown) => {
			clearTokens();
			throw err;
		})
		.finally(() => {
			refreshPromise = null;
		});
}

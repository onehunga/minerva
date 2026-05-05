import axios, {
	AxiosHeaders,
	type AxiosError,
	type AxiosInstance,
	type AxiosResponse,
	type InternalAxiosRequestConfig,
} from "axios";

export type TokenPair = {
	accessToken: string;
	refreshToken: string;
};

type AuthRequestConfig = InternalAxiosRequestConfig & {
	_retry?: boolean;
	skipAuthHeader?: boolean;
	skipAuthRefresh?: boolean;
};

type StoredTokens = {
	accessToken: string | null;
	refreshToken: string | null;
};

const authPaths: Set<string> = new Set(["/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh"]);

const tokens: StoredTokens = {
	accessToken: null,
	refreshToken: null,
};

let refreshPromise: Promise<TokenPair> | null = null;

export const client: AxiosInstance = axios.create({
	baseURL: "/api",
});

export function setTokens(nextTokens: TokenPair): void {
	tokens.accessToken = nextTokens.accessToken;
	tokens.refreshToken = nextTokens.refreshToken;
}

export function clearTokens(): void {
	tokens.accessToken = null;
	tokens.refreshToken = null;
	refreshPromise = null;
}

export function getTokens(): StoredTokens {
	return {
		accessToken: tokens.accessToken,
		refreshToken: tokens.refreshToken,
	};
}

function isAuthPath(url: string | undefined): boolean {
	if (url === undefined) {
		return false;
	}

	const path: string = url.startsWith("/api/") ? url.slice(4) : url;

	return authPaths.has(path);
}

async function refreshTokens(): Promise<TokenPair> {
	if (tokens.refreshToken === null) {
		throw new Error("Cannot refresh without a refresh token.");
	}

	refreshPromise ??= client
		.post<TokenPair>(
			"/v1/auth/refresh",
			{
				refreshToken: tokens.refreshToken,
			},
			{
				skipAuthHeader: true,
				skipAuthRefresh: true,
			} as AuthRequestConfig,
		)
		.then((response: AxiosResponse<TokenPair>): TokenPair => {
			setTokens(response.data);

			return response.data;
		})
		.finally((): void => {
			refreshPromise = null;
		});

	return refreshPromise;
}

client.interceptors.request.use(
	(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
		const authConfig: AuthRequestConfig = config as AuthRequestConfig;

		if (
			authConfig.skipAuthHeader === true ||
			isAuthPath(authConfig.url) ||
			tokens.accessToken === null
		) {
			return config;
		}

		const headers: AxiosHeaders = AxiosHeaders.from(config.headers);

		headers.set("Authorization", `Bearer ${tokens.accessToken}`);
		config.headers = headers;

		return config;
	},
);

client.interceptors.response.use(
	(response: AxiosResponse): AxiosResponse => response,
	async (error: AxiosError): Promise<AxiosResponse> => {
		const config: AuthRequestConfig | undefined = error.config as AuthRequestConfig | undefined;

		if (
			error.response?.status !== 401 ||
			config === undefined ||
			config._retry === true ||
			config.skipAuthRefresh === true ||
			isAuthPath(config.url)
		) {
			return Promise.reject(error);
		}

		config._retry = true;

		try {
			await refreshTokens();
		} catch (refreshError: unknown) {
			clearTokens();

			return Promise.reject(refreshError);
		}

		return client(config);
	},
);

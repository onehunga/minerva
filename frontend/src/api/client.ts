import axios, {
	AxiosHeaders,
	type AxiosError,
	type AxiosInstance,
	type AxiosResponse,
	type InternalAxiosRequestConfig,
} from "axios";

import { clearTokens, getAuthorizationHeader, isAuthPath, refreshTokens } from "@/api/credentials";

type AuthRequestConfig = InternalAxiosRequestConfig & {
	_retry?: boolean;
	skipAuthHeader?: boolean;
	skipAuthRefresh?: boolean;
};

export const client: AxiosInstance = axios.create({
	baseURL: "/api",
});

client.interceptors.request.use(
	(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
		const authConfig: AuthRequestConfig = config as AuthRequestConfig;
		const authorizationHeader: string | null = getAuthorizationHeader();

		if (
			authConfig.skipAuthHeader === true ||
			isAuthPath(authConfig.url) ||
			authorizationHeader === null
		) {
			return config;
		}

		const headers: AxiosHeaders = AxiosHeaders.from(config.headers);

		headers.set("Authorization", authorizationHeader);
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

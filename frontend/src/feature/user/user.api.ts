import { client, type TokenPair } from "@/api";
import type { model } from ".";

export async function login(username: string, password: string): Promise<TokenPair> {
	return client
		.post("/v1/auth/login", {
			username: username,
			password: password,
		})
		.then((res) => res.data);
}

export async function details(): Promise<model.UserDetails> {
	return client.get("/v1/users/me").then((res) => res.data);
}

export async function createUser(
	username: string,
	password: string,
	role: model.UserRole,
): Promise<void> {
	return client.post("/v1/users", {
		username: username,
		password: password,
		role: role,
	});
}

export async function getAllUsers(): Promise<model.UserRecordList> {
	return client.get("/v1/users").then((res) => res.data);
}

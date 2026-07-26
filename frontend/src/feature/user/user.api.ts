import { client, type TokenPair } from "@/api";
import type { UserDetails, UserRecordList, UserRole } from "./user.model";

export async function login(username: string, password: string): Promise<TokenPair> {
	return client
		.post("/v1/auth/login", {
			username: username,
			password: password,
		})
		.then((res) => res.data);
}

export async function logout(refreshToken: string): Promise<void> {
	return client.post("/v1/auth/logout", { refreshToken });
}

export async function details(): Promise<UserDetails> {
	return client.get("/v1/users/me").then((res) => res.data);
}

export async function createUser(
	username: string,
	password: string,
	role: UserRole,
): Promise<void> {
	return client.post("/v1/users", {
		username: username,
		password: password,
		role: role,
	});
}

export async function getAllUsers(): Promise<UserRecordList> {
	return client.get("/v1/users").then((res) => res.data);
}

export async function updateUserRole(userId: number, role: UserRole): Promise<void> {
	return client.patch(`/v1/users/${userId}/role`, {
		role: role,
	});
}

export async function updateUsername(userId: number, username: string): Promise<void> {
	return client.patch(`/v1/users/${userId}/username`, {
		username: username,
	});
}

export async function updateUserPassword(userId: number, password: string): Promise<void> {
	return client.patch(`/v1/users/${userId}/password`, {
		password: password,
	});
}

export async function deleteUser(userId: number): Promise<void> {
	return client.delete(`/v1/users/${userId}`);
}

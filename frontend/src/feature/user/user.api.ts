import { client, type TokenPair } from "@/api";

export type UserRole = "ADMIN" | "USER";

export type UserDetails = {
	id: number;
	username: string;
	role: UserRole;
};

export type UserRecord = {
	id: number;
	username: string;
	role: UserRole;
};

export type UserRecordList = {
	users: UserRecord[];
};

export async function login(username: string, password: string): Promise<TokenPair> {
	return client
		.post("/v1/auth/login", {
			username: username,
			password: password,
		})
		.then((res) => res.data);
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
	return (await client.get("/v1/users")).data;
}

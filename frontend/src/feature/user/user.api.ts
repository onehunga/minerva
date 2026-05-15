import { client } from "@/api";

export type UserRole = "ADMIN" | "USER";

export type UserRecord = {
	id: number;
	username: string;
	role: UserRole;
};

export type UserRecordList = {
	users: UserRecord[];
};

export async function createUser(
	username: string,
	password: string,
	role: UserRole,
): Promise<void> {
	return client.post("/v1/users/create", {
		username: username,
		password: password,
		role: role,
	});
}

export async function getAllUsers(): Promise<UserRecordList> {
	return (await client.get("/v1/users/allUsers")).data;
}

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

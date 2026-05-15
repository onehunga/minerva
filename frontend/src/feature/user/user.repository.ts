import type { TokenPair } from "@/api";
import { api } from ".";
import type { UserDetails, UserRecordList, UserRole } from "./user.model";

export const UserRepositoryKey = Symbol();

export interface IUserRepository {
	login(username: string, password: string): Promise<TokenPair>;
	details(): Promise<UserDetails>;
	createUser(username: string, password: string, role: UserRole): Promise<void>;
	getAllUsers(): Promise<UserRecordList>;
}

/**
 * Standard Implementation für das UserRepository
 */
export class UserRepository implements IUserRepository {
	async login(username: string, password: string): Promise<TokenPair> {
		return api.login(username, password);
	}

	async details(): Promise<UserDetails> {
		return api.details();
	}

	async createUser(username: string, password: string, role: UserRole): Promise<void> {
		return api.createUser(username, password, role);
	}

	async getAllUsers(): Promise<UserRecordList> {
		return api.getAllUsers();
	}
}

import type { TokenPair } from "@/api";
import { api } from ".";
import type { UserDetails, UserRecordList, UserRole } from "./user.model";

export const UserRepositoryKey = Symbol();

export interface IUserRepository {
	login(username: string, password: string): Promise<TokenPair>;
	details(): Promise<UserDetails>;
	createUser(username: string, password: string, role: UserRole): Promise<void>;
	getAllUsers(): Promise<UserRecordList>;
	updateUserRole(userId: number, role: UserRole): Promise<void>;
	deleteUser(userId: number): Promise<void>;
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

	async updateUserRole(userId: number, role: UserRole): Promise<void> {
		return api.updateUserRole(userId, role);
	}

	async deleteUser(userId: number): Promise<void> {
		return api.deleteUser(userId);
	}
}

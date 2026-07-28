import type { TokenPair } from "@/api";
import * as api from "./user.api";
import type { UserDetails, UserRecordList, UserRole } from "./user.model";

export const UserRepositoryKey = Symbol();

export interface IUserRepository {
	login(username: string, password: string): Promise<TokenPair>;
	logout(refreshToken: string): Promise<void>;
	details(): Promise<UserDetails>;
	createUser(username: string, password: string, role: UserRole): Promise<void>;
	getAllUsers(): Promise<UserRecordList>;
	updateUserRole(userId: number, role: UserRole): Promise<void>;
	updateUsername(userId: number, username: string): Promise<void>;
	updateUserPassword(userId: number, password: string): Promise<void>;
	deleteUser(userId: number): Promise<void>;
	deactivateUser(userId: number): Promise<void>;
	reactivateUser(userId: number): Promise<void>;
}

/**
 * Standard Implementation für das UserRepository
 */
export class UserRepository implements IUserRepository {
	async login(username: string, password: string): Promise<TokenPair> {
		return api.login(username, password);
	}

	async logout(refreshToken: string): Promise<void> {
		return api.logout(refreshToken);
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

	async updateUsername(userId: number, username: string): Promise<void> {
		return api.updateUsername(userId, username);
	}

	async updateUserPassword(userId: number, password: string): Promise<void> {
		return api.updateUserPassword(userId, password);
	}

	async deleteUser(userId: number): Promise<void> {
		return api.deleteUser(userId);
	}

	async deactivateUser(userId: number): Promise<void> {
		return api.deactivateUser(userId);
	}

	async reactivateUser(userId: number): Promise<void> {
		return api.reactivateUser(userId);
	}
}

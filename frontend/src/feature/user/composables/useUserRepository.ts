import { inject } from "vue";
import { UserRepositoryKey, type IUserRepository } from "../user.repository";

export function useUserRepository(): IUserRepository {
	const repo = inject<IUserRepository>(UserRepositoryKey);
	if (repo === undefined) {
		throw new Error("UserRepository not provided");
	}

	return repo!;
}

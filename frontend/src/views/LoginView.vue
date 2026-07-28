<script setup lang="ts">
import { ref, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";
import { useUser } from "@/feature/user/composables/useUser";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { HugeiconsIcon } from "@hugeicons/vue";
import { BookOpen01Icon } from "@hugeicons/core-free-icons";

const router: Router = useRouter();

const { login } = useUser();

const username: Ref<string> = ref("");
const password: Ref<string> = ref("");
const errorMessage = ref("");

async function handleLogin(): Promise<void> {
	errorMessage.value = "";

	try {
		await login(username.value, password.value);

		await router.push("/");
	} catch {
		errorMessage.value = "Login failed";
	}
}
</script>

<template>
	<div class="flex min-h-screen items-center justify-center">
		<Card class="w-full max-w-sm">
			<CardHeader>
				<CardTitle>Login</CardTitle>
			</CardHeader>
			<CardContent>
				<form @submit.prevent="handleLogin" class="flex flex-col gap-4">
					<div class="flex flex-col gap-2">
						<Label for="username">Username</Label>
						<Input
							id="username"
							v-model="username"
							name="username"
							autocomplete="username"
						/>
					</div>
					<div class="flex flex-col gap-2">
						<Label for="password">Password</Label>
						<Input
							id="password"
							v-model="password"
							name="password"
							type="password"
							autocomplete="current-password"
						/>
					</div>
					<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
						{{ errorMessage }}
					</p>
					<Button type="submit" class="w-full">Login</Button>
				</form>
			</CardContent>
		</Card>
		<Button
			as="a"
			href="/docs/"
			variant="secondary"
			size="icon-lg"
			class="fixed right-6 bottom-6 rounded-full"
			aria-label="Dokumentation öffnen"
			title="Dokumentation"
		>
			<HugeiconsIcon :icon="BookOpen01Icon" />
		</Button>
	</div>
</template>

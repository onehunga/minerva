<script setup lang="ts">
import { ref, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";
import { useUser } from "@/feature/user/composables/useUser";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";

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
	<main class="flex min-h-screen flex-col items-center justify-center gap-6 px-4">
		<div class="flex flex-col items-center gap-3">
			<img src="/logo.png" alt="" aria-hidden="true" class="size-24 rounded-2xl" />
			<h1 class="cn-font-heading text-3xl font-semibold tracking-tight">Minerva</h1>
		</div>
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
		<a href="/docs/" class="text-sm text-muted-foreground underline-offset-4 hover:underline">
			Dokumentation
		</a>
	</main>
</template>

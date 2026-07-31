<script setup lang="ts">
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useProfile } from "@/feature/user";

const {
	username,
	password,
	passwordConfirmation,
	usernameError,
	usernameSuccess,
	passwordError,
	passwordSuccess,
	isUpdatingUsername,
	isUpdatingPassword,
	isUsernameChanged,
	updateUsername,
	updatePassword,
} = useProfile();
</script>

<template>
	<main class="mx-auto flex w-full max-w-2xl flex-col gap-4">
		<header>
			<h1 class="m-0">Profil</h1>
			<p class="text-muted-foreground m-0">Benutzername und Passwort verwalten.</p>
		</header>

		<div class="flex flex-col gap-4">
			<form @submit.prevent="updateUsername">
				<Card class="h-full">
					<CardHeader>
						<CardTitle>Benutzername</CardTitle>
						<CardDescription
							>Dieser Name wird anderen Nutzern angezeigt.</CardDescription
						>
					</CardHeader>
					<CardContent class="flex flex-1 flex-col gap-2">
						<Label for="profile-username">Benutzername</Label>
						<Input
							id="profile-username"
							v-model="username"
							name="username"
							required
							minlength="3"
							maxlength="50"
							pattern="[A-Za-z0-9._-]+"
							autocomplete="username"
							:disabled="isUpdatingUsername"
						/>
						<p v-if="usernameError" class="m-0 text-sm text-destructive" role="alert">
							{{ usernameError }}
						</p>
						<p
							v-if="usernameSuccess"
							class="m-0 text-sm text-muted-foreground"
							role="status"
						>
							{{ usernameSuccess }}
						</p>
					</CardContent>
					<CardFooter class="justify-end">
						<Button type="submit" :disabled="isUpdatingUsername || !isUsernameChanged">
							{{
								isUpdatingUsername
									? "Wird gespeichert..."
									: "Benutzername speichern"
							}}
						</Button>
					</CardFooter>
				</Card>
			</form>

			<form @submit.prevent="updatePassword">
				<Card class="h-full">
					<CardHeader>
						<CardTitle>Passwort</CardTitle>
						<CardDescription>Verwende mindestens acht Zeichen.</CardDescription>
					</CardHeader>
					<CardContent class="flex flex-1 flex-col gap-4">
						<div class="flex flex-col gap-2">
							<Label for="profile-password">Neues Passwort</Label>
							<Input
								id="profile-password"
								v-model="password"
								name="password"
								type="password"
								required
								minlength="8"
								autocomplete="new-password"
								:disabled="isUpdatingPassword"
							/>
						</div>
						<div class="flex flex-col gap-2">
							<Label for="profile-password-confirmation">Passwort bestätigen</Label>
							<Input
								id="profile-password-confirmation"
								v-model="passwordConfirmation"
								name="password-confirmation"
								type="password"
								required
								minlength="8"
								autocomplete="new-password"
								:disabled="isUpdatingPassword"
							/>
						</div>
						<p v-if="passwordError" class="m-0 text-sm text-destructive" role="alert">
							{{ passwordError }}
						</p>
						<p
							v-if="passwordSuccess"
							class="m-0 text-sm text-muted-foreground"
							role="status"
						>
							{{ passwordSuccess }}
						</p>
					</CardContent>
					<CardFooter class="justify-end">
						<Button type="submit" :disabled="isUpdatingPassword">
							{{ isUpdatingPassword ? "Wird gespeichert..." : "Passwort speichern" }}
						</Button>
					</CardFooter>
				</Card>
			</form>
		</div>
	</main>
</template>

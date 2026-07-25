<script setup lang="ts">
import { Card, CardContent } from "@/components/ui/card";
import { UserList, useUserStore } from "@/feature/user";

const userStore = useUserStore();
</script>

<template>
	<main class="mx-auto flex h-full min-h-0 w-full max-w-6xl flex-col gap-4">
		<header class="shrink-0">
			<h1 class="m-0">Benutzerverwaltung</h1>
			<p class="text-muted-foreground m-0">
				Benutzer anlegen und bestehende Zugänge verwalten.
			</p>
		</header>

		<Card class="min-h-0 flex-1">
			<CardContent class="flex min-h-0 flex-1">
				<UserList :current-user-id="userStore.userDetails?.id" />
			</CardContent>
		</Card>
	</main>
</template>

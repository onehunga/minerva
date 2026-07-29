<script setup lang="ts">
import CenteredPageLayout from "@/components/CenteredPageLayout.vue";
import { Card, CardContent } from "@/components/ui/card";
import { UserList, useUserStore } from "@/feature/user";

const userStore = useUserStore();
</script>

<template>
	<CenteredPageLayout>
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
	</CenteredPageLayout>
</template>

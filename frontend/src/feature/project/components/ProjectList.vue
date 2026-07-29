<script setup lang="ts">
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type { ProjectRecord } from "../project.model";
import { RouterLink } from "vue-router";

defineProps<{
	projects: ProjectRecord[];
}>();
</script>

<template>
	<div class="flex flex-col gap-4">
		<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
			<RouterLink
				v-for="project in projects"
				:key="project.id"
				:to="{ name: 'project', params: { id: project.id } }"
				class="rounded-xl outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
			>
				<Card class="h-full transition-shadow hover:shadow-md">
					<CardHeader>
						<CardTitle>{{ project.name }}</CardTitle>
						<CardDescription>
							{{ project.description || "Keine Beschreibung hinterlegt." }}
						</CardDescription>
					</CardHeader>
				</Card>
			</RouterLink>
		</div>
	</div>
</template>

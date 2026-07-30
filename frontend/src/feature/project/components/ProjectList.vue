<script setup lang="ts">
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
	Table,
	TableBody,
	TableCell,
	TableEmpty,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import type { ProjectRecord } from "../project.model";
import { useRouter } from "vue-router";

withDefaults(
	defineProps<{
		projects: ProjectRecord[];
		emptyTitle?: string;
		emptyDescription?: string;
		showCreateAction?: boolean;
	}>(),
	{
		emptyTitle: "Keine Projekte vorhanden",
		emptyDescription: "In dieser Ansicht sind derzeit keine Projekte verfügbar.",
		showCreateAction: false,
	},
);

const router = useRouter();
</script>

<template>
	<Card class="overflow-hidden py-0">
		<Table>
			<TableHeader>
				<TableRow>
					<TableHead>Name</TableHead>
					<TableHead>Beschreibung</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				<TableEmpty v-if="projects.length === 0" :colspan="2">
					<div class="flex flex-col items-center gap-2 py-4">
						<p class="font-medium text-foreground">{{ emptyTitle }}</p>
						<p>{{ emptyDescription }}</p>
						<Button
							v-if="showCreateAction"
							class="mt-2"
							@click="router.push('/create-project')"
						>
							Projekt erstellen
						</Button>
					</div>
				</TableEmpty>
				<TableRow
					v-for="project in projects"
					:key="project.id"
					class="cursor-pointer focus-visible:bg-muted focus-visible:outline-none"
					tabindex="0"
					@click="router.push({ name: 'project', params: { id: project.id } })"
					@keydown.enter="router.push({ name: 'project', params: { id: project.id } })"
				>
					<TableCell class="font-medium">{{ project.name }}</TableCell>
					<TableCell class="text-muted-foreground">
						{{ project.description || "Keine Beschreibung hinterlegt." }}
					</TableCell>
				</TableRow>
			</TableBody>
		</Table>
	</Card>
</template>

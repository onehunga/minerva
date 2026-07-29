<script setup lang="ts">
import { Card } from "@/components/ui/card";
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

defineProps<{
	projects: ProjectRecord[];
}>();

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
					Keine Projekte vorhanden.
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

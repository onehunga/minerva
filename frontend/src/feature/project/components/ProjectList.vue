<script setup lang="ts">
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
	Table,
	TableBody,
	TableCell,
	TableEmpty,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import type { ProjectRecord, ProjectRole } from "../project.model";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";

const props = withDefaults(
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
type SortKey = "name" | "projectRole" | "openTicketCount" | "ticketCount";

const search = ref("");
const sortKey = ref<SortKey>("name");
const sortDirection = ref<"asc" | "desc">("asc");
const roleLabels: Record<ProjectRole, string> = {
	OWNER: "Owner",
	CONTRIBUTOR: "Mitwirkend",
	VIEWER: "Lesend",
};

const visibleProjects = computed(() => {
	const query = search.value.trim().toLocaleLowerCase("de");
	return [...props.projects]
		.filter(
			(project) =>
				!query ||
				project.name.toLocaleLowerCase("de").includes(query) ||
				project.description.toLocaleLowerCase("de").includes(query),
		)
		.sort((left, right) => {
			const leftValue = left[sortKey.value] ?? "";
			const rightValue = right[sortKey.value] ?? "";
			const result =
				typeof leftValue === "number" && typeof rightValue === "number"
					? leftValue - rightValue
					: String(leftValue).localeCompare(String(rightValue), "de");
			return sortDirection.value === "asc" ? result : -result;
		});
});

function sortBy(key: SortKey): void {
	if (sortKey.value === key) {
		sortDirection.value = sortDirection.value === "asc" ? "desc" : "asc";
	} else {
		sortKey.value = key;
		sortDirection.value = "asc";
	}
}

function roleLabel(role: ProjectRecord["projectRole"]): string {
	return role ? roleLabels[role] : "Keine Mitgliedschaft";
}
</script>

<template>
	<div class="space-y-3">
		<Input v-model="search" class="max-w-sm" placeholder="Projekte durchsuchen ..." />
		<Card class="overflow-hidden py-0">
			<div class="overflow-x-auto">
				<Table class="min-w-3xl">
					<TableHeader>
						<TableRow>
							<TableHead
								><button type="button" @click="sortBy('name')">
									Name
								</button></TableHead
							>
							<TableHead>Beschreibung</TableHead>
							<TableHead
								><button type="button" @click="sortBy('projectRole')">
									Rolle
								</button></TableHead
							>
							<TableHead class="text-right"
								><button type="button" @click="sortBy('openTicketCount')">
									Offen
								</button></TableHead
							>
							<TableHead class="text-right"
								><button type="button" @click="sortBy('ticketCount')">
									Gesamt
								</button></TableHead
							>
						</TableRow>
					</TableHeader>
					<TableBody>
						<TableEmpty v-if="visibleProjects.length === 0" :colspan="5">
							<div class="flex flex-col items-center gap-2 py-4">
								<p class="font-medium text-foreground">
									{{ projects.length ? "Keine passenden Projekte" : emptyTitle }}
								</p>
								<p>
									{{
										projects.length
											? "Passe den Suchbegriff an."
											: emptyDescription
									}}
								</p>
								<Button
									v-if="showCreateAction && projects.length === 0"
									class="mt-2"
									@click="router.push('/create-project')"
								>
									Projekt erstellen
								</Button>
							</div>
						</TableEmpty>
						<TableRow
							v-for="project in visibleProjects"
							:key="project.id"
							class="cursor-pointer focus-visible:bg-muted focus-visible:outline-none"
							tabindex="0"
							@click="router.push({ name: 'project', params: { id: project.id } })"
							@keydown.enter="
								router.push({ name: 'project', params: { id: project.id } })
							"
						>
							<TableCell class="font-medium">{{ project.name }}</TableCell>
							<TableCell class="text-muted-foreground">
								{{ project.description || "Keine Beschreibung hinterlegt." }}
							</TableCell>
							<TableCell>{{ roleLabel(project.projectRole) }}</TableCell>
							<TableCell class="text-right tabular-nums">{{
								project.openTicketCount ?? 0
							}}</TableCell>
							<TableCell class="text-right tabular-nums">{{
								project.ticketCount ?? 0
							}}</TableCell>
						</TableRow>
					</TableBody>
				</Table>
			</div>
		</Card>
	</div>
</template>

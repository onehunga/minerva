<script setup lang="ts">
import { computed, useId } from "vue";
import { HugeiconsIcon } from "@hugeicons/vue";
import { Folder, Ticket, User } from "@hugeicons/core-free-icons";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { formatTicketPriority } from "@/feature/ticket";
import { useUsers } from "@/feature/user";
import { formatDate } from "@/lib/date";
import type { ActivityEvent, ActivityEventType } from "../activity.model";

withDefaults(
	defineProps<{
		events: ActivityEvent[];
		isLoading: boolean;
		errorMessage: string;
		heading?: string;
		description?: string;
		fillHeight?: boolean;
	}>(),
	{
		heading: "Aktivitäten",
		fillHeight: false,
	},
);

const headingId = useId();
const descriptionId = useId();
const { users } = useUsers();
const usernames = computed(() => new Map(users.value.map((user) => [user.id, user.username])));

const ACTIVITY_TYPE_LABELS: Record<ActivityEventType, string> = {
	PROJECT_CREATED: "Projekt erstellt",
	PROJECT_DETAILS_UPDATED: "Projektname oder Beschreibung geändert",
	PROJECT_ARCHIVED: "Projekt archiviert",
	PROJECT_RESTORED: "Projekt wiederhergestellt",
	PROJECT_USER_ADDED: "Nutzer hinzugefügt",
	PROJECT_USER_ROLE_CHANGED: "Nutzerrolle geändert",
	PROJECT_USER_REMOVED: "Nutzer entfernt",
	USER_CREATED: "Nutzer erstellt",
	USER_USERNAME_CHANGED: "Benutzername geändert",
	USER_PASSWORD_CHANGED: "Passwort geändert",
	USER_WORKSPACE_ROLE_CHANGED: "Nutzerrolle geändert",
	USER_DEACTIVATED: "Nutzer deaktiviert",
	USER_REACTIVATED: "Nutzer reaktiviert",
	USER_DELETED: "Nutzer gelöscht",
	TICKET_CREATED: "Ticket erstellt",
	TICKET_COMMENT_CREATED: "Kommentar erstellt",
	TICKET_STATUS_CHANGED: "Status geändert",
	TICKET_DETAILS_UPDATED: "Titel oder Beschreibung geändert",
	TICKET_PRIORITY_CHANGED: "Priorität geändert",
	TICKET_ASSIGNEE_CHANGED: "Bearbeiter geändert",
	TICKET_SUBTICKET_ADDED: "Subticket hinzugefügt",
	TICKET_ARCHIVED: "Ticket archiviert",
	TICKET_RESTORED: "Ticket wiederhergestellt",
	TICKET_DELETED: "Ticket gelöscht",
};

function formatType(type: ActivityEventType): string {
	return ACTIVITY_TYPE_LABELS[type] ?? type;
}

function category(type: ActivityEventType): "project" | "ticket" | "user" {
	if (type.startsWith("PROJECT_")) return "project";
	if (type.startsWith("TICKET_")) return "ticket";
	return "user";
}

function categoryIcon(type: ActivityEventType) {
	switch (category(type)) {
		case "project":
			return Folder;
		case "ticket":
			return Ticket;
		case "user":
			return User;
	}
}

type Payload = Record<string, unknown>;

function str(payload: Payload, key: string): string | null {
	const value = payload[key];
	return typeof value === "string" && value.length > 0 ? value : null;
}

function num(payload: Payload, key: string): number | null {
	const value = payload[key];
	return typeof value === "number" ? value : null;
}

function userRef(id: number | null): string {
	return id == null ? "—" : (usernames.value.get(id) ?? `#${id}`);
}

function describeDetailsUpdated(payload: Payload): string {
	const parts: string[] = [];
	const previousName = str(payload, "previousName");
	const newName = str(payload, "newName");
	if (previousName !== newName && newName != null) {
		parts.push(`Name: „${previousName}" → „${newName}"`);
	}
	if (str(payload, "previousDescription") !== str(payload, "newDescription")) {
		parts.push("Beschreibung geändert");
	}
	return parts.join(", ");
}

function describe(event: ActivityEvent): string {
	const payload = event.payload;
	switch (event.type) {
		case "PROJECT_CREATED":
		case "PROJECT_ARCHIVED":
		case "PROJECT_RESTORED":
		case "TICKET_CREATED":
		case "TICKET_ARCHIVED":
		case "TICKET_RESTORED":
		case "TICKET_DELETED":
			return `„${str(payload, "name") ?? "?"}"`;
		case "PROJECT_DETAILS_UPDATED":
		case "TICKET_DETAILS_UPDATED":
			return describeDetailsUpdated(payload);
		case "PROJECT_USER_ADDED":
			return `${userRef(num(payload, "userId"))} (Rolle: ${str(payload, "role") ?? "?"})`;
		case "PROJECT_USER_ROLE_CHANGED":
			return `${userRef(num(payload, "userId"))}: ${str(payload, "oldRole") ?? "?"} → ${str(payload, "newRole") ?? "?"}`;
		case "PROJECT_USER_REMOVED":
			return userRef(num(payload, "userId"));
		case "USER_CREATED":
			return `„${str(payload, "username") ?? "?"}" (Rolle: ${str(payload, "role") ?? "?"})`;
		case "USER_USERNAME_CHANGED":
			return `„${str(payload, "previousUsername") ?? "?"}" → „${str(payload, "newUsername") ?? "?"}"`;
		case "USER_PASSWORD_CHANGED":
			return userRef(num(payload, "userId"));
		case "USER_WORKSPACE_ROLE_CHANGED":
			return `${userRef(num(payload, "userId"))}: ${str(payload, "oldRole") ?? "?"} → ${str(payload, "newRole") ?? "?"}`;
		case "USER_DEACTIVATED":
		case "USER_REACTIVATED":
			return userRef(num(payload, "userId"));
		case "USER_DELETED":
			return `„${str(payload, "username") ?? "?"}"`;
		case "TICKET_COMMENT_CREATED":
			return str(payload, "content") ?? "";
		case "TICKET_STATUS_CHANGED": {
			const transition = str(payload, "transitionName");
			const change = `„${str(payload, "previousStatusName") ?? "?"}" → „${str(payload, "newStatusName") ?? "?"}"`;
			return transition ? `${change} (Übergang „${transition}")` : change;
		}
		case "TICKET_PRIORITY_CHANGED":
			return `${formatTicketPriority(str(payload, "previousPriority"))} → ${formatTicketPriority(str(payload, "newPriority"))}`;
		case "TICKET_ASSIGNEE_CHANGED":
			return `${userRef(num(payload, "previousAssigneeId"))} → ${userRef(num(payload, "newAssigneeId"))}`;
		case "TICKET_SUBTICKET_ADDED":
			return `„${str(payload, "subticketName") ?? "?"}"`;
	}
}
</script>

<template>
	<section
		class="activity-timeline"
		:aria-labelledby="headingId"
		:aria-describedby="description ? descriptionId : undefined"
		:aria-busy="isLoading"
	>
		<h4 :id="headingId">{{ heading }}</h4>
		<p v-if="description" :id="descriptionId" class="text-sm text-muted-foreground">
			{{ description }}
		</p>

		<div v-if="isLoading" class="flex flex-col gap-2" data-testid="activity-skeleton">
			<span class="sr-only">Aktivitäten werden geladen...</span>
			<div
				v-for="index in 3"
				:key="index"
				class="flex flex-col gap-2 rounded-md border px-3 py-2"
			>
				<Skeleton class="h-4 w-2/5" />
				<Skeleton class="h-3 w-4/5" />
				<Skeleton class="h-3 w-1/3" />
			</div>
		</div>
		<Alert v-else-if="errorMessage" variant="destructive">
			<AlertDescription>{{ errorMessage }}</AlertDescription>
		</Alert>
		<p v-else-if="events.length === 0">Keine Aktivitäten vorhanden.</p>
		<ScrollArea v-else :class="fillHeight ? 'min-h-0 flex-1' : undefined">
			<ul class="m-0 flex flex-col gap-1">
				<li
					v-for="event in events"
					:key="event.id"
					class="flex gap-3 rounded-md border px-3 py-2"
					:data-activity-category="category(event.type)"
				>
					<span
						class="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-md bg-muted"
					>
						<HugeiconsIcon
							:icon="categoryIcon(event.type)"
							class="size-4"
							aria-hidden="true"
						/>
					</span>
					<div class="min-w-0 flex-1">
						<p class="activity-timeline__title">{{ formatType(event.type) }}</p>
						<p
							v-if="describe(event)"
							class="activity-timeline__detail truncate"
							:title="describe(event)"
						>
							{{ describe(event) }}
						</p>
						<small>
							{{ userRef(event.actorUserId) }}
							-
							{{ formatDate(event.occurredAt) }}
						</small>
					</div>
				</li>
			</ul>
		</ScrollArea>
	</section>
</template>

<style scoped>
.activity-timeline {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.activity-timeline h4,
.activity-timeline p {
	margin: 0;
}

.activity-timeline__title {
	font-weight: 700;
}

.activity-timeline__detail {
	font-size: 0.9rem;
}
</style>

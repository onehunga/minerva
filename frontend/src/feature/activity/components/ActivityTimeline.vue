<script setup lang="ts">
import { useId } from "vue";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ActivityEvent, ActivityEventType } from "../activity.model";

withDefaults(
	defineProps<{
		events: ActivityEvent[];
		isLoading: boolean;
		errorMessage: string;
		heading?: string;
		fillHeight?: boolean;
	}>(),
	{
		heading: "Aktivitäten",
		fillHeight: false,
	},
);

const headingId = useId();

const ACTIVITY_TYPE_LABELS: Record<ActivityEventType, string> = {
	PROJECT_CREATED: "Projekt erstellt",
	PROJECT_DETAILS_UPDATED: "Projektname oder Beschreibung geändert",
	PROJECT_ARCHIVED: "Projekt archiviert",
	PROJECT_USER_ADDED: "Nutzer hinzugefügt",
	PROJECT_USER_ROLE_CHANGED: "Nutzerrolle geändert",
	PROJECT_USER_REMOVED: "Nutzer entfernt",
	TICKET_CREATED: "Ticket erstellt",
	TICKET_COMMENT_CREATED: "Kommentar erstellt",
	TICKET_STATUS_CHANGED: "Status geändert",
	TICKET_DETAILS_UPDATED: "Titel oder Beschreibung geändert",
	TICKET_PRIORITY_CHANGED: "Priorität geändert",
	TICKET_ASSIGNEE_CHANGED: "Bearbeiter geändert",
	TICKET_SUBTICKET_ADDED: "Subticket hinzugefügt",
	TICKET_ARCHIVED: "Ticket archiviert",
	TICKET_DELETED: "Ticket gelöscht",
};

const PRIORITY_LABELS: Record<string, string> = {
	LOWEST: "Sehr niedrig",
	LOW: "Niedrig",
	NORMAL: "Normal",
	HIGH: "Hoch",
	HIGHEST: "Sehr hoch",
};

function formatType(type: ActivityEventType): string {
	return ACTIVITY_TYPE_LABELS[type] ?? type;
}

function formatDate(value: string | null): string {
	if (value == null) {
		return "-";
	}

	return new Intl.DateTimeFormat("de-DE", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
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
	return id == null ? "—" : `#${id}`;
}

function priorityLabel(value: string | null): string {
	return (value && PRIORITY_LABELS[value]) || value || "?";
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
		case "TICKET_CREATED":
		case "TICKET_ARCHIVED":
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
		case "TICKET_COMMENT_CREATED":
			return str(payload, "content") ?? "";
		case "TICKET_STATUS_CHANGED": {
			const transition = str(payload, "transitionName");
			const change = `„${str(payload, "previousStatusName") ?? "?"}" → „${str(payload, "newStatusName") ?? "?"}"`;
			return transition ? `${change} (Übergang „${transition}")` : change;
		}
		case "TICKET_PRIORITY_CHANGED":
			return `${priorityLabel(str(payload, "previousPriority"))} → ${priorityLabel(str(payload, "newPriority"))}`;
		case "TICKET_ASSIGNEE_CHANGED":
			return `${userRef(num(payload, "previousAssigneeId"))} → ${userRef(num(payload, "newAssigneeId"))}`;
		case "TICKET_SUBTICKET_ADDED":
			return `„${str(payload, "subticketName") ?? "?"}"`;
	}
}
</script>

<template>
	<section class="activity-timeline" :aria-labelledby="headingId">
		<h4 :id="headingId">{{ heading }}</h4>

		<p v-if="isLoading">Aktivitäten werden geladen...</p>
		<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
		<p v-else-if="events.length === 0">Keine Aktivitäten vorhanden.</p>
		<ScrollArea v-else :class="fillHeight ? 'min-h-0 flex-1' : undefined">
			<ul class="m-0 flex flex-col gap-1">
				<li
					v-for="event in events"
					:key="event.id"
					class="flex flex-col gap-1 rounded-md border px-3 py-2"
				>
					<p class="activity-timeline__title">{{ formatType(event.type) }}</p>
					<p
						v-if="describe(event)"
						class="activity-timeline__detail truncate"
						:title="describe(event)"
					>
						{{ describe(event) }}
					</p>
					<small>
						{{
							event.actorDeleted
								? "Gelöschter Nutzer"
								: (event.actorUsername ?? `#${event.actorUserId}`)
						}}
						-
						{{ formatDate(event.occurredAt) }}
					</small>
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

.activity-timeline__item {
	display: flex;
	flex-direction: column;
	gap: 0.3rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
}

.activity-timeline__title {
	font-weight: 700;
}

.activity-timeline__detail {
	font-size: 0.9rem;
}
</style>

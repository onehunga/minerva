<script setup lang="ts">
import {
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { formatTicketPriority } from "@/feature/ticket";
import { formatDate } from "@/lib/date";
import type { Notification, NotificationType } from "../notification.model";
import { useNotifications } from "../composables/useNotifications";

const { notifications, unreadCount, isLoading, errorMessage, markAsRead } = useNotifications();

const NOTIFICATION_TITLES: Record<NotificationType, string> = {
	TICKET_ASSIGNED: "Ticket zugewiesen",
	TICKET_UNASSIGNED: "Ticket abgegeben",
	TICKET_COMMENT_CREATED: "Neuer Kommentar",
	TICKET_STATUS_CHANGED: "Ticketstatus geändert",
	TICKET_DETAILS_UPDATED: "Ticketdetails geändert",
	TICKET_PRIORITY_CHANGED: "Ticketpriorität geändert",
	TICKET_SUBTICKET_ADDED: "Subticket hinzugefügt",
	TICKET_ARCHIVED: "Ticket archiviert",
	TICKET_RESTORED: "Ticket wiederhergestellt",
	TICKET_DELETED: "Ticket gelöscht",
	PROJECT_USER_ADDED: "Zum Projekt hinzugefügt",
	PROJECT_USER_ROLE_CHANGED: "Projektrolle geändert",
	PROJECT_USER_REMOVED: "Aus Projekt entfernt",
	USER_CREATED: "Benutzerkonto erstellt",
	USER_USERNAME_CHANGED: "Benutzername geändert",
	USER_PASSWORD_CHANGED: "Passwort geändert",
	USER_WORKSPACE_ROLE_CHANGED: "Arbeitsbereichsrolle geändert",
	USER_DEACTIVATED: "Benutzerkonto deaktiviert",
	USER_REACTIVATED: "Benutzerkonto reaktiviert",
};

const ROLE_LABELS: Record<string, string> = {
	OWNER: "Owner",
	CONTRIBUTOR: "Mitwirkender",
	VIEWER: "Betrachter",
	ADMIN: "Administrator",
	USER: "Benutzer",
};

function titleFor(type: NotificationType): string {
	return NOTIFICATION_TITLES[type];
}

function text(notification: Notification, key: string, fallback: string = "?"): string {
	const value = notification.payload[key];
	return typeof value === "string" && value.length > 0 ? value : fallback;
}

function number(notification: Notification, key: string): number | null {
	const value = notification.payload[key];
	return typeof value === "number" ? value : null;
}

function role(notification: Notification, key: string): string {
	const value = text(notification, key);
	return ROLE_LABELS[value] ?? value;
}

function descriptionFor(notification: Notification): string {
	const ticket = `Ticket #${number(notification, "ticketId") ?? "?"}`;
	const project = `Projekt #${number(notification, "projectId") ?? "?"}`;

	switch (notification.type) {
		case "TICKET_ASSIGNED": {
			const previousAssigneeId = number(notification, "previousAssigneeId");
			return previousAssigneeId === null
				? `${ticket} in ${project} wurde dir zugewiesen.`
				: `${ticket} in ${project} wurde von Nutzer #${previousAssigneeId} an dich übergeben.`;
		}
		case "TICKET_UNASSIGNED": {
			const newAssigneeId = number(notification, "newAssigneeId");
			return newAssigneeId === null
				? `Du bist nicht mehr für ${ticket} in ${project} zuständig.`
				: `${ticket} in ${project} wurde von dir an Nutzer #${newAssigneeId} übergeben.`;
		}
		case "TICKET_COMMENT_CREATED":
			return `Zu ${ticket}: „${text(notification, "content")}“`;
		case "TICKET_STATUS_CHANGED":
			return `Status von ${ticket}: „${text(notification, "previousStatusName")}“ → „${text(notification, "newStatusName")}“.`;
		case "TICKET_DETAILS_UPDATED": {
			const previousName = text(notification, "previousName");
			const newName = text(notification, "newName");
			const descriptionChanged =
				text(notification, "previousDescription", "") !==
				text(notification, "newDescription", "");
			if (previousName === newName) {
				return `Die Beschreibung von ${ticket} wurde geändert.`;
			}
			return `${ticket} wurde von „${previousName}“ in „${newName}“ umbenannt${descriptionChanged ? " und die Beschreibung wurde geändert" : ""}.`;
		}
		case "TICKET_PRIORITY_CHANGED":
			return `Priorität von ${ticket}: ${formatTicketPriority(text(notification, "previousPriority"))} → ${formatTicketPriority(text(notification, "newPriority"))}.`;
		case "TICKET_SUBTICKET_ADDED":
			return `Subticket „${text(notification, "subticketName")}“ (#${number(notification, "subticketId") ?? "?"}) wurde ${ticket} hinzugefügt.`;
		case "TICKET_ARCHIVED":
			return `Ticket „${text(notification, "name")}“ (#${number(notification, "ticketId") ?? "?"}) wurde archiviert.`;
		case "TICKET_RESTORED":
			return `Ticket „${text(notification, "name")}“ (#${number(notification, "ticketId") ?? "?"}) wurde wiederhergestellt.`;
		case "TICKET_DELETED":
			return `Ticket „${text(notification, "name")}“ (#${number(notification, "ticketId") ?? "?"}) wurde gelöscht.`;
		case "PROJECT_USER_ADDED":
			return `Du wurdest ${project} als ${role(notification, "role")} hinzugefügt.`;
		case "PROJECT_USER_ROLE_CHANGED":
			return `Deine Rolle in ${project} wurde von ${role(notification, "oldRole")} zu ${role(notification, "newRole")} geändert.`;
		case "PROJECT_USER_REMOVED":
			return `Du wurdest aus ${project} entfernt.`;
		case "USER_CREATED":
			return `Dein Benutzerkonto „${text(notification, "username")}“ wurde als ${role(notification, "role")} erstellt.`;
		case "USER_USERNAME_CHANGED":
			return `Dein Benutzername wurde von „${text(notification, "previousUsername")}“ zu „${text(notification, "newUsername")}“ geändert.`;
		case "USER_PASSWORD_CHANGED":
			return "Das Passwort deines Benutzerkontos wurde geändert.";
		case "USER_WORKSPACE_ROLE_CHANGED":
			return `Deine Arbeitsbereichsrolle wurde von ${role(notification, "oldRole")} zu ${role(notification, "newRole")} geändert.`;
		case "USER_DEACTIVATED":
			return "Dein Benutzerkonto wurde deaktiviert.";
		case "USER_REACTIVATED":
			return "Dein Benutzerkonto wurde reaktiviert.";
	}
}
</script>

<template>
	<div class="w-[min(22rem,calc(100vw-2rem))]">
		<DropdownMenuLabel class="flex items-center justify-between px-3 py-2">
			<span class="text-foreground text-sm font-semibold">Benachrichtigungen</span>
			<span class="text-muted-foreground font-normal">
				{{ unreadCount === 0 ? "Alles gelesen" : `${unreadCount} ungelesen` }}
			</span>
		</DropdownMenuLabel>
		<DropdownMenuSeparator />
		<div class="max-h-80 overflow-y-auto p-1">
			<p v-if="isLoading" class="text-muted-foreground px-3 py-6 text-center text-sm">
				Benachrichtigungen werden geladen...
			</p>
			<p v-else-if="errorMessage" class="text-destructive px-3 py-6 text-center text-sm">
				{{ errorMessage }}
			</p>
			<DropdownMenuItem
				v-for="notification in isLoading || errorMessage ? [] : notifications"
				:key="notification.id"
				class="items-start gap-3 px-2 py-2.5"
				@select.prevent="markAsRead(notification.id)"
			>
				<span
					class="mt-1.5 size-2 shrink-0 rounded-full"
					:class="notification.readAt === null ? 'bg-primary' : 'bg-muted'"
					aria-hidden="true"
				></span>
				<span class="min-w-0 flex-1">
					<span class="flex items-start justify-between gap-3">
						<span class="font-medium">{{ titleFor(notification.type) }}</span>
						<time
							class="text-muted-foreground shrink-0 text-[0.7rem]"
							:datetime="notification.createdAt"
						>
							{{ formatDate(notification.createdAt) }}
						</time>
					</span>
					<span
						class="text-muted-foreground mt-1 block line-clamp-2 text-xs leading-relaxed"
					>
						{{ descriptionFor(notification) }}
					</span>
				</span>
			</DropdownMenuItem>
			<p
				v-if="!isLoading && !errorMessage && notifications.length === 0"
				class="text-muted-foreground px-3 py-6 text-center text-sm"
			>
				Keine Benachrichtigungen
			</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import {
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { formatDate } from "@/lib/date";
import type { Notification, NotificationType } from "../notification.model";
import { useNotifications } from "../composables/useNotifications";

const { notifications, unreadCount, markAsRead } = useNotifications();

function titleFor(type: NotificationType): string {
	switch (type) {
		case "TICKET_ASSIGNED":
			return "Ticket zugewiesen";
		case "TICKET_COMMENT_CREATED":
			return "Neuer Kommentar";
		default:
			return "Neue Benachrichtigung";
	}
}

function valueFrom(notification: Notification, key: string): unknown {
	return notification.payload[key];
}

function descriptionFor(notification: Notification): string {
	const ticketId = valueFrom(notification, "ticketId");
	const projectId = valueFrom(notification, "projectId");

	if (notification.type === "TICKET_ASSIGNED") {
		return `Dir wurde Ticket #${String(ticketId)} in Projekt #${String(projectId)} zugewiesen.`;
	}
	if (notification.type === "TICKET_COMMENT_CREATED") {
		return String(valueFrom(notification, "content"));
	}
	return "Es gibt eine neue Änderung für dich.";
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
			<DropdownMenuItem
				v-for="notification in notifications"
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
				v-if="notifications.length === 0"
				class="text-muted-foreground px-3 py-6 text-center text-sm"
			>
				Keine Benachrichtigungen
			</p>
		</div>
	</div>
</template>

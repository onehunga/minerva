<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

defineProps<{
	title: string;
	description: string;
}>();
</script>

<template>
	<Card class="min-w-0 h-full">
		<CardHeader>
			<CardTitle>{{ title }}</CardTitle>
			<CardDescription>{{ description }}</CardDescription>
		</CardHeader>
		<CardContent class="flex min-h-0 min-w-0 flex-1 flex-col">
			<slot />
		</CardContent>
	</Card>
</template>

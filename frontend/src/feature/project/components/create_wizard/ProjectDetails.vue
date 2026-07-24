<script setup lang="ts">
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { ref } from "vue";

const name = ref<string>("");
const description = ref<string>("");

defineProps<{
	onStep: (name: string, description: string) => void;
}>();
</script>

<template>
	<div class="pl-6">
		<Card class="max-w-xl">
			<CardHeader>
				<CardTitle>Projektdetails</CardTitle>
			</CardHeader>
			<CardContent class="flex flex-col gap-4">
				<Input
					v-model="name"
					label="Projektname"
					placeholder="Geben Sie den Namen des Projekts ein"
				/>
				<Textarea
					v-model="description"
					label="Projektbeschreibung"
					placeholder="Geben Sie eine kurze Beschreibung des Projekts ein"
				/>
			</CardContent>
		</Card>
	</div>
</template>

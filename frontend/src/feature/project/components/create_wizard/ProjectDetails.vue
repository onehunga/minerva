<script setup lang="ts">
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

const name = defineModel<string>("name", { required: true });
const description = defineModel<string>("description", { required: true });
</script>

<template>
	<Card class="max-w-2xl">
		<CardHeader>
			<CardTitle>Projektdetails</CardTitle>
		</CardHeader>
		<CardContent class="flex flex-col gap-5">
			<div class="flex flex-col gap-2">
				<Label for="project-name">Projektname</Label>
				<Input id="project-name" v-model="name" required placeholder="Name des Projekts" />
			</div>
			<div class="flex flex-col gap-2">
				<Label for="project-description">Projektbeschreibung</Label>
				<Textarea
					id="project-description"
					v-model="description"
					placeholder="Kurze Beschreibung des Projekts"
				/>
			</div>
		</CardContent>
	</Card>
</template>

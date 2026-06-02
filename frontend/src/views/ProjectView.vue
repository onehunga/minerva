<script setup lang="ts">
import { ProjectDetails } from "@/feature/project";
import { onBeforeMount } from "vue";

const props = defineProps<{
	id: string;
}>();

onBeforeMount(() => {
	if (isNaN(Number(props.id))) {
		throw new Error("Invalid project ID");
	}
});
</script>

<template>
	<ProjectDetails :id="Number($props.id)" />
</template>

<script setup lang="ts">
import { ProjectDetails } from "@/feature/project";
import { useProject } from "@/feature/project/composables/useProject";

const props = defineProps<{
	id: string;
}>();

useProject(props.id);
</script>

<template>
	<ProjectDetails :id="Number($props.id)" />
</template>

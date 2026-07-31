<script setup lang="ts">
import { ProjectPage } from "@/feature/project";
import { useProject } from "@/feature/project/composables/useProject";

const props = defineProps<{
	id: string;
}>();

useProject(() => props.id);
</script>

<template>
	<ProjectPage :id="Number($props.id)" />
</template>

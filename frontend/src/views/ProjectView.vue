<script setup lang="ts">
import { ProjectDetails } from "@/feature/project";
import { useProject } from "@/feature/project/composables/useProject";
import { onBeforeMount } from "vue";

const props = defineProps<{
	id: string;
}>();

onBeforeMount(() => {
	useProject(props.id);
});
</script>

<template>
	<ProjectDetails :id="Number($props.id)" />
</template>

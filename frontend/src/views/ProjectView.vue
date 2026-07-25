<script setup lang="ts">
import { ProjectPage } from "@/feature/project";
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
	<ProjectPage :id="Number($props.id)" />
</template>

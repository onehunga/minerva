<script setup lang="ts">
import BaseModal from "@/components/BaseModal.vue";
import { CreateProjectForm, ProjectList } from "@/feature/project";
import { useUser } from "@/feature/user";
import { ref } from "vue";

const { userDetails } = useUser();

const showCreateProjectForm = ref(false);
</script>

<template>
	<p v-if="!userDetails">Loading...</p>
	<p v-else>{{ userDetails!.username }}</p>

	<button @click="showCreateProjectForm = true">Create Project</button>

	<BaseModal
		:open="showCreateProjectForm"
		@close="showCreateProjectForm = false"
		title="Create Project"
	>
		<CreateProjectForm />
	</BaseModal>

	<ProjectList />
</template>

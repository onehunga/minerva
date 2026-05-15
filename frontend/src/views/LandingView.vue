<script setup lang="ts">
import { useUser } from "@/feature/user";

const { userDetails } = useUser();
</script>

<template>
	<p v-if="!userDetails">Loading...</p>
	<p v-else>{{ userDetails!.username }}</p>
</template>

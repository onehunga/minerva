<script setup lang="ts" generic="T">
import { ref } from "vue";

const showOptions = ref(false);

defineProps<{
	options: T[];
	modelValue: T | null;
}>();

defineEmits<{
	(e: "update:modelValue", value: T | null): void;
}>();
</script>

<template>
	<div class="select">
		<button @click.prevent="showOptions = !showOptions" class="trigger">
			<slot name="trigger" />
		</button>

		<div v-if="showOptions" class="options">
			<button
				v-for="(v, k) in $props.options"
				:key="k"
				@click="
					$emit('update:modelValue', v);
					showOptions = false;
				"
			>
				<slot name="option" :value="v" />
			</button>
		</div>
	</div>
</template>

<style scoped>
.trigger {
	background-color: white;
	border: 1px solid currentColor;
	padding: 0.45rem 0.55rem;
}

.select {
	position: relative;
	display: inline-block;
}

.options {
	display: flex;
	flex-direction: column;
	position: absolute;
	top: 100%;
	left: 0;
	background-color: white;
	border: 1px solid currentColor;
	margin-top: 0.25rem;
	min-width: 100%;
	z-index: 10;
}

.options button {
	padding: 0.45rem 0.55rem;
	border: none;
	background: transparent;
	text-align: left;
	color: inherit;
	cursor: pointer;
}

.options button:hover {
	padding: 0.45rem 0.55rem;
	border: none;
	background: rgba(0, 0, 0, 0.1);
	text-align: left;
	color: inherit;
	cursor: pointer;
}
</style>

<script setup lang="ts" generic="T">
import { onBeforeUnmount, ref, watch } from "vue";

const showOptions = ref(false);
const triggerRef = ref<HTMLButtonElement | null>(null);
const optionsRef = ref<HTMLDivElement | null>(null);
const optionsStyle = ref<Record<string, string>>({});

defineProps<{
	options: T[];
	modelValue: T | null;
}>();

const emit = defineEmits<{
	(e: "update:modelValue", value: T | null): void;
}>();

function updateOptionsPosition(): void {
	if (!triggerRef.value) {
		return;
	}

	const rect = triggerRef.value.getBoundingClientRect();
	optionsStyle.value = {
		top: `${rect.bottom + 4}px`,
		left: `${rect.left}px`,
		minWidth: `${rect.width}px`,
	};
}

function openOptions(): void {
	showOptions.value = true;
	updateOptionsPosition();
}

function closeOptions(): void {
	showOptions.value = false;
}

function toggleOptions(): void {
	if (showOptions.value) {
		closeOptions();
	} else {
		openOptions();
	}
}

function selectOption(value: T): void {
	emit("update:modelValue", value);
	closeOptions();
}

function handlePointerDown(event: PointerEvent): void {
	if (!showOptions.value) {
		return;
	}

	const target = event.target as Node;
	if (triggerRef.value?.contains(target) || optionsRef.value?.contains(target)) {
		return;
	}

	closeOptions();
}

function handleKeydown(event: KeyboardEvent): void {
	if (showOptions.value && event.key === "Escape") {
		closeOptions();
	}
}

watch(showOptions, (isOpen) => {
	if (isOpen) {
		updateOptionsPosition();
	}
});

window.addEventListener("pointerdown", handlePointerDown);
window.addEventListener("keydown", handleKeydown);
window.addEventListener("resize", updateOptionsPosition);
window.addEventListener("scroll", updateOptionsPosition, true);

onBeforeUnmount(() => {
	window.removeEventListener("pointerdown", handlePointerDown);
	window.removeEventListener("keydown", handleKeydown);
	window.removeEventListener("resize", updateOptionsPosition);
	window.removeEventListener("scroll", updateOptionsPosition, true);
});
</script>

<template>
	<div class="select">
		<button ref="triggerRef" type="button" class="trigger" @click.prevent="toggleOptions">
			<slot name="trigger" />
		</button>

		<Teleport to="body">
			<div v-if="showOptions" ref="optionsRef" class="options" :style="optionsStyle">
				<button
					v-for="(v, k) in $props.options"
					:key="k"
					type="button"
					@click="selectOption(v)"
				>
					<slot name="option" :value="v" />
				</button>
			</div>
		</Teleport>
	</div>
</template>

<style scoped>
.trigger {
	background-color: white;
	border: 1px solid currentColor;
	padding: 0.45rem 0.55rem;
}

.select {
	display: inline-block;
}

.options {
	display: flex;
	flex-direction: column;
	position: fixed;
	z-index: 100;
	background-color: white;
	border: 1px solid currentColor;
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
	background: rgb(0 0 0 / 10%);
}
</style>

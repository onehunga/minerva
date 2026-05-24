<script setup lang="ts">
import { onBeforeUnmount, onMounted } from "vue";

const props = defineProps<{
	open: boolean;
	title?: string;
}>();

const emit = defineEmits<{
	(event: "close"): void;
}>();

const titleId = `base-modal-title-${Math.random().toString(36).slice(2)}`;

function close(): void {
	emit("close");
}

function handleKeydown(event: KeyboardEvent): void {
	if (props.open && event.key === "Escape") {
		close();
	}
}

onMounted((): void => {
	window.addEventListener("keydown", handleKeydown);
});

onBeforeUnmount((): void => {
	window.removeEventListener("keydown", handleKeydown);
});
</script>

<template>
	<Teleport to="body">
		<div v-if="open" class="base-modal-backdrop" @click.self="close">
			<section class="base-modal" role="dialog" aria-modal="true" :aria-labelledby="titleId">
				<header class="base-modal-header">
					<h2 :id="titleId">
						<slot name="title">{{ title }}</slot>
					</h2>
					<button type="button" aria-label="Schliessen" @click="close">x</button>
				</header>

				<div class="base-modal-body">
					<slot />
				</div>

				<footer v-if="$slots.footer" class="base-modal-footer">
					<slot name="footer" />
				</footer>
			</section>
		</div>
	</Teleport>
</template>

<style scoped>
.base-modal-backdrop {
	position: fixed;
	inset: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 1rem;
	background: rgb(0 0 0 / 30%);
	z-index: 10;
}

.base-modal {
	width: min(100%, 32rem);
	max-height: calc(100vh - 2rem);
	overflow: auto;
	background: Canvas;
	color: CanvasText;
	border: 1px solid currentColor;
	padding: 1rem;
}

.base-modal-header,
.base-modal-footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 1rem;
}

.base-modal-header h2 {
	margin: 0;
}

.base-modal-body {
	margin-top: 1rem;
}

.base-modal-footer {
	margin-top: 1rem;
}
</style>

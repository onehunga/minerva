import type { ChartConfig } from ".";
import { isClient } from "@vueuse/core";
import { useId } from "reka-ui";
import { h, render } from "vue";

const cache = new Map<string, string>();

function serializeKey(key: Record<string, unknown>): string {
	return JSON.stringify(key, Object.keys(key).sort());
}

interface Constructor<P = Record<string, unknown>> {
	__isFragment?: never;
	__isTeleport?: never;
	__isSuspense?: never;
	new (...args: never[]): {
		$props: P;
	};
}

type TooltipTemplate = (data: Record<string, unknown>, x: number | Date) => string;

export function componentToString<P>(
	config: ChartConfig,
	component: Constructor<P>,
	props?: P,
): TooltipTemplate | undefined {
	if (!isClient) return;

	const id = useId();

	// https://unovis.dev/docs/auxiliary/Crosshair#component-props
	return (_data: Record<string, unknown>, x: number | Date) => {
		const nestedData = _data.data;
		const data =
			typeof nestedData === "object" && nestedData !== null
				? (nestedData as Record<string, unknown>)
				: _data;
		const serializedKey = `${id}-${serializeKey(data)}`;
		const cachedContent = cache.get(serializedKey);
		if (cachedContent) return cachedContent;

		const vnode = h<unknown>(component, { ...props, payload: data, config, x });
		const div = document.createElement("div");
		render(vnode, div);
		cache.set(serializedKey, div.innerHTML);
		return div.innerHTML;
	};
}

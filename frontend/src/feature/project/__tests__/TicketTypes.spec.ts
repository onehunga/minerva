import { afterEach, describe, expect, it } from "vitest";
import { enableAutoUnmount, mount } from "@vue/test-utils";
import { nextTick } from "vue";
import type { TicketWorkflow } from "@/feature/ticket";
import TicketTypes from "../components/create_wizard/TicketTypes.vue";

enableAutoUnmount(afterEach);

const ticket: TicketWorkflow = {
	name: "Aufgabe",
	description: "",
	states: [],
	transitions: [],
	children: [],
};

describe("TicketTypes", () => {
	it("removes a ticket type only after confirmation", async () => {
		const wrapper = mount(TicketTypes, {
			attachTo: document.body,
			props: { tickets: [ticket] },
		});

		await clickButton(wrapper.element, "Löschen");
		await nextTick();

		expect(getDialog()?.textContent).toContain("Aufgabe");
		expect(wrapper.emitted("remove")).toBeUndefined();

		clickDialogButton("Abbrechen");
		await nextTick();
		expect(wrapper.emitted("remove")).toBeUndefined();

		await clickButton(wrapper.element, "Löschen");
		await nextTick();
		clickDialogButton("Ticket-Typ löschen");
		await nextTick();

		expect(wrapper.emitted("remove")).toEqual([["Aufgabe"]]);
	});
});

function getDialog(): Element | null {
	return document.body.querySelector("[data-slot='alert-dialog-content']");
}

async function clickButton(root: Element, label: string): Promise<void> {
	const button = Array.from(root.querySelectorAll("button")).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected button ${label}`);
	}

	button.click();
}

function clickDialogButton(label: string): void {
	const button = Array.from(getDialog()?.querySelectorAll("button") ?? []).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected dialog button ${label}`);
	}

	button.click();
}

import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import type { TicketComment } from "../ticket.model";
import { TicketRepositoryKey, type ITicketRepository } from "../ticket.repository";
import TicketComments from "../components/TicketComments.vue";

const existingComment: TicketComment = {
	id: 1,
	ticketId: 2,
	authorId: 3,
	authorUsername: "admin",
	authorDeleted: false,
	content: "Bestehender Kommentar",
	createdAt: "2026-07-26T10:00:00Z",
	updatedAt: null,
};

describe("TicketComments", () => {
	it("loads and creates comments through the composable", async () => {
		const createdComment = {
			...existingComment,
			id: 2,
			content: "Neuer Kommentar",
			createdAt: null,
		};
		const repository = {
			getTicketComments: vi.fn<ITicketRepository["getTicketComments"]>(async () => [
				existingComment,
			]),
			createTicketComment: vi.fn<ITicketRepository["createTicketComment"]>(
				async () => createdComment,
			),
		} satisfies Partial<ITicketRepository>;
		const wrapper = mountComments(repository);

		await flushPromises();
		expect(repository.getTicketComments).toHaveBeenCalledExactlyOnceWith(1, 2);
		expect(wrapper.text()).toContain(existingComment.content);

		await wrapper.get("textarea").setValue(createdComment.content);
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(repository.createTicketComment).toHaveBeenCalledExactlyOnceWith(1, 2, {
			content: createdComment.content,
		});
		expect(wrapper.text()).toContain(createdComment.content);
		expect(wrapper.get("textarea").element.value).toBe("");
	});

	it("keeps the displayed error texts in the component", async () => {
		const repository = {
			getTicketComments: vi.fn<ITicketRepository["getTicketComments"]>(async () =>
				Promise.reject(new Error("Request failed")),
			),
			createTicketComment: vi.fn<ITicketRepository["createTicketComment"]>(async () =>
				Promise.reject(new Error("Request failed")),
			),
		} satisfies Partial<ITicketRepository>;
		const wrapper = mountComments(repository);

		await flushPromises();
		expect(wrapper.text()).toContain("Kommentare konnten nicht geladen werden.");

		await wrapper.get("textarea").setValue("Neuer Kommentar");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(wrapper.text()).toContain("Kommentar konnte nicht gespeichert werden.");
	});
});

function mountComments(repository: Partial<ITicketRepository>) {
	return mount(TicketComments, {
		props: {
			projectId: 1,
			ticketId: 2,
			canCreateComment: true,
		},
		global: {
			provide: {
				[TicketRepositoryKey as symbol]: repository,
			},
		},
	});
}

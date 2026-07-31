const dateFormatter = new Intl.DateTimeFormat("de-DE", {
	dateStyle: "medium",
	timeStyle: "short",
});

export function formatDate(value: string | null): string {
	return value === null ? "-" : dateFormatter.format(new Date(value));
}

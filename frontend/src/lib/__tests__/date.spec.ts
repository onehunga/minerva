import { describe, expect, it } from "vitest";
import { formatDate } from "../date";

describe("formatDate", () => {
	it("formats timestamps and handles missing values", () => {
		expect(formatDate(null)).toBe("-");
		expect(formatDate("2025-01-02T12:34:00")).toBe("02.01.2025, 12:34");
	});
});

import type { TicketWorkflow } from "@/feature/ticket";

export const ticketLibrary: TicketWorkflow[] = [
	{
		name: "Bug-Report",
		description:
			"Standard-Workflow zum Melden und Beheben von Fehlern. Von der ersten Meldung über die Bearbeitung bis zur Verifikation und Schließung.",
		states: [
			{ name: "Offen", category: "OPEN" },
			{ name: "In Bearbeitung", category: "IN_PROGRESS" },
			{ name: "Behoben", category: "COMPLETED" },
		],
		transitions: [
			{ name: "Bearbeiten", from: "Offen", to: "In Bearbeitung" },
			{ name: "Abschließen", from: "In Bearbeitung", to: "Behoben" },
			{ name: "Wiedereröffnen", from: "Behoben", to: "Offen" },
		],
		children: [],
	},
	{
		name: "Feature-Request",
		description:
			"Workflow für neue Funktionen und Verbesserungen. Von der Idee über Planung, Umsetzung und Test bis zur Fertigstellung.",
		states: [
			{ name: "Offen", category: "OPEN" },
			{ name: "Geplant", category: "OPEN" },
			{ name: "In Bearbeitung", category: "IN_PROGRESS" },
			{ name: "Im Test", category: "IN_PROGRESS" },
			{ name: "Fertig", category: "COMPLETED" },
		],
		transitions: [
			{ name: "Planen", from: "Offen", to: "Geplant" },
			{ name: "Umsetzen", from: "Geplant", to: "In Bearbeitung" },
			{ name: "Testen", from: "In Bearbeitung", to: "Im Test" },
			{ name: "Abschließen", from: "Im Test", to: "Fertig" },
			{ name: "Zurückweisen", from: "Im Test", to: "In Bearbeitung" },
			{ name: "Ablehnen", from: "Offen", to: "Fertig" },
			{ name: "Ablehnen", from: "Geplant", to: "Fertig" },
		],
		children: [],
	},
	{
		name: "Aufgabe",
		description:
			"Einfacher Workflow für allgemeine Aufgaben und To-dos. Klassischer Drei-Schritt-Prozess von Offen über In Bearbeitung zu Fertig.",
		states: [
			{ name: "Offen", category: "OPEN" },
			{ name: "In Bearbeitung", category: "IN_PROGRESS" },
			{ name: "Fertig", category: "COMPLETED" },
		],
		transitions: [
			{ name: "Bearbeiten", from: "Offen", to: "In Bearbeitung" },
			{ name: "Abschließen", from: "In Bearbeitung", to: "Fertig" },
			{ name: "Wiedereröffnen", from: "Fertig", to: "Offen" },
		],
		children: [],
	},
	{
		name: "Review",
		description:
			"Workflow für Code-Reviews, Dokumenten-Prüfungen oder Qualitätssicherung. Tickets durchlaufen eine dedizierte Prüfphase vor dem Abschluss.",
		states: [
			{ name: "Offen", category: "OPEN" },
			{ name: "In Prüfung", category: "IN_PROGRESS" },
			{ name: "Überarbeitung", category: "IN_PROGRESS" },
			{ name: "Abgeschlossen", category: "COMPLETED" },
			{ name: "Abgelehnt", category: "COMPLETED" },
		],
		transitions: [
			{ name: "Prüfen", from: "Offen", to: "In Prüfung" },
			{ name: "Überarbeiten", from: "In Prüfung", to: "Überarbeitung" },
			{ name: "Erneut prüfen", from: "Überarbeitung", to: "In Prüfung" },
			{ name: "Freigeben", from: "In Prüfung", to: "Abgeschlossen" },
			{ name: "Ablehnen", from: "In Prüfung", to: "Abgelehnt" },
		],
		children: [],
	},
	{
		name: "Support-Ticket",
		description:
			"Workflow für Helpdesk- oder Kundensupport-Anfragen. Ticket wird klassifiziert, bearbeitet und mit einer Lösung geschlossen.",
		states: [
			{ name: "Neu", category: "OPEN" },
			{ name: "Klassifiziert", category: "OPEN" },
			{ name: "In Bearbeitung", category: "IN_PROGRESS" },
			{ name: "Warten auf Kunde", category: "IN_PROGRESS" },
			{ name: "Gelöst", category: "COMPLETED" },
			{ name: "Geschlossen", category: "COMPLETED" },
		],
		transitions: [
			{ name: "Klassifizieren", from: "Neu", to: "Klassifiziert" },
			{ name: "Bearbeiten", from: "Klassifiziert", to: "In Bearbeitung" },
			{ name: "Nachfragen", from: "In Bearbeitung", to: "Warten auf Kunde" },
			{ name: "Antworten", from: "Warten auf Kunde", to: "In Bearbeitung" },
			{ name: "Lösen", from: "In Bearbeitung", to: "Gelöst" },
			{ name: "Schließen", from: "Gelöst", to: "Geschlossen" },
			{ name: "Wiedereröffnen", from: "Geschlossen", to: "In Bearbeitung" },
		],
		children: [],
	},
];

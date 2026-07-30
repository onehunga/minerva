# Design-Roadmap: UI-Verbesserungen

> Kontext: Technische Ticket-Management-Anwendung. Zielgruppe: technische Nutzer.
> UI soll aufgeräumt, effizient und visuell ansprechend bleiben – kein "Gamification"-Stil.
> `src/components/ui/` (shadcn-vue) bleiben unverändert.

---

## 1. Landing / Dashboard

| # | Problem | Verbesserung |
|---|---------|-------------|
| 1.1 | Dashboard bietet vor Charts und Tabelle keine kompakte Einordnung | Sachliche Zusammenfassung wie "3 Projekte, 12 offene Tickets" vor den Charts; keine rein dekorative Begrüßungslogik |
| 1.2 | Keine Schnellzugriffe auf Projekte | Erst bei belegtem Bedarf: zuletzt geöffnete oder angeheftete Projekte anzeigen; keine erfundene "häufig genutzt"-Sortierung |
| 1.3 | Dashboard-Tickets sind nicht interaktiv und Tickets besitzen keinen Deep-Link | Adressierbare Ticket-URL einführen und Tabellenzeilen als echte Links umsetzen |
| 1.4 | Die eigentlichen Chart-Flächen haben unterschiedliche Höhen | Chart-Höhen vereinheitlichen; keinen Hover-Effekt ohne Interaktion verwenden |
| 1.5 | Status-Prioritäts-Chart wird auf kleinen Screens eng | Informationsgehalt erhalten: horizontales Scrollen oder gestapelte horizontale Balken statt informationsärmerem Donut |

---

## 2. Login-Seite

| # | Problem | Verbesserung |
|---|---------|-------------|
| 2.1 | Kein Branding/Logo auf der ansonsten schlichten Login-Seite | Einfachen "Minerva"-Schriftzug oberhalb der Login-Card ergänzen; Textur oder Gradient nur bei konkreter Designvorgabe |
| 2.2 | Docs-Button schwebt losgelöst rechts unten | Als dezenten Textlink unter die Login-Card setzen; nicht entfernen, da die Sidebar vor dem Login nicht sichtbar ist |

---

## 3. Navigation / Sidebar

| # | Problem | Verbesserung |
|---|---------|-------------|
| 3.1 | Aktive und archivierte Projekte nutzen dasselbe `Folder`-Icon | Für archivierte Gruppen und Projekte ein Archiv-Icon verwenden; Admin-Zugehörigkeit wird bereits durch die Gruppierung vermittelt |
| 3.2 | Dokumentationslink ist ein externer Link, öffnet aber regulär im selben Tab | Bestehendes Verhalten beibehalten; nur bei bewusstem `target="_blank"` ein External-Link-Icon und einen zugänglichen Hinweis ergänzen |
| 3.3 | Theme-Umschalter bietet System/Hell/Dunkel in einem Dropdown | Beibehalten, solange häufiges Umschalten nicht belegt ist; kein zusätzliches Toggle-plus-Dropdown-System einführen |
| 3.4 | Benachrichtigungen sind zwei Klicks tief im User-Menü vergraben | Bell-Icon mit Badge direkt in die Sidebar-Footer-Leiste (neben User), öffnet Benachrichtigungs-Popover |
| 3.5 | Sidebar hat kein aktives Projekt-Highlighting in der Sub-Liste | Aktuell geöffnetes Projekt in der Projekt-Sub-Liste visuell hervorheben (`is-active`-State) |
| 3.6 | "Projekt erstellen" ist optisch nicht von normalen Navigationspunkten unterscheidbar | Aktiven Zustand auf der Erstellseite ergänzen; stärkere Hervorhebung nur bei nachgewiesener Nutzung als Hauptaktion |

---

## 4. Projekt-Liste

| # | Problem | Verbesserung |
|---|---------|-------------|
| 4.1 | Nur Name + Beschreibung in der Tabelle – keine Metadaten | Eigene Rolle und offene/gesamte Ticketanzahl über die Listen-API ergänzen; "zuletzt aktiv" erst nach klarer fachlicher Definition |
| 4.2 | Tabelle ist unsortiert | Nach Name sowie nach den tatsächlich aus 4.1 verfügbaren Metadaten sortierbar machen |
| 4.3 | Keine Suchfunktion | Suchfeld über der Tabelle (filtert Client-seitig nach Name/Beschreibung) |
| 4.4 | Leerzustand nur Text "Keine Projekte vorhanden" | Kontext- und berechtigungsabhängigen Leerzustand ergänzen; Erstellen-CTA nur in der aktiven eigenen Projektliste |

---

## 5. Projekt-Detailseite

| # | Problem | Verbesserung |
|---|---------|-------------|
| 5.1 | Keine Breadcrumbs oder stabile übergeordnete Navigation | Kontextabhängige Breadcrumbs für eigene, archivierte und Admin-Projekte über den Tabs ergänzen |
| 5.2 | "Archiviert"-Banner besitzt wenig visuelle Hierarchie; Wiederherstellen liegt nur in den Settings | Warning-Ton mit Archiv-Icon und berechtigungsabhängigem Wiederherstellen-Button verwenden |
| 5.3 | Overview verwendet weitgehend dasselbe Dashboard wie die Landingpage | Bereits verfügbare Ticketanzahl und eigene Rolle kompakt anzeigen; weitere Daten nur ohne N+1-Abfragen ergänzen |
| 5.4 | Mobile werden Ticketliste und komplettes Detail untereinander dargestellt | Mobile Master-Detail-Navigation: zunächst Liste, nach Auswahl Detail mit Zurück-zur-Liste-Button |
| 5.5 | "Ticketansicht"-Filter (Aktiv/Archiviert) ist ein Select – braucht viel Platz | Toggle-Button-Gruppe (zwei Buttons) statt Select für binäre Auswahl |
| 5.6 | Settings sind bereits in drei Cards gegliedert, aber Projektverwaltung trennt reversible und destruktive Aktionen nicht klar | Bestehende Struktur beibehalten; Löschen klar von Archivieren/Wiederherstellen trennen, ohne die gesamte Card als Gefahrenzone zu markieren |

---

## 6. Ticket-Liste & Ticket-Detail

| # | Problem | Verbesserung |
|---|---------|-------------|
| 6.1 | Ticket-Listitems zeigen nur Namen + ID – kein Status, keine Priorität | Status-Badge + Prioritäts-Indikator (farbiger Balken/Punkt) im Listitem anzeigen |
| 6.2 | Ausgewähltes Ticket ist bereits durch Hintergrund und stärkere Schrift hervorgehoben | Beibehalten; zusätzlichen Randbalken nur ergänzen, falls Nutzertests mangelnde Erkennbarkeit zeigen |
| 6.3 | Ticket-Detail: "Aktionen"-Sidebar enthält Status, Priorität, Bearbeiter UND Archivieren/Löschen – alles in einem Block | Aktionen in "Workflow" (Status/Priorität/Bearbeiter) und "Verwaltung" (Archivieren/Löschen) trennen, mit visuellem Abstand |
| 6.4 | Löschen steht direkt neben Archivieren/Wiederherstellen | Durch Überschrift oder Separator abgrenzen; destruktiver Button und Bestätigungsdialog bieten bereits die zentrale Absicherung |
| 6.5 | Inline-Edit für Ticketname/Beschreibung ist nicht als editierbar erkennbar | Dauerhaft sichtbares Edit-Icon mit zugänglichem Label ergänzen, nicht nur bei Hover |
| 6.6 | Priorität wird nur als Text dargestellt | Text plus konsistente visuelle Kodierung für alle fünf Stufen (`LOWEST` bis `HIGHEST`) verwenden |
| 6.7 | Ticket-Metadaten-Liste ist schwer zu scannen | Auf breiten Screens Label und Wert zweispaltig anzeigen; mobil beim vertikalen Layout bleiben |
| 6.8 | Eigene und fremde Kommentare sind visuell identisch | Eigene Kommentare dezent per Hintergrund unterscheiden; keine rechtsbündige Chat-Darstellung |
| 6.9 | Aktivitäten werden bei jedem Ticketwechsel neu geladen und währenddessen geleert | Zunächst strukturellen Skeleton verwenden; Caching erst bei messbarem Latenzproblem samt Invalidierungsstrategie |

---

## 7. Aktivitäten

| # | Problem | Verbesserung |
|---|---------|-------------|
| 7.1 | Aktivitäten-Liste zeigt nur gleichartige Text-Boxes | Ein Icon je Oberkategorie Ticket/Projekt/Benutzer ergänzen, nicht je einzelnem Event-Typ |
| 7.2 | Event-Einträge sind alle gleich gestylt | Zunächst Icons aus 7.1 umsetzen; zusätzliche Farben nur ergänzen, falls die Kategorien danach weiterhin schwer erfassbar sind |
| 7.3 | "Letzte Aktivitäten" erklärt den Geltungsbereich nicht | Untertitel "Alle Aktivitäten in deinen Projekten" ergänzen; "Meine Aktivitäten" ist bereits eindeutig |
| 7.4 | Timestamps zeigen bereits Datum und Uhrzeit, aber keine relative Einordnung | Absolute Zeit beibehalten; relative Zeit nur ergänzen, wenn Aktualisierung und Mehrwert den Aufwand rechtfertigen |

---

## 8. Profil-Seite

| # | Problem | Verbesserung |
|---|---------|-------------|
| 8.1 | Profil-Seite zeigt zwei Formulare, aber keinen separaten Account-Überblick | Keine zusätzliche Card nur zur Wiederholung vorhandener Daten; erst bei verfügbaren, relevanten Account-Metadaten neu bewerten |
| 8.2 | Passwortänderung besitzt bereits eine zugängliche Inline-Erfolgsmeldung | Beibehalten; nur im Rahmen eines anwendungsweiten Feedback-Konzepts durch Toast ergänzen oder ersetzen |

---

## 9. Benutzerverwaltung (Admin)

| # | Problem | Verbesserung |
|---|---------|-------------|
| 9.1 | Tabelle ohne client-seitige Suche | Suchfeld über der Tabelle zum Filtern nach Benutzername |
| 9.2 | Rollen/Status als reiner Text | Badge-Komponenten für Rolle (Admin/Nutzer) und Status (Aktiv/Deaktiviert) |
| 9.3 | Feature-Code mischt Lucide und Hugeicons; shadcn-vue verwendet weiterhin Lucide | Bei neuen Feature-Icons Hugeicons verwenden; keine kosmetische Migration der unveränderten shadcn-Komponenten und Lucide nicht entfernen |

---

## 10. Seiten-übergreifend / Global

| # | Problem | Verbesserung |
|---|---------|-------------|
| 10.1 | Mehrere strukturierte Ladezustände bestehen nur aus Text; `Skeleton` ist bereits vorhanden | Skeleton gezielt für Dashboard, Tabellen und Timelines einsetzen; Button-Ladezustände als Text/Spinner belassen |
| 10.2 | Viele Leerzustände bestehen nur aus Text | Kontext- und berechtigungsabhängig Beschreibung, optional Icon und nur bei möglicher Aktion einen CTA ergänzen |
| 10.3 | Fehlerzustände sind visuell inkonsistent | Seiten- und Bereichsfehler mit `Alert variant="destructive"` vereinheitlichen; feldnahe Validierungsfehler lokal belassen |
| 10.4 | Kein globales Toast-System; einige Aktionen besitzen bereits Inline-Erfolgsmeldungen | Toast nur für Aktionen ohne anderweitig sichtbares Ergebnis einführen; direkte Inline-Rückmeldungen beibehalten |
| 10.5 | Kein Skip-Link; Fokusverhalten ist nicht vollständig geprüft; Dokumentensprache ist leer | `skip-to-content` ergänzen, `lang="de"` setzen und Fokus-Reihenfolge manuell per Tastatur testen |
| 10.6 | `JetBrains Mono` wird global für UI-Text und Headings verwendet | Sans-Serif als UI-Grundschrift einsetzen; Mono gezielt für Code, IDs und technische Daten behalten |
| 10.7 | Warmes Taupe-Farbschema ist eine subjektive Designentscheidung | Nur bei Branding-Vorgabe oder Nutzerfeedback evaluieren; CSS-Variablen ermöglichen bereits einen zentralen Wechsel |
| 10.8 | Responsive Grundlagen bestehen, einzelne Ansichten sind mobil aber nur grob gestapelt | Konkrete Defekte beheben: Ticket-Master-Detail, breite Tabellen, Dashboard-Chart und Aktivitäten-Layout |
| 10.9 | Kein globaler Farbübergang beim Theme-Wechsel | Niedrige Priorität; nur mit `prefers-reduced-motion` und komponentenübergreifender Prüfung ergänzen |
| 10.10 | Keine PWA-/Offline-Funktionalität | Nicht umsetzen, solange Installierbarkeit oder Offline-Betrieb keine Produktanforderung sind |

---

## 11. Optionale Themen (Nice-to-Have, Aufwand höher)

| # | Problem | Verbesserung |
|---|---------|-------------|
| 11.1 | Kein Onboarding für neue Nutzer | Zunächst gute, berechtigungsabhängige Leerzustände einsetzen; Dialog erst bei belegtem Onboarding-Problem |
| 11.2 | Ticket-Detail besitzt eine hohe Informationsdichte | Selten benötigte Eckdaten oder Verwaltungsaktionen einklappbar machen; Workflow-Aktionen sichtbar lassen |
| 11.3 | Keine Kanban-Ansicht | Nur bei konkreter Produktanforderung umsetzen; Workflowregeln, Mobile und Tastaturbedienung müssen erhalten bleiben |
| 11.4 | Aktivitäten-Timeline ist nicht filterbar | Bei ausreichend langen Listen clientseitigen Kategoriefilter ergänzen; API-Begrenzung vorher prüfen |

---

## Priorisierung (Empfehlung)

**Quick Wins (geringer Aufwand, hohe Wirkung):**
10.5 (`lang="de"` + Skip-Link), 3.5 (aktives Projekt markieren), 9.1 (Benutzersuche),
9.2 (Rollen-/Status-Badges), 10.3 (Fehlerdarstellung), 6.5 (Editierbarkeit anzeigen),
5.5 (Toggle statt Select), 4.4 (kontextueller Projekt-Leerzustand)

**Mittlerer Aufwand:**
10.6 (UI-Schrift umstellen), 6.1 + 6.6 (Ticketstatus und fünf Prioritätsstufen),
10.1 (gezielte Skeletons), 10.2 (kontextuelle Leerzustände), 5.1 (Breadcrumbs),
6.3 (Workflow/Verwaltung trennen), 6.8 (Kommentar-Stil), 7.1 (Event-Icons),
4.1–4.3 (Projekt-Metadaten, Sortierung, Suche), 1.1 (Dashboard-Zusammenfassung)

**Größere Umbauten (nur bei konkretem Bedarf):**
5.4 (Mobile Master-Detail-Navigation), 1.3 (Ticket-Deep-Links), 10.4 (globales Toast-System),
11.3 (Kanban-View), 1.2 (Projekt-Schnellzugriffe mit belastbarer Datengrundlage)

**Vorerst nicht umsetzen:**
3.3 (zusätzlicher Theme-Toggle), 8.1 (redundante Account-Card), 9.3 (Icon-Migration),
10.7 (Farbschema ohne Designvorgabe), 10.9 (Theme-Transition), 10.10 (PWA ohne Anforderung),
11.1 (Onboarding-Dialog ohne belegten Bedarf)

# Design-Roadmap II: Konsolidierung und Feinschliff

> Stand: 31. Juli 2026
>
> Grundlage: statische Prüfung des aktuellen Frontends gegen die vorherige Roadmap. Der Stand ist insgesamt gut und deutlich über einen reinen Funktionsprototyp hinaus. Die nächste Phase sollte kein Redesign sein, sondern Navigation, Zustände, Accessibility, Responsive-Verhalten und visuelle Konsistenz vervollständigen.
>
> Bewusste Designentscheidung: `JetBrains Mono` bleibt vorerst die globale UI-Schrift. Eine Umstellung auf Sans-Serif ist kein offener Roadmap-Punkt.
>
> Weiterhin unverändert: `src/components/ui/` wird nicht kosmetisch angepasst. Neue Großfeatures entstehen nur bei belegtem Produktbedarf.

---

## 1. Gesamtbewertung

### Was bereits gut funktioniert

- Das warme Farbsystem, die Cards, Abstände und Zustandsfarben ergeben eine erkennbare, technisch-sachliche Produktsprache.
- Light- und Dark-Theme basieren auf zentralen Tokens und wirken strukturell konsistent.
- Dashboard, Projektlisten, Ticketlisten und Admin-Tabellen besitzen wesentlich mehr Informationsdichte, ohne überladen zu wirken.
- Login, Sidebar, Projektarchivierung, Ticketworkflow und destruktive Aktionen haben eine klare visuelle Hierarchie.
- Die mobile Ticket-Master-Detail-Navigation löst den größten früheren Responsive-Defekt.
- Skeletons, Alerts, Badges, kontextuelle Leerzustände und permanente Editierhinweise sind an vielen zentralen Stellen vorhanden.
- Status und Priorität werden nicht ausschließlich über Farbe vermittelt.
- `lang="de"`, Skip-Link, zugängliche Dialoge und Bestätigungen zeigen eine gute Accessibility-Basis.

### Aktuelle Qualitätsstufe

Das Frontend ist visuell bereits **gut und vorzeigbar**, aber noch nicht vollständig poliert. Der größte verbleibende Qualitätsgewinn entsteht nicht durch mehr Dekoration, sondern durch:

1. stabile, adressierbare Navigation;
2. vollständige Lade-, Fehler- und Erfolgszustände;
3. saubere Fokus- und Tastaturführung;
4. bewusst gestaltete mobile Tabellen und Charts;
5. konsistente Seitentitel, Sprache und Rückmeldungen.

### Größte verbleibende Risiken

| Priorität | Risiko | Auswirkung |
|---|---|---|
| Hoch | Tickets und Projekt-Tabs sind nicht adressierbar | Teilen, Reload, Browser-Zurück und Benachrichtigungsnavigation funktionieren nicht erwartungsgemäß |
| Hoch | Initialer Projektload unterscheidet Laden und Fehler nicht | Nutzer können dauerhaft bei `Loading...` hängen bleiben |
| Hoch | Navigation wird teilweise mit Buttons oder klickbaren Tabellenzeilen simuliert | Schwächere Tastatur-, Screenreader- und Browserbedienung |
| Hoch | Fokus bleibt nach Navigation oder Mobile-Ansichtswechsel am alten Ort | Tastaturnutzer verlieren den Kontext |
| Mittel | Breite Tabellen und das Prioritätschart sind mobil nur technisch, nicht gestalterisch gelöst | Hoher Scrollaufwand und schlechte Scanbarkeit |
| Mittel | Erfolgs-, Fehler- und Ladefeedback variiert je Feature | Anwendung wirkt stellenweise unfertig oder reagiert scheinbar nicht |

---

## 2. Navigation und Adressierbarkeit

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 2.1 | Ticketauswahl verändert die URL nicht; Dashboard-Tickets sind nicht interaktiv | Ticketroute wie `/project/:projectId/ticket/:ticketId` ergänzen und Auswahl mit der Route synchronisieren | Tickets können geteilt, neu geladen und über Zurück/Vorwärts navigiert werden |
| 2.2 | Projekt-Tabs leben nur im lokalen Zustand | Tab über Query-Parameter wie `?tab=settings` abbilden | Direkte Links zu Einstellungen/Aktivitäten und korrekte Browsernavigation |
| 2.3 | Dashboard-Ticketzeilen sind reine Anzeige | Nach 2.1 Ticketname als echten `RouterLink` ausführen; nicht die gesamte Tabellenzeile künstlich klickbar machen | Klare Interaktion und normale Browserfunktionen |
| 2.4 | Projektzeilen simulieren Links per `tabindex`, Click und Enter | Projektname als echten `RouterLink` verwenden; Zeilenhover nur als visuelle Unterstützung | Semantisch korrekt, Kontextmenü und Öffnen in neuem Tab möglich |
| 2.5 | Viele Sidebar-Ziele sind programmatische Buttons | Ziele über `RouterLink` im vorhandenen Sidebar-Primitive rendern; Buttons nur für Menüs und Auf-/Zuklappen verwenden | Konsistente Navigation für Browser und assistive Technik |
| 2.6 | Aktives Projekt kann in einer geschlossenen Sidebar-Gruppe verborgen sein | Die Gruppe des aktuell geöffneten Projekts automatisch öffnen | Aktiver Kontext bleibt sichtbar |
| 2.7 | Aktivitäten und Benachrichtigungen nennen Ressourcen, führen aber nicht dorthin | Verfügbare Projekt- und Ticketreferenzen nach 2.1 verlinken | Kürzerer Flow von Ereignis zu Ursache |

**Nicht ausweiten:** Kein eigener globaler Router-State, keine komplexe Breadcrumb-Infrastruktur und keine neue Navigationsbibliothek. Route, Query-Parameter und vorhandene Links reichen aus.

---

## 3. Projektseite und Ticket-Arbeitsbereich

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 3.1 | Fehlende Projektdetails zeigen nur englisches `Loading...`; Fehler und Not-found sind nicht unterscheidbar | Expliziten Initial-Loading-, Error- und Not-found-State aus dem Composable liefern; Skeleton, destruktiven Alert mit Retry und Rücklink anzeigen | Kein stilles Hängen, klare Wiederherstellung |
| 3.2 | Über Tabs fehlt ein dauerhaft sichtbarer Projektkontext | Projektname und knappe Kontextzeile oberhalb der Tabs dauerhaft anzeigen; Breadcrumb nur ergänzen, wenn Herkunft sicher bestimmbar ist | Nutzer wissen auch in Settings/Aktivitäten, welches Projekt offen ist |
| 3.3 | Ticketlistenwechsel zeigt nur Text und ersetzt den Inhalt abrupt | Strukturelles Listenskeleton und `aria-busy` verwenden; bei Refresh vorhandene Daten möglichst stehen lassen | Weniger Layoutsprung und bessere wahrgenommene Geschwindigkeit |
| 3.4 | Mobile Liste/Detail ist visuell gelöst, aber Fokus bleibt im ausgeblendeten Bereich | Beim Öffnen Detailüberschrift oder Zurück-Button fokussieren; beim Zurückgehen das gewählte Ticket wieder fokussieren | Vollständige Tastaturbedienung |
| 3.5 | Inline-Edit ersetzt den auslösenden Button ohne explizite Fokusführung | Eingabe nach Aktivierung fokussieren und nach Speichern/Abbrechen Fokus zum Edit-Button zurückführen | Editieren wirkt unmittelbar und vorhersehbar |
| 3.6 | Archiviertes Projekt und archiviertes Ticket verwenden unterschiedliche Warnhierarchie | Ticket-Hinweis an Icon, Warning-Ton und Formulierung des Projekt-Hinweises angleichen | Einheitliche Bedeutung archivierter Ressourcen |
| 3.7 | Projekteinstellungen speichern ohne sichtbare Erfolgsmeldung; Button kann ohne Änderung aktiv sein | `dirty`-Zustand ableiten, Speichern bis zur Änderung deaktivieren und kurze Inline-Erfolgsmeldung zeigen | Klare Rückmeldung, weniger unnötige Requests |
| 3.8 | Mitgliedsrollen heißen je nach Ansicht unterschiedlich | Eine bestehende zentrale Label-Zuordnung für `Owner`, `Mitwirkender`, `Betrachter` verwenden | Konsistente Fachsprache |
| 3.9 | Dauerhaftes Projektlöschen benötigt nur einen Bestätigungsklick | Nur für Projektlöschung Eingabe des Projektnamens verlangen | Angemessene Absicherung der weitreichendsten Aktion |

---

## 4. Dashboard und Datenvisualisierung

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 4.1 | Prioritätschart wird mit fünf Kategorien und drei Reihen auf kleinen Screens eng | Auf schmalen Screens gestapelte horizontale Balken bevorzugen; alternativ Plot mit Mindestbreite in klar erkennbarem Scrollcontainer | Werte und Labels bleiben lesbar |
| 4.2 | Charts sind visuell verständlich, aber nicht vollständig nicht-visuell erfassbar | Für beide Charts eine zugängliche textuelle Zusammenfassung oder versteckte Datentabelle bereitstellen; dekorative SVG-Ausgabe entsprechend kennzeichnen | Daten bleiben ohne Maus und Grafik verständlich |
| 4.3 | Statuschart und Prioritätschart nutzen unterschiedlich wirkende Plotflächen | Eine gemeinsame Plot-Mindesthöhe und konsistente Innenabstände festlegen | Ruhigeres Kartenraster |
| 4.4 | Recent-Tickets-Tabelle hat keine eigene mobile Strategie | Tabelle horizontal absichern; auf sehr kleinen Screens weniger wichtige Spalten ausblenden oder kompakte Ticketzeilen verwenden | Keine Seitenüberbreite, schnelleres Scannen |
| 4.5 | Status und Priorität erscheinen im Dashboard nur als Text | Vorhandene Status-Badges und Prioritätspunkte aus der Ticketdarstellung wiederverwenden | Gleiche Information sieht überall gleich aus |
| 4.6 | Erfolg ohne Daten kann zu einem fast leeren Dashboard führen | Neutralen Leer-/Unavailable-State für unerwartet fehlende Dashboarddaten ergänzen | Kein stiller Leerzustand |

**Nicht ausweiten:** Keine zusätzlichen Diagramme, Animationen oder KPI-Kacheln ohne neue fachliche Aussage.

---

## 5. Tabellen, Suche und Responsive-Verhalten

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 5.1 | Projektsortierung funktioniert, zeigt aber Spalte und Richtung nicht | Sichtbaren Pfeil ergänzen und `aria-sort` am Tabellenkopf setzen | Sortierzustand ist sofort verständlich |
| 5.2 | Projekt- und Benutzersuche verlassen sich auf Placeholder | `type="search"` und sichtbares oder `sr-only` Label ergänzen | Dauerhafter zugänglicher Name |
| 5.3 | Fehlende optionale Ticketzahlen werden in der Projektliste als `0` dargestellt | Unbekannte Werte als `–` anzeigen oder das Modell verbindlich machen | Keine falsche Aussage „keine Tickets“ |
| 5.4 | Projekt-, Benutzer- und Mitgliedstabellen erzwingen auf Mobile horizontales Scrollen | Unterhalb des Desktop-Breakpoints kompakte Zeilen/Cards mit Name, Rolle, Status/Zahlen und Aktion verwenden | Kerninformationen ohne seitliches Suchen sichtbar |
| 5.5 | Rechte Aktionsspalten können mobil außerhalb des Viewports starten | Bis zur mobilen Darstellung Aktionen am rechten Rand sticky halten oder direkt in die kompakte Zeile integrieren | Aktionen bleiben auffindbar |
| 5.6 | Admin-Leerzustand ist schwächer als Projekt- und Ticket-Leerzustände | Titel, Erklärung und passenden Erstellen-CTA ergänzen; bei Suche explizit „keine Treffer“ unterscheiden | Konsistente Orientierung |
| 5.7 | Ticketbibliotheksdialog verwendet eine feste innere Höhe | Dialog als Flex-Layout mit `min-h-0` und flexiblem Scrollbereich aufbauen | Funktioniert auch bei niedrigen oder quer gehaltenen Displays |

**Prüfgrößen:** mindestens 320 px Breite, Mobile Landscape, 200 % Browserzoom und Desktop mit eingeklappter Sidebar.

---

## 6. Accessibility und Fokus

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 6.1 | Der Skip-Link zielt auf eine ScrollArea, während Shell und mehrere Views eigene `<main>`-Landmarks erzeugen | Genau einen semantischen `<main id="main-content" tabindex="-1">` in der Shell verwenden; innere `<main>` durch `section`/`div` ersetzen | Saubere Landmark-Struktur |
| 6.2 | Routewechsel verschieben den Fokus nicht und der Dokumenttitel bleibt allgemein | Nach Navigation Seitenüberschrift oder Main fokussieren und Route-Metadaten für Titel wie `Projekte – Minerva` setzen | Kontextwechsel wird verständlich angekündigt |
| 6.3 | Mehrere Seiten beginnen bei `h2`, Aktivitäten sogar bei `h4` | Pro Route genau ein `h1`, Bereiche als `h2`, Unterbereiche ohne Sprünge strukturieren | Bessere visuelle und semantische Hierarchie |
| 6.4 | Wizard-Schritte sind nur über Buttonstil erkennbar | Geordnete Schrittliste, `aria-current="step"` und „Schritt X von Y“ ergänzen | Fortschritt ist eindeutig |
| 6.5 | Wizard-Schrittwechsel verschiebt den Fokus nicht | Neue Schrittüberschrift nach erfolgreichem Wechsel fokussieren | Tastaturnutzer landen direkt im neuen Inhalt |
| 6.6 | Wiederholte Workflow-Selects und Löschen-Buttons sind nicht ressourcenspezifisch benannt | Labels wie „Kategorie für Zustand X“ und „Zustand X löschen“ vergeben | Wiederholte Controls bleiben unterscheidbar |
| 6.7 | Auswahl in der Ticketbibliothek ist nur visuell markiert | `aria-pressed` an den vorhandenen Toggle-Buttons ergänzen | Auswahlstatus wird angekündigt |
| 6.8 | Auf-/Zuklappen-Labels nennen teilweise immer nur „anzeigen“ oder „aufklappen“ | Label abhängig vom Zustand zwischen anzeigen/ausblenden wechseln; generiertes `aria-expanded` prüfen | Aktion beschreibt den tatsächlichen nächsten Schritt |
| 6.9 | Farbige Prioritätspunkte duplizieren vorhandenen Text | Dekorative Punkte mit `aria-hidden="true"` markieren | Keine unnötige Ausgabe assistiver Technik |

---

## 7. Rückmeldungen und Zustände

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 7.1 | Loading wird je nach Feature als Skeleton, Text, geleerte Liste oder englischer Fallback dargestellt | Skeleton nur für Erstladen, vorhandene Inhalte bei Refresh erhalten und Ladebereiche mit `aria-busy` markieren | Ruhigeres, konsistentes Verhalten |
| 7.2 | Kommentare und Benachrichtigungen nutzen nur Ladetext | Kleine strukturelle Skeletons plus nicht-visuellen Status ergänzen | Weniger Layoutsprung und bessere Rückmeldung |
| 7.3 | Bereichsfehler erscheinen teils als Alert, teils als roter Text | Feldvalidierung lokal lassen; Request-/Bereichsfehler immer als destruktiven Alert mit Retry, sofern sinnvoll | Einheitliche Fehlerhierarchie |
| 7.4 | Admin-Nutzeraktionen erzeugen eine Erfolgsmeldung im Composable, zeigen sie aber nicht | Vorhandenen Status oberhalb der Tabelle rendern | Aktionen wirken bestätigt statt still |
| 7.5 | Benachrichtigungen werden zweimal initialisiert und beim Laden geleert | Einmal im Shell-/Session-Kontext laden; Komponenten nur Store-Refs konsumieren | Kein doppelter Request und Badge-Flackern |
| 7.6 | Fehler beim „als gelesen markieren“ sind unbehandelt | Mutation absichern, Eintrag ungelesen lassen und retryfähige Meldung zeigen | Zustand bleibt korrekt und nachvollziehbar |
| 7.7 | Mehrere Mutationen haben kein sichtbares Ergebnis | Ein leichtes gemeinsames Erfolgsmuster verwenden; Toast erst einführen, wenn Inline-Feedback räumlich nicht funktioniert | Konsistenz ohne unnötiges globales System |

---

## 8. Aktivitäten und Benachrichtigungen

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 8.1 | Aktivitätenseite besitzt keinen Seitentitel | `Aktivitäten` als `h1` mit knapper Einordnung ergänzen; Feedtitel auf `h2` setzen | Klare Seitenhierarchie |
| 8.2 | „Letzte Aktivitäten“ und „Meine Aktivitäten“ unterscheiden sich sprachlich nur schwach | Parallel benennen: „In meinen Projekten“ und „Von mir ausgeführt“ | Geltungsbereich ist sofort verständlich |
| 8.3 | Fehler leeren Aktivitäten und bieten keinen Retry | Bestehende Einträge bei fehlgeschlagenem Refresh erhalten und Wiederholen-Aktion anzeigen | Kein unnötiger Informationsverlust |
| 8.4 | Zwei Timelines laden jeweils alle Benutzer für Anzeigenamen | Benutzerauflösung einmal teilen oder Anzeigenamen über API-Daten nutzen | Weniger redundante Requests |
| 8.5 | Benachrichtigungen können einzeln gelesen, aber nicht gesammelt abgeschlossen werden | „Alle als gelesen“ ergänzen, sofern Repository/API dies unterstützt | Schneller Umgang mit mehreren Meldungen |
| 8.6 | Benachrichtigungstexte werden abgeschnitten, ohne einen Zielpfad zu bieten | Primär zur Ressource navigieren; nur rein informative Meldungen ohne Ziel belassen | Vollständiger Kontext ohne übergroßes Popover |
| 8.7 | Aktivitäten besitzen noch keinen Filter | Erst bei nachweislich langen Feeds kleinen Kategoriefilter ergänzen | Kein vorzeitiger UI-Ballast |

---

## 9. Login, Profil und Formulare

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 9.1 | Login mischt Deutsch und Englisch | „Anmelden“, „Benutzername“, „Passwort“ und deutsche Fehlermeldung verwenden | Einheitliche Produktsprache |
| 9.2 | Login kann während des Requests mehrfach abgesendet werden | Pending-State führen, Felder und Button vorübergehend deaktivieren, Buttontext anpassen | Keine Doppelrequests, klare Reaktion |
| 9.3 | Benutzername erzwingt Zeichenregeln, erklärt sie aber nicht | Kurzen Hilfetext mit `aria-describedby` ergänzen | Fehler werden vor dem Absenden vermieden |
| 9.4 | Passwortänderung zeigt Anforderungen und Abweichungen erst spät | Sichtbarkeitsschalter und direkten Hinweis bei nicht übereinstimmenden Passwörtern ergänzen | Weniger Eingabefehler |
| 9.5 | Native Selects im Projektwizard wirken anders als übrige Controls | Eine gemeinsame Feature-Klasse für Höhe, Fokus, Disabled-State und Abstände verwenden | Visuelle Konsistenz ohne Primitive-Umbau |
| 9.6 | „Benutzer“ und „Nutzer“ werden gemischt verwendet | „Benutzer“ für Personen und Verwaltung konsistent verwenden; Rollenlabel nur fachlich abweichend benennen | Ruhigere Microcopy |

---

## 10. Visueller Feinschliff

Diese Punkte machen die Anwendung ruhiger und hochwertiger, sind aber nach Navigation und Zuständen zu bearbeiten.

| # | Beobachtung | Verbesserung | Nutzen |
|---|---|---|---|
| 10.1 | Seiten verwenden unterschiedliche Breiten und Headerabstände | Drei bestehende Layoutmuster festlegen: Formularseite, Standardseite, breiter Arbeitsbereich; zunächst nur Klassen vereinheitlichen | Konsistenter Rhythmus ohne neue Komponentenarchitektur |
| 10.2 | Nicht jede Seite hat Titel plus Einordnung | Für primäre Routen einheitlichen Header aus `h1` und optional einem Satz Beschreibung verwenden | Bessere Orientierung und visuelle Hierarchie |
| 10.3 | Mobile Shell zeigt nur einen losgelösten Menübutton | Kompakte mobile Kopfzeile mit Menübutton und aktuellem Seitentitel; Benachrichtigung nur ergänzen, wenn sie in der Sidebar zu versteckt bleibt | App wirkt mobil vollständiger |
| 10.4 | Sidebar kann bei vielen Projekten sehr lang werden | Projektkürzel begrenzen und „Alle anzeigen“ anbieten; erst bei realer Listenlänge umsetzen | Weniger Navigationsrauschen |
| 10.5 | Admin-Aktionen sind teilweise deaktiviert, ohne den Grund zu nennen | Kurze Tooltips/Hilfetexte für eigene Rolle, Selbstdeaktivierung und Admin-Löschung ergänzen | Regeln wirken nachvollziehbar statt kaputt |
| 10.6 | Erfolgsnachrichten sehen wie normaler Hilfetext aus | Einheitlichen positiven Status mit dezentem Icon/Farbton verwenden | Wichtige Bestätigung ist schneller erkennbar |

### Bewusst nicht ändern

- Die globale `JetBrains Mono`-Schrift bleibt bestehen.
- Das warme Taupe-/Rot-Farbschema bleibt bestehen.
- Keine kosmetische Migration von Lucide-Icons in generierten shadcn-vue-Komponenten.
- Kein globaler Theme-Übergang ohne konkrete Designvorgabe.
- Keine Animationen, Gradients oder dekorativen Hintergründe nur für mehr „Wow“.

---

## 11. Umsetzung in Phasen

### Phase A: Stabilität und Kernflow

1. Projekt-Loading, Error, Not-found und Retry sauber trennen.
2. Ticket-Deep-Link einführen und Dashboard-Tickets verlinken.
3. Projekt-Tabs über URL synchronisieren.
4. Projekt- und Sidebar-Navigation auf echte Links umstellen.
5. Benachrichtigungen nur einmal laden und Mutationsfehler behandeln.

**Ergebnis:** Die Anwendung verhält sich bei Navigation, Reload und Fehlern wie ein fertiges Produkt.

### Phase B: Accessibility und Feedback

1. Einen einzigen Main-Landmark und korrekte Skip-Link-Zielstruktur herstellen.
2. Route-, Mobile-Detail-, Inline-Edit- und Wizard-Fokusführung ergänzen.
3. Überschriftenhierarchie, Suchlabels, Sortierstatus und wiederholte Control-Labels korrigieren.
4. Chartdaten textuell zugänglich machen.
5. Erfolgsmeldungen und Bereichsfehler vereinheitlichen.

**Ergebnis:** Tastatur-, Screenreader- und Statuskommunikation sind durchgängig statt punktuell.

### Phase C: Responsive Feinschliff

1. Projekt-, Benutzer- und Mitgliedslisten mobil als kompakte Zeilen/Cards darstellen.
2. Prioritätschart für schmale Displays umbauen.
3. Dashboard-Tickets und Ticketbibliotheksdialog mobil absichern.
4. Mobile Kopfzeile und Sidebar-Länge prüfen.

**Ergebnis:** Mobile funktioniert nicht nur, sondern wirkt bewusst gestaltet.

### Phase D: Visuelle Konsolidierung

1. Seitenheader, Breiten und vertikale Abstände angleichen.
2. Archiv-, Lade-, Leer-, Fehler- und Erfolgszustände über Features hinweg angleichen.
3. Rollen-, Status- und Prioritätsdarstellung wiederverwenden.
4. Deutsche Begriffe und Microcopy vereinheitlichen.

**Ergebnis:** Weniger kleine Brüche, ruhigeres und professionelleres Gesamtbild.

---

## 12. Bewusst zurückstellen

| Thema | Entscheidung | Neu bewerten, wenn ... |
|---|---|---|
| Projektschnellzugriffe | Nicht ergänzen | Nutzungsdaten oder Interviews häufige Wiederkehr belegen |
| Globale Toast-Infrastruktur | Nicht vorab einführen | Aktionen regelmäßig kein sinnvolles Inline-Ziel besitzen |
| Aktivitätsfilter | Noch nicht ergänzen | Feeds real so lang werden, dass Kategorien schwer auffindbar sind |
| Vollständige Benachrichtigungsseite | Nicht ergänzen | Popover und API-Limit für den tatsächlichen Verlauf nicht ausreichen |
| Kanban | Nicht ergänzen | Fachliche Anforderung inklusive Workflow-, Mobile- und Tastaturkonzept vorliegt |
| Onboarding-Dialog | Nicht ergänzen | Gute Leerzustände und klare Navigation nachweislich nicht reichen |
| PWA/Offline | Nicht ergänzen | Installierbarkeit oder Offline-Arbeit Produktanforderung wird |
| Weitere Dashboard-Charts | Nicht ergänzen | Eine konkrete Entscheidung durch zusätzliche Daten besser getroffen wird |
| Schriftwechsel | Nicht einplanen | Die aktuelle Mono-Schrift in Nutzerfeedback nachweislich die Lesbarkeit beeinträchtigt |

---

## 13. Abnahme-Checkliste

Die nächste UI-Runde gilt als sauber abgeschlossen, wenn:

- jede Route genau ein `h1` und die Anwendung genau einen `main`-Landmark besitzt;
- Dashboard, Benachrichtigungen und Aktivitäten direkt zu vorhandenen Projekten/Tickets führen;
- Ticket und Projekt-Tab nach Reload wieder geöffnet sind;
- initiales Laden, Refresh, leer, Fehler, kein Zugriff und nicht gefunden unterscheidbar sind;
- jeder Request-Fehler eine verständliche Meldung und, wo sinnvoll, einen Retry besitzt;
- Tastaturfokus nach Route-, Wizard- und Mobile-Detailwechsel sinnvoll landet;
- Suche, Sortierung, Auswahl und wiederholte Formularcontrols zugängliche Namen/Zustände besitzen;
- Projekt-, Benutzer- und Mitgliedslisten bei 320 px ohne versteckte Kernaktionen bedienbar sind;
- Charts bei schmaler Breite lesbar und ohne Grafik inhaltlich verständlich bleiben;
- sichtbare Texte konsistent deutsch und Rollenbezeichnungen einheitlich sind;
- Light- und Dark-Theme sowie 200 % Zoom manuell geprüft wurden.

### Empfohlene manuelle Prüfrouten

1. Login mit Fehlversuch und langsamem Request.
2. Dashboard → Ticket → Browser zurück → Ticketlink neu laden.
3. Projektliste suchen, sortieren und Projekt in neuem Tab öffnen.
4. Projekt auf Mobile öffnen, Ticket wählen, zurück zur Liste und Fokus prüfen.
5. Projekt-Settings direkt per URL öffnen, speichern, archivieren und wiederherstellen.
6. Benachrichtigung öffnen, als gelesen markieren und Fehlerfall simulieren.
7. Aktivitäten mit leerem Feed, Ladefehler und langen Details prüfen.
8. Admin-Benutzer bei 320 px, 200 % Zoom und nur per Tastatur verwalten.

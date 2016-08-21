# Seanox Review
Eine textbasierte Code-Analyse zur Prüfung und Korrektur stiller Standards.

Stille Standards sind projektspezifische sowie konzeptionelle Vorgaben und Standards
sowie Richtlinien (Code of Conduct), die nicht durch die syntaktische und grammatikalische
Prüfung eines Compilers abgedeckt werden und durch technische und fachliche Konzepte
definiert werden.

Das Werkzeug basiert auf regulären Ausdrücken, Dateifiltern und kombiniert Muster
und Ausschlüsse, was u.a. die Suche nach nicht existierenden, unvollständigen sowie
nicht erfüllten Mustern ermöglich.


## Motivation
In einem Projekt mit vielen Beteiligten gibt es eine Vielzahl von persönlichen und
individuellen Vorlieben bei der Implementierung. Einige, wie z.B. Formatierung sind
rein kosmetisch, andere wie z.B. die Fehlerbehandlung oder die Implementierung
und Verwendung der verschiedenen Schichten einer Software sind projekt- und architekturrelevant.
In komplexen Projekten mit zahlreichen Komponenten und Diensten fallen Verfehlungen
und Irrwege oft erst sehr spät auf, da die Implementierung syntaktisch richtig,
für eine Programmiersprache bekannte Anti-Pattern nicht gefunden werden und der
Test der Implementierung erfolgreich durchlaufen wird.

Erst Änderung und Erweiterung der Architektur und Schnittstellen oder bei der späteren 
Fehleranalyse zeigen dann erst Verfehlungen und Irrwege auf.

So sind Code- und Architektur-Reviews feste Bestandteile vom Qualitätsmanagement
und laufen parallel zur Implementierung.

Auffälligkeiten und Verfehlungen wiederholt zu finden, zu dokumentieren und wenn
möglich zu korrigieren. Das sind die Erwartungen an Review.


## Lizenz
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden Seanox Software
Solutions oder kurz Seanox genannt. Diese Software unterliegt der Version 2 der
GNU General Public License.

Review, text based code analyzer  
Copyright (C) 2016 Seanox Software Solutions

This program is free software; you can redistribute it and/or modify it under the 
terms of version 2 of the GNU General Public License as published by the Free Software
Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this
program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA.


## Funktionsumfang
- Werkzeug für die Kommandozeile
- textbasierte Suche mit regulären Ausdrücken mit Ein- und Ausschlüssen
- Dateifiler mit Ein- und Ausschlüssen
- einfache Definition der Reviews
- parallele Ausführung der Reviews


## Systemanforderungen
- Java 8


## Downloads
[Seanox Review 1.3.4 (Build 20160820)](https://github.com/seanox/review/raw/master/releases/seanox-review-1_3_4.zip)


## Verwendung
TODO:

### Pattern
TODO:

#### Aufbau
TODO:

#### Syntax
- zeilenorientiert
- Kommentare beginnen mit ``#``
- maskieren von Zeichen ``\x<HEX>``  
    ``\x23`` = #  
    ``\x20`` = Leerzeichen  
- weitere Steuerbefehle  
    ``\R   ``= Zeilenumbruch (plattformübergreifend)  
    ``VOID ``= leerer String beim Ersetzen  
    ``...  ``= Fortführung der Zeile

##### Dateifilter
- Backslash/Slash werden gleich behandelt
- Platzhalter ``*`` und ``?`` werden unterstützt
- Trennzeichen ``+`` (einbeziehen) und ``-``(ausschliessen)
- Ausschlüsse auf Ebene der Fundstelle 
    ``file[line]`` oder ``file[line:char]``

##### Suchmuster
- regulärer Ausdruck der auf den Inhalt einer Datei angewandt wird
- folgt dem ersten Leerzeichen ein ``!`` wird der Folgeausdruck als Ausnahme interpretiert:  
    ``Face\.\w+ !Face\.(xhtml|on(Show|Validate|Error|Event))``  
  sucht nach ``Face\.\w+``, ignoriert aber _Face.xhtml_, _Face.onShow_, _Face.onValidate_, ...  
  __WICHTIG__:  
    Die Ausnahme wird nur innerhalb der Fundstelle geprüft.   
    Liegen die Merkmale zur Unterscheidung ausserhalb, muss das Muster 
    entsprechend ausgedehnt werden.

##### Aktion
- Ausdruck zum Ersetzen (``$1`` - ``$9`` werden unterstützt)
- beginnt eine Aktion mit ``INFO:`` wird nur eine Meldung ausgegeben
- beginnt eine Aktion mit ``ECHO:`` oder ``TEST:`` wird nur eine Meldung mit der
    Ersetzung als Vorschau ausgegeben
- entspricht die Aktion ``VOID``, wird die Fundstelle durch einen leeren String ersetzt,
    was dem Löschen entspricht

#### Beispiele
TODO:


## Best Practice / Erfolgsrezept
TODO:


## Historie

### 1.3.4
- Erweiterung: integrierter Hilfe
- Optimierung: allgemein in Grösse und Verarbeitungsgeschwindigkeit
- Optimierung: besserer Verarbeitung von zeilenübergreifenden Mustern
- Optimierung: verbesserter Build-Prozess
- Änderung: Umstellung der Aktion zum Löschen auf das Schlüsselwort ``VOID``
- Änderung: Umzug des Projekts nach GitHub

### 1.3.3
- Erweiterung: mit der Aktion ``TEST:`` und ``ECHO:`` wird eine Vorschau der Ersetzung
    ausgegeben
- Optimierung: allgemein in Grösse und Verarbeitungsgeschwindigkeit
- Korrektur: allgemeine kleine Fehler ohne funktionale Auswirkung 

### 1.3.2
- Erweiterung: mit der Aktion \x00 wird das Löschen ein Fundstelle unterstützt

### 1.3.1
- Erweiterung: Ausgabe der Reviews in der Zusammenfassung

### 1.3
- Korrektur: bei den Verarbeitung der Ausschlüsse
- Optimierung: Dateifilter/Zeilenausschlüsse
- Optimierung: in der Verarbeitung/Anwendung zeilenübergreifender Muster
- Änderung: Startparameter ``-ì`` wird zu ``-v``

### 1.2.1
- Erweiterung: in der Ausgabe während der Verarbeitung und der Zusammenfassung

### 1.2
- Erweiterung: um Ausschlüsse bei Inhalten und Dateifiltern
- Optimierung: Umstellung von Thread auf Worker
- Optimierung: der nebenläufigen Verarbeitung
- Optimierung: in der Mustererkennung

### 1.1
- Erweiterung: Ausgabe der Zeilennummer aus der pattern-Datei

### 1.0
- Initiale Version

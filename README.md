# Seanox Review
Eine textbasierte Code-Analyse zur Pr�fung und Korrektur stiller Standards.

Stille Standards sind projektspezifische sowie konzeptionelle Vorgaben und Standards
sowie Richtlinien (Code of Conduct), die nicht durch die syntaktische und grammatikalische
Pr�fung eines Compilers abgedeckt werden und durch technische und fachliche Konzepte
definiert werden.

Das Werkzeug basiert auf regul�ren Ausdr�cken, Dateifiltern und kombiniert Muster
und Ausschl�sse, was u.a. die Suche nach nicht existierenden, unvollst�ndigen sowie
nicht erf�llten Mustern erm�glich.


## Motivation
In einem Projekt mit vielen Beteiligten gibt es eine Vielzahl von pers�nlichen und
individuellen Vorlieben bei der Implementierung. Einige, wie z.B. Formatierung sind
rein kosmetisch, andere wie z.B. die Fehlerbehandlung oder die Implementierung
und Verwendung der verschiedenen Schichten einer Software sind projekt- und architekturrelevant.
In komplexen Projekten mit zahlreichen Komponenten und Diensten fallen Verfehlungen
und Irrwege oft erst sehr sp�t auf, da die Implementierung syntaktisch richtig,
f�r eine Programmiersprache bekannte Anti-Pattern nicht gefunden werden und der
Test der Implementierung erfolgreich durchlaufen wird.

Erst �nderung und Erweiterung der Architektur und Schnittstellen oder bei der sp�teren 
Fehleranalyse zeigen dann erst Verfehlungen und Irrwege auf.

So sind Code- und Architektur-Reviews feste Bestandteile vom Qualit�tsmanagement
und laufen parallel zur Implementierung.

Auff�lligkeiten und Verfehlungen wiederholt zu finden, zu dokumentieren und wenn
m�glich zu korrigieren. Das sind die Erwartungen an Review.


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
- Werkzeug f�r die Kommandozeile
- textbasierte Suche mit regul�ren Ausdr�cken mit Ein- und Ausschl�ssen
- Dateifiler mit Ein- und Ausschl�ssen
- einfache Definition der Reviews
- parallele Ausf�hrung der Reviews


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
    ``\R   ``= Zeilenumbruch (plattform�bergreifend)  
    ``VOID ``= leerer String beim Ersetzen  
    ``...  ``= Fortf�hrung der Zeile

##### Dateifilter
- Backslash/Slash werden gleich behandelt
- Platzhalter ``*`` und ``?`` werden unterst�tzt
- Trennzeichen ``+`` (einbeziehen) und ``-``(ausschliessen)
- Ausschl�sse auf Ebene der Fundstelle 
    ``file[line]`` oder ``file[line:char]``

##### Suchmuster
- regul�rer Ausdruck der auf den Inhalt einer Datei angewandt wird
- folgt dem ersten Leerzeichen ein ``!`` wird der Folgeausdruck als Ausnahme interpretiert:  
    ``Face\.\w+ !Face\.(xhtml|on(Show|Validate|Error|Event))``  
  sucht nach ``Face\.\w+``, ignoriert aber _Face.xhtml_, _Face.onShow_, _Face.onValidate_, ...  
  __WICHTIG__:  
    Die Ausnahme wird nur innerhalb der Fundstelle gepr�ft.   
    Liegen die Merkmale zur Unterscheidung ausserhalb, muss das Muster 
    entsprechend ausgedehnt werden.

##### Aktion
- Ausdruck zum Ersetzen (``$1`` - ``$9`` werden unterst�tzt)
- beginnt eine Aktion mit ``INFO:`` wird nur eine Meldung ausgegeben
- beginnt eine Aktion mit ``ECHO:`` oder ``TEST:`` wird nur eine Meldung mit der
    Ersetzung als Vorschau ausgegeben
- entspricht die Aktion ``VOID``, wird die Fundstelle durch einen leeren String ersetzt,
    was dem L�schen entspricht

#### Beispiele
TODO:


## Best Practice / Erfolgsrezept
TODO:


## Historie

### 1.3.4
- Erweiterung: integrierter Hilfe
- Optimierung: allgemein in Gr�sse und Verarbeitungsgeschwindigkeit
- Optimierung: besserer Verarbeitung von zeilen�bergreifenden Mustern
- Optimierung: verbesserter Build-Prozess
- �nderung: Umstellung der Aktion zum L�schen auf das Schl�sselwort ``VOID``
- �nderung: Umzug des Projekts nach GitHub

### 1.3.3
- Erweiterung: mit der Aktion ``TEST:`` und ``ECHO:`` wird eine Vorschau der Ersetzung
    ausgegeben
- Optimierung: allgemein in Gr�sse und Verarbeitungsgeschwindigkeit
- Korrektur: allgemeine kleine Fehler ohne funktionale Auswirkung 

### 1.3.2
- Erweiterung: mit der Aktion \x00 wird das L�schen ein Fundstelle unterst�tzt

### 1.3.1
- Erweiterung: Ausgabe der Reviews in der Zusammenfassung

### 1.3
- Korrektur: bei den Verarbeitung der Ausschl�sse
- Optimierung: Dateifilter/Zeilenausschl�sse
- Optimierung: in der Verarbeitung/Anwendung zeilen�bergreifender Muster
- �nderung: Startparameter ``-�`` wird zu ``-v``

### 1.2.1
- Erweiterung: in der Ausgabe w�hrend der Verarbeitung und der Zusammenfassung

### 1.2
- Erweiterung: um Ausschl�sse bei Inhalten und Dateifiltern
- Optimierung: Umstellung von Thread auf Worker
- Optimierung: der nebenl�ufigen Verarbeitung
- Optimierung: in der Mustererkennung

### 1.1
- Erweiterung: Ausgabe der Zeilennummer aus der pattern-Datei

### 1.0
- Initiale Version

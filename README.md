# Description
Seanox Review, eine textbasierte Code-Analyse zur Prüfung und Korrektur stiller
Standards.

Stille Standards sind projektspezifische sowie konzeptionelle Vorgaben und
Standards sowie Richtlinien (Code of Conduct), die nicht durch die syntaktische
und grammatikalische Prüfung eines Compilers abgedeckt werden und durch
technische und fachliche Konzepte definiert werden.

Das Werkzeug basiert auf regulären Ausdrücken, Dateifiltern und kombiniert
Muster und Ausschlüsse, was u.a. die Suche nach nicht existierenden,
unvollständigen sowie nicht erfüllten Mustern ermöglicht.


## Motivation
In einem Projekt mit vielen Beteiligten gibt es eine Vielzahl von persönlichen
und individuellen Vorlieben bei der Implementierung. Einige, wie z.B.
Formatierung sind rein kosmetisch, andere wie z.B. die Fehlerbehandlung oder die
Implementierung und Verwendung der verschiedenen Schichten einer Software sind
projekt- und architekturrelevant. In komplexen Projekten mit zahlreichen
Komponenten und Diensten fallen Verfehlungen und Irrwege oft erst sehr spät auf,
da die Implementierung syntaktisch richtig, für eine Programmiersprache bekannte
Anti-Pattern nicht gefunden werden und der Test der Implementierung erfolgreich
durchlaufen wird.

Erst Änderung und Erweiterung der Architektur und Schnittstellen oder bei der
späteren Fehleranalyse zeigen dann erst Verfehlungen und Irrwege auf.

So sind Code- und Architektur-Reviews feste Bestandteile vom Qualitätsmanagement
und laufen parallel zur Implementierung.

Auffälligkeiten und Verfehlungen wiederholt zu finden, zu dokumentieren und wenn
möglich zu korrigieren. Das sind die Erwartungen an Review.


# Features
- Werkzeug für die Kommandozeile
- textbasierte Suche mit regulären Ausdrücken mit Ein- und Ausschlüssen
- Dateifiler mit Ein- und Ausschlüssen
- einfache Definition der Reviews
- parallele Ausführung der Reviews


# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der GNU General Public License.

Copyright (C) 2018 Seanox Software Solutions

This program is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published by the
Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
Street, Fifth Floor, Boston, MA 02110-1301, USA.


# System Requirement
- Java 8


# Downloads
[Seanox Review 1.4.1](https://github.com/seanox/review/raw/master/releases/seanox-review-1.4.1.zip)  
[Seanox Review Sources 1.4.1](https://github.com/seanox/review/raw/master/releases/seanox-review-1.4.1-src.zip) 


# Changes (Change Log)
## 1.4.1 20180221 (summary of the current version)  
BF: Correction of the preview and continuation when replacing  

[Read more](https://raw.githubusercontent.com/seanox/review/master/CHANGES)


# Contact
[Support](http://seanox.de/contact?support)  
[Development](http://seanox.de/contact?development)  
[Project](http://seanox.de/contact?service)  
[Page](http://seanox.de/contact)  

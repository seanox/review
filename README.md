<p>
  <a href="https://github.com/seanox/review/pulls"
      title="Development is waiting for new issues / requests / ideas">
    <img src="https://img.shields.io/badge/development-passive-blue?style=for-the-badge">
  </a>
  <a href="https://github.com/seanox/review/issues">
    <img src="https://img.shields.io/badge/maintenance-active-green?style=for-the-badge">
  </a>
  <a href="http://seanox.de/contact">
    <img src="https://img.shields.io/badge/support-active-green?style=for-the-badge">
  </a>
</p>


# Description
Seanox Review, eine textbasierte Code-Analyse zur Pr�fung und Korrektur stiller
Standards.

Stille Standards sind projektspezifische sowie konzeptionelle Vorgaben und
Standards sowie Richtlinien (Code of Conduct), die nicht durch die syntaktische
und grammatikalische Pr�fung eines Compilers abgedeckt werden und durch
technische und fachliche Konzepte definiert werden.

Das Werkzeug basiert auf regul�ren Ausdr�cken, Dateifiltern und kombiniert
Muster und Ausschl�sse, was u.a. die Suche nach nicht existierenden,
unvollst�ndigen sowie nicht erf�llten Mustern erm�glicht.


## Motivation
In einem Projekt mit vielen Beteiligten gibt es eine Vielzahl von pers�nlichen
und individuellen Vorlieben bei der Implementierung. Einige, wie z.B.
Formatierung sind rein kosmetisch, andere wie z.B. die Fehlerbehandlung oder die
Implementierung und Verwendung der verschiedenen Schichten einer Software sind
projekt- und architekturrelevant. In komplexen Projekten mit zahlreichen
Komponenten und Diensten fallen Verfehlungen und Irrwege oft erst sehr sp�t auf,
da die Implementierung syntaktisch richtig, f�r eine Programmiersprache bekannte
Anti-Pattern nicht gefunden werden und der Test der Implementierung erfolgreich
durchlaufen wird.

Erst �nderung und Erweiterung der Architektur und Schnittstellen oder bei der
sp�teren Fehleranalyse zeigen dann erst Verfehlungen und Irrwege auf.

So sind Code- und Architektur-Reviews feste Bestandteile vom Qualit�tsmanagement
und laufen parallel zur Implementierung.

Auff�lligkeiten und Verfehlungen wiederholt zu finden, zu dokumentieren und wenn
m�glich zu korrigieren. Das sind die Erwartungen an Review.


# Features
- Werkzeug f�r die Kommandozeile
- textbasiertes Suchen und Ersetzen mit regul�ren Ausdr�cken mit Ein- und Ausschl�ssen
- Dateifiler mit Ein- und Ausschl�ssen
- einfache Definition der Reviews
- parallele Ausf�hrung der Reviews


# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der GNU General Public License.

Copyright (C) 2020 Seanox Software Solutions

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
[Seanox Review 1.4.2](https://github.com/seanox/review/raw/master/releases/seanox-review-1.4.2.zip)  
[Seanox Review Sources 1.4.2](https://github.com/seanox/review/raw/master/releases/seanox-review-1.4.2-src.zip) 


# Changes (Change Log)
## 1.4.3 2020xxxx (summary of the next version)  
BF: Correction of repeated/multiple searches and replacements in the same file  
CR: Update of the comment format  
CR: Uniform use of ./LICENSE and ./CHANGES  
CR: Update to use Java 11  

[Read more](https://raw.githubusercontent.com/seanox/review/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/review/issues)  
[Requests](https://github.com/seanox/review/pulls)  
[Mail](http://seanox.de/contact)


# Thanks!
<img src="https://raw.githubusercontent.com/seanox/seanox/master/sources/resources/images/thanks.png">

[JetBrains](https://www.jetbrains.com/?from=seanox)  
Sven Lorenz  
Andreas Mitterhofer  
[novaObjects GmbH](https://www.novaobjects.de)  
Leo Pelillo  
Gunter Pfannm&uuml;ller  
Annette und Steffen Pokel  
Edgar R&ouml;stle  
Michael S&auml;mann  
Markus Schlosneck  
[T-Systems International GmbH](https://www.t-systems.com)

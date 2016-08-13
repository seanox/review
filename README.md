# Review
tools for programming languages independent code reviews


# Beschreibung
Review wurde zur Pr�fung und Korrektur stiller Standards entwickelt.

Stille Standards sind projekt- und architekturspezifische Vorgaben und Standards,
die nicht durch die syntaktische und grammatikalische Pr�fung eines Compilers abgedeckt
werden, sondern mehr durch technische- und fachliche Konzepte definiert werden.

Das Werkzeug basiert komplett auf regul�ren Ausdr�cken und Dateifiltern, es arbeitet
rein textbasiert, findet, schliesst aus und ersetzt. Die Kombination von Finden
und Ausschluss erm�glich die Suche nach nicht existierenden, unvollst�ndigen sowie
nicht erf�llten Mustern.

# Motivation
In einem Projekt mit einer Vielzahl von Beteiligten gibt es eine Vielzahl von pers�nlichen
und individuellen Vorlieben bei der Implementierung. Einige, wie z.B. Formatierung
sind rein kosmetisch, andere wie z.B. die Fehlerbehandlung oder die Implementierung
und Verwendung der verschiedenen Schichten einer Software sind projekt- und architekturrelevant.
In komplexen Projekten mit einer Vielzahl von Komponenten und Diensten fallen Verfehlungen
und Irrwege erst sehr sp�t auf, da die Implementierung syntaktisch richtig, f�r
eine Programmiersprache bekannte Anti-Pattern nicht gefunden und der Test der Implementierung
erfolgreich durchlaufen wird.

Erst mit �nderung und Erweiterung der Architektur und Schnittstellen oder bei der
sp�teren  Fehleranalyse in der Abnahmephase werden Verfehlungen und Irrwege sichtbar.

So sind Code- und Architektur-Reviews feste Bestandteile vom Qualit�tsmanagement
und laufen parallel zur Implementierung.

Auff�lligkeiten und Verfehlungen wiederholt zu finden, zu dokumentieren und wenn
m�glich zu korrigieren. Das sind die Erwartungen an Review.

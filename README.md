# Review
Tool for programming languages independent code reviews.


# Beschreibung
Review wurde zur Prüfung und Korrektur stiller Standards entwickelt.

Stille Standards sind projekt- und architekturspezifische Vorgaben und Standards,
die nicht durch die syntaktische und grammatikalische Prüfung eines Compilers abgedeckt
werden, sondern mehr durch technische- und fachliche Konzepte definiert werden.

Das Werkzeug basiert komplett auf regulären Ausdrücken und Dateifiltern, es arbeitet
rein textbasiert, findet, schliesst aus und ersetzt. Die Kombination von Finden
und Ausschluss ermöglich die Suche nach nicht existierenden, unvollständigen sowie
nicht erfüllten Mustern.


# Motivation
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


# Verwendung
TODO:

## Pattern
TODO:

### Aufbau
TODO:

### Syntax
- zeilenorientiert
- Kommentare beginnen mit ``#``
- maskieren von Zeichen ``\x<HEX>``  
    ``\x23`` = #  
    ``\x20`` = Leerzeichen  
- weitere Steuerbefehle  
    ``\R   ``= Zeilenumbruch (plattformübergreifend)  
    ``VOID ``= leerer String beim Ersetzen  
    ``...  ``= Fortführung der Zeile

#### Dateifilter
- Backslash/Slash werden gleich behandelt
- Platzhalter ``*`` und ``?`` werden unterstützt
- Trennzeichen ``+`` (einbeziehen) und ``-``(ausschliessen)
- Ausschlüsse auf Ebene der Fundstelle 
    ``file[line]`` oder ``file[line:char]``

#### Suchmuster
- regulärer Ausdruck der auf den Inhalt einer Datei angewandt wird
- folgt dem ersten Leerzeichen ein ``!`` wird der Folgeausdruck als Ausnahme interpretiert:  
    ``Face\.\w+ !Face\.(xhtml|on(Show|Validate|Error|Event))``  
  sucht nach:  
    ``Face\.\w+`` ignoriert aber _Face.xhtml_, _Face.onShow_, _Face.onValidate_, ...  
  __WICHTIG__:  
    Die Ausnahme wird nur innerhalb der Fundstelle geprüft.   
    Liegen die Merkmale zur Unterscheidung ausserhalb, muss das Muster 
    entsprechend ausgedehnt werden.

#### Aktion
- Ausdruck zum Ersetzen (``$1`` - ``$9`` werden unterstützt)
- beginnt eine Aktion mit ``INFO:`` wird nur eine Meldung ausgegeben
- beginnt eine Aktion mit ``ECHO:`` oder ``TEST:`` wird nur eine Meldung mit der
    Ersetzung als Vorschau ausgegeben
- entspricht die Aktion ``VOID``, wird die Funstelle durch einen leeren String ersetzt,
    was dem Löschen entspricht

### Beispiele
TODO:


# Best Practice / Erfolgsrezept
TODO:
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
Seanox Review, a text-based code analysis for checking and correcting silent
standards.

Silent standards are project-specific as well as conceptual regulations and
standards and guidelines (code of conduct) that are not covered by the syntactic
and grammatical check of a compiler and are defined by technical and
professional concepts.

The tool is based on regular expressions, file filters and combines patterns and
exclusions, which enables, among other things, the search for non-existent,
un-complete as well as not-fulfilled patterns.


## Motivation
In a project with many participants, there are many personal and individual
preferences during implementation. Some, such as formatting, are purely
cosmetic, others, such as error handling or the implementation and use of the
different layers of a software are relevant to the project and architecture. In
complex projects with numerous components and services, mistakes and aberrations
often only become apparent at a late stage because the implementation is
syntactically correct, anti-patterns known for a programming language are not
found and the implementation is successfully tested.

Only changes and extensions to the architecture and interfaces or later error
analysis will reveal mistakes and aberrations.

Thus code and architecture reviews are fixed components of quality management
and run parallel to implementation.

To repeatedly find, to document and, if possible, to correct conspicuousness and
misconduct, these are the expectations of review.


# Features
- Command line tool
- text-based search and replace with regular expressions with inclusions and exclusions
- File filter with inclusions and exclusions
- simple definition of the reviews
- parallel execution of the reviews


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

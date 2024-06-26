<p>
  <a href="https://github.com/seanox/review/pulls"
      title="Development is waiting for new issues / requests / ideas"
    ><img src="https://img.shields.io/badge/development-passive-blue?style=for-the-badge"
  ></a>
  <a href="https://github.com/seanox/review/issues"
    ><img src="https://img.shields.io/badge/maintenance-active-green?style=for-the-badge"
  ></a>
  <a href="http://seanox.de/contact"
    ><img src="https://img.shields.io/badge/support-active-green?style=for-the-badge"
  ></a>
</p>


# Description
Seanox Review, a text-based static code analysis for checking and correcting
silent standards.

Silent standards are project-specific as well as conceptual regulations and
standards and guidelines that are not covered by the syntactic and grammatical
check of a compiler and are defined by technical and professional concepts.

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
- expression-based static code analysis
- text-based search and replace with regular expressions, supports inclusions
  and exclusions
- File filter with inclusions and exclusions
- simple definition of the reviews
- parallel execution of the reviews


# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der Apache License.

Copyright (C) 2024 Seanox Software Solutions

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.


# System Requirement
- Java 11 or higher


# Downloads
[Seanox Review 1.5.1](https://github.com/seanox/review/releases/download/1.5.1/seanox-review-1.5.1.zip)  


# Changes
## 1.5.1 20240602  
BF: Optimization and corrections  
BF: Correction of the synchronization of CHANGES and README.md at the release  
BF: Correction of the missing CHANGES in the release file  

[Read more](https://raw.githubusercontent.com/seanox/review/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/review/issues)  
[Requests](https://github.com/seanox/review/pulls)  
[Mail](http://seanox.de/contact)

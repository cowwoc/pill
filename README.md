# What is Pill?

Pill is a software build system that relieves the pain caused by Make, Ant and Maven. In contrast with these systems:

* Pill uses an **imperative execution model**. You control what executes, in what order.
* Scripts are **written in Java**. This makes it easy to learn and extend.
* Pill **keeps its opinions to itself**. Contrast this with Maven, where you are in for a world of pain unless you adhere to "[The Maven Way](http://developer-blog.cloudbees.com/2013/04/the-maven-way.html)".

# What works?

* Scripts for building Pill itself.
* Basic unit tests.

# Current work

* Querying/downloading Maven artifacts from any Maven repository.
* Build script for a sample project.

# Future work

* Ability to download dependencies from arbitrary locations (e.g. Sourceforge, Google Code).
* Ability to deploy to Maven Central.
* Transitive dependency resolution.
* IDE plugins for Netbeans and Eclipse.

# Non-Goals

* **A declarative project model**. It is our belief that declarative formats, such as XML, are meant for computer consumption, not for human beings. You are certainly welcome to layer a declarative format on top of Pill, but we don't go out of our way to provide one.
* **Following the herd**. We avoid technologies that suffer from conceptual problems (e.g. [ORM Impedence Mismatch](http://en.wikipedia.org/wiki/Object-relational_impedance_mismatch)) regardless of how popular they might be. In our experience, this leads to a simpler design and cleaner code.

# How can I help?

* Get involved on the mailing list: https://groups.google.com/d/forum/pill-users
* Fork the code, and contribute pull requests.

AuthKit
=======
AuthKit is a Java library created by Greg Guerin to provide a Java API to the
macOS [Authorization Services](https://developer.apple.com/reference/security/authorization_services).
It allows a Java application to run programs as root after the user authenticates as an administrator.

The original version of this library is obsolete and its hosting site is no longer available.
This repo contains a copy of the AuthKit library sources, updated for more recent versions of Java and macOS.
The major change is to use 64 bit integers to represent native pointers in the JNI interfaces.

Disclaimer
----------
AuthKit uses an Authentication Services API that has been *deprecated* for a long time, although as of
macOS 10.12 (Sierra), it still is usable. This API was deprecated because it is too easy to create
security vulnerabilities using it. **Any use of the AuthKit library is at your own risk.**

See the [Apple documentation](https://developer.apple.com/reference/security/authorization_services)
for information about the recommended techniques.

Building
--------
An ant build script is provided. It builds a JAR file and a JNI dynamic library in the JARs subdirectory. 

Current status
--------------
AuthKit has been built using JDK 8 and tested on macOS 10.12 (Sierra).

Tests
-----
The tests described in the [original documentation](./Docs/examples.html) are out of date.
Read the document to understand what the tests do and what the expected output should be,
but use the [updated command snippets](./tests.txt) instead.

Documentation
-------------
* [License](artistic-license.html) - The license that governs the use of this software
* [Documentation](Docs/index.html) - The original documentation
* [API](API/packages.html) - The original API documentation
* [Sources](Source/about-the-source.html) - Information about the sources

C24 iO Components for Spring 3.0.3 Release Notes
====================================================

Added JsonSourceFactory - biz.c24.io.spring.source.JsonSourceFactory
====================================================


C24 iO Components for Spring 3.0.2 Release Notes
====================================================

Changes to parent pom
====================================================
Changed dependency to c24-io-api:4.6.1
This itself has an updated version of Saxon to address a bug https://saxonica.plan.io/issues/1944


C24 iO Components for Spring 3.0.1 Release Notes
====================================================

Changes to spring integration module
====================================================

New release of C24 spring-integration-3.0.xsd
Minor changes to catch up with changes in Spring Integration 4.x
Removed channel-resolver attribute
Fixed references to Spring objects that have been moved from Spring Integration to Spring Core


C24 iO Components for Spring 3.0.0 Release Notes
====================================================

Upgraded to latest releases of Spring Dependencies
==================================================


Enhancements to C24 iO Components for Spring Batch
==================================================

A new failfast flag has been added to the C24ItemReader. This enables all validation failures on an
item to be captured rather than always aborting on the first failure. Please see the reference manual
for details.

The same failfast capability has been added to the C24ValidatingItemProcessor and 
C24 TransformItemProcessor. As a result, the exceptions thrown have been brought in to line
with those thrown by the C24ItemReader (ie iO ValidationExceptions are now wrapped in exceptions
derived from Spring Batch exceptions).


C24 iO Components for Spring 2.2.2 Release Notes
================================================

This is a maintenance release which resolves a bug in the C24 Spring Integration project

Bugfixes to C24 iO Components for Spring Integration
====================================================

The C24UnmarshallingTransformer would generate an exception when trying to instantiate a BinarySource.
SourceFactories now provide a mechanism to get a Source without requiring a Reader or InputStream.


C24 iO Components for Spring 2.2.1 Release Notes
================================================

This is a maintenance release which resolves a bug in the C24 Spring Batch project and
updates the default iO version to 4.2.0

Bugfixes to C24 iO Components for Spring Batch
==================================================

Encoding attribute was being set incorrectly on some Sources when a resource was specified.



C24 iO Components for Spring 2.2.0 Release Notes
================================================

Enhancements to C24 iO Components for Spring Batch
==================================================

Zip File Processing
-------------------

* Zip file processing now correctly detects ZipEntries that are directories and skips over them.

* If Splitting is being used, one of the factors that the ZipFileSource uses to determine
whether or not to advise multiple threads to consume from the same ZipEntry concurrently
or whether each thread should read from their own ZipEntry is the size of the first ZipEntry
encountered. The size at which multiple threads will be used is raised from 10,000 bytes to
100,000 bytes. All other factors remain unchanged.


SplittingReader
---------------

The C24ItemReader now uses a custom component to buffer and read input streams. The major
consequences of this are:

* Perfect reproduction of line terminators (previously all line terminators were assumed to be
the same within a file for performance purposes)

* A performance increase on the splitting process where a single character line terminator is
used consistently (see consistent-line-terminators on the file-source and zip-file-source)

* The ability to split into lines by a token other than \n, \r or \r\n (see XML splitting)

An additional consequence of this change is that lines passed to elementStartPattern and elementStopPattern
for matching as well as the ParseListener.processLine callback will now see the line feed and carriage
return characters. To help minimise the impact of this change, the C24ItemReader now uses the 
DOTALL flag when it compiles the pattern so that the '.' regular expression character will now also
match carriage returns and line feeds characters.


XML Splitting
-------------

The new C24XmlItemSplitter splits lines around '<' characters as opposed to \n, \r or \r\n.
This only affects parsing where an elementStartPattern is being used.


Enhancements to C24 iO Components for Spring Integration
========================================================

Source & Transform Caching
-------------------------- 

Sources (in C24UnmarshallingTransformer) and Transforms (in C24Transformer) are now cached per-thread. 
In tests with small messages this has shown up to a 5x reduction in latency.

These objects will be cached for the lifetime of the thread.




C24 iO Components for Spring 2.1.1 Release Notes
================================================

The 2.1.1 release contains a number of enhancements to the C24 iO Components for Spring 
Batch.

File Handling
-------------

The FileSource, ZipFileSource, FileWriterSource & ZipFileWriterSource all now allow the
encoding they will use to read/write the file to be specified. If not explicitly specified
they default to UTF-8.

The sources also can now have their underlying file specified by a Spring Resource. This 
allows for greater flexibility in setting the file to use (e.g. classpath sources or use
of Spring's late binding). The previous behaviour (reading a JobParameter called [input|
output].file is still available if a Resource is not supplied.

The inbound file sources have a new configuration variable to skip the first N lines of 
each file. This is typically used to skip over header rows.


C24ItemReader & Writer - simpler configuration
----------------------------------------------

An enhancement to the C24 namespace allows FileSource, ZipFileSource, FileWriterSource & 
ZipFileWriterSource to be created directly as nested children of C24ItemReader & Writer.
See the Reference Documentation for full details on syntax and options.


C24ItemReader - default source
------------------------------

The C24ItemReader source-ref attribute is now optional. If not specified, the Model's 
default Source will be used instead.


C24ItemReader - splitter support for end pattern
------------------------------------------------

Where an elementStartPattern is specified, users can now optionally also define an 
elementStopPattern. This allows the splitter to identify the last line of an element
explicitly, rather than inferring it from the start line of the next element of the end
of the file.
A typical usage pattern would be to split out child elements from an XML document by 
matching the start (<child ... >) and end (</child>) tags.


C24ItemReader - intercepting the parsing process
------------------------------------------------

Callers can now optionally register a listener with the C24ItemReader that will be able
to interact with the parsing process. This can be used to extract information from parts
of the file that are not part of the parsed element and to directly influence the parsing
(for example, modifying lines before the parser sees them and enriching or wrapping the 
parsed ComplexDataObject before it is handed off to downstream components).


C24ItemReader - improved detection of end-of-line sequence
----------------------------------------------------------

On first run, each C24ItemReader will attempt to determine the end of line sequence based
on the first file it parses. This pattern is used by the splitter when reconstructing a 
message.


C24ItemReader & ZipFileSource - more parallel parsing
-----------------------------------------------------

In the previous release, if the parser encountered a zip file containing multiple files, 
the zip would be processed linearly (i.e. all threads would parse from the first file 
until exhausted, at which point they would move on to the next file in the zip).
Under specific circumstances, the parser will now assign a separate file in the zip to 
each thread. This potentially decreases the synchronisation between the parsing threads
at the cost of increased seek time due to multiple parts of the zip file being accessed
concurrently.


This release also contains a number of other smaller fixes and enhancements to the 
Spring Batch support including:

* The types of exceptions thrown adhere more closely to those from the core Spring Batch
components.

* Improved performance in certain edge cases.

* Ensure usage of ValidationManager in the C24TransformItemProcessor is thread-safe.

* Ensure that the C24TransformItemProcessor and C24ItemReader clean up their 
ValidationManagers.

* Fixed a bug where source-factory-ref was ignored under specific conditions.

* Exception messages during parsing now more accurately report the source of the error.

* There is now a cleaner abort process where the parser encounters a structurally invalid
message in the middle of a file and is not able to use the splitter to determine the start
of the next element.


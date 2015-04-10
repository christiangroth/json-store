Release Notes
=====================

0.3.0 (in progress)
-------------------
- using copy of data internally when creating streams to avoid ConcurrentModificationException if underlying set is changed during stream handling
- added support for Java 8 Time API. Beneath java.util.date you may also use java.time.LocalDateTime in JSON entities
- refactored package structure, com.github.christiangroth.jsonstore.JsonStore moved to com.github.christiangroth.jsonstore.store subpackage
- added com.github.christiangroth.jsonstore.store.JsonSingletonStore to provide storage for single object only
- fixed NullPointerException during initialization of transient JsonStores

0.2.0
-----
- changed package names to match Maven group id
- using copied data set to produce JSON data to avoid ConcurrentModificationException (happens while saving data to file and changing store data simultaneously)
- added slf4j logging api
- using UTF-8 for file operations

0.1.0
-----
- initial release
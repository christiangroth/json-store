Release Notes
=====================

0.9.0 (in progress)
-------------------
- BREAKING: removed backwards compatibility to load stores without metadata (introduced in version 0.5.0)
- convenience method to load all stores in case auto save mode is disabled
- added optional payload class qualifier to be able to handle multiple stores using the same payload class
- created interface de.chrgroth.jsonstore.json.JsonService regarding all JSON operations and a default implementation for currently used flexjson library
- created interface de.chrgroth.jsonstore.storage.StorageService regarding all storage operations and a default implementation for currently used file based storage
- ...

0.8.1
-----
- avoid direct save after load without changes
- enhanced lifecycle logging

0.8.0
-----
- fixed bug if a custom handler for java.util.Date or java.time.LocalDateTime is registered
- added methods to add handlers for specific store only
- added deep serialization mode to json stores, see flexjson documentation for detailed information
- fixed NPE during deserialization for transient stores

0.7.1
-----
- avoid StringInterningHandler to be set on flexjson serializer, this will kill all stings in resulting json and produce invalid json syntax

0.7.0
-----
- fixed handling of stores created date
- added simple on demand statistics computation to store instances
- added option to intern all Strings on deserialization to FlexjsonHelperBuilder

0.6.1
-----
- version migration did not save new version number in metadata correctly
- avoid duplicate loading of json data from file during ensure json store
 
0.6.0
-----
- fixed payload migration without metadats
- payload without metadata is considered to have version number 0, allowing direct migration
- removed deprecated ensure methods on JsonStores
- removed JsonStores#load() due to mandatory version per store. Ensure methods will trigger load now if auto save is enabled.
- toJson() without explicit pretty print mode takes pretty print value from store, was false before.
- JsonStoreException will be thrown if data loading fails, because this may lead to overriding existing data with next (auto-)save operation. Errors during save and drop are still just logged and silently ignored otherwise. 

0.5.0
-----
- introduced metadata to JSON store wrapping stored payload, existing persistent data will be converted automatically during first load
- added payload version migration before deserialization to java instances based on metadata
- added options to provide flexjson transformers and object factories for JSON serialization and deserialization using AbstractFlexjsonTypeHandler
- enhanced testcases

0.4.0
-----
- made date time pattern configurable
- refactored package structure from com.github.christiangroth to de.chrgroth
- changed maven group id from com.github.christiangroth to de.chrgroth.json-store
- including java debug information

0.3.0
-----
- using copy of data internally when creating streams to avoid ConcurrentModificationException if underlying set is changed during stream handling
- added support for Java 8 Time API. Beneath java.util.date you may also use java.time.LocalDateTime in JSON entities
- refactored package structure, com.github.christiangroth.jsonstore.JsonStore moved to com.github.christiangroth.jsonstore.store subpackage
- added com.github.christiangroth.jsonstore.store.JsonSingletonStore to provide storage for single object only
- fixed NullPointerException during initialization of transient JsonStores
- switched to builder pattern to create JsonStores instance, use JsonStores.builder() to get started
- made charset configurable using stores builder

0.2.0
-----
- changed package names to match Maven group id
- using copied data set to produce JSON data to avoid ConcurrentModificationException (happens while saving data to file and changing store data simultaneously)
- added slf4j logging api
- using UTF-8 for file operations

0.1.0
-----
- initial release
JSON Stores
=====================
Easy and simple POJO persistence using JSON (de)serialization, filesystem storage and Java stream API…

Requirements
------------

- [Java SDK 1.8+][1]
- flexjson

Usage
-----

Configure transient in-memory storage only:

	new JSONStores();

Configure JSON stores for persistent storage using auto-save and pretty-print modes:

	JSONStores stores = new JSONStores(new File(...), true, true);

Get/Ensure stores per type:

	JSONStore<MyType> store = stores.ensure(MyType.class);

All further actions use java.util.Set delegations methods:

	store.add(...);
	store.remove(...);
	store.parallelStream();
	...

Check the JavaDoc for a detailed explanation of the methods.

[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
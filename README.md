[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.christiangroth/json-store/badge.svg)](http://search.maven.org/#artifactdetails|com.github.christiangroth|json-store)
[![Build Status](https://secure.travis-ci.org/christiangroth/json-store.svg)](http://travis-ci.org/christiangroth/json-store)
[![Coverage Status](https://coveralls.io/repos/christiangroth/json-store/badge.svg?branch=develop)](https://coveralls.io/r/christiangroth/json-store?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2)

JSON Store
=====================
Easy and simple POJO persistence using JSON (de)serialization, filesystem storage and Java stream API.

Requirements
------------

- [Java SDK 1.8+][1]
- [flexjson][2]
- [slf4j][3]

Usage
-----

Configure JSON stores:
	
	// transient mode
	JsonStores stores = new JsonStores();
	
	// persistent mode
	stores = new JsonStores(new File("..."));

Storing single objects:

	// store with single value
	JsonSingletonStore<String> singletonStore = stores.ensureSingleton(MyType.class);	
	singletonStore.set("my data");
	String myData = singletonStore.get();
	...

Storing multiple values (backed by HashSet<T>):
	
	// store with multiple values
	JsonStore<String> store = stores.ensure(MyType.class);
	store.add("one");
	store.addAll(Arrays.asList("two", "three", "four"));
	
	// remove
	store.remove("three")
	store.removeAll(Arrays.asList("two", "one"));
	...

Check the JavaDoc or test sources for a detailed explanation of the methods.

[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]: http://flexjson.sourceforge.net/
[3]: http://www.slf4j.org/
Development: [![Build Status](https://secure.travis-ci.org/christiangroth/json-store.svg)](http://travis-ci.org/christiangroth/json-store)
[![Coverage Status](https://coveralls.io/repos/christiangroth/json-store/badge.svg?branch=develop)](https://coveralls.io/r/christiangroth/json-store?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2)

Stable: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.chrgroth.json-store/json-store/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.chrgroth.json-store/json-store)

# JSON Store
Easy and simple POJO persistence using JSON (de)serialization, filesystem storage and Java stream API.

## Table of Contents
- [Creating stores](#creating-stores)
- [JSON Service](#json-service)
- [Storage Service](#storage-service)
- [Define payload classes](#define-payload-classes)
- [Add and remove data](#add-and-remove-data)
- [Query data](#query-data)
- [Migration of existing data on class changes](#migration-of-existing-data-on-class-changes)
- [String interning](#string-interning)
- [Requirements](#requirements)

## Creating stores

JSON Store instances are created using static builder invoked via de.chrgroth.jsonstore.JsonStores. This central object is responsible for managing all your concrete store instances.
	
	// create stores instance
	JsonService jsonService = ...
	StorageService storageService = ...
	JsonStores stores = JsonStores.builder(jsonService, storageService).build();
	
A concrete JSON store instance is created for it's root type and might be a singleton store for exactly one instance only or a regular store containing multiple instances backed by a Set. You also have to specify the version of your model (java class) to be able to migrate the data on model changes (see Migration of existing data on class changes).

	try {
	
		// single instance store
		String singletonUid = JsonStoreUtils.buildUid(MyEntity.class, "singleton");
		JsonSingletonStore<MyEntity> singletonStore = stores.ensureSingleton(singletonUid, MyEntity.VERSION);	
		singletonStore.set(new MyEntity(...));
	
		// multiple instances store
		String uid = JsonStoreUtils.buildUid(MyEntity.class, null);
		JsonStore<MyEntity> store = stores.ensure(uid, MyEntity.VERSION);
		store.add(new MyEntity(...));
	) catch(JsonStoreException e) {
		
		// TODO handle error during load of existing data
	}

back to [top](#table-of-contents).

## JSON service

*de.chrgroth.jsonstore.JsonService* interface defines an exchangeable JSON implementation. Currently only flexjson is available out of the box.

### Flexjson

*de.chrgroth.jsonstore.json.flexjson.FlexjsonService* provides access to flexjson library. Be sure to use the builder and configure flexjson accordingly.

#### Deep serialize

You may configure to use flexjsons deep serialize mode to avoid usng annotations spread out on your models.

	FlexjsonService.builder().setDeepSerialize(true).build();

#### Pretty print

You may configure flexjsons to prett print the serialized JSON data.

	FlexjsonService.builder().setPrettyPrint(true).build();

#### Date/Time patterns

You may configure the date/time pattern used to serialize and deserialize instances of types java.util.Date and java.time.LocalDateTime. Please refer to java.time.format.DateTimeFormatter to specify the pattern.

	FlexjsonService.builder().dateTimePattern("HH:mm:ss.SSS dd.MM.yyyy").build();

#### Custom handlers

You may also provide custom handlers for serialization (transformer) and deserialization (object factory). The abstract ase class de.chrgroth.jsonstore.json.flexjson.custom.AbstractFlexjsonTypeHandler is used to provide both transformations with one implementation. In case the date timer pattern configured will also be passed to a predefined custom handler of this type. 

You don't need to implement this classes to be able to handle your POJOs in a generic way (see Define payload classes). However if you want to customize JSON serialization and deserialization you may provide your custom handlers using the following methods.

	FlexjsonService.builder().typeHandler(MyEntity.class, new MyEntityTypeHandler()).build();
	FlexjsonService.builder().pathHandler("myEntity.someAttribute", new MyEntityPathBasedTypeHandler()).build();

Please refer to [flexjson][2] documentation for more details about custom type object factories and transformers.

#### String interning

Depending on the data used a lot of instances of java.util.String will be created during deserialization. For better and more efficient memory usage java.util.String#intern() may be used. A custom handler de.chrgroth.jsonstore.json.flexjson.custom.StringInterningHandler is contained since version 0.7.0 and can be activated.

	FlexjsonService.builder().useStringInterning().build();

The effect heavily depends on the data being deserialized.

#### Per store settings

All settings can also be configured per store using the store uid. There are overloaded methods with store uid as first parameter.

back to [top](#table-of-contents).

## Storage service

*de.chrgroth.jsonstore.StorageService* interface is responsible for data storage operations. Currently there are two implementations available.

- *de.chrgroth.jsonstore.storage.TransientStorageService*: Does not storage at all. In memory only.
- *de.chrgroth.jsonstore.storage.FileStorageService*: Creates on file per store. Be sure to configure base directory and optional charset using builder.

back to [top](#table-of-contents).

## Define payload classes

Defining payload classes and class hierarchies does not require to implement special interfaces or extend base classes. Just restrict to simple java POJO classes and everything is fine. However to control flexjsons shallow serialization be sure to annotate all non-primite members with @flexjson.JSON to include this data otherwise it won't be recognized.

	public class MyEntity {
		private int id;
		private String name;
		
		@JSON
		private Map<String, String> data;
		
		@JSON
		private MyEntity parent;
		
		// provide getters and setters
	}

Also be sure to have an default no-arg contructor in order flexjson may create new instances for deserialization. Be aware that @JSON annotations are needed only when not using deep serialization mode.

back to [top](#table-of-contents).

## Add and remove data

In case auto save mode is enabled (see Creating Stores) you don't have to call save() method explicitly if the datacontainer in store is changed directly using set, add, addAll, retianAll, remove, removeAll, removeIf or clear. However json store does not reflect changes to any of added objects so it's not able to detect changes on already added instances and you have to call save by yourself.
	
	MyEntity myEntity = new MyEntity(...);
	store.add(new MyEntity(); // auto-saved
	
	myEntity.setWhatever("foo"); // change not recognized
	store.save();
	
	store.remove(myEntity); // auto-saved
	store.clear(); // auto-saved
	
back to [top](#table-of-contents).

## Query data

Querying data is all about java collection and streams, if your're not familiar with this concepts you may take a look at the [official documentation][4] or any tutorial. In case of singleton store there is of course no need to search for any data if you store exactly one instance only. In case of regular store you'll be able to create stream or parallel stream on a copy of backed data. A copy is created to prevent concurrent modifications and breaking your stream.
	
	// searching for data
	Set<MyEntity> entities = store.stream().filter(e -> e.isInteressingStuff()).collect(Collectors.toSet());
	MyEntity theOneAndOnly = store.stream().filter(e -> e.isUnique()).findAny().orElse(null);
	
	// mapping data
	Set<String> allEntityNames = store.parallelStream().map(e -> e.getName()).collect(Collectors.toSet());
	
	// apply changes to data
	store.forEach(e -> e.setUpdate(new Date()));

Note that all operations are done using out of the box java API and nothing is reinvented for JSON store.

back to [top](#table-of-contents).

## Migration of existing data on class changes

Image you already have a running project and some persistent JSON store data. You might need to change your datamodel due to new feature implementations or whatever. If you have to remove attributes or change types, JSON deserialization will fail and you won't be able to load the data. Of course you may open the json store file and fix this manually during deployment. JSON store provides the option of data version migrations. At first you have to define the current version during store creation. It#s a good idea to keep version maintained using a static value in your datamodel class and adapt the value for each new version if class is changed. Even if changes would not break JSON deserialization you will be able to use migration handler and perhaps initialize a value for a newly created attribute.

Let's assume the following simplified code for first version when our project starts.

	// datamodel
	public class MyEntity {
		public static final int VERSION = 1;
		String id;
		String name;
		
		// provide getters and setters
	}

	try {
	
		// store creation	
		JsonStore<MyEntity> store = stores.ensure(MyEntity.class, MyEntity.VERSION);
	) catch(JsonStoreException e) {
		
		// TODO handle error during load of existing data
	}

Let's say the following data is stored as serialized JSON
	
	MyEntity#1 -> id="#1", name="first entity"
	MyEntity#2 -> id="#2", name="a second one"

For next version we might change the entity but still reuse and migrate our existing data. We define a migration handler to migrate from version 1 to 2. The handler uses an intermediate and generic representation before deserialization is completed. You may change values in given structure directly. Be aware that map values may contain e.g. JsonNumber in case of a number due to internal usage in [flexjson][2] so we have to deal with this, otherwise a completely new abstraction and/ir JSON parsing had to be implemented, we don't want this.

	// datamodel: changed id from Strig to in, added description
	public class MyEntity {
		public static final int VERSION = 2;
		int id;
		String name;
		String description;
		
		// provide getters and setters
	}

	public class MyEntityVersionOneMigration implements VersionMigrationHandler {
		public int sourceVersion() { return 2; }
		
		public void migrate(Map<String, Object> genericPlayload) {
			
			// migrate id
			String oldId = (String) genericPayload.get("id");
			genericPayload.put("id", new JsonNumber((oldId).replaceAll("#", "")));
			
			// provide some default values
			genericPayload.put("description", "Some auto-generated description for " + genericPlayload.get("name"));
		}
	}

	try {
	
		// store creation: using migration handler 
		JsonStore<MyEntity> store = stores.ensure(MyEntity.class, MyEntity.VERSION);
	) catch(JsonStoreException e) {
		
		// TODO handle error during load of existing data
	}
	
During load of data JSON store will execute all registered migration handlers and data will be available in JSON store. If auto save mode is enabled, the fole contents will be updated right after data migration. The result is shown below.

	MyEntity#1 -> id="1", name="first entity", description="Some auto-generated description for first entity"
	MyEntity#2 -> id="2", name="a second one", description="Some auto-generated description for a second one"

back to [top](#table-of-contents).

## Requirements
- [Java SDK 1.8+][1]
- [flexjson][2]
- [slf4j][3]

[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]: http://flexjson.sourceforge.net/
[3]: http://www.slf4j.org/
[4]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html

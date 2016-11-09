Development: [![Build Status](https://secure.travis-ci.org/christiangroth/json-store.svg)](http://travis-ci.org/christiangroth/json-store)
[![Coverage Status](https://coveralls.io/repos/christiangroth/json-store/badge.svg?branch=develop)](https://coveralls.io/r/christiangroth/json-store?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2)

Stable: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.chrgroth.json-store/json-store/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.chrgroth.json-store/json-store)

# JSON Store
Easy and simple POJO persistence using JSON (de)serialization, filesystem storage and Java stream API.

## Table of Contents
- [Creating stores](#creating-stores)
- [Define payload classes](#define-payload-classes)
- [Add and remove data](#add-and-remove-data)
- [Query data](#query-data)
- [Migration of existing data on class changes](#migration-of-existing-data-on-class-changes)
- [Flexjson configuration](#flexjson-configuration)
- [String interning](#string-interning)
- [Requirements](#requirements)

back to [top](#table-of-contents).

## Creating stores

JSON Store instances are created using static builder invoked via de.chrgroth.jsonstore.JsonStores. This central object is responsible for managing all your concrete store instances. You may create transient in-memory only instances or persistent instances saving contents to one file per store in given directory.
	
	// transient mode, do not provide any storage options
	JsonStores stores = JsonStores.builder().build();
	
	// persistent mode, provide path to directory
	File storageDir = new File("path/to/some/directory/that/must/not/exist");
	JsonStores stores = JsonStores.builder().storage(storageDir).build();

Persistent mode may also be configured using some more details, like charset, pretty print mode and auto save mode (see Add and remove data).

	// persistent mode (full control)
	Charset charset = StandardCharsets.UTF_8;
	boolean prettyPrint = true;
	boolean autoSave = true;
	JsonStores stores = JsonStores.builder().storage(storageDir, charset, prettyPrint, autoSave).build();

A concrete JSON store instance is created for it's root type and might be a singleton store for exactly one instance only or a regular store containing multiple instances backed by a Set. Additionally you may specify the version of your data (see Migration of existing data on class changes).

	try {
	
		// single instance store
		JsonSingletonStore<MyEntity> singletonStore = stores.ensureSingleton(MyEntity.class, MyEntity.VERSION);	
		singletonStore.set(new MyEntity(...));
	
		// multiple instances store
		JsonStore<MyEntity> store = stores.ensure(MyEntity.class, MyEntity.VERSION);
		store.add(new MyEntity(...));
	) catch(JsonStoreException e) {
		
		// TODO handle error during load of existing data
	}

**Please use a payload version value >= 1 to start with. The version with value 0 is expected for old legacy stores running on json-store version prior to 0.5.0. These stores do not contain any any metadata and get converted on first load to version 0. Afterwards all migration handlers are applied, see also Migration of existing data on class changes. **

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

Also be sure to have an default no-arg contructor in order flexjson may create new instances for deserialization.

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

## Flexjson configuration

FlexjsonHelper is used to configure JSON serialization and deserialization of payload in JSON stores. To influence configuration of flexjson the JsonStoresBuilder provides some configuration methods. If you want to create and maintain instances of JsonStore and JsonSingletonStore by yourself, you may use FlexjsonHelperBuilder.

You may configure the date/time pattern used to serialize and deserialize instances of types java.util.Date and java.time.LocalDateTime. Please refer to java.time.format.DateTimeFormatter to specify the pattern.

	JsonStores.builder().dateTimePattern("HH:mm:ss.SSS dd.MM.yyyy").build();

You may also provide custom handlers for serialization (transformer) and deserialization (object factory). The abstract ase class de.chrgroth.jsonstore.json.AbstractFlexjsonTypeHandler is used to provide both transformations with one implementation. In case the date timer pattern configured will also be passed to a predefined custom handler of this type. 

You don't need to implement this classes to be able to handle your POJOs in a generic way (see Define payload classes). However if you want to customize JSON serialization and deserialization you may provide your custom handlers using the following methods.

	JsonStores.builder().handler(MyEntity.class, new MyEntityTypeHandler()).build();
	JsonStores.builder().handler("myEntity.someAttribute", new MyEntityPathBasedTypeHandler()).build();

Please refer to [flexjson][2] documentation for more details about custom type object factories and transformers.

back to [top](#table-of-contents).

## String interning

Depending on the data used a lot of instances of java.util.String will be created during deserialization. For better and more efficient memory usage java.util.String#intern() may be used. A custom handler de.chrgroth.jsonstore.json.custom.StringInterningHandler is contained since version 0.7.0 and can be activated using de.chrgroth.jsonstore.json.FlexjsonHelper.FlexjsonHelperBuilder.useStringInterning(). The effect heavily depends on the data being deserialized.

back to [top](#table-of-contents).

## Requirements
- [Java SDK 1.8+][1]
- [flexjson][2]
- [slf4j][3]

[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]: http://flexjson.sourceforge.net/
[3]: http://www.slf4j.org/
[4]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html

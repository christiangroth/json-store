Development: [![Build Status](https://secure.travis-ci.org/christiangroth/json-store.svg)](http://travis-ci.org/christiangroth/json-store)
[![Coverage Status](https://coveralls.io/repos/christiangroth/json-store/badge.svg?branch=develop)](https://coveralls.io/r/christiangroth/json-store?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/551efcaf971f7847ca0003e2)

Stable: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.chrgroth.json-store/json-store/badge.svg)](http://search.maven.org/#artifactdetails|de.chrgroth.json-store|json-store)

JSON Store
=====================
Easy and simple POJO persistence using JSON (de)serialization, filesystem storage and Java stream API.

Requirements
------------

- [Java SDK 1.8+][1]
- [flexjson][2]
- [slf4j][3]


Creating stores
---------------

JSON Store instances are created using static builder invoked via de.chrgroth.jsonstore.JsonStores. You may create transient in-memory only instances or persistent instances saving contents to one file per store in given directory.
	
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

	// just for simple demo cases
	Integer dataVersion = 1;
	
	// single instance store
	JsonSingletonStore<MyEntity> singletonStore = stores.ensureSingleton(MyEntity.class, dataVersion);	
	singletonStore.set(new MyEntity(...));

	// multiple instances store
	JsonStore<MyEntity> store = stores.ensure(MyEntity.class, dataVersion);
	store.add(new MyEntity(...));

Add and remove data
-------------------

In case auto save mode is enabled (see Creating Stores) you don't have to call save() method explicitly if the datacontainer in store is changed directly using set, add, addAll, retianAll, remove, removeAll, removeIf or clear. However json store does not reflect changes to any of added objects so it's not able to detect changes on already added instances and you have to call save by yourself.
	
	MyEntity myEntity = new MyEntity(...);
	store.add(new MyEntity(); // auto-saved
	
	myEntity.setWhatever("foo"); // change not recognized
	store.save();
	
	store.remove(myEntity); // auto-saved
	store.clear(); // auto-saved
	
Query data
----------

Querying data is all about java collection and streams, if your're not familiar with this concepts you may take a look at the [official documentation][4] or any tutorial. In case of singleton store there is of course no need to search for any data if you store exactly one instance only. In case of regular store you'll be able to create stream or parallel stream on a copy of backed data. A copy is created to prevent concurrent modifications and breaking your stream.
	
	// searching for data
	Set<MyEntity> entities = store.stream().filter(e -> e.isInteressingStuff()).collect(Collectors.toSet());
	MyEntity theOneAndOnly = store.stream().filter(e -> e.isUnique()).findAny().orElse(null);
	
	// mapping data
	Set<String> allEntityNames = store.parallelStream().map(e -> e.getName()).collect(Collectors.toSet());
	
	// apply changes to data
	store.forEach(e -> e.setUpdate(new Date()));

Note that all operations are done using out of the box java API and nothing is reinvented for JSON store.

Migration of existing data on class changes
-------------------------------------------

Image you already have a running project and some persistent JSON store data. You might need to change your datamodel due to new feature implementations or whatever. If you have to remove attributes or change types, JSON deserialization will fail and you won't be able to load the data. Of course you may open the json store file and fix this manually during deployment. JSON store provides the option of data version migrations. At first you have to define the current version during store creation. It#s a good idea to keep version maintained using a static value in your datamodel class and adapt the value for each new version if class is changed. Even if changes would not break JSON deserialization you will be able to use migration handler and perhaps initialize a value for a newly created attribute.

Let's assume the following simplified code for first version when our project starts.

	// datamodel
	public class MyEntity {
		public static final int VERSION = 1;
		String id;
		String name;
	}

	// store creation	
	JsonStore<MyEntity> store = stores.ensure(MyEntity.class, MyEntity.VERSION);

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
	}

	public class MyEntityVersionOneMigration implements VersionMigrationHandler {
		public int sourceVersion() { return 1; }
		
		public void migrate(Map<String, Object> genericPlayload) {
			
			// migrate id
			String oldId = (String) genericPayload.get("id");
			genericPayload.put("id", new JsonNumber((oldId).replaceAll("#", "")));
			
			// provide some default values
			genericPayload.put("description", "Some auto-generated description for " + genericPlayload.get("name"));
		}
	}

	// store creation: using migration handler 
	JsonStore<MyEntity> store = stores.ensure(MyEntity.class, MyEntity.VERSION);

During load of data JSON store will execute all registered migration handlers and data will be available in JSON store. If auto save mode is enabled, the fole contents will be updated right after data migration. The result is shown below.

	MyEntity#1 -> id="1", name="first entity", description="Some auto-generated description for first entity"
	MyEntity#2 -> id="2", name="a second one", description="Some auto-generated description for a second one"

[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]: http://flexjson.sourceforge.net/
[3]: http://www.slf4j.org/
[4]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html
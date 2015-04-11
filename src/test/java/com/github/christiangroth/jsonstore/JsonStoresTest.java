package com.github.christiangroth.jsonstore;

import java.io.File;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.christiangroth.jsonstore.store.JsonSingletonStore;
import com.github.christiangroth.jsonstore.store.JsonStore;
import com.google.common.io.Files;

// TODO text autoSave true|false explicitly
public class JsonStoresTest {
	
	private File tempDir;
	private JsonStores persistentStores;
	private JsonStores persistentStoresCopy;
	private JsonStores transientStores;
	private JsonStores transientStoresCopy;
	
	private String testData;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
		persistentStores = JsonStores.builder().storage(tempDir, true, true).build();
		persistentStoresCopy = JsonStores.builder().storage(tempDir, false, true).build();
		transientStores = JsonStores.builder().build();
		transientStoresCopy = JsonStores.builder().build();
		testData = "test data";
	}
	
	@Test
	public void storeLifecycle() {
		assertStoreLifecycle(persistentStores, persistentStoresCopy, true);
	}
	
	@Test
	public void transientStoreLifecycle() {
		assertStoreLifecycle(transientStores, transientStoresCopy, false);
	}
	
	private void assertStoreLifecycle(JsonStores stores, JsonStores storesCopy, boolean isPersistent) {
		
		// nothing there
		Class<String> dataClass = String.class;
		JsonStore<String> store = stores.resolve(dataClass);
		Assert.assertNull(store);
		if (isPersistent) {
			Assert.assertTrue(tempDir.exists());
			Assert.assertTrue(tempDir.isDirectory());
			Assert.assertTrue(tempDir.canRead());
			Assert.assertEquals(0, tempDir.listFiles().length);
		}
		
		// ensure store
		store = stores.ensure(dataClass);
		Assert.assertNotNull(store);
		if (isPersistent) {
			Assert.assertEquals(0, tempDir.listFiles().length);
		}
		store = stores.resolve(dataClass);
		Assert.assertNotNull(store);
		
		// explicit save
		stores.save();
		if (isPersistent) {
			Assert.assertEquals(1, tempDir.listFiles().length);
			Assert.assertEquals(store.getFile(), tempDir.listFiles()[0]);
		}
		
		// drop
		JsonStore<String> droppedStore = stores.drop(dataClass);
		Assert.assertEquals(store, droppedStore);
		if (isPersistent) {
			Assert.assertEquals(0, tempDir.listFiles().length);
		}
		store = stores.resolve(dataClass);
		Assert.assertNull(store);
		
		// create again with data
		store = stores.ensure(dataClass);
		store.add(testData);
		if (isPersistent) {
			Assert.assertEquals(1, tempDir.listFiles().length);
		}
		
		// copy still empty
		JsonStore<String> storeCopy = storesCopy.resolve(dataClass);
		Assert.assertNull(storeCopy);
		
		// load into stores copy
		storesCopy.load();
		storeCopy = storesCopy.resolve(dataClass);
		if (isPersistent) {
			Assert.assertNotNull(storeCopy);
			Assert.assertEquals(1, tempDir.listFiles().length);
			Assert.assertEquals(storeCopy.getFile(), tempDir.listFiles()[0]);
			Assert.assertEquals(1, storeCopy.size());
			Assert.assertEquals(testData, storeCopy.copy().iterator().next());
		} else {
			Assert.assertNull(storeCopy);
		}
	}
	
	@Test
	public void singletonStoreLifecycle() {
		assertSingletonStoreLifecycle(persistentStores, persistentStoresCopy, true);
	}
	
	@Test
	public void transientSingletonStoreLifecycle() {
		assertSingletonStoreLifecycle(transientStores, transientStoresCopy, false);
	}
	
	private void assertSingletonStoreLifecycle(JsonStores stores, JsonStores storesCopy, boolean isPersistent) {
		
		// nothing there
		Class<String> dataClass = String.class;
		JsonSingletonStore<String> store = stores.resolveSingleton(dataClass);
		Assert.assertNull(store);
		if (isPersistent) {
			Assert.assertTrue(tempDir.exists());
			Assert.assertTrue(tempDir.isDirectory());
			Assert.assertTrue(tempDir.canRead());
			Assert.assertTrue(tempDir.listFiles().length < 1);
		}
		
		// ensure
		store = stores.ensureSingleton(dataClass);
		Assert.assertNotNull(store);
		if (isPersistent) {
			Assert.assertEquals(0, tempDir.listFiles().length);
		}
		store = stores.resolveSingleton(dataClass);
		Assert.assertNotNull(store);
		
		// explicit save
		stores.save();
		if (isPersistent) {
			Assert.assertEquals(1, tempDir.listFiles().length);
			Assert.assertEquals(store.getFile(), tempDir.listFiles()[0]);
		}
		
		// drop
		JsonSingletonStore<String> droppedStore = stores.dropSingleton(dataClass);
		Assert.assertEquals(store, droppedStore);
		if (isPersistent) {
			Assert.assertEquals(0, tempDir.listFiles().length);
		}
		store = stores.resolveSingleton(dataClass);
		Assert.assertNull(store);
		
		// create again with data
		store = stores.ensureSingleton(dataClass);
		store.set(testData);
		if (isPersistent) {
			Assert.assertEquals(1, tempDir.listFiles().length);
		}
		
		// copy still empty
		JsonSingletonStore<String> storeCopy = storesCopy.resolveSingleton(dataClass);
		Assert.assertNull(storeCopy);
		
		// load into stores copy
		storesCopy.load();
		storeCopy = storesCopy.resolveSingleton(dataClass);
		if (isPersistent) {
			Assert.assertNotNull(storeCopy);
			Assert.assertEquals(1, tempDir.listFiles().length);
			Assert.assertEquals(storeCopy.getFile(), tempDir.listFiles()[0]);
			Assert.assertEquals(testData, storeCopy.get());
		} else {
			Assert.assertNull(storeCopy);
		}
	}
	
	@Test
	public void nonExistentStorage() {
		
		// create stores with non existent storage directory
		String subdir = UUID.randomUUID().toString();
		File storage = new File(tempDir, subdir);
		Assert.assertFalse(storage.exists());
		
		// check empty storage created on startup
		JsonStores.builder().storage(storage, false, false).build();
		Assert.assertTrue(storage.exists());
		Assert.assertTrue(storage.isDirectory());
		Assert.assertTrue(storage.canRead());
		Assert.assertEquals(0, storage.listFiles().length);
	}
}

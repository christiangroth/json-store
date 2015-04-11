package com.github.christiangroth.jsonstore.store;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class JsonSingletonStoreTest {
	
	private File tempDir;
	private JsonSingletonStore<String> persistentStore;
	private JsonSingletonStore<String> persistentStoreCopy;
	private JsonSingletonStore<String> transientStore;
	private JsonSingletonStore<String> transientStoreCopy;
	
	private String testDataOne;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
		persistentStore = new JsonSingletonStore<>(String.class, tempDir, StandardCharsets.UTF_8, true, true);
		persistentStoreCopy = new JsonSingletonStore<>(String.class, tempDir, StandardCharsets.UTF_8, true, true);
		transientStore = new JsonSingletonStore<>(String.class, null, null, false, false);
		transientStoreCopy = new JsonSingletonStore<>(String.class, null, null, false, false);
		testDataOne = "test data";
	}
	
	@Test
	public void dataLifecycle() {
		dataLifecycle(persistentStore);
	}
	
	@Test
	public void transientDataLifecycle() {
		dataLifecycle(transientStore);
	}
	
	public void dataLifecycle(JsonSingletonStore<String> store) {
		
		// no data
		Assert.assertTrue(store.isEmpty());
		Assert.assertNull(store.get());
		
		// set element
		store.set(testDataOne);
		Assert.assertFalse(store.isEmpty());
		Assert.assertEquals(testDataOne, store.get());
		
		// clear data
		store.clear();
		Assert.assertTrue(store.isEmpty());
		Assert.assertNull(store.get());
		
		// clear implicitly
		store.set(testDataOne);
		Assert.assertFalse(store.isEmpty());
		Assert.assertEquals(testDataOne, store.get());
		store.set(null);
		Assert.assertTrue(store.isEmpty());
		Assert.assertNull(store.get());
	}
	
	@Test
	public void jsonLifecycle() {
		jsonLifecycle(persistentStore, persistentStoreCopy);
	}
	
	@Test
	public void transientJsonLifecycle() {
		jsonLifecycle(transientStore, transientStoreCopy);
	}
	
	private void jsonLifecycle(JsonSingletonStore<String> store, JsonSingletonStore<String> storeCopy) {
		
		// export data
		store.set(testDataOne);
		String json = store.toJson();
		Assert.assertNotNull(json);
		
		// copy still empty
		Assert.assertTrue(storeCopy.isEmpty());
		
		// import into copy
		storeCopy.fromJson(json);
		Assert.assertFalse(storeCopy.isEmpty());
		Assert.assertEquals(store.get(), storeCopy.get());
	}
}

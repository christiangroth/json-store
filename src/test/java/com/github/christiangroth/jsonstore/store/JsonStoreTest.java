package com.github.christiangroth.jsonstore.store;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class JsonStoreTest {
	
	private File tempDir;
	private JsonStore<String> persistentStore;
	private JsonStore<String> persistentStoreCopy;
	private JsonStore<String> transientStore;
	private JsonStore<String> transientStoreCopy;
	
	private String testDataOne;
	private String testDataTwo;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
		persistentStore = new JsonStore<>(String.class, tempDir, true, true);
		persistentStoreCopy = new JsonStore<>(String.class, tempDir, true, true);
		transientStore = new JsonStore<>(String.class, null, false, false);
		transientStoreCopy = new JsonStore<>(String.class, null, false, false);
		testDataOne = "test data foo";
		testDataTwo = "test data bar";
	}
	
	@Test
	public void dataLifecycle() {
		dataLifecycle(persistentStore);
	}
	
	@Test
	public void transientDataLifecycle() {
		dataLifecycle(transientStore);
	}
	
	public void dataLifecycle(JsonStore<String> store) {
		
		// no data
		Assert.assertTrue(store.isEmpty());
		Assert.assertTrue(store.copy().isEmpty());
		
		// add element
		store.add(testDataOne);
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
		
		// add element to copy only
		store.copy().add(testDataTwo);
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
	}
	
	@Test
	public void jsonLifecycle() {
		jsonLifecycle(persistentStore, persistentStoreCopy);
	}
	
	@Test
	public void transientJsonLifecycle() {
		jsonLifecycle(transientStore, transientStoreCopy);
	}
	
	private void jsonLifecycle(JsonStore<String> store, JsonStore<String> storeCopy) {
		
		// export data
		store.add(testDataOne);
		String json = store.toJson();
		Assert.assertNotNull(json);
		
		// copy still empty
		Assert.assertTrue(storeCopy.isEmpty());
		
		// import into copy
		storeCopy.fromJson(json);
		Assert.assertEquals(1, storeCopy.size());
		Assert.assertEquals(store.copy().iterator().next(), storeCopy.copy().iterator().next());
	}
}

package com.github.christiangroth.jsonstore.store;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
	private List<String> testData;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
		persistentStore = new JsonStore<>(String.class, tempDir, true, true);
		persistentStoreCopy = new JsonStore<>(String.class, tempDir, true, true);
		transientStore = new JsonStore<>(String.class, null, false, false);
		transientStoreCopy = new JsonStore<>(String.class, null, false, false);
		testDataOne = "test data foo";
		testDataTwo = "test data bar";
		testData = Arrays.asList(testDataOne, testDataTwo);
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
		Assert.assertFalse(store.isEmpty());
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
		Assert.assertTrue(store.contains(testDataOne));
		
		// add element two to copy only
		store.copy().add(testDataTwo);
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
		
		// add element two
		store.add(testDataTwo);
		Assert.assertEquals(2, store.size());
		Assert.assertTrue(store.containsAll(testData));
		
		// retain
		store.retainAll(Arrays.asList(testDataOne));
		Assert.assertEquals(1, store.size());
		Assert.assertTrue(store.contains(testDataOne));
		
		// remove
		store.remove(testDataOne);
		Assert.assertTrue(store.isEmpty());
		
		// remove all
		store.addAll(testData);
		Assert.assertEquals(2, store.size());
		store.removeAll(testData);
		Assert.assertTrue(store.isEmpty());
		
		// remove if
		store.addAll(testData);
		Assert.assertEquals(2, store.size());
		store.removeIf(s -> testDataTwo.equals(s));
		Assert.assertEquals(1, store.size());
		Assert.assertTrue(store.contains(testDataOne));
		
		// clear
		store.addAll(testData);
		Assert.assertEquals(2, store.size());
		store.clear();
		Assert.assertTrue(store.isEmpty());
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
	
	@Test
	public void nullJson() {
		nullJson(persistentStoreCopy);
	}
	
	@Test
	public void transientNullJson() {
		nullJson(transientStoreCopy);
	}
	
	private void nullJson(JsonStore<String> store) {
		
		// still empty
		Assert.assertTrue(store.isEmpty());
		
		// import into copy
		store.fromJson(null);
		Assert.assertTrue(store.isEmpty());
	}
	
	@Test
	public void concurrentStreamAccessAndModification() {
		concurrentStreamAccessAndModification(persistentStore);
	}
	
	@Test
	public void transientConcurrentStreamAccessAndModification() {
		concurrentStreamAccessAndModification(transientStore);
	}
	
	private void concurrentStreamAccessAndModification(JsonStore<String> store) {
		
		// prepare stream
		store.addAll(testData);
		Stream<String> stream = store.stream();
		
		// concurrent change
		store.remove(testDataTwo);
		
		// go on with stream
		Assert.assertEquals(2, stream.count());
	}
	
	@Test
	public void concurrentParallelStreamAccessAndModification() {
		concurrentParallelStreamAccessAndModification(persistentStore);
	}
	
	@Test
	public void transientConcurrentParallelStreamAccessAndModification() {
		concurrentParallelStreamAccessAndModification(transientStore);
	}
	
	private void concurrentParallelStreamAccessAndModification(JsonStore<String> store) {
		
		// prepare stream
		store.addAll(testData);
		Stream<String> stream = store.parallelStream();
		
		// concurrent change
		store.remove(testDataTwo);
		
		// go on with stream
		Assert.assertEquals(2, stream.count());
	}
}

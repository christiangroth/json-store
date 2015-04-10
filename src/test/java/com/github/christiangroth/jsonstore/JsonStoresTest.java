package com.github.christiangroth.jsonstore;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.christiangroth.jsonstore.model.TestEntity;
import com.google.common.io.Files;

// TODO enhance testing
public class JsonStoresTest {
	
	private File tempDir;
	private JsonStores stores;
	
	private TestEntity foo;
	private TestEntity bar;
	
	@Before
	public void init() {
		
		// stores
		tempDir = Files.createTempDir();
		stores = new JsonStores(tempDir, true);
		
		// data
		foo = new TestEntity(1, "foo");
		bar = new TestEntity(2, "bar");
	}
	
	@Test
	public void dataLifecycle() {
		
		// no stores present
		JsonStore<TestEntity> store = stores.resolve(TestEntity.class);
		Assert.assertNull(store);
		
		// store exists, no data
		store = stores.ensure(TestEntity.class);
		Assert.assertNotNull(store);
		Assert.assertTrue(store.isEmpty());
		Assert.assertTrue(store.copy().isEmpty());
		
		// add element
		store.add(foo);
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
		
		// add element to copy only
		store.copy().add(bar);
		Assert.assertEquals(1, store.size());
		Assert.assertEquals(1, store.copy().size());
	}
	
	@Test
	public void jsonLifecycle() {
		
		// add data
		JsonStore<TestEntity> store = stores.ensure(TestEntity.class);
		store.add(foo);
		
		// export
		String json = store.toJson();
		Assert.assertNotNull(json);
		
		// create new store and import data
		stores.drop(TestEntity.class);
		JsonStore<TestEntity> newStore = stores.ensure(TestEntity.class);
		newStore.fromJson(json);
		Assert.assertEquals(1, newStore.size());
		
		// assert data
		TestEntity importedEnity = newStore.copy().iterator().next();
		Assert.assertEquals(1, importedEnity.getId());
		Assert.assertEquals("foo", importedEnity.getData());
	}
	
	@Test
	public void fileLifecycle() {
		
		// empty store
		JsonStore<TestEntity> store = stores.ensure(TestEntity.class);
		Assert.assertNotNull(store);
		
		// add data
		store.add(foo);
		store.add(bar);
		
		// load from file into fresh instance
		stores = new JsonStores(tempDir, true);
		
		// assert old data
		store = stores.resolve(TestEntity.class);
		Assert.assertNotNull(store);
		Assert.assertEquals(2, store.size());
	}
	
	@Test
	public void fileDrop() {
		
		// empty store
		JsonStore<TestEntity> store = stores.ensure(TestEntity.class);
		Assert.assertNotNull(store);
		
		// add data
		store.add(foo);
		store.add(bar);
		
		// drop store
		stores.drop(TestEntity.class);
		
		// load from file into fresh instance
		stores = new JsonStores(tempDir, true);
		
		// assert old data
		store = stores.resolve(TestEntity.class);
		Assert.assertNull(store);
	}
}

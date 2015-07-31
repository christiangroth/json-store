package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.json.FlexjsonHelperTest;
import de.chrgroth.jsonstore.store.model.TestDataVersion1;
import de.chrgroth.jsonstore.store.model.TestDataVersion2;
import flexjson.JsonNumber;

public class AbstractJsonStoreTest {
	
	public static boolean migrationHandlerCalled;
	
	private File tempDir;
	
	// untyped stores because different classes are used to simulate class changes
	private JsonStore<Object> persistentStore;
	private JsonSingletonStore<Object> persistentSingletonStore;
	
	private VersionMigrationHandler versionMigrationHandler;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
		migrationHandlerCalled = false;
		versionMigrationHandler = new VersionMigrationHandler() {
		
		@Override
		public int sourceVersion() {
			return 1;
		}
		
		@Override
		public void migrate(Map<String, Object> genericPayload) {
			migrationHandlerCalled = true;
			
			// fake class for tests only, normally class would stay the same but content / structures only change
			genericPayload.put("class", TestDataVersion2.class.getName());
			
			// fix id
			genericPayload.put("id", new JsonNumber(((String) genericPayload.get("id")).replaceAll("#", "")));
			
			// fill new field
			genericPayload.put("description", ((JsonNumber) genericPayload.get("id")).intValue() + ": " + genericPayload.get("name"));
		}
		};
	}
	
	private void createStores(Integer version, VersionMigrationHandler... migrationHandlers) {
		persistentStore = new JsonStore<>(Object.class, version, FlexjsonHelperTest.DATE_TIME_PATTERN, tempDir, StandardCharsets.UTF_8, true, true, migrationHandlers);
		persistentSingletonStore = new JsonSingletonStore<>(Object.class, version, FlexjsonHelperTest.DATE_TIME_PATTERN, tempDir, StandardCharsets.UTF_8, true, true, migrationHandlers);
	}
	
	// TODO test handler called and auto saved
	
	@Test
	public void versionMigration() {
		
		// add elements
		createStores(1);
		TestDataVersion1 testData = new TestDataVersion1();
		testData.id = "#1";
		testData.name = "first";
		persistentStore.add(testData);
		testData = new TestDataVersion1();
		testData.id = "#2";
		testData.name = "second";
		persistentStore.add(testData);
		
		// create new store for version two and load data
		migrationHandlerCalled = false;
		createStores(2, versionMigrationHandler);
		Assert.assertFalse(migrationHandlerCalled);
		persistentStore.load();
		Assert.assertTrue(migrationHandlerCalled);
		
		// assert new instances
		TestDataVersion2 first = (TestDataVersion2) persistentStore.stream().filter(i -> i instanceof TestDataVersion2 && ((TestDataVersion2) i).id == 1).findFirst().get();
		Assert.assertNotNull(first);
		Assert.assertEquals(first.name, "first");
		Assert.assertEquals(first.description, "1: first");
		TestDataVersion2 second = (TestDataVersion2) persistentStore.stream().filter(i -> i instanceof TestDataVersion2 && ((TestDataVersion2) i).id == 2).findFirst().get();
		Assert.assertNotNull(second);
		Assert.assertEquals(second.name, "second");
		Assert.assertEquals(second.description, "2: second");
		
		// create store again and check data was saved so there is no need to call migration handler again
		migrationHandlerCalled = false;
		createStores(2, versionMigrationHandler);
		Assert.assertFalse(migrationHandlerCalled);
		persistentStore.load();
		Assert.assertFalse(migrationHandlerCalled);
	}
	
	@Test
	public void singletonVersionMigration() {
		
		// add elements
		createStores(1);
		TestDataVersion1 testData = new TestDataVersion1();
		testData.id = "#1";
		testData.name = "first";
		persistentSingletonStore.set(testData);
		
		// create new store for version two and load data
		migrationHandlerCalled = false;
		createStores(2, versionMigrationHandler);
		Assert.assertFalse(migrationHandlerCalled);
		persistentSingletonStore.load();
		Assert.assertTrue(migrationHandlerCalled);
		
		// assert new instances
		TestDataVersion2 first = (TestDataVersion2) persistentSingletonStore.get();
		Assert.assertNotNull(first);
		Assert.assertEquals(first.name, "first");
		Assert.assertEquals(first.description, "1: first");
		
		// create store again and check data was saved so there is no need to call migration handler again
		migrationHandlerCalled = false;
		createStores(2, versionMigrationHandler);
		Assert.assertFalse(migrationHandlerCalled);
		persistentSingletonStore.load();
		Assert.assertFalse(migrationHandlerCalled);
	}
}

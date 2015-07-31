package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import de.chrgroth.jsonstore.store.model.TestDataVersion1;
import de.chrgroth.jsonstore.store.model.TestDataVersion2;
import flexjson.JsonNumber;

public class AbstractJsonStoreTest {
		public static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
		
		public static boolean migrationHandlerCalled;
		
		private File tempDir;
		
		// untyped stores because different classes are used to simulate class changes
		private FlexjsonHelper flexjsonHelper;
		private JsonStore<Object> persistentStore;
		private JsonSingletonStore<Object> persistentSingletonStore;
		
		private TestDataVersion1 testData1_1;
		private TestDataVersion1 testData1_2;
		private Set<TestDataVersion1> testData1;
		private String testData1_1_json;
		private String testData1_2_json;
		private String testData1_json;
		private VersionMigrationHandler versionMigrationHandler;
		
		@Before
		public void init() {
			testData1_1 = new TestDataVersion1();
			testData1_1.id = "#1";
			testData1_1.name = "first";
			testData1_2 = new TestDataVersion1();
			testData1_2.id = "#2";
			testData1_2.name = "second";
			
			flexjsonHelper = FlexjsonHelper.builder().dateTimePattern(DATE_TIME_PATTERN).build();
			testData1_1_json = flexjsonHelper.serializer(false).serialize(testData1_1);
			testData1_2_json = flexjsonHelper.serializer(false).serialize(testData1_2);
			testData1 = new HashSet<>();
			testData1.add(testData1_1);
			testData1.add(testData1_2);
			testData1_json = flexjsonHelper.serializer(false).serialize(testData1);
			
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
			
			tempDir = Files.createTempDir();
			createStores(1);
		}
		
		@Test
		public void payloadWithoutMetadata() {
			
			// load data in old format
			persistentStore.fromJson(testData1_json);
			Assert.assertEquals(testData1.size(), persistentStore.size());
			
			// save and check it's loaded again
			createStores(1);
			persistentStore.load();
			Assert.assertEquals(testData1.size(), persistentStore.size());
		}
		
		@Test
		public void versionMigration() {
			
			// add elements
			persistentStore.add(testData1_1);
			persistentStore.add(testData1_2);
			
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
			persistentSingletonStore.set(testData1_1);
			
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
		
		private void createStores(Integer version, VersionMigrationHandler... migrationHandlers) {
			persistentStore = new JsonStore<>(Object.class, version, flexjsonHelper, tempDir, StandardCharsets.UTF_8, true, true, migrationHandlers);
			persistentSingletonStore = new JsonSingletonStore<>(Object.class, version, flexjsonHelper, tempDir, StandardCharsets.UTF_8, true, true, migrationHandlers);
		}
}
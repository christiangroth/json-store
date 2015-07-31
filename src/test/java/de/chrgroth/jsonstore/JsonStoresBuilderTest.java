package de.chrgroth.jsonstore;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class JsonStoresBuilderTest {
	
	private File tempDir;
	
	@Before
	public void init() {
		tempDir = Files.createTempDir();
	}
	
	// TODO enhance testcases
	
	@Test
	public void transientStores() {
		JsonStores stores = JsonStores.builder().build();
		Assert.assertFalse(stores.isPersistent());
	}
	
	@Test
	public void persistentStores() {
		JsonStores stores = JsonStores.builder().storage(tempDir).build();
		Assert.assertTrue(stores.isPersistent());
	}
}

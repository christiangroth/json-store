package de.chrgroth.jsonstore.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.JSONTokener;
import flexjson.ObjectBinder;

// TODO test logic moved to AbstractJsonStore
public class JSONDeserializerVersioningHookTest {
	
	private static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
	
	private static class TestDataParentOldVersion {
		private String id;
		@JSON
		private List<Integer> numbers = new ArrayList<>();
		@JSON
		private Map<String, String> data = new HashMap<>();
		@JSON
		private Set<Boolean> bools = new HashSet<>();
		@JSON
		private TestDataChild[] children;
		
		public TestDataParentOldVersion() {
		
		}
		
		public TestDataParentOldVersion(String id, List<Integer> numbers, Map<String, String> data, Set<Boolean> bools, TestDataChild... children) {
		this.id = id;
		this.numbers = numbers;
		this.data = data;
		this.bools = bools;
		this.children = children;
		}
		
		public String getId() {
		return id;
		}
		
		public void setId(String id) {
		this.id = id;
		}
		
		public List<Integer> getNumbers() {
		return numbers;
		}
		
		public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
		}
		
		public Map<String, String> getData() {
		return data;
		}
		
		public void setData(Map<String, String> data) {
		this.data = data;
		}
		
		public Set<Boolean> getBools() {
		return bools;
		}
		
		public void setBools(Set<Boolean> bools) {
		this.bools = bools;
		}
		
		public TestDataChild[] getChildren() {
		return children;
		}
		
		public void setChildren(TestDataChild[] children) {
		this.children = children;
		}
	}
	
	private static class TestDataParentNewVersion {
		private String id;
		@JSON
		private List<Integer> numbers = new ArrayList<>();
		@JSON
		private Map<String, String> data = new HashMap<>();
		
		// REMOVED private Set<Boolean> bools = new HashSet<>();
		
		@JSON
		private TestDataChild[] children;
		
		// added
		private String newProperty;
		
		public TestDataParentNewVersion() {
		
		}
		
		public void assertProperties(TestDataParentOldVersion target) {
		Assert.assertEquals(numbers, target.getNumbers());
		Assert.assertEquals(data, target.getData());
		Assert.assertArrayEquals(children, target.getChildren());
		}
		
		public String getId() {
		return id;
		}
		
		public void setId(String id) {
		this.id = id;
		}
		
		public List<Integer> getNumbers() {
		return numbers;
		}
		
		public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
		}
		
		public Map<String, String> getData() {
		return data;
		}
		
		public void setData(Map<String, String> data) {
		this.data = data;
		}
		
		public TestDataChild[] getChildren() {
		return children;
		}
		
		public void setChildren(TestDataChild[] children) {
		this.children = children;
		}
		
		public String getNewProperty() {
		return newProperty;
		}
		
		public void setNewProperty(String newProperty) {
		this.newProperty = newProperty;
		}
	}
	
	private static class TestDataChild {
		private int code;
		
		public TestDataChild() {
		
		}
		
		public TestDataChild(int code) {
		this.code = code;
		}
		
		public int getCode() {
		return code;
		}
		
		public void setCode(int code) {
		this.code = code;
		}
		
		@Override
		public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		return result;
		}
		
		@Override
		public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestDataChild other = (TestDataChild) obj;
		if (code != other.code)
			return false;
		return true;
		}
	}
	
	@Test
	public void versionHook() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		// create testdata
		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("foo", "bar");
		dataMap.put("you", "idiot");
		Set<Boolean> bools = new HashSet<>();
		bools.add(Boolean.TRUE);
		bools.add(Boolean.FALSE);
		TestDataParentOldVersion target = new TestDataParentOldVersion("parentId", Arrays.asList(1, 2, 3, 5, 7, 11), dataMap, bools, new TestDataChild(1), new TestDataChild(2), new TestDataChild(3));
		JSONSerializer serializer = new FlexjsonHelper(DATE_TIME_PATTERN).serializer(false);
		String serialized = serializer.serialize(target);
		
		// tokenize
		Object genericStructure = new JSONTokener(serialized).nextValue();
		Assert.assertNotNull(genericStructure);
		Assert.assertTrue(genericStructure instanceof Map);
		Map<String, Object> rawData = (Map<String, Object>) genericStructure;
		
		// assert first level
		Assert.assertEquals(rawData.get("class"), TestDataParentOldVersion.class.getName());
		Assert.assertEquals(rawData.get("id"), "parentId");
		
		// try to change something / versioning simulation
		rawData.put("class", TestDataParentNewVersion.class.getName()); // FAKED same cluss but different structure in production environments
		rawData.put("id", "CHANGED");
		rawData.put("newProperty", "addedValue");
		rawData.remove("bools");
		
		// create binder hack
		JSONDeserializer<TestDataParentNewVersion> deserializer = new FlexjsonHelper(DATE_TIME_PATTERN).deserializer(TestDataParentNewVersion.class);
		Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
		method.setAccessible(true);
		ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);
		
		// proceed deserialization
		TestDataParentNewVersion testData = (TestDataParentNewVersion) binder.bind(rawData);
		Assert.assertNotNull(testData);
		
		// assert new and changed properties
		Assert.assertEquals("CHANGED", testData.getId());
		Assert.assertEquals("addedValue", testData.getNewProperty());
		testData.assertProperties(target);
	}
}

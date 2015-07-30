package de.chrgroth.jsonstore.json;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.json.model.DateTestEntity;

// TODO create test to check different data settings: list, set, map
public class FlexjsonHelperTest {
	public static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
	
	private FlexjsonHelper flexjsonHelper;
	private DateTestEntity entity;
	private Date date;
	private LocalDateTime localDateTime;
	
	@Before
	public void init() {
		flexjsonHelper = new FlexjsonHelper(DATE_TIME_PATTERN);
		entity = new DateTestEntity();
		date = new Date();
		entity.setDate(date);
		localDateTime = LocalDateTime.now();
		entity.setDateTime(localDateTime);
	}
	
	@Test
	public void entityData() {
		
		// JSON roundtrip
		String json = flexjsonHelper.serializer(false).serialize(entity);
		DateTestEntity deserialized = flexjsonHelper.deserializer(DateTestEntity.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertNotNull(deserialized.getDate());
		Assert.assertEquals(date.getTime(), deserialized.getDate().getTime());
		Assert.assertNotNull(deserialized.getDateTime());
		Assert.assertEquals(localDateTime, deserialized.getDateTime());
	}
	
	@Test
	public void entityWithNullValues() {
		
		// edit to null
		entity.setDate(null);
		entity.setDateTime(null);
		
		// JSON roundtrip
		String json = flexjsonHelper.serializer(false).serialize(entity);
		DateTestEntity deserialized = flexjsonHelper.deserializer(DateTestEntity.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertNull(deserialized.getDate());
		Assert.assertNull(deserialized.getDateTime());
	}
	
	@Test
	public void booleanData() {
		
		// JSON roundtrip
		Boolean data = Boolean.TRUE;
		String json = flexjsonHelper.serializer(false).serialize(data);
		Boolean deserialized = flexjsonHelper.deserializer(Boolean.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(data, deserialized);
	}
	
	@Test
	public void characterData() {
		
		// JSON roundtrip
		Character data = new Character('-');
		String json = flexjsonHelper.serializer(false).serialize(data);
		// Character is handled as String internally
		String deserialized = flexjsonHelper.deserializer(String.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(data.toString(), deserialized);
	}
	
	@Test
	public void integerData() {
		
		// JSON roundtrip
		Integer data = new Integer(13);
		String json = flexjsonHelper.serializer(false).serialize(data);
		// integer will be handled as long internally
		Long deserialized = flexjsonHelper.deserializer(Long.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(new Long(data), deserialized);
	}
	
	@Test
	public void floatData() {
		
		// JSON roundtrip
		Float data = new Float(1.23);
		String json = flexjsonHelper.serializer(false).serialize(data);
		// float will be handled as double internally
		Double deserialized = flexjsonHelper.deserializer(Double.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(new Double(data), deserialized, 0.0000001);
	}
	
	@Test
	public void doubleData() {
		
		// JSON roundtrip
		Double data = new Double(1.23);
		String json = flexjsonHelper.serializer(false).serialize(data);
		// float will be handled as double internally
		Double deserialized = flexjsonHelper.deserializer(Double.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(data, deserialized);
	}
	
	// TODO primitive set
	// TODO primitive list
	// TODO primitive to primitive map
	
	// TODO entity set
	// TODO entity list
	// TODO primitive to entity map
	// TODO entity to primitive map
	// TODO entity to entity map
}

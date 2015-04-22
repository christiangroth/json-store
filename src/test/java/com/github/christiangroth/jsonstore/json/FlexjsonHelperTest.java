package com.github.christiangroth.jsonstore.json;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.christiangroth.jsonstore.json.model.DateTestEntity;

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
	public void jsonLifecycle() {
		
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
	public void nullValues() {
		
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
}

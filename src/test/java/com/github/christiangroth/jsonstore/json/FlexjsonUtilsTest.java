package com.github.christiangroth.jsonstore.json;

import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.christiangroth.jsonstore.json.model.DateTestEntity;

public class FlexjsonUtilsTest {
	
	@Test
	public void jsonLifecycle() {
		
		// create data
		DateTestEntity entity = new DateTestEntity();
		Date date = new Date();
		entity.setDate(date);
		LocalDateTime localDateTime = LocalDateTime.now();
		entity.setDateTime(localDateTime);
		
		// JSON roundtrip
		String json = FlexjsonUtils.serializer(false).serialize(entity);
		DateTestEntity deserialized = FlexjsonUtils.deserializer(DateTestEntity.class).deserialize(json);
		
		// assert data
		Assert.assertNotNull(deserialized);
		Assert.assertEquals(date.getTime(), deserialized.getDate().getTime());
		Assert.assertEquals(localDateTime, deserialized.getDateTime());
	}
}

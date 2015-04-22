package com.github.christiangroth.jsonstore.json;

import java.time.LocalDateTime;
import java.util.Date;

import com.github.christiangroth.jsonstore.json.custom.DateTimeTransformer;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;

/**
 * Helper class encapsulating flexjson configuration.
 * 
 * @author Christian Groth
 */
public final class FlexjsonHelper {
	
	private final DateTransformer dateTransformer;
	private final DateTimeTransformer dateTimeTransformer;
	
	public FlexjsonHelper(String dateTimePattern) {
		dateTransformer = new DateTransformer(dateTimePattern);
		dateTimeTransformer = new DateTimeTransformer(dateTimePattern);
	}
	
	/**
	 * Creates a preconfigured serializer.
	 * 
	 * @param prettyPrint
	 *            pretty print mode
	 * @return serializer
	 */
	public JSONSerializer serializer(boolean prettyPrint) {
		return new JSONSerializer().prettyPrint(prettyPrint).transform(dateTransformer, Date.class).transform(dateTimeTransformer, LocalDateTime.class);
	}
	
	/**
	 * Creates a preconfigured deserializer.
	 * 
	 * @param dataClass
	 *            data class to be deserialized.
	 * @return deserializer
	 * @param <T>
	 *            data class
	 */
	public <T> JSONDeserializer<T> deserializer(Class<T> dataClass) {
		return new JSONDeserializer<T>().use(Date.class, dateTransformer).use(LocalDateTime.class, dateTimeTransformer);
	}
}

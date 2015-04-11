package com.github.christiangroth.jsonstore.json;

import java.time.LocalDateTime;
import java.util.Date;

import com.github.christiangroth.jsonstore.custom.DateTimeTransformer;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;

/**
 * Utility class encapsulating flexjson configuration.
 * 
 * @author Christian Groth
 */
public final class FlexjsonUtils {
	
	// TODO make date time pattern configurable
	private static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
	
	/**
	 * Creates a preconfigured serializer.
	 * 
	 * @param prettyPrint
	 *            pretty print mode
	 * @return serializer
	 */
	public static JSONSerializer serializer(boolean prettyPrint) {
		return new JSONSerializer().prettyPrint(prettyPrint).transform(dateTransformer(), Date.class).transform(dateTimeTransformer(), LocalDateTime.class);
	}
	
	/**
	 * Creates a preconfigured deserializer.
	 * 
	 * @param dataClass
	 *            data class to be deserialized.
	 * @return deserializer
	 */
	public static <T> JSONDeserializer<T> deserializer(Class<T> dataClass) {
		return new JSONDeserializer<T>().use(Date.class, dateTransformer()).use(LocalDateTime.class, dateTimeTransformer());
	}
	
	private static DateTransformer dateTransformer() {
		return new DateTransformer(DATE_TIME_PATTERN);
	}
	
	private static DateTimeTransformer dateTimeTransformer() {
		return new DateTimeTransformer(DATE_TIME_PATTERN);
	}
	
	private FlexjsonUtils() {
		// private constructor, utility class
	}
}

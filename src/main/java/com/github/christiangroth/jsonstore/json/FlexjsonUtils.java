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
	
	private static final DateTransformer DATE_TRANSFORMER = new DateTransformer(DATE_TIME_PATTERN);
	private static final DateTimeTransformer DATE_TIME_TRANSFORMER = new DateTimeTransformer(DATE_TIME_PATTERN);
	
	/**
	 * Creates a preconfigured serializer.
	 * 
	 * @param prettyPrint
	 *            pretty print mode
	 * @return serializer
	 */
	public static JSONSerializer serializer(boolean prettyPrint) {
		return new JSONSerializer().prettyPrint(prettyPrint).transform(DATE_TRANSFORMER, Date.class).transform(DATE_TIME_TRANSFORMER, LocalDateTime.class);
	}
	
	/**
	 * Creates a preconfigured deserializer.
	 * 
	 * @param dataClass
	 *            data class to be deserialized.
	 * @return deserializer
	 */
	public static <T> JSONDeserializer<T> deserializer(Class<T> dataClass) {
		return new JSONDeserializer<T>().use(Date.class, DATE_TRANSFORMER).use(LocalDateTime.class, DATE_TIME_TRANSFORMER);
	}
	
	private FlexjsonUtils() {
		// private constructor, utility class
	}
}

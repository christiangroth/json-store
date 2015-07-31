package de.chrgroth.jsonstore.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.chrgroth.jsonstore.JsonStores;
import de.chrgroth.jsonstore.json.custom.DateTimeTransformer;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.ObjectFactory;
import flexjson.transformer.DateTransformer;
import flexjson.transformer.Transformer;

/**
 * Helper class encapsulating flexjson configuration.
 * 
 * @author Christian Groth
 */
// TODO test transformers and factories are used
public final class FlexjsonHelper {
		
		/**
		 * Builder class to control creation of {@link JsonStores}.
		 * 
		 * @author Christian Groth
		 */
		public static class FlexjsonHelperBuilder {
			
			private static final String DEFAULT_DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
			
			private String dateTimePattern;
			
			private final Map<Class<?>, ObjectFactory> factories;
			private final Map<String, ObjectFactory> pathFactories;
			private final Map<Class<?>, Transformer> transformers;
			private final Map<String, Transformer> pathTransformers;
			
			public FlexjsonHelperBuilder() {
				dateTimePattern = DEFAULT_DATE_TIME_PATTERN;
				
				factories = new HashMap<>();
				pathFactories = new HashMap<>();
				transformers = new HashMap<>();
				pathTransformers = new HashMap<>();
			}
			
			/**
			 * Configures given date time pattern for JSON transformation. Take a look at {@link DateTimeFormatter} for concrete syntax.
			 * 
			 * @param dateTimePattern
			 *            date time pattern to be used
			 * @return builder
			 */
			public FlexjsonHelperBuilder dateTimePattern(String dateTimePattern) {
				this.dateTimePattern = dateTimePattern;
				return this;
			}
			
			/**
			 * Registers a type based factory for JSON deserialization.
			 * 
			 * @param type
			 *            type the factory applies on
			 * @param factory
			 *            factory to be applied
			 * @return builder
			 * @see JSONDeserializer
			 */
			public FlexjsonHelperBuilder factory(Class<?> type, ObjectFactory factory) {
				this.factories.put(type, factory);
				return this;
			}
			
			/**
			 * Registers a path based factory for JSON deserialization.
			 * 
			 * @param path
			 *            path the factory applies on
			 * @param factory
			 *            factory to be applied
			 * @return builder
			 * @see JSONDeserializer
			 */
			public FlexjsonHelperBuilder factory(String path, ObjectFactory factory) {
				this.pathFactories.put(path, factory);
				return this;
			}
			
			/**
			 * Registers a type based transformer for JSON serialization.
			 * 
			 * @param type
			 *            type the transformer applies on
			 * @param transformer
			 *            transformer to be applied
			 * @return builder
			 * @see JSONDeserializer
			 */
			public FlexjsonHelperBuilder transformer(Class<?> type, Transformer transformer) {
				this.transformers.put(type, transformer);
				return this;
			}
			
			/**
			 * Registers a path based transformer for JSON serialization.
			 * 
			 * @param path
			 *            path the transformer applies on
			 * @param transformer
			 *            transformer to be applied
			 * @return builder
			 * @see JSONDeserializer
			 */
			public FlexjsonHelperBuilder transformer(String path, Transformer transformer) {
				this.pathTransformers.put(path, transformer);
				return this;
			}
			
			/**
			 * Creates the {@link FlexjsonHelper} instance.
			 * 
			 * @return flexjson helper
			 */
			public FlexjsonHelper build() {
				
				// create date transformer and object factory
				DateTransformer dateTransformer = new DateTransformer(dateTimePattern);
				factories.put(Date.class, dateTransformer);
				transformers.put(Date.class, dateTransformer);
				
				// create locate date time transformer and object factory
				DateTimeTransformer dateTimeTransformer = new DateTimeTransformer(dateTimePattern);
				factories.put(LocalDateTime.class, dateTimeTransformer);
				transformers.put(LocalDateTime.class, dateTimeTransformer);
				
				// create flexjson helper
				return new FlexjsonHelper(factories, pathFactories, transformers, pathTransformers);
			}
		}
		
		/**
		 * Creates a new builder instance.
		 * 
		 * @return flexjson helper builder
		 */
		public static FlexjsonHelperBuilder builder() {
			return new FlexjsonHelperBuilder();
		}
		
		private JSONSerializer serializer;
		private JSONSerializer prettyPrintSerializer;
		private JSONDeserializer<?> deserializer;
		
		private FlexjsonHelper(Map<Class<?>, ObjectFactory> factories, Map<String, ObjectFactory> pathFactories, Map<Class<?>, Transformer> transformers, Map<String, Transformer> pathTransformers) {
			
			// create serializers
			serializer = createSerializer(transformers, pathTransformers, false);
			prettyPrintSerializer = createSerializer(transformers, pathTransformers, true);
			
			// create deserializers
			deserializer = createDeserializer(factories, pathFactories);
		}
		
		private JSONSerializer createSerializer(Map<Class<?>, Transformer> transformers, Map<String, Transformer> pathTransformers, boolean prettyPrint) {
			JSONSerializer serializer = new JSONSerializer();
			serializer.prettyPrint(prettyPrint);
			transformers.forEach((k, v) -> serializer.transform(v, k));
			pathTransformers.forEach((k, v) -> serializer.transform(v, k));
			return serializer;
		}
		
		private JSONDeserializer<?> createDeserializer(Map<Class<?>, ObjectFactory> factories, Map<String, ObjectFactory> pathFactories) {
			JSONDeserializer<?> deserializer = new JSONDeserializer<>();
			factories.forEach((k, v) -> deserializer.use(k, v));
			pathFactories.forEach((k, v) -> deserializer.use(k, v));
			return deserializer;
		}
		
		/**
		 * Returns a preconfigured serializer.
		 * 
		 * @param prettyPrint
		 *            pretty print mode
		 * @return serializer
		 */
		public JSONSerializer serializer(boolean prettyPrint) {
			return prettyPrint ? prettyPrintSerializer : serializer;
		}
		
		/**
		 * Returns a preconfigured deserializer.
		 * 
		 * @return deserializer
		 */
		public JSONDeserializer<?> deserializer() {
			return deserializer;
		}
}

package de.chrgroth.jsonstore.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.chrgroth.jsonstore.JsonStores;
import de.chrgroth.jsonstore.json.custom.DateTimeTypeHandler;
import de.chrgroth.jsonstore.json.custom.DateTypeHandler;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Helper class encapsulating flexjson configuration.
 *
 * @author Christian Groth
 */
public final class FlexjsonHelper {
    
    /**
     * Builder class to control creation of {@link JsonStores}.
     * 
     * @author Christian Groth
     */
    public static class FlexjsonHelperBuilder {
        
        private static final String DEFAULT_DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";
        
        private String dateTimePattern;
        
        private final Map<Class<?>, AbstractFlexjsonTypeHandler> handlers;
        private final Map<String, AbstractFlexjsonTypeHandler> pathHandlers;
        
        public FlexjsonHelperBuilder() {
            dateTimePattern = DEFAULT_DATE_TIME_PATTERN;
            
            handlers = new HashMap<>();
            pathHandlers = new HashMap<>();
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
         * Registers a custom flexjson type handler.
         * 
         * @param type
         *            type the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         * @see JSONDeserializer
         */
        public FlexjsonHelperBuilder handler(Class<?> type, AbstractFlexjsonTypeHandler handler) {
            handlers.put(type, handler);
            return this;
        }
        
        /**
         * Registers a path based flexjson type handler.
         * 
         * @param path
         *            path the factory applies on
         * @param handler
         *            handler to be applied
         * @return builder
         * @see JSONDeserializer
         */
        public FlexjsonHelperBuilder handler(String path, AbstractFlexjsonTypeHandler handler) {
            pathHandlers.put(path, handler);
            return this;
        }
        
        /**
         * Creates the {@link FlexjsonHelper} instance.
         * 
         * @return flexjson helper
         */
        public FlexjsonHelper build() {
            
            // create type handlers for date and local date time
            DateTypeHandler dateTransformer = new DateTypeHandler(dateTimePattern);
            handlers.put(Date.class, dateTransformer);
            DateTimeTypeHandler dateTimeTransformer = new DateTimeTypeHandler(dateTimePattern);
            handlers.put(LocalDateTime.class, dateTimeTransformer);
            
            // create flexjson helper
            return new FlexjsonHelper(handlers, pathHandlers);
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
    
    private FlexjsonHelper(Map<Class<?>, AbstractFlexjsonTypeHandler> handlers, Map<String, AbstractFlexjsonTypeHandler> pathHandlers) {
        
        // create serializers
        serializer = createSerializer(handlers, pathHandlers, false);
        prettyPrintSerializer = createSerializer(handlers, pathHandlers, true);
        
        // create deserializers
        deserializer = createDeserializer(handlers, pathHandlers);
    }
    
    private JSONSerializer createSerializer(Map<Class<?>, AbstractFlexjsonTypeHandler> handlers,
            Map<String, AbstractFlexjsonTypeHandler> pathHandlers, boolean prettyPrint) {
        JSONSerializer serializer = new JSONSerializer();
        serializer.prettyPrint(prettyPrint);
        handlers.forEach((k, v) -> serializer.transform(v, k));
        pathHandlers.forEach((k, v) -> serializer.transform(v, k));
        return serializer;
    }
    
    private JSONDeserializer<?> createDeserializer(Map<Class<?>, AbstractFlexjsonTypeHandler> handlers,
            Map<String, AbstractFlexjsonTypeHandler> pathHandlers) {
        JSONDeserializer<?> deserializer = new JSONDeserializer<>();
        handlers.forEach((k, v) -> deserializer.use(k, v));
        pathHandlers.forEach((k, v) -> deserializer.use(k, v));
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

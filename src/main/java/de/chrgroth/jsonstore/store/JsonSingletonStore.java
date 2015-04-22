package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.Charset;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a JSON store for a concrete class holding none or one instance. You may use flexjson annotations to control conversion from/to
 * JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonSingletonStore<T> extends AbstractJsonStore<T> {
	
	private static final String FILE_SINGLETON = "singleton";
	
	private T data;
	
	/**
	 * Creates a new JSON store.
	 * 
	 * @param dataClass
	 *            type of objects to be stored
	 * @param dateTimePattern
	 *            date time pattern
	 * @param storage
	 *            global storage path
	 * @param charset
	 *            storage charset
	 * @param prettyPrint
	 *            pretty-print mode
	 * @param autoSave
	 *            auto-save mode
	 */
	public JsonSingletonStore(Class<T> dataClass, String dateTimePattern, File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		super(dataClass, dateTimePattern, storage, charset, FILE_SINGLETON + FILE_SEPARATOR, prettyPrint, autoSave);
	}
	
	/**
	 * Returns stored data.
	 * 
	 * @return data, may be null
	 */
	public T get() {
		return data;
	}
	
	/**
	 * Checks if store is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return data == null;
	}
	
	/**
	 * Stores the given object. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param data
	 *            object to store
	 * @return previous stored object or null
	 */
	public T set(T data) {
		
		// switch data
		T old = data;
		this.data = data;
		
		// save
		if (autoSave) {
			save();
		}
		
		// done
		return old;
	}
	
	/**
	 * Clears the store. Will invoke {@link #save()} if using auto-save mode.
	 */
	public void clear() {
		set(null);
	}
	
	@Override
	protected String serialize(JSONSerializer serializer) {
		return serializer.serialize(data);
	}
	
	@Override
	protected Object deserialize(JSONDeserializer<?> deserializer, String json) {
		@SuppressWarnings("unchecked")
		T deserialized = (T) deserializer.deserialize(json);
		
		// store
		if (deserialized != null) {
			data = deserialized;
		}
		
		// done
		return deserialized;
	}
}

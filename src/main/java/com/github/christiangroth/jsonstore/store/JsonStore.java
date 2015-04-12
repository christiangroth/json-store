package com.github.christiangroth.jsonstore.store;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a JSON store for a concrete class holding zero to many instances. Access is provided using delegate methods to Java built in
 * stream API. You may use flexjson annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonStore<T> extends AbstractJsonStore<T> {
	
	private final Set<T> data;
	
	/**
	 * Creates a new JSON store.
	 * 
	 * @param dataClass
	 *            type of objects to be stored
	 * @param storage
	 *            global storage path
	 * @param charset
	 *            storage charset
	 * @param prettyPrint
	 *            pretty-print mode
	 * @param autoSave
	 *            auto-save mode
	 */
	public JsonStore(Class<T> dataClass, File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		super(dataClass, storage, charset, prettyPrint, autoSave);
		data = new HashSet<>();
	}
	
	/**
	 * Returns copy of data
	 * 
	 * @return copy of data
	 */
	public Set<T> copy() {
		return new HashSet<>(data);
	}
	
	/**
	 * Returns store size.
	 * 
	 * @return size
	 */
	public int size() {
		return data.size();
	}
	
	/**
	 * Checks if store is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	/**
	 * Checks if store contains given element.
	 * 
	 * @param o
	 *            object to be checked
	 * @return true if object is contained, false otherwise
	 */
	public boolean contains(Object o) {
		return data.contains(o);
	}
	
	/**
	 * Checks if store contains all objects in given collection.
	 * 
	 * @param c
	 *            collection to be checked
	 * @return true if all objects are contained, false otherwise
	 */
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}
	
	/**
	 * Adds given object to store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param e
	 *            object to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean add(T e) {
		boolean add = data.add(e);
		if (autoSave && add) {
			save();
		}
		return add;
	}
	
	/**
	 * Adds all objects from given collection to store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *            objects to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean addAll(Collection<? extends T> c) {
		boolean addAll = data.addAll(c);
		if (autoSave && addAll) {
			save();
		}
		return addAll;
	}
	
	/**
	 * Retains elements in given collection.Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *            collection of elements to be retained
	 * @return true if store was changed, false otherwise
	 */
	public boolean retainAll(Collection<?> c) {
		boolean retainAll = data.retainAll(c);
		if (autoSave && retainAll) {
			save();
		}
		return retainAll;
	}
	
	/**
	 * Removed the given element from store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param t
	 *            element to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean remove(T t) {
		boolean remove = data.remove(t);
		if (autoSave) {
			save();
		}
		return remove;
	}
	
	/**
	 * Removed all elements in given collection from store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *            collection with elements to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeAll(Collection<T> c) {
		boolean removeAll = data.removeAll(c);
		if (autoSave) {
			save();
		}
		return removeAll;
	}
	
	/**
	 * Removes all elements satisfying given predicate from store. Will invoke {@link #save()} if using auto-save mode and store was
	 * changed.
	 * 
	 * @param filter
	 *            predicate to be used
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		boolean removeIf = data.removeIf(filter);
		if (autoSave) {
			save();
		}
		return removeIf;
	}
	
	/**
	 * Clears all elements in store. Will invoke {@link #save()} if using auto-save mode.
	 */
	public void clear() {
		data.clear();
		if (autoSave) {
			save();
		}
	}
	
	/**
	 * Creates a stream over a copy of all elements in this store.
	 * 
	 * @return stream over elements in store
	 */
	public Stream<T> stream() {
		return copy().stream();
	}
	
	/**
	 * Creates a parallel stream over a copy of all elements in this store.
	 * 
	 * @return parallel stream over elements in store
	 */
	public Stream<T> parallelStream() {
		return copy().parallelStream();
	}
	
	/**
	 * Performs given action on a copy of all elements in store.<br>
	 * <br>
	 * <b>Attention: Even if using auto-save mode you have to call {@link #save()} yourself!!</b>
	 * 
	 * @param action
	 *            action to be performed on store elements
	 */
	public void forEach(Consumer<? super T> action) {
		copy().forEach(action);
	}
	
	@Override
	protected String serialize(JSONSerializer serializer) {
		return serializer.serialize(copy());
	}
	
	@Override
	protected Object deserialize(JSONDeserializer<?> deserializer, String json) {
		@SuppressWarnings("unchecked")
		List<T> deserialized = (List<T>) deserializer.deserialize(json);
		
		// add data
		if (deserialized != null) {
			data.clear();
			data.addAll(deserialized);
		}
		
		// done
		return deserialized;
	}
}

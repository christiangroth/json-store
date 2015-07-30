package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents a JSON store for a concrete class holding zero to many instances. Access is provided using delegate methods to Java built in
 * stream API. You may use flexjson annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *          concrete type stored in this instance
 */
public class JsonStore<T> extends AbstractJsonStore<T, Set<T>> {
	
	/**
	 * Creates a new JSON store.
	 * 
	 * @param payloadClass
	 *          type of objects to be stored
	 * @param payloadTypeVersion
	 *          version of payload type class
	 * @param dateTimePattern
	 *          date time pattern
	 * @param storage
	 *          global storage path
	 * @param charset
	 *          storage charset
	 * @param prettyPrint
	 *          pretty-print mode
	 * @param autoSave
	 *          auto-save mode
	 */
	public JsonStore(Class<T> payloadClass, Integer payloadTypeVersion, String dateTimePattern, File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		super(payloadClass, payloadTypeVersion, false, dateTimePattern, storage, charset, prettyPrint, autoSave);
		getMetadata().setPayload(new HashSet<>());
	}
	
	@Override
	protected void metadataRefreshed() {
		
		// change payload to set, gets loaded as list by flexjson
		Set<T> payload = new HashSet<T>();
		payload.addAll(getMetadata().getPayload());
		getMetadata().setPayload(payload);
	}
	
	/**
	 * Returns copy of data
	 * 
	 * @return copy of data
	 */
	public Set<T> copy() {
		return new HashSet<>(getMetadata().getPayload());
	}
	
	/**
	 * Returns store size.
	 * 
	 * @return size
	 */
	public int size() {
		return getMetadata().getPayload().size();
	}
	
	/**
	 * Checks if store is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return getMetadata().getPayload().isEmpty();
	}
	
	/**
	 * Checks if store contains given element.
	 * 
	 * @param o
	 *          object to be checked
	 * @return true if object is contained, false otherwise
	 */
	public boolean contains(Object o) {
		return getMetadata().getPayload().contains(o);
	}
	
	/**
	 * Checks if store contains all objects in given collection.
	 * 
	 * @param c
	 *          collection to be checked
	 * @return true if all objects are contained, false otherwise
	 */
	public boolean containsAll(Collection<?> c) {
		return getMetadata().getPayload().containsAll(c);
	}
	
	/**
	 * Adds given object to store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param e
	 *          object to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean add(T e) {
		boolean add = getMetadata().getPayload().add(e);
		if (autoSave && add) {
		save();
		}
		return add;
	}
	
	/**
	 * Adds all objects from given collection to store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *          objects to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean addAll(Collection<? extends T> c) {
		boolean addAll = getMetadata().getPayload().addAll(c);
		if (autoSave && addAll) {
		save();
		}
		return addAll;
	}
	
	/**
	 * Retains elements in given collection.Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *          collection of elements to be retained
	 * @return true if store was changed, false otherwise
	 */
	public boolean retainAll(Collection<?> c) {
		boolean retainAll = getMetadata().getPayload().retainAll(c);
		if (autoSave && retainAll) {
		save();
		}
		return retainAll;
	}
	
	/**
	 * Removed the given element from store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param t
	 *          element to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean remove(T t) {
		boolean remove = getMetadata().getPayload().remove(t);
		if (autoSave) {
		save();
		}
		return remove;
	}
	
	/**
	 * Removed all elements in given collection from store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *          collection with elements to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeAll(Collection<T> c) {
		boolean removeAll = getMetadata().getPayload().removeAll(c);
		if (autoSave) {
		save();
		}
		return removeAll;
	}
	
	/**
	 * Removes all elements satisfying given predicate from store. Will invoke {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param filter
	 *          predicate to be used
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		boolean removeIf = getMetadata().getPayload().removeIf(filter);
		if (autoSave) {
		save();
		}
		return removeIf;
	}
	
	/**
	 * Clears all elements in store. Will invoke {@link #save()} if using auto-save mode.
	 */
	public void clear() {
		getMetadata().getPayload().clear();
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
	 *          action to be performed on store elements
	 */
	public void forEach(Consumer<? super T> action) {
		copy().forEach(action);
	}
}

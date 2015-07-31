package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.chrgroth.jsonstore.json.FlexjsonHelper;

/**
 * Represents a JSON store for a concrete class holding zero to many instances. Access is provided using delegate methods to Java built in
 * stream API. You may use flexjson annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonStore<T> extends AbstractJsonStore<T, Set<T>> {
		
		/**
		 * Creates a new JSON store.
		 * 
		 * @param payloadClass
		 *            type of objects to be stored
		 * @param payloadTypeVersion
		 *            version of payload type class
		 * @param flexjsonHelper
		 *            helper for JSON serialization and deserialization
		 * @param storage
		 *            global storage path
		 * @param prettyPrint
		 *            pretty print mode
		 * @param charset
		 *            storage charset
		 * @param flexjsonHelper
		 *            helper for JSON serialization and deserialization
		 * @param autoSave
		 *            auto-save mode
		 * @param migrationHandlers
		 *            all migration handlers to be applied
		 */
		public JsonStore(Class<T> payloadClass, Integer payloadTypeVersion, FlexjsonHelper flexjsonHelper, File storage, Charset charset, boolean prettyPrint, boolean autoSave, VersionMigrationHandler... migrationHandlers) {
			super(payloadClass, payloadTypeVersion, false, flexjsonHelper, storage, charset, prettyPrint, autoSave, migrationHandlers);
			metadata.setPayload(new HashSet<>());
		}
		
		@Override
		protected void metadataRefreshed() {
			
			// change payload to set, gets loaded as list by flexjson
			Set<T> payload = new HashSet<T>();
			payload.addAll(metadata.getPayload());
			metadata.setPayload(payload);
		}
		
		/**
		 * Returns copy of data
		 * 
		 * @return copy of data
		 */
		public Set<T> copy() {
			return new HashSet<>(metadata.getPayload());
		}
		
		/**
		 * Returns store size.
		 * 
		 * @return size
		 */
		public int size() {
			return metadata.getPayload().size();
		}
		
		/**
		 * Checks if store is empty
		 * 
		 * @return true if empty, false otherwise
		 */
		public boolean isEmpty() {
			return metadata.getPayload().isEmpty();
		}
		
		/**
		 * Checks if store contains given element.
		 * 
		 * @param o
		 *            object to be checked
		 * @return true if object is contained, false otherwise
		 */
		public boolean contains(Object o) {
			return metadata.getPayload().contains(o);
		}
		
		/**
		 * Checks if store contains all objects in given collection.
		 * 
		 * @param c
		 *            collection to be checked
		 * @return true if all objects are contained, false otherwise
		 */
		public boolean containsAll(Collection<?> c) {
			return metadata.getPayload().containsAll(c);
		}
		
		/**
		 * Adds given object to store. Will invoke {@link #save()} if using auto-save mode and store was changed.
		 * 
		 * @param e
		 *            object to add
		 * @return true if store was changed, false otherwise
		 */
		public boolean add(T e) {
			boolean add = metadata.getPayload().add(e);
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
			boolean addAll = metadata.getPayload().addAll(c);
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
			boolean retainAll = metadata.getPayload().retainAll(c);
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
			boolean remove = metadata.getPayload().remove(t);
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
			boolean removeAll = metadata.getPayload().removeAll(c);
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
			boolean removeIf = metadata.getPayload().removeIf(filter);
			if (autoSave) {
				save();
			}
			return removeIf;
		}
		
		/**
		 * Clears all elements in store. Will invoke {@link #save()} if using auto-save mode.
		 */
		public void clear() {
			metadata.getPayload().clear();
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
}

/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BiDiMap<K extends Object, V extends Object> implements Map<K, V> {
	
	private Map<K, V> map;
	private Map<V, K> reverseMap;

	public BiDiMap() {
		map = new IdentityHashMap<>();
		reverseMap = new IdentityHashMap<>();
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public synchronized int size() {
		return map.size();
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsValue(Object value) {
		return reverseMap.containsKey(value);
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public synchronized V get(Object key) {
		return map.get(key);
	}
	
	public synchronized K getKey(Object value) {
		return reverseMap.get(value);
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized V put(K key, V value) {
		V oldValue = map.put(key, value);
		reverseMap.put(value, key);
		
		return oldValue;
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public synchronized V remove(Object key) {
		V value = map.remove(key);
		if(value!=null) {
			reverseMap.remove(value);
		}
		return value;
	}
	
	public synchronized K removeValue(Object value) {
		K key = reverseMap.get(value);
		if(key!=null) {
			map.remove(key);
		}
		return key;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
			reverseMap.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public synchronized void clear() {
		map.clear();
		reverseMap.clear();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public synchronized Set<K> keySet() {
		return map.keySet();
	}
	
	public synchronized Set<V> valueSet() {
		return reverseMap.keySet();
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public synchronized Collection<V> values() {
		return reverseMap.keySet();
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public synchronized Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}
	
	public synchronized Set<Entry<V, K>> reverseEntrySet() {
		return reverseMap.entrySet();
	}
}

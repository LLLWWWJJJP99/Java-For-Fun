package chat.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 29648
 * SELF designed data structure to keep record of username as key
 * and keep corresponding printstream as value
 * @param <K>
 * @param <V>
 */
public class CrazyMap <K, V> {
	public HashMap<K, V> map = new HashMap<>();
	
	public synchronized K removeByValue(V value) {
		for(K k : map.keySet()) {
			if(value.equals(map.get(k))) {
				map.remove(k);
				return k;
			}
		}
		return null;
	}
	
	public synchronized V remove(K key) {
		for(K k : map.keySet()) {
			if(k == key || k.equals(key)) {
				return map.remove(key);
			}
		}
		return null;
	}
	
	public synchronized Set<V> valueSet(){
		HashSet<V> res = new HashSet<>();
		map.forEach((key, value) -> res.add(value));
		return res;
	}
	
	public synchronized K getByValue(V value) {
		for(K key : map.keySet()) {
			if(map.get(key) == value || map.get(key).equals(value)) {
				return key;
			}
		}
		return null;
	}
	
	public synchronized V put(K key, V value) {
		for(V v : map.values()) {
			if(v.hashCode() == value.hashCode() && v.equals(value)) {
				throw new RuntimeException("Cannot add duplicated value");
			}
		}
		return map.put(key, value);
	}
}

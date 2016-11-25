package picoded;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import picoded.JStruct.KeyValueMap;

public class CommonDefaultAcessInterface implements KeyValueMap {
	
	@Override
	public int size() {
		return 0;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean containsValue(Object value) {
		return false;
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
	}
	
	@Override
	public Collection<String> values() {
		return null;
	}
	
	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return null;
	}
	
	@Override
	public boolean getTempHint() {
		return false;
	}
	
	@Override
	public boolean setTempHint(boolean mode) {
		return false;
	}
	
	@Override
	public void systemSetup() {
	}
	
	@Override
	public void systemTeardown() {
	}
	
	@Override
	public void maintenance() {
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public long getExpiry(String key) {
		return 0;
	}
	
	@Override
	public long getLifespan(String key) {
		return 0;
	}
	
	@Override
	public void setExpiry(String key, long time) {
	}
	
	@Override
	public void setLifeSpan(String key, long lifespan) {
	}
	
	@Override
	public Set<String> getKeys(String value) {
		return null;
	}
	
	@Override
	public String get(Object key) {
		return null;
	}
	
	@Override
	public String remove(Object key) {
		return null;
	}
	
	@Override
	public String put(String key, String value) {
		return null;
	}
	
	@Override
	public String putWithLifespan(String key, String value, long lifespan) {
		return null;
	}
	
	@Override
	public String putWithExpiry(String key, String value, long expireTime) {
		return null;
	}
	
	@Override
	public String generateNonce(String val) {
		return null;
	}
	
	@Override
	public String generateNonce(String val, long lifespan) {
		return null;
	}
	
}

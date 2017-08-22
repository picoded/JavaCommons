# Structure


## Interfaces

### GenericConvertList

Common list class used to implement all generic conversion convenience functions in a map interface.

{% hint style='Working' %}
**Methods Summary**

+ build

Primitives

+ getString/Boolean/Number/Int/Long/Float/Double/Byte/Short

Standards

+ getObjectList/StringArray/ObjectArray/NestedObject/UUID/GUID

Library Modules (unique to our library)

+ getGenericConvertStringMap/GenericConvertList

{% endhint %}

#### UnsupportedDefaultList

Interface pattern, that implements most of the default list functions, This builds on top of core functions, in which implementors of this interface will need to support.

These core functions are as followed + get + set + remove + size

All other functions are then built on top of these core function, with suboptimal usage patterns. For example .set(), calls up the get(), and put(), to mimic its usage. When there are probably much more efficient implementation.

Also certain compromises were done to achieve the polyfill. The most prominent one, being that iterators and sublist do not guarantee a “ConcurrentModificationException”, on array change, when size does not change (such as via “set”)

This interface allows a programmer, to rapidly implement a Map object from any class, with just 4 function, instead of 24++ (with iterators).

{% hint style='tip' %}
**Example Usage**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

{% endhint %}



### GenericConvertMap

Common map class used to implement all generic conversion convenience functions in a map interface.

{% hint style='Working' %}
**Methods Summary**

+ build

Primitives

+ getString/Boolean/Number/Int/Long/Float/Double/Byte/Short/List

Standards

+ getObjectList/StringArray/StringMap/ObjectArray/NestedObject/UUID/GUID

Library Modules (unique to our library)

+ getGenericConvertList/GenericConvertStringMap, unpackFullyQualifiedNameKeys

{% endhint %}



#### UnsupportedDefaultMap

Interface pattern, that implements most of the default map functions, This builds on top of core functions, in which implementors of this interface will need to support.

These core functions are as followed + get + put + remove + keyset

All other functions are then built on top of these core function, with suboptimal usage patterns. For example .isEmpty(), calls up the keyset(), and check its length. When there are probably much more efficient implementation.

This interface allows a programmer, to rapidly implement a Map object from any class, with just 4 function, instead of 12++ (with iterators).

{% hint style='tip' %}
**Example Usage**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

{% endhint %}


## Classes

### ArbitaryListIterator

Arbitary List Iterator implementation.

However instead of “true” ConcurrentModificationException protection. This functions by monitoring the “size()” value, between the core functionality.

This can be used either as Iterator, or ListIterator

{% hint style='Working' %}
**Constructor**

+ ArbitaryListIterator(java.util.List<E> inBase, int inIdx)

**Methods Summary**

+ add(), hasNext(), hasPrevious(), next(), nextIndex(), previous(), previousIndex(), remove(), set()

{% endhint %}



### ArrayListMap

Provides a HashMap<K,A ArrayList>, with utility functions to edit records.

{% hint style='Working' %}
**Constructor**

+ ArrayListMap()

**Methods Summary**

+ addToList()/addToListIfNotExists(), genericMap(), toString()

{% endhint %}


### CaseInsensitiveHashMap

Normalizes, stores, and retrieves all keys in lowercase.

As this class extends HashMap directly, several of its common functionalities are inherited.

{% hint style='tip' %}
**Example Usage**

	CaseInsensitiveHashMap tObj = new CaseInsensitiveHashMap<String, String>();

	Case insensitive put
	tObj.put("Hello", "WORLD");

	Outputs "WORLD"
	String ret = tObj.get("HeLLO");

{% endhint %}

{% hint style='Working' %}
**Constructor**

+ CaseInsensitiveHashMap()
+ CaseInsensitiveHashMap(java.util.Map<K,V> map)

**Methods Summary**

+ containsKey(), get(), put(), putAll(), remove(), toString()

{% endhint %}


### DeferredMapEntry

Utility function to create a Map.Entry, which gets the value only when requested.

Note: put / get is only called when needed. Allowing skipping value checks by key name to be optimized out.

This is used by default in UnsupportedDefaultMap.

{% hint style='Working' %}
**Constructor**

+ DeferredMapEntry(java.util.Map<K,V> map, K inKey)

**Methods Summary**

+ equals(), getKey(), getValue(), hashCode(), setValue(), toString()

{% endhint %}


### GenericConvertArrayList

Generic utility to convert array lists into other types.

{% hint style='Working' %}
**Constructor**

+ GenericConvertArrayList()
+ GenericConvertArrayList(java.util.List<E> m)

**Methods Summary**

+ toString()

{% endhint %}


### GenericConvertHashMap

HashMap implementation of GenericConvertMap.

Note: If you are programming interfaces, use GenericConvertMap instead, it has a lot more reuses. It is highly suggested to pass this object around as a GenericConvertMap (similar to HashMap vs Map)

{% hint style='tip' %}
**Example Usage**

	map.put("this", "[\"is\",\"not\",\"the\",\"beginning\"]");
	map.put("nor", new String[] { "this", "is", "the", "end" });

	assertEquals(new String[] { "is", "not", "the", "beginning" },
	map.getStringArray("this"));
	assertEquals("[\"this\",\"is\",\"the\",\"end\"]", map.getString("nor"));

{% endhint %}

{% hint style='Working' %}
**Constructor**

+ GenericConvertHashMap()
+ GenericConvertHashMap(java.util.Map<? extends K,? extends V> m)

**Methods Summary**

+ toString()

{% endhint %}


### HashMapList

Convenience class, for creating a Map containing list values, and appending them.

{% hint style='Working' %}
**Constructor**

+ HashMapList()

**Methods Summary**

+ append(), toMapArray()

{% endhint %}


### MutablePair

Java MutablePair implementation.

See: http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/tuple/Pair.html

{% hint style='tip' %}
**Example Usage**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

{% endhint %}

{% hint style='Working' %}
**Constructor**

+ MutablePair()
+ MutablePair(L left, R right)

**Methods Summary**

+ append(), toMapArray()

{% endhint %}


### ProxyGenericConvertList

This class provides a static constructor, that builds the wrapper to ensure a GenericConvertMap is returned when needed.

{% hint style='tip' %}
**Example Usage**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

{% endhint %}

{% hint style='Working' %}
**Constructor**

+ ProxyGenericConvertList(java.util.List<V> inList)

**Methods Summary**

+ ensure(), toString()

{% endhint %}


### ProxyGenericConvertMap

{% hint style='Working' %}
**Constructor**

+ ProxyGenericConvertMap()
+ ProxyGenericConvertMap(java.util.Map<? extends K,? extends V> m)

**Methods Summary**

+ ensure(), toString()

{% endhint %}

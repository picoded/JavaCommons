# Set


## Classes

{% hint style='Working' %}
**Method Summary**

Similar through all classes.

Inherited from class java.lang.Object

+ equals, getClass, hashCode, notify, notifyAll, toString, wait

{% endhint %}



### EmptyArray

Provides a standardized access to empty arrays.

This proxies the object found in ArrayUtils (from apache).

{% hint style='Working' %}
**Constructor**

+ EmptyArray()

**Fields Summary**

+ STRING, BOOLEAN, BOOLEAN_OBJECT, BYTE, BYTE_OBJECT, CHAR, CHARACTER, CLASS, DOUBLE, DOUBLE_OBJECT, FLOAT, FLOAT_OBJECT, INT, INT_OBJECT, LONG, LONG_OBJECT, SHORT, SHORT_OBJECT, OBJECT, INDEX_NOT_FOUND

{% endhint %}



### JSqlType.JSqlTypeSet

TypeMap to be extended, and stored in their respective package usage.

{% hint style='Working' %}
**Constructor**

+ JSqlTypeSet()

**Fields Summary**

+ INVALID, MSSQL, MYSQL, ORACLE, OTHERS, SQLITE

{% endhint %}



### ObjectToken

In certain circumstances, an object that represents "something" temporary and considered "equal" to itself is required.

Think 'temp object', as they have no persistence between JVM initiations runs. Use this in place of internal class variables for increased readability, and consistency.

Note: EXTREME care should be taken if these objects are passed between different classes. Make sure you contact both sides' respective authors as this can cause unexpected behaviours.

{% hint style='Working' %}
**Constructor**

+ ObjectToken()

**Fields Summary**

+ BOOLEAN_FALSE, BOOLEAN_TRUE, NULL

{% endhint %}


## Enumerations

### HttpRequestType

HttpRequestType enum, used in webUtils, servletUtils, servlet, and RESTBuilder.

{% hint style='Working' %}
**Enum Constants**

+ DELETE, GET, HEAD, OPTION, POST, PUT

**Methods Summary**

+ fromName(), hasFlag(), toEnum(), toInt(), toString(), value(), values(), valueOf()

{% endhint %}


### JSqlType

JSql type options enum, see JSql.sqlType for its usage.

{% hint style='Working' %}
**Enum Constants**

+ DB2, INVALID, MSSQL, MYSQL, ORACLE, OTHERS, POSTGRESQL, SQLITE

**Methods Summary**

+ getName(), toEnum(), toInt(), toString(), valueOf(), values()

{% endhint %}

### MetaType

MetaType enums represents the various data types, that the struct/JSql/JCache/JStack variants of MetaTable can support.

{% hint style='Working' %}
**Enum Constants**

+ BINARY, BINARY_ARRAY, DOUBLE, DOUBLE_ARRAY, FLOAT, FLOAT_ARRAY, INTEGER, INTEGER_ARRAY, JSON, JSON_ARRAY, LONG, LONG_ARRAY, METATABLE, METATABLE_ARRAY, STRING, STRING_ARRAY, TEXT, TEXT_ARRAY, UUID, UUID_ARRAY, NULL

**Methods Summary**

+ fromId(), fromName(), fromTypeObject(), getName(), getValue(), toString(), valueOf(), values()

{% endhint %}

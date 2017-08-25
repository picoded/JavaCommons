# Core Stack Interface

For this "Data Stack" implementation to work, the following standardized interface was implemented. Each serving a specialized role. These form the foundation of "The Data Stack" which is supported across a variety of backend.

Further break down on its details will be elaborated subsequently.


## MetaTable / MetaObject

Probably the core blessing & curse of the whole "Data Stack" concept. This represents a NO-SQL interface. In which object maps, are stored in "whichever" format that is given.

Allowing front-end and application developers to quickly, and very rapidly build up the backend and the data required for storing.

Fixed indexes can then be put in place to subsequently optimize certain key information, for query and/or aggregation purposes.

Main purpose is to function as map object storage, for any form of data.

### MetaTable

{% hint style='Working' %}

**Methods Summary**

+ clear(), ,containsKey(), get(), getArrayFromID(), getFromKeyName_id(), getFromKeyName(), getKeyNames(), looselyIterateObject(), looselyIterateObjectID(), newObject(), query_id(), query(), queryCount(), randomObject(), randomObjectID(), remove()

{% endhint %}

### Core_MetaTable

{% hint style='Working' %}

**Constructors**

+ Core_MetaTable()

**Methods Summary**

+ detachValue(), get(), getFromKeyName(), maintenance(), newObject(), query_id(), remove(), sortAndOffsetList()

{% endhint %}

### JSql_MetaTable

{% hint style='Working' %}

**Constructors**

+ JSql_MetaTable()

**Methods Summary**

+ clear(), getKeyNames(), keySet(), looselyIterateObjectID(), query(), randomObjectID(), systemDestroy(), systemSetup()

{% endhint %}

### JSql_MetaTableUtils

{% hint style='Working' %}

**Constructors**

+ JSql_MetaTableUtils()

**Methods Summary**

+ escapeQueryKey(), extractObjectFRomJSqlResult(), getCurrentTimestamp(), getOrderByObject(), JSqlObjectMapAppend(), JSqlObjectMapFetch(), metaTableCount(), metaTableQuery(), metaTableQueryKey(), searchValueToMetaType(), valueToValueTypeSet()

{% endhint %}

### MetaObject

{% hint style='Working' %}
**Constructors**

+ Core_MetaObject()

**Methods Summary**

+ oid(), get(), keySet(), put(), remove(), saveAll(), saveDelta()

{% endhint %}

### Core_MetaObject

{% hint style='Working' %}

**Constructors**

+ Core_MetaObject()

**Methods Summary**

+ oid(), get(), keySet(), put(), remove(), saveAll(), saveDelta(), toString()

{% endhint %}

## KeyValueMap

High performance lookup of information from key's to values. This in addition support key-value expirary.

Intentionally this does not optimize value based query / lookup, which should be avoided unless in certain deployments where optimization is provided (such as SQL)

Main purpose is to function as high performance key-to-value backend, which in many cases would be strictly in-memory. Additional limitations maybe put in place to facilitate such performance, such as key/value storage length limit.

{% hint style='Working' %}

**Methods Summary**

+ clear(), containsKey(), generateNonceKey(), get(), getExpiry(), getLifeSpan(), keySet(), put(), putWithExpiry(), putWithLifeSpan(), remove(), setExpiry(), setLifeSpan()

{% endhint %}

### Core_KeyValueMap

{% hint style='Working' %}

**Constructors**

+ Core_KeyValueMap()

**Methods Summary**

+ get(), getExpiry(), getLifeSpan(), put(), putWithExpiry(), putWithLifeSpan(), remove(), setExpiry(), setExpiryRaw(), setLifeSpan()

{% endhint %}

## KeyBlobMap

Provides a standardised interface for large object storage. This is meant to work with most major distributed blob object storage systems. Famously Amazon S3.

As such functionality is extremely limited. Nor is in-memory performance designed as a priority.

## AtomicLongMap

Provides atomic numeric support, which is considered to many the "fatal flaw" of NO-SQL. In general this is used mainly for money related tracking, or optimizing aggregation where MetaTable with aggregation is insufficent.

Note that it does not actually provide an AtomicLong object, but a Long instead for easier direct use of getters.

{% hint style='Working' %}
**Constructors**

+ Core_AtomicLongMap()

**Methods Summary**

+ addAndGet(), clear(), decrementAndGet(), get(), getAndAdd(), getAndDecrement(), getAndIncrement(), incrementAndGet(), put(), remove(), weakCompareAndSet()

+ maintenance()

{% endhint %}

## AtomicLockMap

Provides atomic exclusive lock, tied to a specific thread. This is used to provide larger more complex locking functionalities at scale for data related access.

Rarely needed with properly designed process and data flows, however is still useful for "hackish" functionality. Such as locking out a single thread as "master" thread, in a cluster pool.

## MessageQueue

Provides job / message queing services, this is meant for much more complicated process flows, where external threads would accept and process a respective job.

This is normally either to distribute a job / message among multiple speacilized application. Or to funnel the job / message into a single dedicated thread for a task (used together with AtomicLockMap).

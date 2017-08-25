# The Data Stack

The data stack represents a common interface to access and mainpulate data information across a variety of backends. 

**Pros:**

Platform agnostic, simplified NO-SQL interface, that is easily usable by the application developers. Which does most setup steps automatically. Change from mysql to oracle with a single line configuration.

**Cons:**

Compromises were made, not all features of a particular backend is made accessible to the developer (transactions, foreign keys for example), certain unsupprted feature set may have been polyfilled, to detrimental performance results.

There is also a theorectical race condition, where a cache can go out of sync. Though in practise, unless a single row record has high concurrent writes. This should not occur. And on the rare chance it did, with proper cache renewal or expirary, eventual consistency will kick in.

{% hint style='Working' %}
**Constructors**

+ Dstack(), Dconfig()

{% endhint %}

## Stack Ordering

While the general model is based on the [data levels concept](./CONCEPT-data-levels.md), where the lowest level is at the start of the stack, and the highest level last. This is not enforced.

For simplicity sake, we consider the backend at the end of the stack (assuming the highest level), the 'source-of-truth'.

In event of any data conflict is found, the results defer to the 'source-of-truth'.

Read operations is done from the start of the stack, where data is present, until it reaches the 'source-of-truth'

Write operations is done from the end of the stack, the 'source-of-truth', and up the stack till be beginning.

## Example deployment stacks

### Typical small deployment / development

A typical small deployment / development environment, is typically a 2 layer stack.

(@TODO : Insert pancake of 2 layers)
``` javascript
[
	"RequestCache",
	"SQL"
]
```

#### (Level 0) : request data cache

At the first layer, is the `L0 : request data cache`, which helps cache various repeated read calls made within a single request.

A common example would be user information, where it is used for authentication, routing logic, and application logic. Which rarely changes, and is read in a single SQL call, instead of 3.

However due to the lack of data in this layer, typically it does not have any query or aggregation capabilities, which is handled by the other stack layers.

With the exception of long running background threads, or a single request spanning multiple thread. This should be used all the time.

#### (Level 2~3) : SQL backend

At the second layer, is the `L2 / 3 : SQL backend`, this act as the 'source of truth', and facilitate all operations beyond simple read and write. Such as query or aggregations.

Development is typically an `L2` with SQLite as local backend, or a development shared mysql instance.

Deployment is typically an `L3` with mysql backend, with a failover read-replica.

### Large scale deployment

Once the `source-of-truth` backend starts to face performance issue, typically a cache is deployed. Typically this would be a multiple application node deployment.

(@TODO : Insert pancake of 3 layers)
``` javascript
[
	"RequestCache",
	"DistributedCache",
	"SQL"
]
```

#### (level 0) : request data cache

Once again this provides read cache for a single API request

#### (level 3) : distributed cache
Distributed data cache, that is shared across all instances and requests. Typically this would be a subset of the data, which is most typically used. [Under the hot and cold data concept](http://www.ibmbigdatahub.com/blog/your-big-data-hot-warm-or-cold), this is typically hot data.

#### (level 3) : SQL backend
`L3` sql backend with failover read-replica

### Cloud scale deployment

Other more "interesting" deployments can also be performed for 'cloud scale', with no single point of failure.

(@TODO : Insert pancake of 3 layers)
``` javascript
[
	"RequestCache",
	"AggregationAndQueryIndex",
	"ObjectStorage"
]
```

The interesting thing of such a deployment, is that the typical SQL will no longer be needed.

#### (level 3) : Distributed aggregation and query index

A typical use case would be ElasticSearch cluster : in such a deployment, data that is not part of the aggregation / query index, will be discarded. To reduce the overhead involved in maintaining such a cluster. In such a deployment, query and aggregation follows the concept of eventual consistency (a limitation of elasticsearch)

#### (level 3~4) : Object storage backend

A varient of S3 where atomic read-after-write, and read-after-update is maintained. Major examples including Google Cloud storage.

This represents the "source of truth" for data, but does not provide any aggregation or query functionality. While such functionality could be polyfilled, this will typically require the scanning of all objects. Which would in most cases be extremely slow.

### Wrapping up

In overall, the key take away of building an application via "Data Stack" is that application developers can rapidly implement their front end without worrying the backend.

And the backend have full flexibility for the dev-ops team on how it would be deployed.

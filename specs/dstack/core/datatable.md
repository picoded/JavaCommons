# DataTable / DataObject

Represent the core backend data structure in the JavaCommons stack. In its simplist sense, it represents a database backend storage of map objects, each with its own unique GUID. 

This backend can then be searched and aggregated respectively.

{% hint style='info' %}
**Note:** This was originally called MetaTable / MetaObject respectively. This was renamed to better reflect its use case and nature. 

- ObjectTable / ObjectObject was rejected cause "Object" was too generic, and ObjectObject <s>was</s> is kinda dumb.
- DocumentTable / DocumentObject while "correct" is much longer to type. Additionally its confusing for those not familiar with what ["Document Database"](https://en.wikipedia.org/wiki/Document-oriented_database) means. For starters it has nothing to do with word, or PDF files.
{% endhint %}


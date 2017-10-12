# Core Structure, Utilities, and Data Conversion

This uses the `picoded.core` namespace, and contains the following.

+ `picoded.core.conv` : Perform the conversion of one in-memory data strcture, from one type to another.
+ `picoded.core.struct` : Easy to implement interfaces for common data structure types, alone with ready to use implementation.
+ `picoded.core.common` : Predefined enums / set / interfaces used for data exchange between larger components. 

In general due to to its lower level usage, this library is used extensively by the additional library layers, with limited dependencies (beyond apache commons).

{% hint style='tip' %}
For most parts, many would consider this "boring" but unfortunately its effectively the bread that build up the sandwich of an application.
{% endhint %}

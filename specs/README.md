# Picoded.JavaCommons

![Optimize Everything! - JavaCommons in a nutshell](./images/xkcd_the_general_problem.png)

JavaCommons is a not a thin specific use case library. 

Its an extremly fat server-side general purpose library, with more then a 160+ dependency libraries (before counting nested dependencies), taking over 120+ MB in space. 

All before even including any of its own code.

As this contains pretty much ALMOST EVERYTHING we used in one way or another, for a project somewhere, (and still growing). Our new servlet project setup, is normally just use this, and start coding.

## Summary of benefits

It has one core purpose : To let application developers write : __small, efficent, code__

This is facilitated by the following features (for application developers)

+ Allow application developers to write as little backend code as possible (Basic user accounts in 10 lines of code)
+ Write infrastructure / backend independent code, switch between MySQL and NO-SQL with a single config change
+ Reduced dependency-library-hell, by never needing to add another jar. What you need is probably in the 160+ jars already.
+ Integration with vue.js (required node.js to be installed for development / compilation)

## Summary of downsides

To achieve these goals, the following compromises have been accepted

- monolithic library size : We deploy on servers, not mobile phones nor embedded devices. 100MB is nothing.
- Many many potentially unused JARS : thankfully java does a good job not loading them (http://stackoverflow.com/questions/10980483/impacts-of-having-unused-jar-files-in-classpath)

In reality though, just as the image above well illustrate. These problems do not just disappear. They are mitigated away from application to within the library. Where its "time-savings" can only been seen after deploying at scale.

## Design philosophy

![Hmmm....](./images/thinking_emoji.png)

Anything in this project draws its roots from one client project or another. Generally for any project that we build, with this package. As we add on to the project. We ask this one simple question.

`Is there a good chance this could be reused elsewhere?`

If so, the code is refactored and moved to JavaCommons. Hence over time, the library grew (PS: The library was originally named ServletCommons)

With components going through one, or many iterations of meeting the following guidelines

+ KISS : Keep It Simple Stupid
+ Code maintenance is king
+ Generics over custom classes. (If you can use a Map, use it!, we do not need a class collections hell)
+ Abstract out commonly use patterns into components
+ REUSE INC.
+ 0 Java Warnings (suppress as last resort)
+ Code Coverage with unit test please

## Slicing the library

![Sandwich...](./images/sandwich_on_toast.jpg)

To make this who specification guide more digestable, it will be split into the following.

* [Core Structure and Data Conversion](core/the-core.md)
* [Utilities of random stuff](util/the-random-stuff.md)
* [Data Stack : SQL and NO-SQL](dstack/the-dstack.md)
* Servlet and API Builder
* Frontend vue.js
* PDF Generator

{% hint style='tip' %}
If JavaCommons was a sandwich, the following would be the ingrediants

* Core - Bread
* Util - Butter on the bread
* Data Stack - Meaty ham
* Servlet - Menu, Plate, Fork and Spoons
* Frontend - Lecttuce and dressings
* PDF Generator - condiment of Mayonnaise and tomato sauce

Hmm... where was the salt again?
{% endhint %}

{% hint style='danger' %}
**Breaking Namespace Changes:** in an effort to better segment the libraries, namespace changes may occur incrementally, to confrom to this specification. For example `picoded.struct` would be migrated to `picoded.core.struct`
{% endhint %}

Note that "pages" in this "book" prefixed with `[Concept]` refers to meta concepts that heavily influence / explaines certain design decisions. And is not an actual specification.

With that.... lets get this rolling!

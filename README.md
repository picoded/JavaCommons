# Hello Picoded.JavaCommons 

## What is this?
This is THE HUMONGOUS java library package that is pretty much used as a standard package across all Picoded (or its clients / partners) java related projects. Containing over 80 MB's of dependency libraries.

And because it contains pretty much EVERYTHING we used in one way or another, for a project somewhere, (and still growing). Our new servlet project setup, is normally just use this, and start coding.

## The benefits?

+ One giant dependency package for all new java servlet based package : DONE!
+ Maintaining common used toolchains and coding patterns. Across multiple projects.
+ Our projects only uses "this" package. This allow us to swap out the internal underlying dependencies,
  without breaking our various business logic / application specific code.

## The downside?

- Humongous package size : We deploy on servers, not mobile phones nor embedded devices. 100MB is nothing.
- Many many potentially unused JARS : thankfully java does a good job not loading them (http://stackoverflow.com/questions/10980483/impacts-of-having-unused-jar-files-in-classpath)

## The 2 doc sets

+ stable-docs : Code-Gems, for external users of this package, consider everything not in this document, "private" namespace.
+ raw-docs : Covers all the various other code in this package. Future support or compatibility is up to our whims and fancies.

# Design philosophy
Anything in this project draws its roots from one client project or another. Generally for any project that we build, with this package. As we add on to the project. We ask this one simple question.

`Is there a good chance this could be reused elsewhere?`

If so, its refactored and moved to JavaCommons. And so the library started (originally servletCommons) and grew.

However, it also means, until the code gets reused and refined across multiple project, the quality of various components very. As such refactoring, and restructuring happens here and there, with the following applied.

+ KISS : Keep It Simple Stupid
+ Code maintenance is king
+ Generics over custom classes. (If you can use a Map, USE IT)
+ Abstract out commonly use patterns into components
+ REUSE INC.
+ 0 Java Warnings (suppress if you must)

Additionally for components under stable docs, the following is applied.

+ High Quality Documentation. Leave nothing unexplained!
+ Unit test, everything!
+ 0 Java Errors (CI will fail otherwise)

# Developer commands

## The following covers the ANT build commands

| ant command   | Description                              |
| ------------- | ---------------------------------------- |
| setup         | Sets-up the precommit hook script        |
| clean         | Cleanup all the various build files, built docs, and caches |
| compile-lib   | Copies all the various required lib files into the build area |
| compile-src   | Compiles all the various source code under 'src/picoded' |
| compile-srcX  | Compiles all the various experimental source code under 'src/picodedx' |
| compile-tests | Compiles all the various test cases      |
| build         | Package the current build (or compile-src, if not called previously) |
| src-beautify  | Applies the java beautifier script with current project convention |
| compile       | Alias to compile-src                     |
| source        | Alias to compile-src                     |

## Running a single test case

To run a test case inside the picodedTests. name space, use the following, *without the _test*
+ `./runTest.sh {test namespace}` 

So for example, to run the test case `picodedTest.conv.Base62_test`. Run the following.
+ `./runTest.sh conv.Base62` 

Additionally for those who want to loop the test on file changes (Mac / linux only)
+ `./auto-runtest.sh {test namespace}` 

Alternatively you can loop the code compile
+ `./auto-recompile.sh` 

## JAVA source beautifier before merge-requests

Please do the following before merge-requests (hopefully jenkins will properly to do this automatically in future)
`ant src-beautify` 

Alternatively you can use the following pre-commit hook, to automate java beautifier on commit (use either of the following commands)
	+ `ant setup` 
	+ `ln -s ./pre-commit.sh .git/hooks/pre-commit` 

# Some rant notes

## Why no maven for dependencies!
The amount of time spent fixing broken / missing packages, intermittent internet, 
BROKEN CACHES, broken mirrors, and more BROKEN CACHES. (especially for off-site developers, and remote deployments)  

That at several times i felt like i was losing my sanity, over dependency systems that is meant to help me.  

So enough is enough, I rather just dump everything as part of the source control.
So that everyone pretty much only need to download ONE dependency (this project).  

Ideally though perhaps I could one day switch back to maven/gradle (or whatever there is),
after setting up and including into the source control. A proper local repository cache (like how npm works).  

That would certainly make updating libraries much easier. While allowing me to make sure everything is in ONE git package.  

PS: This is filed as T231 in picoded phabriator.

## Why are there 2 docs set?
OK, I will be honest here. There is many things in here that is glued together by duck-tape, and is used in somewhat experimental fashion in one project or another. A huge mix of high quality code, to code that just works.  

Same thing for documentation, some contains essays, others contains nearly nothing.  

Hence the raw-docs docset contains the documentation for every thing in the entire project. The good, bad and ugly.  

While the stable-docs are components that are throughly documented, with extremely high reuse value. Chances are, these are components that are either used directly or seen similar uses across multiple production projects. Having being burned, refined and tested by various trials of fires. They been polished to coding gem stones.  

It also means it is highly unlikely we would break backwards compatibility nor drop them.   

Everything else in raw-docs, is use at your own risk! (Which multiplies rapidly if your not working directly with us)

<h1>
[Concept] Why is instance thread isolation enforced between all requests?

<p style="font-size:0.5em; font-weight:normal;">This question is also known as ...</p>

Why are we disallowed variable sharing between requests?

<p style="font-size:0.5em; font-weight:normal;">or</p>

Why is volatile keyword, and singletons generally bad?

<p style="font-size:0.5em; font-weight:normal;">...</p>
</h1>

Ahh, you probably wondering why Jenkins denied your build, and sent you here with: volatile keyword / singletons is disallowed. Or why I have redirected you to this entry. Or perhaps you been messing into the internals of the Servlet stack.

{% hint style='info' %}**TLDR:** Programmers are humans; I do not trust them to make perfect code.{% endhint %}

Oh your still here, I thought you would be hurt by the previous TLDR statement. Alright then time for some programming lessons. 

Take for example the following. Pseudo code (not actual code)

``` java
// Check if its an admin, and saves the admin flag
if( accountObject.isSuperAdmin() ) {
	CustomerManagementObject.isAdmin = true;
} else {
	// The default is already false, so optimizing this out
	// CustomerManagementObject.isAdmin = false;
}

// Does the required job requested
if( req.jobType() == "something-complicated" ) {
	CustomerManagementObject.doSomethingSlowAndComplicated();
} else {
	CustomerManagementObject.doSomethingFast();
}

// Show the data
if( CustomerManagementObject.isAdmin ) {
	CustomerManagementObject.showConfidentialData();
} else {
	CustomerManagementObject.showLimitedData();
}

// Cleanup the isAdmin flag
CustomerManagementObject.isAdmin = false;
```

Now ask yourself. What is wrong with this?

...

Now what if I said this caused one of the natiest obscure security bug in the company history?

Found it yet?
Well I sort of lied. This alone did not cause the bug (and that is the point).

You see the big spanner for this code, is the fact that "in general" each Servlet is only instantiated **once** by the Servlet Container (eg: tomcat), and reused for all request.

{% hint style='info' %}See: https://stackoverflow.com/questions/2183974/difference-between-each-instance-of-servlet-and-each-thread-of-servlet-in-servle for more details, in certain Servlet Container platforms however, a small handful of instances may be initiated and pooled instead instead of strictly 'once'. The bug still persist however, and is made more obscure due to its random nature.{% endhint %}

In addition, I misdirected you. `CustomerManagementObject` was a class not an object.

A class which was "optimized" to be static, with a shared isAdmin variable.

If this was simply an instance object, where it isnt shared between request there wouldn't be a problem. But in this case; All of sudden that code sample becomes insecure. And starts dumping out confidential data very randomly. Boom. Dynamite.

Because if an admin, were to do a slow complicated request. Within that time-window, a non admin can extract confidential data, using a fast request. Due to that split second, where isAdmin flag is set true for everyone.

While this example may seem like a very obvious mistake to experienced programmer trained in security exploits. Or perhaps you, now that I have enlightened you.

This is made a lot less obvious, if for example, SomeObj was made by one developer. And another developer is implementing the above example.

Made even less obvious if it was a function instead of a variable, like isAdmin(boolean) for the write, and isAdmin() for the read. Or that this it was called something else altogether like isFileOutputEnabled(), but is functionally similar.

This is actually, based on a real life example we faced in our previous framework, where the sequence above was split across 5 files, and over 300 lines of code. However, in concept, it is the same.

The most amusing part, the bug was so obscure, the vulnerability was not found till a year later after being reported by a few user multiple times that they were randomly seeing admin data, despite having been “pen tested” at launch, by several ridiculously expensive pen testers. 

![Crtical Issue, Cannot Reproduce](../images/critical-issue-cannot-reproduce.gif)

As this could not be replicated and occured randomly, the initial reports was "dismissed" as probably an admin using the wrong account, due to the lack of detailed bug report / screenshots.

Made worse that the “something complex and slow” was in milliseconds. And happens only daily. 

In fact, if you never have learnt thread safety, and race conditions, you may even find this impossible to debug.

Thankfully this was several years ago, and much earlier in company history. Where we were dealing with just internal data for a small internal team, and not customer data of a public website.

As such: By design and policy, we avoid sharing of resources between request in the java layer. 

With few notable exceptions: Configuration file are read only memory mapped files, and DB connections uses connection pooling via the driver, making them notable exemptions. 

While it is possible to bypass the thread isolation using java “volatile” which is technically not complicated. The above explanation is why that keyword is banned. Because `volatile` keyword simply gurantee's the variable sharing properties in across requests.

**Remember, you are not writing code for yourself now. But for someone else in the future.** Because if you are writing the application code you will eventually hand it over to someone else in X years’ time. Who will not be aware of the shared variable and its trap. Where a very innocent misstep can carelessly vomit out data in production, while virtually undetectable in development.

In conclusion I am not saying this framework and workflow is correct in having a policy against shared data between request. 

Shared data if controlled properly, do have performance benefits, and do make secure applications. However, we simply made the decision upfront not to do so, to prevent such possible non-obvious errors. Where microseconds gained in performance, are simply not worth it.

Unless of course, you plan to deploy the app on a raspberry pi. Then your pretty much 'forced' to do such micro-optimization. In fact you probably might want to use C++ instead.

Happy Coding,
Eugene

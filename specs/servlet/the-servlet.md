# Servlet & API Builder

`JavaCommons` servlet and API management, to help serve the main content.

{% hint style='tip' %}
Menu, Plate, Fork and spoons. Helping serve the main sandwich to the consumer.
{% endhint %}

## CorePage's and its extensions

Servlet, which provides many in built functionality which would be exteremly common in many server applications. 

These functionalities are built incrementally across multiple servlet classes.
However as these starts to makes assumption on the typical requirement, there may be edge cases,
where lower level servlet implementation may make more sense.

| Name        	| Description                                                                                                                                             	|
|-------------	|---------------------------------------------------------------------------------------------------------------------------------------------------------	|
| CorePage    	| Provides the basic servlet template, where instance thread isolation is enforced. This extends the standard HttpServlet implementation.                 	|
| CoreApiPage 	| Predefined JSON API endpoint which integrates with APIBuilder                                                                                           	|
| DStackPage  	| Additional integration with DStack data, and their various data providers. In addition, this integrate with local file configuration provider (DConfig) 	|
| BasePage    	| Adds in default user authentication, and static file serving                                                                                            	|
| CommonsPage 	| Add in most common functionality used in a complete project. Such as integration with Vue.js (via node) front end, and development mode API.            	|

Both CorePage, and CoreApiPage represents the minimum required for servlets of their respective purpose. 

DStackPage, BasePage and CommonsPage incrementally introduce default functionalities with configuration options.

## APIRunner and its modules

APIRunner, faciltates the setup of both customized endpoints, and modularised endpoints.
Authentication filters faciltates the security aspects of the API.

APiRunner modules are intentionally designed to work in conjuncture with dstack.
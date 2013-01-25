#About pelzer-util
Pelzer-util is a collection of utilities that I've built over a period of about 10 years. They're built to have as few dependencies as possible. The only hard requirement is the Google GSON library for pojo serialization.

The first components of it came together as a solution for property-file hell, where you have many files, each named for the environment where it will be deployed (ie dev, test, stage, prod, etc). The PropertyManager and OverrideableFields classes work to address the issues that arise when you need to modify a parameter across multiple environments.

Other major components are a Logging system (based around Java logging, but using the PropertyManager for configuration) and various unix process-oriented tools.

##pelzer-util environments
An environment is the most important parameter for the pelzer-util package. An environment can span multiple servers, so every system that participates in your production environment would run with the 'PROD' environment (for example). By default, an app that uses the pelzer-util package runs in the 'DEV' environment. More about what this means later.

An environment can be any alphanumeric string, case sensitive. To specify a different environment at startup, add the following parameter to the command line:

	-Dpelzer.environment=FOO
	
##Environments + Properties
During startup, the PropertyManager loads a set of property files (how it does this is covered later) into memory. **Generally**, you do not access the PropertyManager directly, instead interacting with it via other classes. But here's an example property file:

	    com.example.foo=I'm the default
	DEV.com.example.foo=I'm the dev version
	FOO.com.example.foo=I'm the foo version
	
Here're some example calls and their results:

	// If pelzer.environment = "DEV"
	PropertyManager.getProperty("com.example.foo");
		returns "I'm the dev version"

	// If pelzer.environment = "BAR"
	PropertyManager.getProperty("com.example.foo");
		returns "I'm the default"

	// If pelzer.environment = "FOO"
	PropertyManager.getProperty("com.example.foo");
		returns "I'm the foo version"

The steps that the PropertyManager follows to match a given key "example.key" to the keys stored in memory are:

1. look for {environment}.example.key (ie "DEV.example.key")
2. follow any cascades (cascading environments discussed below, but essentially runs the same check as #1 above for each environment in the cascade)
3. look for "example.key"
4. give up

## Environment cascades
In many cases, you'll want one environment to extend another environment, only changing a few properties. An example would be a developer workstation, you might want to have all properties match the 'TEST' environment, but you'd like to override a database connect string to account for some difference on the developer machine. 

Let's pretend I'd like to set up my own environment called 'JPELZER' that would be a duplicate of TEST, except for any properties that I explicitly override. To do this, you'd have properties that look like this:

	JPELZER.ENVIRONMENTS=TEST
	
	   TEST.some.property=abc
	JPELZER.some.property=def
	
	   TEST.something.else=ghi
	   
If we run java with -Dpelzer.environment=JPELZER we'll get the following:

	PropertyManager.getProperty("some.property");
		returns "def"
	PropertyManager.getProperty("something.else");
		returns "ghi"

## Property replacements
The PropertyManager supports token replacement using a simple "{...}" syntax. This allows you to define a property once, and use it symbolically elsewhere. An example would be a base URL and a number of exact URLs:

	 DEV.BASE_URL=http://dev.foo.com
	TEST.BASE_URL=http://test.foo.com
	PROD.BASE_URL=http://www.foo.com
	
	login.url={BASE_URL}/login/
	
So depending on the -Dpelzer.environment variable, we'd get:

	// If pelzer.environment = "DEV"
	PropertyManager.getProperty("login.url");
		returns "http://dev.foo.com/login/"
		
	// If pelzer.environment = "TEST"
	PropertyManager.getProperty("login.url");
		returns "http://test.foo.com/login/"	
	
	// If pelzer.environment = "PROD"
	PropertyManager.getProperty("login.url");
		returns "http://www.foo.com/login/"
		
When doing replacements, the system follows the same procedure to determine the correct environment, following cascades, etc. These replacements are then cached for fast access.


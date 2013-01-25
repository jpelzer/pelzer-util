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
	
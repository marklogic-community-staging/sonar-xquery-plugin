# Sonar XQuery Plugin

This language plugin for the SonarQube code analysis tool (http://sonarqube.org) is intended to work on recent versions of the SonarQube platform 
(currently version 8.9 LTS). It is based on the original plugin work by Chris Cieslinkski (https://github.com/malteseduck/sonar-xquery-plugin/) 
which was last updated to work on Sonar 4.3. This plugin is a COMPLETE rewrite, and has been written using the Kotlin language.

The XQuery parser imported is from xqDocs (https://github.com/xqdoc/xqdoc) and is based on ANTLR4. This parser includes some xqDocs
specific comment extensions to the language definition.  

Documentation and tests will be added in the future.

For any new feature requests please create an Issue within this github project.

# Building the Plugin

To build the plugin, follow the steps below:

1. Build the plugin artifact. 
From the sonar-xquery-extension directory, run:

    ```mvn clean package sonar-packaging:sonar-plugin```
    
    This will generate the jar file to be deployed into SonarQube in the target directory.
    
2. Copy the jar into [SONARQUBE_HOME]/extensions/plugins
3. Restart SonarQube

# Configure your project to run an analysis

In order to run an analysis on a project, you need to setup both [sonar](https://docs.sonarqube.org/latest/setup/install-server/),
and [sonar-scanner](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/).  For more information on those installations, 
please see the documentation from the preceeding links.

After installation is complete, the simplest way to run an analysis on a project is to configure a sonar-project.properties
file in the root of your project as specified in the sonar-scanner documentation.
    
You can run the sonar-scanner using gradle, maven, ant, jenkins or other mechanisms, but I have found that the most
performant method is the stand alone sonar-scanner. 

# Language Checks

The following "Rules" are grouped by one of five severity levels:  INFO, MINOR, MAJOR, CRITICAL, and BLOCKING.  Each 
check gives a basic description based on the original [Sonar XQuery plugin](https://github.com/malteseduck/sonar-xquery-plugin) and the [Blakely.com blog article]( 
https://blakeley.com/blogofile/archives/518/).      

## BLOCKING Checks

These are things that should prevent release of an application into production if any violations of these conventions exist.

There are currently no checks of this severity have been created

## CRITICAL Checks

These are rules that are important to address and should be looked into before
releasing an application.  Following these can help prevent major problems in an
application or significantly increase readability and/or maintainability.

### XQueryVersion
XQuery Version
- **Issue Type:** CODE SMELL
- **Description:** Ensure that you declare the latest XQuery version (1.0-ml/3.0)
at the top of each of your scripts (as opposed to declaring an older version - 0.9-ml - or not declaring a version at all).
This ensures better compatibility of code after server upgrades and consistent behavior in XQuery processing.
- **Note:** Users should not rely on the server default, and instead include this in EVERY xquery file.

### StrongTypingInFunctionDeclaration</dt>
Use Strong Typing in Function Declarations

- **Issue Type:** CODE SMELL
- **Description:** Declare types for function parameters and return types to increase readability and catch potential bugs. 
Also try to scope the types as narrowly as possible (i.e. use 'element()' instead of 'item()' when returning 
an element) and include quantifiers on each type.
- **Notes:** This may not increase readability for inline-functions, which are included in this check
   
### StrongTypingInModuleVariables
Use Strong Typing when Declaring Module Variables

- **Issue Type:** CODE SMELL
- **Description:** Declare types for declared variables to increase readability and catch potential bugs.
Also try to scope the types as narrowly as possible
(i.e. use 'element()' instead of 'item()' when the value is an element)
and include quantifiers on each type.
- **Notes:** This may not increase readability in all cases, but is highly recommended.      

## MAJOR Checks

These are rules about things that *could* cause problems in an application - but
that may not - so it is not critical to address violations immedately.  It would
be a good idea to make plans to address them eventually, though, to avoid any
future problems.

### XdmpEval
Dynamic Function Usage (Marklogic)

- **Issue Type:** VULNERABILITY
- **Description:** Avoid using xdmp:eval() where possible. Instead use either xdmp:invoke(),  
or if possible assign functions to variables to dynamically evaluate code logic. 
Please note that this check is Marklogic specific.
- **Notes:** The use of eval() is a possible injection attack vectors, as user variables passed in from outside
the baseline could be problematic.      

### OperationsInPredicate
Avoid Operations in Predicates

- **Issue Type:** CODE SMELL
- **Description:** Instead of calling functions or performing operations in predicates
try assigning the results to a variable before the predicate.
- **Notes:** This can be a performance issue, however, in cases where it is imperative to stream data out of the database
[let-free programming](https://blakeley.com/blogofile/2012/03/19/let-free-style-and-streaming/) may be needed.

## MINOR Checks

These are rules about things that should be done but generally won't cause too
many problems with an application.  Optimizing to follow these may help prevent
problems, they may not, but in many cases they can increase readability and/or
maintainability.

### XdmpValue
Dynamic Function Usage (Marklogic Specific)

- **Issue Type:** VULNERABILITY
- **Description:** Avoid using xdmp:value() where possible. Instead use either xdmp:unpath() 
or if possible assign functions to variables to dynamically evaluate code logic. 
Please note that this check is Marklogic specific.
- **Notes:** The use of value() is a possible injection attack vectors, as user variables passed in from outside
the baseline could be problematic.      

### EffectiveBoolean -- PLANNED
Effective Boolean in Conditional Predicate
- **Issue Type:** CODE SMELL
- **Description:** Unless the value in the conditional is of type xs:boolean it is recommended you use
                   fn:exists(), fn:empty(), or other boolean functions inside of conditional predicates to check values.

### StrongTypingInFLWOR</dt>
Use Strong Typing in FLWOR Expressions
- **Issue Type:** CODE SMELL
- **Description:** Declare types for FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs.
Also try to scope the types as narrowly as possible
(i.e. use 'element()' instead of 'item()' when the value is an element)
and include quantifiers on each type.
- **Notes:** The standard MarkLogic method of using $_ for variable names that are not going to be used is IGNORED as part of this check.
In some cases, this may make it more difficult to read the source code.

### FunctionMapping
Function Mapping Usage (Marklogic Specific)
- **Issue Type:** CODE SMELL
- **Description:** Make sure you are intentionally using and/or understand function mapping - otherwise disable it with 'declare option xdmp:mapping "false";'.
If you wish to use it you should explicitly declare 'declare option xdmp:mapping "true";'
for readability/maintainability.
- **Notes:** Please note that this check is Marklogic specific.

### XPathDescendantSteps
(Avoid Using '//' in XPath)
- **Issue Type:** CODE SMELL
- **Description:** Favor fully-qualified paths in XPath for readability and to avoid potential performance problems.
- **Notes:** There is a potential impact to the usfeulness of indexes when using the // operator.

### XPathTextSteps
Avoid Using text() in XPath
- **Issue Type:** CODE SMELL
- **Description:** Generally avoid using /text() in your XPath in favor of using fn:string() or allowing atomization (through strong typing or default atomization).

INFO Checks
-----------

These are purely informational either because they require manual checking, they
are just "good to know," or their full validity is in question.

### OrderByRange - PLANNED
Range Evaulation in Order By Clause
- **Issue Type:** CODE SMELL
- **Description:** Order bys or gt/lt checks on large numbers of documents might achieve better performance with a range index.

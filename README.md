# Tealisp
Tealisp is a small, embedded implementation of a Lisp interpreter,
designed to easily interface with Java and intended for use on small,
computation-light programs.

Currently, it is not very optimized, so it is not recommended to do
any kind of heavy lifting with Tealisp; rather, Tealisp is intended
to serve as a kind of 'declarative language' on-bot: to tell
the bot which operations you want it to perform, while implementing
the operations themselves in Java.

Tealisp is still very much a work in progress - please submit a
GitHub issue and/or ping @Majora320 on slack or discord if you find
a bug or want any features to be added.

## Structure
The core of Tealisp is contained in three classes: TokenStream, 
Parser, and Interpreter. As an end-user, you will probably only have 
to care about Interpreter; however, if you want to do fancy things
such as execute a file (not function) multiple times, you may need
to read through the Parser and Interpreter classes to get an idea of
how the two interact.

## LispObject
LispObject is a Java type that represents any value that can be used
in Lisp. Each subclass of LispObject is a distinct type in Lisp.
Specific types, like Integer or String, are referenced via
LispObject.Integer or LispObject.String, respectively.

## Execution lifecycle
When you load a program into the Tealisp interpreter, two things will
happen: all global functions will be bound so that you will be able
to call them from Java, and all top-level code will be called. The
result of the last top-level statement executing is made available
by the funciton `getGlobalResult()`; this value will never change
over the lifetime of the Interpreter. After all top-level expressions
are evaluated, control will return to the caller, and you will be
able to call Tealisp functions from Java.

## Basic Usage
### Initialization
There are several different ways to initialize an Interpreter.
The two most common are the constructor that takes a Reader, 
and the constructor that takes a Reader and a JavaRegistry; the 
JavaRegistry can be used to expose Java functions to be called from Lisp.


#### Readers
Readers are normally created from files, but you can also create
them from strings for testing, like so:
```java
String str = "'(1 2 3)";
Interpreter interp = new Interpreter(new StringReader(str));
```
 
#### JavaRegistry
The JavaRegistry class allows you to expose Java functions to Lisp. To
use it, you must first create an instance; go with the no-argument
constructor by default:

```java
JavaRegistry registry = new JavaRegistry();
```

To actually be able to call Java functions from Lisp, you must
register a JavaInterface. Create a class that extends
JavaInterface, and implement the getSupportedFunctions and
runFunction methods; functon calls in the Lisp code that reference
a function in the getSupportedFunctions list will now be passed
to runFunction. 

##### checkParams
The checkParams function allows for automated checking of
parameter types passed in to runFunction. To use, pass your
actual list of parameters that was passed to your runFunction
as the second argument and a list of expected types (you can
get a Class from most types in java by doing e.g. `Integer.class`)
as the third. If the variadic is true, then the function will accept
any number (from 0 to infinity) of additional parameters, of the type
specified as the last element in the type list passed as the third
parameter. Some examples:

```java
LispObject[] params = new LispObject[] { new LispObject.Integer(0) };
checkParams("function1", params, new Class[] { LispObject.Integer.class }, true); // Checks out

LispObject[] params = new LispObject[] { new LispObject.Integer(0), new LispObject.Integer(2), new LispObject.Integer(3) };
checkParams("function2", params, new Class[] { LispObject.Integer.class,  }, true); // Also checks out

LispObject[] params = new LispObject[] { new LispObject.Integer(0) };
checkParams("function3", params, new Class[] { LispObject.String.class, LispObject.Integer.class }, true); // Does not check out
```

If you want both an integer or double to match with checkParams, use a
LispObject.Number.

##### Registration
To actually register your JavaInterface with the Lisp runtime, you
should call the registerInterface method on your JavaRegistry, with
your interface as the only parameter.

You should then pass the JavaRegistry as a second argument to the 
Interpreter constructor.

### Executing Lisp functions from Java

To execute a Lisp function from Java, you need a Runtime instance.
You can get one from an Interpreter using the getRuntime() method.
Once you have your runtime, simply pass your desired function name
and parameters to callFunction.

## Programming in Tealisp
### The standard library
TODO: Write documentation about the language. What functions are
available, etc.
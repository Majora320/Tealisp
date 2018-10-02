# Tealisp
Tealisp is a small, embedded implementation of a lisp interpreter,
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
and the constructor that takes a Reader and a Runtime; the Runtime
can be used to expose Java functions to be called from lisp.


#### Readers
Readers are normally created from files, but you can also create
them from strings for testing, like so:
```java
String str = "'(1 2 3)";
Interpreter interp = new Interpreter(new StringReader(str));
```
 
#### Runtime
The Runtime class allows you to expose Java functions to lisp. To
use it, you must first create an instance of Runtime; go with the
no-argument constructor by default:

```java
Runtime runtime = new Runtime();
```

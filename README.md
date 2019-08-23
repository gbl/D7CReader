# D7CReader

This is a small program used to explore the contents of STP files, which are used for knitting patterns, which I wrote out of
curiosity as an answer to a stackexchange question (https://reverseengineering.stackexchange.com/questions/12235/reading-stp-designaknit-stitch-pattern-files-ii/12239#12239).

As .stp files are somewhat encrypted, it's not easily possible to read or convert them to anything else. 
The intention of this software is not to be used by end-users, rather, it shows how to read and decrypt those files. It is
aimed at programmers who want to write their own STP decoder and want to see how it's done. If you want to write this kind
of software in Java, feel free to use my classes, but beware that they come with no warranty of any kind.

The program will read three kinds of data from the .stp file:

* stitch patterns
* colors
* stitch types

and dump them to standard output. 

In the first part, each ascii character is a placeholder for a stitch in one certain color. This ascii map gives you a rough
idea of the pattern.
The second part maps those ascii characters to colors; the software will output a list of all color definitions, together with 
4 numbers. The first of those, I don't really understand; the other three are hex RGB values.
The third part is the stitch map, mapping positions to stitch types. I don't really know much about stitching so I can't tell
which character is what type of stitch.

To run the program, import into your favourite IDE, compile, and run. Give the name of the stp file you want to decode on the
command line; without any parameters, the name sterne.stp is assumed (which I used for testing).

If you have no idea of how to compile and use Java:
- install a java SDK (Software Development Kit), the JRE (Java Runtime Environment) is not enough as it doesn't include a compiler.
- Once installed, run `javac src/de/guntram/D7C*java` to compile the .java files to class files.
- Run `java -cp src de.guntram.D7CReader <filename.stp>`

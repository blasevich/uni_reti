JC=javac

SRC=src
BIN=bin

PWD := $(shell pwd)

CP=$(PWD)/src/client:$(PWD)/src/server:$(PWD)/lib/gson-2.10.1.jar

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CP) $*.java

CLASSES := $(shell find . -name '*.java')

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) $(SRC)/*/*.class
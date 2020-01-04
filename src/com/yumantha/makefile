JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./ASTNode.java \
	./FileHandler.java \
	./Token.java \
	./Scanner.java \
	./Parser.java \
	./Compiler.java \
	./Main.java\

./Scanner.class : ./Token$$Type.class

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
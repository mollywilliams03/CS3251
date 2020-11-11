
JFLAGS = -g
JCC = javac
.SUFFIXES: .java .class
.java.class: $(JC) $(JFLAGS) $*.java

CLASSES = \
		ttweetser.java \
		ttweetcli.java

default: classes

classes: $(CLASSES:.java=.class)



#classes:
ttweetser.class: ttweetser.java
		$(JCC) $(JFLAGS) ttweetser.java
ttweetcli.class: ttweetcli.java
		$(JCC) $(JFLAGS) ttweetcli.java
ClientHandler.class: ttweetser.java
		$(JCC) $(JFLAGS) ttweetser.java

clean:
	$(RM) *.class
#clean: rm *.class
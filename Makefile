# A tiny bookstore.
#
# to compile: $ make all
# to clean: $ make clean

all:
	javac -cp $(CLASSPATH) *.java

clean:
	rm *.class
	rm *~
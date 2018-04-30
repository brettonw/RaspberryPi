appname := main

CPP := g++
CPPFLAGS := -std=c++14

sourceDirs := $(shell find src/main/cpp/com/brettonw -type d -exec sh -c '(ls -p "{}"|grep />/dev/null)||echo "{}"' \;)
libs := $()

# just build all the cpp files in this directory
srcfiles := $(shell find . -name "*.cpp")
objects  := $(patsubst %.C, %.o, $(srcfiles))

all: $(appname)

$(appname): $(objects)
	$(CPP) $(CPPFLAGS) $(LDFLAGS) -o $(appname) $(objects) $(LDLIBS)

depend: .depend

.depend: $(srcfiles)
	rm -f ./.depend
	$(CPP) $(CPPFLAGS) -MM $^>>./.depend;

clean:
	rm -f $(objects)

dist-clean: clean
	rm -f *~ .depend

include .depend

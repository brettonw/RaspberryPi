applicationName := main

cpp := gcc-7
cppFlags := -std=c++14
cppInclude := 
ldFlags := 
ldLibs := -lstdc++
ldInclude := 

targetDir := target
builtApplication = $(targetDir)/$(applicationName)
dependenciesFile := "$(targetDir)/dependencies"
sourceDir := src/main/cpp
testDir := src/test/cpp

sourceFiles := $(shell find $(sourceDir) -name "*.cpp")
#sourceFiles := $(shell find $(sourceDir) -name "*.cpp" \! -name "main.cpp")
#compiledFiles := $(patsubst %.cpp,$(targetDir)/$(notdir %).o,$(sourceFiles))
#compiledFiles := $(patsubst $(SRCDIR)/%,$(BUILDDIR)/%,$(SOURCES:.$(SRCEXT)=.$(OBJEXT)))
compiledFiles := $(addprefix $(targetDir)/,$(notdir $(patsubst %.cpp,%.o,$(sourceFiles))))
OUTPUT_OPTION = -o $(targetDir)/$@

all: $(builtApplication)

$(builtApplication): $(compiledFiles)
	$(cpp) $(cppFlags) $(ldFlags) -o $(builtApplication) $(compiledFiles) $(ldLibs) $(ldInclude)

$(targetDir)/%.o: $(sourceDir)/%.cpp
	$(cpp) $(cppFlags) $(cppInclude) -c -o $@ $<

dependencies: $(dependenciesFile)

$(dependenciesFile): $(sourceFiles)
	rm -f $(dependenciesFile)
	$(cpp) $(cppFlags) -MM $^ >> $(dependenciesFile);

clean:
	rm -f $(compiledFiles)

dist-clean: clean
	rm -rf $(targetDir)

include $(dependenciesFile)

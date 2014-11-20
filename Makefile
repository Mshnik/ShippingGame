SRC      = src
OUTDIR   = bin
DOC      = doc
RES      = res
README   = README.md
MAKEFILE = Makefile
ORG      = $(wildcard $(SRC)/org/json/*.java)
GUI      = $(wildcard $(SRC)/gui/*.java)
GAME     = $(wildcard $(SRC)/game/*.java)
SOLUTION  = $(wildcard $(SRC)/solution/*.java)
STUDENT  = $(wildcard $(SRC)/student/*.java)
BUTT     =
ARGS     =


A6 = $(DOC) \
	 $(README) \
	 $(MAKEFILE) \
	 $(RES) \
	 $(DANAUS) \
	 $(STUDENT)

.PHONY: build clean doc run headless testmaps a6

all: build

build: $(OUTDIR)

run: build
	cd $(OUTDIR) && java danaus.Simulator $(BUTT) $(ARGS)

headless: build
	cd $(OUTDIR) && java danaus.Simulator -h $(BUTT) $(ARGS)

testmaps:
	cd $(OUTDIR);                                  \
	for map in `ls ../res/maps/*.xml`; do          \
		echo $$map;                                \
		java danaus.Simulator -h $(BUTT) -f $$map; \
		echo "";                                   \
	done                                             


a6: a6.jar
a6.jar:
	jar cf a6.jar $(A6)

$(OUTDIR): $(ORG) $(GAME) $(GUI) $(STUDENT)
	test -d $(OUTDIR) || mkdir $(OUTDIR)
	javac -d $(OUTDIR) $(ORG) $(GAME) $(GUI) $(SOLUTION) $(STUDENT)

doc:
	cd doc && make

clean:
	! test -d $(OUTDIR) || rm -r $(OUTDIR)
	-rm -f *.jar
	! test -d doc || (cd doc && make clean)

default:	classes

all:		classes

classes:	FORCE
	@case "$(JAVASRCS)" in \
	    '') set -x; $(JAVAC) *.java ;; \
	    *)  set -x; $(JAVAC) $(JAVASRCS) ;; \
	esac

test:		classes

#jar:		$(JARDIR)/$(JARFILE)

$(JARDIR)/$(JARFILE):	classes $(JARDIR)
	case "$(JARFILE)" in \
	    '') ;; \
	    *)  $(JAR) cf $@ $(JAR_CLASSES);; \
	esac

$(DOCDIR) \
$(JARDIR) \
$(FTPDIR):
	mkdir -p $@

clean:
	rm -f $(GARBAGE) *.class;

$(SUBDIR_TARGETS):	FORCE
	@subdir=`echo $@ | sed 's,/.*,,'`; \
	target=`echo $@ | sed 's,.*/,,'`; \
	case $$target in default) target=;; esac; \
	$(MAKE) subdir=$$subdir target=$$target subdir_target

subdir_target:
	@echo ""
	@cd $(subdir) && \
	    echo "Making \"$(target)\" in directory `pwd`" && \
	    echo "" && \
	    $(MAKE) $(target) || exit 1
	@echo ""
	@echo "Returning to directory `pwd`"
	@echo ""

.SUFFIXES:
.SUFFIXES:	.debug .test .class .java .jj

.jj.java:
	$(JAVACC) $<
.java.class:
	$(JAVAC) $<
.class.test:
	$(JAVA) $(PACKAGE).$*
.class.debug:
	$(JDB) $(PACKAGE).$*

# The following entry may be used as a dependency in order to force
# execution of the associated rule.
FORCE:

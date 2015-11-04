.PHONY:all clean
SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))

#need local settings ? create a file 'local.mk' in this directory
ifneq ($(realpath local.mk),)
include $(realpath local.mk)
endif


EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
bin.dir =${this.dir}dist
src.dir=${this.dir}src/main/java
tmp.dir=${this.dir}tmp
JAVAC?=javac
JAR?=jar
gatk-jar?=/commun/data/packages/gatk/3.3.0/GenomeAnalysisTK.jar



all: ${bin.dir}/gatk-ui.jar

${bin.dir}/gatk-ui.jar :${tmp.dir}/GATK_public.key  \
			$(addprefix ${src.dir}/com/github/lindenb/gatkui/,$(addsuffix .java,GatkUi AbstractGatkUi)) \
			${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java \
			${gatk-jar}
	mkdir -p $(dir $@)
	${JAVAC} -d ${tmp.dir} -g -classpath "${gatk-jar}" -sourcepath "${src.dir}:${this.dir}src/main/generated-code/java" $(filter %.java,$^)
	echo "Manifest-Version: 1.0" > ${tmp.dir}/META-INF/MANIFEST.tmp
	echo "Main-Class: com.github.lindenb.gatkui.GatkUi" >> ${tmp.dir}/META-INF/MANIFEST.tmp
	${JAR} cfm $@ ${tmp.dir}/META-INF/MANIFEST.tmp  -C ${tmp.dir} .
	#rm -rf ${tmp.dir}

${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java :  \
		src/main/resources/xsl/programs2java.xsl \
		src/main/resources/xml/programs.xml
	mkdir -p $(dir $@)
	xsltproc --stringparam outdir "$(dir $@)" -o $@ $^

${tmp.dir}/GATK_public.key : ${gatk-jar}
	mkdir -p ${tmp.dir}
	unzip -o ${gatk-jar}  -d ${tmp.dir}
	mv ${tmp.dir}/META-INF/MANIFEST.MF ${tmp.dir}/META-INF/MANIFEST.old
	touch -c $@

clean:
	rm -rf "${tmp.dir}"



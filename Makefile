.PHONY:all
SHELL=/bin/bash

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
bin.dir = dist
src.dir=src/main/java
tmp.dir=tmp
JAVAC?=javac
JAR?=jar
gatk-jar=/commun/data/packages/gatk/3.3.0/GenomeAnalysisTK.jar



all: ${bin.dir}/gatk-ui.jar

${bin.dir}/gatk-ui.jar :${tmp.dir}/GATK_public.key  \
			$(addprefix ${src.dir}/com/github/lindenb/gatkui/,$(addsuffix .java,GatkUi AbstractGatkUi)) \
			src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java \
			${gatk-jar}
	${JAVAC} -d ${tmp.dir} -g -classpath "${gatk-jar}" -sourcepath "${src.dir}:src/main/generated-code/java" $(filter %.java,$^)
	echo "Manifest-Version: 1.0" > ${tmp.dir}/META-INF/MANIFEST.tmp
	echo "Main-Class: com.github.lindenb.gatkui.GatkUi" >> ${tmp.dir}/META-INF/MANIFEST.tmp
	${JAR} cfm $@ ${tmp.dir}/META-INF/MANIFEST.tmp  -C ${tmp.dir} .
	#rm -rf ${tmp.dir}

src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java :  \
		src/main/resources/xsl/programs2java.xsl \
		src/main/resources/xml/programs.xml
	mkdir -p $(dir $@)
	xsltproc -o $@ $^

${tmp.dir}/GATK_public.key : ${gatk-jar}
	mkdir -p ${tmp.dir}
	unzip -o ${gatk-jar}  -d ${tmp.dir}
	mv ${tmp.dir}/META-INF/MANIFEST.MF ${tmp.dir}/META-INF/MANIFEST.old
	touch -c $@
		


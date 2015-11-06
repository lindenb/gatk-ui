.PHONY:all clean
SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))

#need local settings ? create a file 'local.mk' in this directory
ifneq ($(realpath local.mk),)
include $(realpath local.mk)
endif

# proxy for curl, etc...
curl.proxy=$(if ${http.proxy.host}${http.proxy.port},-x "${http.proxy.host}:${http.proxy.port}",)


EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
bin.dir =${this.dir}dist
src.dir=${this.dir}src/main/java
tmp.dir=${this.dir}tmp
JAVAC?=javac
JAR?=jar

ifeq ($(gatk.jar),) 
$(error variable gatk.jar is not defined)
endif



all: ${bin.dir}/gatk-ui.jar programs.tmp.xml

programs.tmp.xml : ${bin.dir}/gatk-scanengines.jar 
	java -jar ${bin.dir}/gatk-scanengines.jar ${gatk.jar} 2> $(addsuffix .err,$@) | xmllint --format -o $@ -

${bin.dir}/gatk-ui.jar : \
			${tmp.dir}/GATK_public.key  \
			$(addprefix ${src.dir}/com/github/lindenb/gatkui/,$(addsuffix .java,GatkUi AbstractGatkUi)) \
			${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java \
			${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/GATKVersion.java \
			${this.dir}src/main/resources/images/splash.png \
			${gatk.jar}
	mkdir -p $(dir $@)
	${JAVAC} -d ${tmp.dir} -g -classpath "${gatk.jar}" -sourcepath "${src.dir}:${this.dir}src/main/generated-code/java" $(filter %.java,$^)
	cp "${this.dir}src/main/resources/images/splash.png" ${tmp.dir}/META-INF/splash.png
	echo "Manifest-Version: 1.0" > ${tmp.dir}/META-INF/MANIFEST.tmp 
	echo "SplashScreen-Image: META-INF/splash.png" >> ${tmp.dir}/META-INF/MANIFEST.tmp
	echo "Main-Class: com.github.lindenb.gatkui.GatkUi" >> ${tmp.dir}/META-INF/MANIFEST.tmp
	${JAR} cfm $@ ${tmp.dir}/META-INF/MANIFEST.tmp  -C ${tmp.dir} .
	#rm -rf ${tmp.dir}

${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/GATKVersion.java: ${tmp.dir}/GATK_public.key
	mkdir -p $(dir $@)
	grep '^version' "${tmp.dir}/META-INF/maven/org.broadinstitute.gatk/gatk-tools-public/pom.properties" |\
	cut -d'=' -f2 | awk 'BEGIN{V="";} {V=$$1;} END{printf("package com.github.lindenb.gatkui;\npublic class GATKVersion{public static String getVersion() { return \"%s\";}}\n",V);}' > $@

${bin.dir}/gatk-scanengines.jar: \
			${tmp.dir}/GATK_public.key  \
			$(addprefix ${src.dir}/com/github/lindenb/gatkui/,$(addsuffix .java,ScanEngines)) \
			${gatk.jar} 
	mkdir -p $(dir $@) ${tmp.dir}2/META-INF
	${JAVAC} -d ${tmp.dir}2 -g -classpath "${gatk.jar}" -sourcepath "${src.dir}" $(filter %.java,$^)
	echo "Manifest-Version: 1.0" > ${tmp.dir}2/META-INF/MANIFEST.tmp
	echo "Main-Class: com.github.lindenb.gatkui.ScanEngines" >> ${tmp.dir}2/META-INF/MANIFEST.tmp
	echo "Class-Path: $(realpath $(filter %.jar,$^)) $@" | fold -w 71 | awk '{printf("%s%s\n",(NR==1?"": " "),$$0);}' >>  ${tmp.dir}2/META-INF/MANIFEST.tmp
	${JAR} cfm $@ ${tmp.dir}2/META-INF/MANIFEST.tmp  -C ${tmp.dir}2 .
	rm -rf ${tmp.dir}2

${bin.dir}/json2xml.jar: \
			${tmp.dir}/GATK_public.key  \
			$(addprefix ${src.dir}/com/github/lindenb/gatkui/,$(addsuffix .java,Json2Xml)) \
			${gatk.jar}
	mkdir -p $(dir $@) ${tmp.dir}2/META-INF
	${JAVAC} -d ${tmp.dir}2 -g -classpath "${gatk.jar}" -sourcepath "${src.dir}" $(filter %.java,$^)
	echo "Manifest-Version: 1.0" > ${tmp.dir}2/META-INF/MANIFEST.tmp
	echo "Main-Class: com.github.lindenb.gatkui.Json2Xml" >> ${tmp.dir}2/META-INF/MANIFEST.tmp
	echo "Class-Path: $(realpath $(filter %.jar,$^)) $@" | fold -w 71 | awk '{printf("%s%s\n",(NR==1?"": " "),$$0);}' >>  ${tmp.dir}2/META-INF/MANIFEST.tmp
	${JAR} cfm $@ ${tmp.dir}2/META-INF/MANIFEST.tmp  -C ${tmp.dir}2 .
	rm -rf ${tmp.dir}2


${this.dir}src/main/generated-code/json/DepthOfCoverage.json :
	mkdir -p $(dir $@)
	curl -Lk ${curl.proxy} -o "$@" "https://www.broadinstitute.org/gatk/gatkdocs/org_broadinstitute_gatk_tools_walkers_coverage_DepthOfCoverage.php.json"


${this.dir}src/main/generated-code/xml/DepthOfCoverage.jsonx : \
		${this.dir}src/main/generated-code/json/DepthOfCoverage.json \
		${bin.dir}/json2xml.jar
	mkdir -p $(dir $@)
	java -jar ${bin.dir}/json2xml.jar $< | xmllint -o "$@" --format -
	
	
${this.dir}src/main/generated-code/xml/DepthOfCoverage.xml : \
		${this.dir}src/main/resources/xsl/jsonx2program.xsl \
		${this.dir}src/main/generated-code/xml/DepthOfCoverage.jsonx
	mkdir -p $(dir $@)
	xsltproc \
		--stringparam www "https://www.broadinstitute.org/gatk/gatkdocs/org_broadinstitute_gatk_tools_walkers_coverage_DepthOfCoverage.php" \
		-o "$@" $^
	

${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java :  \
		src/main/resources/xsl/programs2java.xsl \
		src/main/resources/xsl/commandpreproc.xsl \
		src/main/resources/xml/programs.xml \
		${this.dir}src/main/generated-code/xml/DepthOfCoverage.xml
	mkdir -p $(dir $@)
	xsltproc --xinclude --path "${this.dir}src/main/generated-code/xml" -o "$(addsuffix .tmp.xml,$@)" ${this.dir}src/main/resources/xsl/commandpreproc.xsl src/main/resources/xml/programs.xml
	xsltproc --stringparam outdir "$(dir $@)" -o $@ ${this.dir}src/main/resources/xsl/programs2java.xsl "$(addsuffix .tmp.xml,$@)"
	rm "$(addsuffix .tmp.xml,$@)"

${tmp.dir}/GATK_public.key : ${gatk.jar}
	mkdir -p ${tmp.dir}
	unzip -o ${gatk.jar}  -d ${tmp.dir}
	mv ${tmp.dir}/META-INF/MANIFEST.MF ${tmp.dir}/META-INF/MANIFEST.old
	touch -c $@

${this.dir}src/main/resources/images/splash.png:
	mkdir -p $(dir $@)
	curl -Lk ${curl.proxy} -o "$@" "https://www.broadinstitute.org/gatk/resources/img_shared/logo-gatk-large.png"

clean:
	rm -rf "${tmp.dir}"



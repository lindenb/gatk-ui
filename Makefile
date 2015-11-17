.PHONY:all clean run
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
JAVA?=java

ifeq ($(gatk.jar),) 
$(error variable gatk.jar is not defined)
endif

gatk.docs =	org_broadinstitute_gatk_engine_CommandLineGATK \
		org_broadinstitute_gatk_tools_walkers_coverage_DepthOfCoverage \
		org_broadinstitute_gatk_tools_walkers_variantutils_SelectVariants \
		org_broadinstitute_gatk_tools_walkers_qc_CountReads \
		org_broadinstitute_gatk_tools_walkers_qc_CountLoci \
		org_broadinstitute_gatk_tools_walkers_qc_CountIntervals \


define make_gatk_pane

$${this.dir}src/main/generated-code/json/$$(lastword $$(subst _, ,$(1))).json :
	mkdir -p $$(dir $$@)
	curl -Lk $${curl.proxy} -o "$$(addsuffix .tmp,$$@)" "https://www.broadinstitute.org/gatk/gatkdocs/$(1).php.json"
	mv $$(addsuffix .tmp,$$@) $$@


$${this.dir}src/main/generated-code/xml/$$(lastword $$(subst _, ,$(1))).jsonx : \
		$${this.dir}src/main/generated-code/json/$$(lastword $$(subst _, ,$(1))).json \
		$${bin.dir}/json2xml.jar
	mkdir -p $$(dir $$@)
	$${JAVA} -jar $${bin.dir}/json2xml.jar $$< | xmllint -o "$$(addsuffix .tmp,$$@)" --format -
	mv $$(addsuffix .tmp,$$@) $$@
	
	
$${this.dir}src/main/generated-code/xml/$$(lastword $$(subst _, ,$(1))).xml : \
		$${this.dir}src/main/resources/xsl/jsonx2program.xsl \
		$${this.dir}src/main/generated-code/xml/$$(lastword $$(subst _, ,$(1))).jsonx
	mkdir -p $$(dir $$@)
	xsltproc \
		--stringparam www "https://www.broadinstitute.org/gatk/gatkdocs/$(1).php" \
		-o "$$(addsuffix .tmp,$$@)" $$^
	mv $$(addsuffix .tmp,$$@) $$@

endef


all: ${bin.dir}/gatk-ui.jar 

run: ${bin.dir}/gatk-ui.jar 
	${JAVA} -jar $< 

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


${this.dir}src/main/generated-code/java/com/github/lindenb/gatkui/AbstractGatkPrograms.java :  \
		src/main/resources/xsl/programs2java.xsl \
		src/main/resources/xsl/commandpreproc.xsl \
		$(foreach U,${gatk.docs}, ${this.dir}src/main/generated-code/xml/$(lastword $(subst _, ,${U})).xml )
	mkdir -p $(dir $@) ${this.dir}src/main/generated-code/xml
	echo '<?xml version="1.0" encoding="UTF-8"?>' >  ${this.dir}src/main/generated-code/xml//programs.xml
	echo '<programs xmlns:xi="http://www.w3.org/2001/XInclude">' >>  ${this.dir}src/main/generated-code/xml//programs.xml
	$(foreach U,${gatk.docs}, echo '<xi:include href="$(lastword $(subst _, ,${U})).xml"/>' >>  ${this.dir}src/main/generated-code/xml/programs.xml ;  )
	echo '</programs>' >>  ${this.dir}src/main/generated-code/xml/programs.xml
	xsltproc --xinclude --path "${this.dir}src/main/generated-code/xml" -o "$(addsuffix .tmp.xml,$@)" ${this.dir}src/main/resources/xsl/commandpreproc.xsl src/main/resources/xml/programs.xml
	xsltproc --stringparam outdir "$(dir $@)" -o $@ ${this.dir}src/main/resources/xsl/programs2java.xsl "$(addsuffix .tmp.xml,$@)"
	rm "$(addsuffix .tmp.xml,$@)"


$(eval $(foreach U,${gatk.docs},$(call make_gatk_pane,${U})))


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



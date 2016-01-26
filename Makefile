.PHONY:all clean run jnlp
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

deprecated.docs= \
		org_broadinstitute_gatk_tools_walkers_diagnostics_CoveredByNSamplesSites \
		org_broadinstitute_gatk_tools_walkers_readutils_ReadAdaptorTrimmer \
		org_broadinstitute_gatk_tools_walkers_beagle_BeagleOutputToVCF \
		org_broadinstitute_gatk_tools_walkers_beagle_ProduceBeagleInput \
		org_broadinstitute_gatk_tools_walkers_beagle_VariantsToBeagleUnphased \
		org_broadinstitute_gatk_tools_walkers_variantutils_FilterLiftedVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_LiftoverVariants \
		org_broadinstitute_gatk_tools_ListAnnotations \
		org_broadinstitute_gatk_tools_walkers_variantutils_VariantValidationAssessor 

gatk.docs =	org_broadinstitute_gatk_engine_CommandLineGATK \
		org_broadinstitute_gatk_tools_walkers_coverage_DepthOfCoverage \
		org_broadinstitute_gatk_tools_walkers_variantutils_SelectVariants \
		org_broadinstitute_gatk_tools_walkers_qc_CountReads \
		org_broadinstitute_gatk_tools_walkers_qc_CountLoci \
		org_broadinstitute_gatk_tools_walkers_qc_CountIntervals \
		org_broadinstitute_gatk_tools_walkers_qc_CountMales \
		org_broadinstitute_gatk_tools_walkers_qc_CountRODs \
		org_broadinstitute_gatk_tools_walkers_qc_CountRODsByRef \
		org_broadinstitute_gatk_tools_walkers_qc_CountReadEvents \
		org_broadinstitute_gatk_tools_walkers_qc_CountTerminusEvent \
		org_broadinstitute_gatk_tools_walkers_diagnostics_diagnosetargets_DiagnoseTargets \
		org_broadinstitute_gatk_tools_walkers_diagnostics_ErrorRatePerCycle \
		org_broadinstitute_gatk_tools_walkers_fasta_FastaStats \
		org_broadinstitute_gatk_tools_walkers_diagnostics_FindCoveredIntervals \
		org_broadinstitute_gatk_tools_walkers_qc_FlagStat \
		org_broadinstitute_gatk_tools_walkers_coverage_GCContentByInterval \
		org_broadinstitute_gatk_tools_walkers_qc_Pileup \
		org_broadinstitute_gatk_tools_walkers_qc_PrintRODs \
		org_broadinstitute_gatk_tools_walkers_qc_QCRef \
		org_broadinstitute_gatk_tools_walkers_diagnostics_missing_QualifyMissingIntervals \
		org_broadinstitute_gatk_tools_walkers_qc_ReadClippingStats \
		org_broadinstitute_gatk_tools_walkers_diagnostics_ReadGroupProperties \
		org_broadinstitute_gatk_tools_walkers_diagnostics_ReadLengthDistribution \
		org_broadinstitute_gatk_tools_walkers_simulatereads_SimulateReadsForVariants \
		org_broadinstitute_gatk_tools_walkers_bqsr_BaseRecalibrator \
		org_broadinstitute_gatk_tools_walkers_readutils_ClipReads \
		org_broadinstitute_gatk_tools_walkers_indels_IndelRealigner \
		org_broadinstitute_gatk_tools_walkers_indels_LeftAlignIndels \
		org_broadinstitute_gatk_tools_walkers_readutils_PrintReads \
		org_broadinstitute_gatk_tools_walkers_indels_RealignerTargetCreator \
		org_broadinstitute_gatk_tools_walkers_rnaseq_SplitNCigarReads \
		org_broadinstitute_gatk_tools_walkers_readutils_SplitSamFile \
		\
		org_broadinstitute_gatk_tools_walkers_variantrecalibration_ApplyRecalibration \
		org_broadinstitute_gatk_tools_walkers_variantutils_GenotypeGVCFs \
		org_broadinstitute_gatk_tools_walkers_haplotypecaller_HaplotypeCaller \
		org_broadinstitute_gatk_tools_walkers_phasing_PhaseByTransmission \
		org_broadinstitute_gatk_tools_walkers_phasing_ReadBackedPhasing \
		org_broadinstitute_gatk_tools_walkers_genotyper_UnifiedGenotyper \
		org_broadinstitute_gatk_tools_walkers_variantrecalibration_VariantRecalibrator \
		org_broadinstitute_gatk_tools_walkers_variantutils_CalculateGenotypePosteriors \
		org_broadinstitute_gatk_tools_CatVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_CombineGVCFs \
		org_broadinstitute_gatk_tools_walkers_variantutils_CombineVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_GenotypeConcordance \
		org_broadinstitute_gatk_tools_walkers_haplotypecaller_HaplotypeResolver \
		org_broadinstitute_gatk_tools_walkers_variantutils_LeftAlignAndTrimVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_RandomlySplitVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_RegenotypeVariants \
		org_broadinstitute_gatk_tools_walkers_variantutils_SelectHeaders \
		org_broadinstitute_gatk_tools_walkers_annotator_VariantAnnotator \
		org_broadinstitute_gatk_tools_walkers_varianteval_VariantEval \
		org_broadinstitute_gatk_tools_walkers_filters_VariantFiltration \
		org_broadinstitute_gatk_tools_walkers_variantutils_VariantsToAllelicPrimitives \
		org_broadinstitute_gatk_tools_walkers_variantutils_VariantsToBinaryPed \
		org_broadinstitute_gatk_tools_walkers_variantutils_VariantsToTable \
		org_broadinstitute_gatk_tools_walkers_variantutils_VariantsToVCF \
		org_broadinstitute_gatk_tools_walkers_fasta_FastaAlternateReferenceMaker \
		org_broadinstitute_gatk_tools_walkers_fasta_FastaReferenceMaker \
		org_broadinstitute_gatk_tools_walkers_validation_GenotypeAndValidate \
		org_broadinstitute_gatk_tools_walkers_variantutils_ValidateVariants \
		org_broadinstitute_gatk_tools_walkers_validation_validationsiteselector_ValidationSiteSelector \
		org_broadinstitute_gatk_tools_walkers_haplotypecaller_HCMappingQualityFilter 


define make_gatk_pane

$${this.dir}src/main/generated-code/json/$$(lastword $$(subst _, ,$(1))).json :
	mkdir -p $$(dir $$@)
	curl -Lk $${curl.proxy} -o "$$(addsuffix .tmp,$$@)" "https://www.broadinstitute.org/gatk/gatkdocs/$(1).php.json"
	mv $$(addsuffix .tmp,$$@) $$@


$${this.dir}src/main/generated-code/xml/$$(lastword $$(subst _, ,$(1))).jsonx : \
		$${this.dir}src/main/generated-code/json/$$(lastword $$(subst _, ,$(1))).json \
		$${bin.dir}/json2xml.jar
	mkdir -p $$(dir $$@)
	echo "#converting $$< to xml. If this fails, it could mean that the tool is deprecated and has been removed from GATK. Remove '$(1)' from the Makefile variable \$$$${gatk.docs}"
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
	echo '$(foreach U,${gatk.docs},$(lastword $(subst _, ,${U} )))' | tr " " "\n" | grep -v '^$$' | awk '{printf("<xi:include href=\"%s.xml\"/>\n",$$0);}' >>  ${this.dir}src/main/generated-code/xml/programs.xml 
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


ifneq (${jnlp.dir},)




$(if ${jnlp.dir},$(if ${key.password},,$(warning ---------------------- UNDEFINED $${key.password})))
$(if ${jnlp.dir},$(if ${jnlp.baseurl},,$(warning  ---------------------- UNDEFINED $${jnlp.baseurl})))

key.password?=mysecret
jnlp.baseurl?=http://localhost/gatkui/

.secret.keystore : 
	keytool  -genkeypair -keystore $@ -alias secret \
		-keypass "$(key.password)" -storepass "$(key.password)" \
		-dname "CN=Pierre Lindenbaum, OU=INSERM, O=INSERM, L=Nantes, ST=Nantes, C=Fr"


jnlp: ${this.dir}src/main/resources/images/splash.png \
	${bin.dir}/gatk-ui.jar \
	${this.dir}/src/main/resources/jnlp/gatk-ui.jnlp \
	.secret.keystore
	mkdir -p ${jnlp.dir}
	cp ${bin.dir}/gatk-ui.jar ${this.dir}src/main/resources/images/splash.png ${jnlp.dir}
	sed 's%__CODEBASE__%${jnlp.baseurl}%' ${this.dir}/src/main/resources/jnlp/gatk-ui.jnlp > ${jnlp.dir}/gatk-ui.jnlp
	# http://stackoverflow.com/questions/21695520
	jarsigner -tsa http://timestamp.digicert.com -keystore .secret.keystore  -storepass "$(key.password)" "${jnlp.dir}/gatk-ui.jar" secret
	chmod 755 ${jnlp.dir} ${jnlp.dir}/gatk-ui.jnlp ${jnlp.dir}/gatk-ui.jar ${jnlp.dir}/splash.png


else

jnlp:
	echo "$${jnlp.dir} was not defined in local.mk"

endif


<?xml version='1.0'  encoding="UTF-8" ?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:j="http://www.ibm.com/xmlns/prod/2009/jsonx"
	version='1.0'>

<xsl:output method="xml" indent="yes"/>
<xsl:variable name="programName" />

<xsl:template match="/">
<xsl:apply-templates select="j:object"  mode="program.root"/>
</xsl:template>

<xsl:template match="j:object"  mode="program.root">
<program enabled="true">
<xsl:attribute name="name">
	<xsl:value-of select="@programName"/>
</xsl:attribute>
<xsl:comment>The MIT License (MIT)

Copyright (c) 2015 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
</xsl:comment>
<description><xsl:apply-templates select="j:string[@name='summary']"/></description>
<options>
	<xsl:apply-templates select="j:array[@name='arguments']/j:object" mode="argument"/>
</options>
</program>
</xsl:template>

<xsl:template match="j:object"  mode="argument">
<option>
<xsl:attribute name="opt">
	<xsl:value-of select="substring(j:string[@name='name'],2)"/>
</xsl:attribute>
<xsl:attribute name="type">
	<xsl:variable name="type" select="j:string[@name='type']/text()"/>
	<xsl:value-of select="$type"/>
</xsl:attribute>


	<xsl:choose>
		<xsl:when test="j:string[@name='required']/text() = 'yes'">
			<xsl:attribute name="required">true</xsl:attribute>
		</xsl:when>
		<xsl:when test="j:string[@name='required']/text() = 'no'">
			<xsl:attribute name="required">false</xsl:attribute>
		</xsl:when>
	</xsl:choose>

	<xsl:choose>
		<xsl:when test="j:string[@name='required']/text() = 'no'">
		</xsl:when>
		<xsl:when test="j:string[@name='defaultValue'] != 'NA'">
			<xsl:attribute name="default"><xsl:value-of select="j:string[@name='defaultValue']"/></xsl:attribute>
		</xsl:when>
	</xsl:choose>


	

	<description><xsl:apply-templates select="j:string[@name='summary']"/></description>
</option>
</xsl:template>

</xsl:stylesheet>

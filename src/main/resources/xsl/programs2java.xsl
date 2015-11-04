<?xml version='1.0'  encoding="UTF-8" ?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:u="http://uniprot.org/uniprot"
	version='1.0'>

<xsl:output method="text" />


<xsl:template match="/">
<xsl:apply-templates select="programs"/>
</xsl:template>

<xsl:template match="programs">
package com.github.lindenb.gatkui;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Generated;



@Generated("xslt")
@SuppressWarnings("serial")
public abstract class AbstractGatkPrograms extends AbstractGatkUi
	{
	<xsl:apply-templates select="program" mode="declare"/>
	protected AbstractGatkPrograms()
		{
		}
	@Override
	protected void buildTabbedPane(final JTabbedPane tabbedPane)
		{
		super.buildTabbedPane(tabbedPane);
		<xsl:for-each select="program">
		tabbedPane.addTab("<xsl:value-of select="@name"/>",
			<xsl:value-of select="concat('buildPane',generate-id(.),'()')"/>
			);
		</xsl:for-each>
		}
	@Override
	protected void savePreferences()
		{
		<xsl:apply-templates select="program" mode="prefs"/>
		super.savePreferences();
		}
	
	<xsl:apply-templates select="program"/>
	}

</xsl:template>


<xsl:template match="program" mode="declare">

</xsl:template>

<xsl:template match="program" mode="prefs">

</xsl:template>

<xsl:template match="program" >
/** build panel for command "<xsl:value-of select="@name"/>" */
private JPanel <xsl:value-of select="concat('buildPane',generate-id(.),'()')"/>
	{
	JPanel pane = new JPanel(new BorderLayout(5,5));
	pane.setBorder(new TitledBorder("<xsl:value-of select="@name"/>"));
	
	return pane;
	}

</xsl:template>

</xsl:stylesheet>

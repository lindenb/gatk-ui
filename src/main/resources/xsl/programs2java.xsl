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
import java.io.*;
import java.util.*;

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
			(this.<xsl:value-of select="concat('_instance',@name)"/> = new <xsl:value-of select="concat(@name,'Pane')"/>())
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
protected <xsl:value-of select="concat(@name,'Pane _instance',@name)"/> = null;
</xsl:template>

<xsl:template match="program" mode="prefs">
if(this.<xsl:value-of select="concat('_instance',@name)"/>!=null)
	{
	this.<xsl:value-of select="concat('_instance',@name)"/>.savePreferences();
	}
</xsl:template>

<xsl:template match="program" >
/** class Pane for command "<xsl:value-of select="@name"/>" */
private class <xsl:value-of select="concat(@name,'Pane')"/> extends AbstracCommandPane
	{
	<xsl:apply-templates select="options/option" mode="declare"/>
	<xsl:value-of select="concat(@name,'Pane')"/>()
		{
		this.setBorder(new TitledBorder("<xsl:value-of select="@name"/>"));
	
		JPanel top = new JPanel(new BorderLayout(5,5));
		JLabel lbl = new JLabel("<xsl:apply-templates select="description"/>");
		top.add(lbl,BorderLayout.CENTER);
		this.add(top,BorderLayout.NORTH);
		
		
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		this.add(pane,BorderLayout.CENTER);
		
		<xsl:apply-templates select="options/option" />
		}
	
	@Override
	public String canBuildCommandLine()
		{
		<xsl:apply-templates select="options/option" mode="can.build" />
		return super.canBuildCommandLine();
		}

	
	@Override
	public java.util.List&lt;String&gt; buildCommandLine()
		{
		java.util.List&lt;String&gt; command = super.buildCommandLine();
		<xsl:apply-templates select="options/option" mode="build.cmd" />
		return command;
		}
	
	@Override
	public String getCommandName()
		{
		return "<xsl:value-of select="@name"/>";
		}
	@Override
	public void savePreferences()
		{
		<xsl:apply-templates select="options/option" mode="save.prefs" />
		}
	}

</xsl:template>

<xsl:template match="option" mode="declare">
	<xsl:choose>
		<xsl:when test="@type='input-files'">
			MultipleFileChooser <xsl:value-of select="generate-id(.)"/> = null;
		</xsl:when>
		<xsl:when test="@type='input-file'">
			InputFileChooser <xsl:value-of select="generate-id(.)"/> = null;
		</xsl:when>
		<xsl:when test="@type='output-file'">
			OutputFileChooser <xsl:value-of select="generate-id(.)"/> = null;
		</xsl:when>
		<xsl:when test="@type='boolean'">
			JCheckBox <xsl:value-of select="generate-id(.)"/> = null;
		</xsl:when>
		<xsl:when test="@type='enum'">
				<xsl:choose>
				<xsl:when test="@enum-class">
				JComboBox&lt;<xsl:value-of select="@enum-class"/>&gt; <xsl:value-of select="generate-id(.)"/> = null;
				</xsl:when>
				<xsl:otherwise>
				enum <xsl:value-of select="generate-id(.)"/>E {
					<xsl:for-each select="item">
						<xsl:if test="position()&gt;1">,</xsl:if>
						<xsl:value-of select="text()"/>
					</xsl:for-each>
					};
				JComboBox&lt; <xsl:value-of select="generate-id(.)"/>E &gt; <xsl:value-of select="generate-id(.)"/> = null;
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:when test="@type='int'">
			JTextField <xsl:value-of select="generate-id(.)"/> = null;
		</xsl:when>
		<xsl:otherwise>
			<xsl:message>option:declare unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="option">

/* BEGIN <xsl:value-of select="@label"/> */
{
	<xsl:choose>
		<xsl:when test="@type='input-files'">
			this.<xsl:value-of select="generate-id(.)"/> = new MultipleFileChooser("<xsl:value-of select="@label"/>");
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='input-file'">
			this.<xsl:value-of select="generate-id(.)"/> = new InputFileChooser("<xsl:value-of select="@label"/>");
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			
			String path=	preferences.get(<xsl:apply-templates select="." mode="prefs.key"/>,null);
			if(path!=null) this.<xsl:value-of select="generate-id(.)"/>.setFile(new File(path));

			
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='output-file'">
			this.<xsl:value-of select="generate-id(.)"/> = new OutputFileChooser("<xsl:value-of select="@label"/>");
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			String path=	preferences.get(<xsl:apply-templates select="." mode="prefs.key"/>,null);
			if(path!=null) this.<xsl:value-of select="generate-id(.)"/>.setFile(new File(path));
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='boolean'">
			<xsl:value-of select="generate-id(.)"/> = new JCheckBox("<xsl:value-of select="@label"/>");
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			<xsl:choose>
				<xsl:when test="@default='true'">
					<xsl:value-of select="generate-id(.)"/>.setSelected(true);
				</xsl:when>
				<xsl:when test="@default='false'">
					<xsl:value-of select="generate-id(.)"/>.setSelected(false);
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">boolean missing default</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='enum'">
			<xsl:choose>
				<xsl:when test="@enum-class">
					<xsl:value-of select="generate-id(.)"/> = new JComboBox(<xsl:value-of select="@enum-class"/>.values());
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="generate-id(.)"/> = new JComboBox(<xsl:value-of select="generate-id(.)"/>E.values());
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			<xsl:if test="item[@default='true']">
				 	<xsl:value-of select="generate-id(.)"/>.setSelectedItem(<xsl:value-of select="generate-id(.)"/>E.<xsl:value-of select="item[@default='true']/text()"/>);
			</xsl:if>
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='int'">
			this.<xsl:value-of select="generate-id(.)"/> = new JTextField("");
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			<xsl:if test="@default">
			this.<xsl:value-of select="generate-id(.)"/>.setText("<xsl:value-of select="@default"/>");
			</xsl:if>
			
			String val = preferences.get(<xsl:apply-templates select="." mode="prefs.key"/>,null);
			if(val!=null) this.<xsl:value-of select="generate-id(.)"/>.setText(val);
			
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:message>option unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
}
/* END <xsl:value-of select="@label"/> */

</xsl:template>


<xsl:template match="option" mode="build.cmd">
	<xsl:choose>
		<xsl:when test="@type='input-files'">
			for(File f: this.<xsl:value-of select="generate-id(.)"/>.getFiles())
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(f.getPath());
				}
		</xsl:when>
		<xsl:when test="@type='input-file' or @type='output-file'">
			if(this.<xsl:value-of select="generate-id(.)"/>.getFile()!=null)
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(<xsl:value-of select="generate-id(.)"/>.getFile().getPath());
				}
		</xsl:when>
		<xsl:when test="@type='boolean'">
			if(this.<xsl:value-of select="generate-id(.)"/>.isSelected())
				{
				command.add("-<xsl:value-of select="@opt"/>");
				}
		</xsl:when>
		<xsl:when test="@type='enum'">
			if(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem()!=null)
				{
				command.add("-<xsl:value-of select="@opt"/>");
				
				
				command.add(String.valueOf(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem()));
				}
		</xsl:when>
		
		
		<xsl:when test="@type='int'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
				}
		</xsl:when>
		
		
		<xsl:otherwise>
			<xsl:message>option:declare unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="option" mode="can.build">
	<xsl:choose>
		<xsl:when test="@type='input-files'">
			<xsl:if test="@required='true'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getFiles().isEmpty())
					{
					return "<xsl:value-of select="@label"/> cannot be empty";
					}
			</xsl:if>
		</xsl:when>
		<xsl:when test="@type='input-file' or @type='output-file'">
			<xsl:if test="@required='true'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getFile()==null)
					{
					return "<xsl:value-of select="@label"/> cannot be empty";
					}
			</xsl:if>
		</xsl:when>
		<xsl:when test="@type='boolean'">
			
		</xsl:when>
		<xsl:when test="@type='enum'">
			<xsl:if test="@required='true'">
			if(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem()!=null)
				{
				return "<xsl:value-of select="@label"/> cannot be empty";
				}
			</xsl:if>
		</xsl:when>
		
		<xsl:when test="@type='int'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				try
					{
					int v = Integer.parseInt(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
					<xsl:if test="@min-inclusive">
					if(v&lt;<xsl:value-of select="@min-inclusive"/>)
						{
						return "<xsl:value-of select="@label"/>: should be greater or equal to <xsl:value-of select="@min-inclusive"/>";
						}
					</xsl:if>
					<xsl:if test="@min-exclusive">
					if(v&lt;=<xsl:value-of select="@min-exclusive"/>)
						{
						return "<xsl:value-of select="@label"/>: should be greater to <xsl:value-of select="@min-exclusive"/>";
						}
					</xsl:if>
					<xsl:if test="@max-inclusive">
					if(v&gt;=<xsl:value-of select="@max-inclusive"/>)
						{
						return "<xsl:value-of select="@label"/>: should be lower or equal to <xsl:value-of select="@max-inclusive"/>";
						}
					</xsl:if>
					<xsl:if test="@max-exclusive">
					if(v&gt;=<xsl:value-of select="@max-exclusive"/>)
						{
						return "<xsl:value-of select="@label"/>: should be lower to <xsl:value-of select="@max-exclusive"/>";
						}
					</xsl:if>
					}
				catch(Exception err)
					{
					return "Bad number : <xsl:value-of select="@label"/>";
					}
				}
			<xsl:if test="@required='true'">
			else
				{
				return "<xsl:value-of select="@label"/> cannot be empty";
				}
			</xsl:if>
		</xsl:when>
		
		
		<xsl:otherwise>
			<xsl:message>option:declare unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="option" mode="save.prefs">
	<xsl:choose>
		<xsl:when test="@type='input-files'">
				
		</xsl:when>
		<xsl:when test="@type='input-file' or @type='output-file'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getFile()==null)
					{
					preferences.remove(<xsl:apply-templates select="." mode="prefs.key"/>);
					}
				else
					{
					preferences.put(
								<xsl:apply-templates select="." mode="prefs.key"/>,
								this.<xsl:value-of select="generate-id(.)"/>.getFile().getPath()
								);
					}
		</xsl:when>
		<xsl:when test="@type='int'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
					{
					preferences.remove(<xsl:apply-templates select="." mode="prefs.key"/>);
					}
				else
					{
					preferences.put(
								<xsl:apply-templates select="." mode="prefs.key"/>,
								this.<xsl:value-of select="generate-id(.)"/>.getText().trim()
								);
					}
		</xsl:when>		
		<xsl:otherwise>
			<xsl:message>save.prefs:declare unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="option" mode="prefs.key">
<xsl:text>"</xsl:text>
<xsl:value-of select="concat(../../@name,'.',translate(@opt,'-',''))"/>
<xsl:text>"</xsl:text>
</xsl:template>

<xsl:template match="filter">
this.<xsl:value-of select="generate-id(..)"/>.setFilter(new javax.swing.filechooser.FileFilter() {
	@Override
	public String getDescription() {
		return "<xsl:value-of select="@label"/>";
	}
	
	@Override
	public boolean accept(final java.io.File f)
		{
		if(f.isDirectory()) return true;
		<xsl:apply-templates select="extension"/>
		return false;
		}
	});
</xsl:template>

<xsl:template match="extension[text()='bam']">
if(f.getName().toLowerCase().endsWith(".bam"))
	{
	if(!htsjdk.samtools.BamFileIoUtils.isBamFile(f)) return false;
	File bai =new File(f.getParentFile(), f.getName()+htsjdk.samtools.BAMIndex.BAMIndexSuffix);
	if(bai.exists()) return true;
	int dot = f.getName().lastIndexOf(".bam");
	bai =new File(f.getParentFile(), f.getName().substring(0, dot)+htsjdk.samtools.BAMIndex.BAMIndexSuffix);
	if(bai.exists()) return true;
	return false;
	}
</xsl:template>

<xsl:template match="extension">
if(f.getName().toLowerCase().endsWith(".<xsl:value-of select="text()"/>".toLowerCase())) return true;
</xsl:template>


</xsl:stylesheet>

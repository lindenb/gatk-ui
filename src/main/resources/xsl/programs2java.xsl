<?xml version='1.0'  encoding="UTF-8" ?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version='1.0'>

<xsl:output method="text" />
<xsl:param name="outdir"/>

<xsl:template match="/">
<xsl:apply-templates select="programs"/>
</xsl:template>

<xsl:template match="programs">
<xsl:call-template name="license"/>
package com.github.lindenb.gatkui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Generated;
import java.io.*;
import java.util.*;
import com.github.lindenb.gatkui.swing.*;

@Generated("xslt")
@SuppressWarnings("serial")
public abstract class AbstractGatkPrograms extends AbstractGatkUi
	{
	
	<xsl:apply-templates select="program[not(@disabled='true')]" mode="declare"/>
	protected AbstractGatkPrograms()
		{
		}
	@Override
	protected void buildTabbedPane(final JTabbedPane tabbedPane)
		{
		super.buildTabbedPane(tabbedPane);
		AbstractAction action=null;
		<xsl:apply-templates select="program[@name='CommandLineGATK']" mode="new.instance"/>
		
		<xsl:for-each select="program[not(@disabled='true') and @name!='CommandLineGATK']">
		 <xsl:sort select="@name" />
		 <xsl:apply-templates select="." mode="new.instance"/>
		</xsl:for-each>
		}
	@Override
	protected void savePreferences()
		{
		<xsl:apply-templates select="program[not(@disabled='true')]" mode="prefs"/>
		super.savePreferences();
		}
	
	@Override
	public CommandLineGATKPane getCommandLineGATKPane()
		{
		return this.<xsl:value-of select="concat('_instance',program[@name='CommandLineGATK']/@name)"/>;
		}

	
	<xsl:apply-templates select="program[not(@disabled='true')]"/>
	}

</xsl:template>

<xsl:template match="program" mode="new.instance">
		/** BEGIN <xsl:value-of select="@name"/> */
		final int <xsl:value-of select="generate-id(.)"/>idx=  tabbedPane.getTabCount();
		
		tabbedPane.addTab("<xsl:value-of select="@name"/>",
			(this.<xsl:value-of select="concat('_instance',@name)"/> = new <xsl:value-of select="concat(@name,'Pane')"/>(this))
			);
		action  = new AbstractAction("<xsl:value-of select="@name"/>")
			{
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent evt)
				{
				tabbedPane.setSelectedIndex(<xsl:value-of select="generate-id(.)"/>idx);
				}
			};
		action.putValue(AbstractAction.SHORT_DESCRIPTION,this.<xsl:value-of select="concat('_instance',@name)"/>.getDescription());		
		this.engineMenu.add(new JMenuItem(action));
		/** END <xsl:value-of select="@name"/> */
		
</xsl:template>

<xsl:template match="program" mode="declare">
protected <xsl:value-of select="concat(@name,'Pane _instance',@name)"/>;
</xsl:template>

<xsl:template match="program" mode="prefs">
this.<xsl:value-of select="concat('_instance',@name)"/>.savePreferences();
</xsl:template>

<xsl:template match="program" >
<xsl:variable name="filename" select="concat($outdir,'/',@name,'Pane.java')"/>
<xsl:document href="{$filename}" method="text">
<xsl:call-template name="license"/>
package com.github.lindenb.gatkui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.annotation.Generated;
import java.io.*;
import java.util.*;
import com.github.lindenb.gatkui.swing.*;

@Generated("xslt")
@SuppressWarnings("serial")
/** class Pane for command "<xsl:value-of select="@name"/>" */
public class <xsl:value-of select="concat(@name,'Pane')"/> extends AbstractGatkUi.AbstracCommandPane
	{
	<xsl:apply-templates select="options/option" mode="declare"/>
	<xsl:value-of select="concat(@name,'Pane')"/>(AbstractGatkUi owner)
		{
		super(owner);
		this.setBorder(new javax.swing.border.EmptyBorder(5,5,5,5));
	
		JPanel top = new JPanel(new BorderLayout(5,5));
		top.setBorder(new javax.swing.border.EmptyBorder(5,5,5,5));
		top.setFont(new Font("Dialog",Font.BOLD,18));


		
		JLabel lbl = new JLabel(getDescription());
		lbl.setFont(new Font("Dialog",Font.BOLD,18));
		top.add(lbl,BorderLayout.CENTER);
		
		/** URL button */
		if(getOnlineUrl()!=null &amp;&amp; java.awt.Desktop.isDesktopSupported())
			{
			final AbstractAction wwwAction = new AbstractAction("WWW")
				{
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
						 try {
						 	 java.awt.Desktop.getDesktop().browse(new java.net.URI(getOnlineUrl()));
						 	 }
						 catch(Exception err)
						 	{
						 	
						 	}
					}
				};
			wwwAction.putValue(AbstractAction.SHORT_DESCRIPTION,"Open GATK Documentation for this tool.");
			final JButton www = new JButton(wwwAction);
			top.add(www,BorderLayout.EAST);
			}
		/** end URL button */
		this.add(top,BorderLayout.NORTH);
		
		JPanel pane = new JPanel(new MyLayout());
		pane.setBorder(new javax.swing.border.EmptyBorder(15, 15, 15, 15));

		<xsl:apply-templates select="options/option[@required='true']" />
		pane.add(new javax.swing.JSeparator(javax.swing. SwingConstants.HORIZONTAL));
		<xsl:apply-templates select="options/option[not(@required='true')]" />
		
		JScrollPane scroll = new JScrollPane(pane);
		this.add(scroll,BorderLayout.CENTER);
		}
	
	<xsl:if test="@url">
	@Override
	public String getOnlineUrl()
			{
			return "<xsl:value-of select="@url"/>";
			}
	</xsl:if>
	
	<xsl:if test="@requires-faidx">
	@Override
	public boolean requiresIndexedReference()
			{
			return <xsl:value-of select="@requires-faidx"/>;
			}
	</xsl:if>
	
	<xsl:if test="@requires-pedigree">
	@Override
	public boolean requiresPedigree()
			{
			return <xsl:value-of select="@requires-pedigree"/>;
			}
	</xsl:if>
	
	<xsl:if test="@requires-region">
	@Override
	public boolean requiresRegion()
			{
			return <xsl:value-of select="@requires-region"/>;
			}
	</xsl:if>	
	
	@Override
	public String getDescription()
		{
		return  "<xsl:apply-templates select="description"/>";
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
	
	<xsl:if test="@require-interval='true'">
	@Override
	public boolean isNonEmptyIntervalRequired()
		{
		return true;
		}
	</xsl:if>
		
	}
</xsl:document>
</xsl:template>

<xsl:template match="option" mode="declare">
	<xsl:variable name="s" select="translate(@label,'-_ ','')"/>
	<xsl:variable name="classname">
	<xsl:choose>
		<xsl:when test="@type='input-files'">MultipleFileChooser</xsl:when>
		<xsl:when test="@type='input-file'">InputFileChooser</xsl:when>
		<xsl:when test="@type='output-file'">OutputFileChooser</xsl:when>
		<xsl:when test="@type='boolean'">JCheckBox</xsl:when>
		<xsl:when test="@type='enum' ">JComboBox&lt;String&gt;</xsl:when>
		<xsl:when test="@type='int-list' or @type='string-list' or @type='double-list' ">MultipleStringChooser</xsl:when>
		<xsl:when test="@type='int' or @type='double' or @type='long'">JTextField</xsl:when>
		<xsl:when test="@type='string'">JTextComponent</xsl:when>
		<xsl:when test="@type='enum-set'">EnumSetChooser&lt;<xsl:value-of select="@enum-class"/>&gt;</xsl:when>
		<xsl:when test="@type='strings-or-files'">StringsOrFilesChooser</xsl:when>
		<xsl:when test="@type='rod-files'">MultipleRODChooser</xsl:when>
		<xsl:otherwise>
			<xsl:message terminate='yes'>option:declare unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
	</xsl:variable>
	
	private <xsl:value-of select="$classname"/><xsl:text> </xsl:text><xsl:value-of select="generate-id(.)"/>= null ;
	
	/** getter for <xsl:value-of select="@label"/> */
	public <xsl:value-of select="$classname"/> get<xsl:value-of select="translate(substring($s,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:value-of select="substring($s,2)"/>Option()
		{
		return this.<xsl:value-of select="generate-id(.)"/>;
		}
	
</xsl:template>

<xsl:template match="option">

/* BEGIN <xsl:value-of select="@opt"/> */
{
	JLabel label = <xsl:choose>
			<xsl:when test="@type='boolean'">new JLabel("");</xsl:when>
			<xsl:otherwise>new JLabel(makeLabel("<xsl:value-of select="@label"/>")+" :",JLabel.TRAILING);</xsl:otherwise>
		</xsl:choose>
	<xsl:if test="@required='true'">
	label.setForeground(Color.RED);
	</xsl:if>
	label.setToolTipText("<xsl:apply-templates select="description"/>");
	pane.add(label);
	
	<xsl:choose>
		<xsl:when test="@type='input-files'">
			this.<xsl:value-of select="generate-id(.)"/> = new MultipleFileChooser();
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);			
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='input-file'">
			this.<xsl:value-of select="generate-id(.)"/> = new InputFileChooser();
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='output-file'">
			this.<xsl:value-of select="generate-id(.)"/> = new OutputFileChooser();
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='boolean'">
			<xsl:value-of select="generate-id(.)"/> = new JCheckBox(makeLabel("<xsl:value-of select="@label"/>"));
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
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
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		<xsl:when test="@type='enum'">
			Vector&lt;String&gt; values = new Vector&lt;String&gt;();
			<xsl:if test="@required='false'">
			values.add("");
			</xsl:if>
			<xsl:choose>
				<xsl:when  test="@enum-class">
				for(<xsl:value-of select="@enum-class"/> E : <xsl:value-of select="@enum-class"/>.values())
					{
					values.add(E.name());
					}
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="enum/item">
						values.add("<xsl:value-of select="@value"/>");
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			DefaultComboBoxModel&lt;String&gt; model = new DefaultComboBoxModel&lt;String&gt;(values);
				
			<xsl:value-of select="generate-id(.)"/> = new JComboBox&lt;String&gt;(model)<xsl:if  test="not(@enum-class)">
				{
				@Override
				public String getToolTipText()
						{
						Object s=getSelectedItem();
						if(s==null) return null;
						<xsl:for-each select="enum/item">
						if(s.equals("<xsl:value-of select="@value"/>"))
							{
							return "<xsl:apply-templates select="description"/>";
							}
						</xsl:for-each>
						return null;
						}
				}</xsl:if>;
			
			<xsl:choose>
				<xsl:when  test="@enum-class and @default">
				<xsl:value-of select="generate-id(.)"/>.setSelectedItem(<xsl:value-of select="@enum-class"/>.<xsl:value-of select="@default"/>.name());
				</xsl:when>
				<xsl:when test="@required='false'">
				<xsl:value-of select="generate-id(.)"/>.setSelectedItem("");
				</xsl:when>
			</xsl:choose>
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>,
				<xsl:apply-templates select="." mode="prefs.key"/>);
			
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		
		
		
		<xsl:when test="@type='int' or @type='double' or @type='long'">
			this.<xsl:value-of select="generate-id(.)"/> = new JTextField("");
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			<xsl:if test="@default">
			this.<xsl:value-of select="generate-id(.)"/>.setText("<xsl:value-of select="@default"/>");
			</xsl:if>
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
			
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		
		<xsl:when test="@type='enum-set'">
			this.<xsl:value-of select="generate-id(.)"/> = new EnumSetChooser&lt;<xsl:value-of select="@enum-class"/>&gt;(<xsl:value-of select="@enum-class"/>.class);
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
		</xsl:when>
		
		<xsl:when test="@type='int-list' or @type='string-list' or @type='double-list'">
			this.<xsl:value-of select="generate-id(.)"/> = new MultipleStringChooser()
			<xsl:choose>
			<xsl:when test="@type='int-list'">
				{
				@Override
				public boolean acceptString(String s)
					{
					if(s==null) return false;
					s=s.trim();
					if(s.isEmpty()) return false;
					try
						{
						Integer.parseInt(s);
						}
					catch(Exception err)
						{
						LOG.info("Not a valid integer "+s);
						return false;
						}
					return true;
					}
				}</xsl:when>
				<xsl:when test="@type='double-list'">
				{
				@Override
				public boolean acceptString(String s)
					{
					if(s==null) return false;
					s=s.trim();
					if(s.isEmpty()) return false;
					try
						{
						Double.parseDouble(s);
						}
					catch(Exception err)
						{
						LOG.info("Not a valid double "+s);
						return false;
						}
					return true;
					}
				}</xsl:when>
			</xsl:choose>;
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		
		<xsl:when test="@type='string'">
			<xsl:choose>
				<xsl:when test="@test='multiline'">
				this.<xsl:value-of select="generate-id(.)"/> = new JTextArea(5,40);
				JScrollPane scroll = new JScrollPane(this.<xsl:value-of select="generate-id(.)"/>);
				pane.add(scroll);
				</xsl:when>
				<xsl:otherwise>
				this.<xsl:value-of select="generate-id(.)"/> = new JTextField("");
				pane.add(this.<xsl:value-of select="generate-id(.)"/>);
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="generate-id(.)"/>.setToolTipText("<xsl:apply-templates select="description"/>");
			<xsl:if test="@default">
			this.<xsl:value-of select="generate-id(.)"/>.setText("<xsl:value-of select="@default"/>");
			</xsl:if>
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);			
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		
		<xsl:when test="@type='strings-or-files'">
			this.<xsl:value-of select="generate-id(.)"/> = new StringsOrFilesChooser();
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		
		<xsl:when test="@type='rod-files'">
			this.<xsl:value-of select="generate-id(.)"/> = new MultipleRODChooser();
			label.setLabelFor(<xsl:value-of select="generate-id(.)"/>);
			owner.loadPreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
			pane.add(this.<xsl:value-of select="generate-id(.)"/>);
			<xsl:apply-templates select="filter"/>
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:message terminate='yes'>option unknow type <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
}
/* END <xsl:value-of select="@label"/> */

</xsl:template>


<xsl:template match="option" mode="build.cmd">
	<xsl:choose>
		<xsl:when test="@type='strings-or-files'">
			for(final String f: this.<xsl:value-of select="generate-id(.)"/>.getStrings())
				{
				if(f.isEmpty()) continue;
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(f);
				}
		</xsl:when>
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
			if(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem()!=null &amp;&amp;
				!this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem().toString().trim().isEmpty()
				)
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem().toString());
				}
		</xsl:when>
		
		<xsl:when test="@type='int-list' or @type='string-list'  or @type='double-list' or @type='enum-set'">
			for(final String s:this.<xsl:value-of select="generate-id(.)"/>.getStrings())
				{
				if(s.isEmpty()) continue;
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(s);
				}
		</xsl:when>
		
		<xsl:when test="@type='int' or @type='double' or @type='long'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
				}
		</xsl:when>
		
		<xsl:when test="@type='string'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				command.add("-<xsl:value-of select="@opt"/>");
				command.add(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
				}
		</xsl:when>
		
		
		<xsl:when test="@type='rod-files'">
			for(final com.github.lindenb.gatkui.RodFile s:this.<xsl:value-of select="generate-id(.)"/>.getFiles())
				{
				if(s.getFile()==null) continue;
				command.add("-<xsl:value-of select="@opt"/>"+(s.getPrefix().isEmpty()?"":":"+s.getPrefix()));
				command.add(s.getFile().getPath());
				}
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:message terminate='yes'>option:build.cmd unknow '<xsl:value-of select="@type"/>'</xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="option" mode="can.build">
	<xsl:choose>
		<xsl:when test="@type='rod-files'">
			<xsl:if test="@required='true'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getFiles().isEmpty())
					{
					return "<xsl:value-of select="@label"/> cannot be empty";
					}
			</xsl:if>
		</xsl:when>
		
		
		<xsl:when test="@type='strings-or-files'">
			<xsl:if test="@required='true'">
				if(this.<xsl:value-of select="generate-id(.)"/>.getStrings().isEmpty())
					{
					return "<xsl:value-of select="@label"/> cannot be empty";
					}
			</xsl:if>
		</xsl:when>
	
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
				<xsl:if test="@type='input-file'">
				else if(!this.<xsl:value-of select="generate-id(.)"/>.getFile().exists())
					{
					return "<xsl:value-of select="@label"/>: file doesn't exists "+ this.<xsl:value-of select="generate-id(.)"/>.getFile();
					}
				</xsl:if>
				<xsl:if test="@type='output-file' and count(extension)&gt;0">
				else
					{
					boolean ok=false;
					<xsl:for-each select="extension">
					if(this.<xsl:value-of select="generate-id(..)"/>.getFile().getName().endsWith(".<xsl:value-of select="text()"/>"))
						{
						ok=true;
						}
					</xsl:for-each>
					if(!ok)
						{
						return "<xsl:value-of select="@label"/> not a valid extension:<xsl:for-each select="extension"> .<xsl:value-of select="text()"/></xsl:for-each>";
						}
					}
				</xsl:if>
			</xsl:if>
		</xsl:when>
		<xsl:when test="@type='boolean'">
			
		</xsl:when>
		
		<xsl:when test="@type='enum'">
			<xsl:if test="@required='true'">
			if(this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem()==null ||
			   this.<xsl:value-of select="generate-id(.)"/>.getSelectedItem().toString().trim().isEmpty())
				{
				return "<xsl:value-of select="@label"/> cannot be empty";
				}
			</xsl:if>
		</xsl:when>
		
		<xsl:when test="@type='int-list' or @type='string-list' or @type='double-list'  or @type='enum-set'">
			<xsl:if test="@required='true'">
			if(this.<xsl:value-of select="generate-id(.)"/>.getStrings().isEmpty())
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

		<xsl:when test="@type='long'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				try
					{
					long v = Long.parseLong(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
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

		<xsl:when test="@type='double'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				try
					{
					double v = Double.parseDouble(this.<xsl:value-of select="generate-id(.)"/>.getText().trim());
					<xsl:if test="@min">
					if(v&lt;<xsl:value-of select="@min"/>)
						{
						return "<xsl:value-of select="@label"/>: should be greater or equal to <xsl:value-of select="@min"/>";
						}
					</xsl:if>
					<xsl:if test="@max">
					if(v&gt;<xsl:value-of select="@max"/>)
						{
						return "<xsl:value-of select="@label"/>: should be lower or equal to <xsl:value-of select="@max"/>";
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

		<xsl:when test="@type='string'">
			<xsl:if test="@required='true'">
			if(!this.<xsl:value-of select="generate-id(.)"/>.getText().trim().isEmpty())
				{
				return "<xsl:value-of select="@label"/> cannot be empty";
				}
			</xsl:if>
		</xsl:when>		
		
		<xsl:otherwise>
			<xsl:message terminate='yes'>option:canbuild unknow <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="option" mode="save.prefs">
	<xsl:choose>
		<xsl:when test="@type='strings-or-files'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='input-files'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='input-file' or @type='output-file'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='int' or @type='double' or @type='long'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>		
		<xsl:when test="@type='boolean'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='string'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='enum'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>
		<xsl:when test="@type='rod-files'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>	
		<xsl:when test="@type='int-list' or @type='string-list' or @type='enum-set' or @type='double-list'">
			owner.savePreference(this.<xsl:value-of select="generate-id(.)"/>, <xsl:apply-templates select="." mode="prefs.key"/>);
		</xsl:when>	
		<xsl:otherwise>
			<xsl:message terminate='yes'>save.prefs unknow <xsl:value-of select="@type"/></xsl:message>
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

<xsl:template match="description">
<xsl:value-of select="translate(translate(text(),'&quot;',''),'&#13;&#10;','  ')"/>
</xsl:template>

<xsl:template name="license">
/*
The MIT License (MIT)

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


*/
</xsl:template>


</xsl:stylesheet>

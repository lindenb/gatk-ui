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
package com.github.lindenb.gatkui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.broadinstitute.gatk.utils.commandline.CommandLineProgram;
import org.broadinstitute.gatk.utils.commandline.CommandLineUtils;

import com.github.lindenb.gatkui.swing.AbstractFileChooser;
import com.github.lindenb.gatkui.swing.EnumSetChooser;
import com.github.lindenb.gatkui.swing.MultipleFileChooser;
import com.github.lindenb.gatkui.swing.MultipleStringChooser;
import com.github.lindenb.gatkui.swing.StringsOrFilesChooser;

@SuppressWarnings("serial")
public abstract class AbstractGatkUi extends JFrame
	{
	protected Thread runningThread = null;
	@SuppressWarnings("unused")
	private static Class<?> _force_static = CommandLineProgram.class ;
	protected static final Logger LOG = CommandLineUtils.getStingLogger();
	protected Preferences _preferences = null;
	private JTextComponent logArea;
	//protected InputFileChooser REFFileChooser;
	//protected InputFileChooser captureFileOrRegionChooser;
	//protected InputFileChooser pedigreeFileChooser;
	private JTabbedPane tabbedPane=null;
	JMenu engineMenu;
	
	private static class SwingAppender extends AppenderSkeleton
		{
		private JTextComponent tc;
		private SwingAppender(JTextComponent tc)
			{
			this.tc=tc;
			setLayout(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
			}
		@Override
		protected void append(final LoggingEvent e) {
			try {
				SwingUtilities.invokeLater(new Runnable()
					{
					@Override
					public void run() {
					String logString = getLayout().format(e);
					if(e.getThrowableInformation()!=null)
						{	
						ThrowableInformation ti = e.getThrowableInformation();
						if(ti.getThrowable()!=null)
							{
							logString+=""+ti.getThrowable().getMessage()+"\n";
							}
						}	

					if(!tc.isVisible())
						{
						System.err.println(logString);
						return;
						}
					Document doc = tc.getDocument();	
					try
						{
						int L = doc.getLength();
						if(L>10000) doc.remove(0, L);
						L = doc.getLength();
						doc.insertString(doc.getLength(),logString+"\n", null );
						}
					catch(Exception err)
						{
						err.printStackTrace();
						}
					}
				});
			} catch (Exception e2) {
				System.err.println("Cannot log");
				}
			}

		@Override
		public void close() {
			
		}
		
		@Override
		public boolean requiresLayout() {
			return true;
			}
		}
	
	protected AbstractGatkUi()
		{
		super("GATK-UI : "+GATKVersion.getVersion());
		
		LOG.debug("building ui");
		this._preferences = Preferences.userNodeForPackage(AbstractGatkUi.class);
		
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent e) {
				doMenuClose();
				}
			});
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowOpened(WindowEvent e) {
				LOG.info("GATK-ui: Pierre Lindenbaum 2015\n"
						+ "Version: "+GATKVersion.getVersion()+"\n"
						+ "GATK is a product from the Broad Institute."
						);
				removeWindowListener(this);
				}
			});
		 final JMenuBar menubar= new JMenuBar();
		 this.setJMenuBar(menubar);
		 JMenu menu = new JMenu("File");
		 menubar.add(menu);
		 menu.add(new AbstractAction("Quit")
		 	{
			@Override
			public void actionPerformed(ActionEvent e) {
				doMenuClose();
				}
		 	});
		 this.engineMenu = new JMenu("Engines");
		 menubar.add(this.engineMenu);
		 
		 final JPanel contentPane=new JPanel(new BorderLayout(5,5));
		 setContentPane(contentPane);
		 this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		 this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		 this.tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		 contentPane.add(this.tabbedPane,BorderLayout.CENTER);
		 buildTabbedPane(this.tabbedPane);
		 
		 JPanel logPane= new JPanel(new BorderLayout());
		 contentPane.add(logPane,BorderLayout.SOUTH);
		 logPane.setBorder(new TitledBorder("Log"));
		 this.logArea = new JTextArea(10,100);
		 this.logArea.setEditable(false);
		 this.logArea.setFont(new Font("Courier", Font.PLAIN, 9));
		
		 JScrollPane scroll = new JScrollPane(logArea);
		 logPane.add(scroll,BorderLayout.CENTER);
		 
			final SwingAppender appender = new SwingAppender(this.logArea);
			/*
			 for (	Logger logger = LOG;
					 logger != null;
					 logger = (Logger)logger.getParent())
			 	{
				logger.addAppender(appender);
				//break;
			 	}*/
		Logger.getRootLogger().addAppender(appender);
		/* 
		PrintStream redirectStream=new PrintStream(new OutputStream()
			{
			@Override
			public void write(final int b) throws IOException {
				if(b==-1) return;

				try {
					SwingUtilities.invokeLater(new Runnable()
						{
						@Override
						public void run() {
						
						Document doc = logArea.getDocument();	
						try
							{
							int L = doc.getLength();
							if(L>10000) doc.remove(0, L);
							L = doc.getLength();
							doc.insertString(doc.getLength(),String.valueOf((char)b), null );
							}
						catch(Exception err)
							{
							}
						}
					});
				} catch (Exception e2) {
					}
				}
			});
		System.setErr(redirectStream);
		System.setOut(redirectStream);
		*/
		}
	
	protected void buildTabbedPane(JTabbedPane tabbedPane)
		{
		//tabbedPane.addTab("GATK", builGatkTab());
		}
	
	//private static final String GATK_REF_PATH_PREF="gatk.ref.path";
	//private static final String GATK_REGFILE_PREF="gatk.regfile";
	//private static final String GATK_PED_PREF="gatk.ped";
	
	/*
	public JPanel builGatkTab()
		{
		JPanel pane2 = new JPanel(new MyLayout());
		JLabel label = new JLabel("Reference:",JLabel.TRAILING);
		label.setForeground(Color.RED);
		pane2.add(label);
		
		this.REFFileChooser  = new InputFileChooser();
		this.REFFileChooser.setFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public String getDescription() {
				return "REFERENCE FILE";
			}
			
			@Override
			public boolean accept(final java.io.File f)
				{
				if(f.isDirectory()) return true;
				if(!(f.getName().toLowerCase().endsWith(".fa") ||
					 f.getName().toLowerCase().endsWith(".fasta") ))
					{
					return false;
					}
				return true;
				}
			});
		loadPreference(this.REFFileChooser, GATK_REF_PATH_PREF);
		pane2.add(this.REFFileChooser);
		
		JLabel lbl = new JLabel("Region BED:",JLabel.TRAILING);
		lbl.setToolTipText("BED region or One or more genomic intervals over which to operate");
		pane2.add(lbl);
				
		this.captureFileOrRegionChooser = new InputFileChooser()
			{
			public boolean isTextFieldEditable()
				{
				return true;
				}
			};
		this.captureFileOrRegionChooser.setFilter("Region", "bed");
		loadPreference(this.captureFileOrRegionChooser, GATK_REGFILE_PREF);
		pane2.add(this.captureFileOrRegionChooser);
		
		
		
		lbl=new JLabel("Pedigree:",JLabel.TRAILING);
		lbl.setToolTipText("Pedigree files for samples");
		pane2.add(lbl);
		this.pedigreeFileChooser=new InputFileChooser();
		this.pedigreeFileChooser.setFilter("Pedigree", "ped");
		pane2.add(this.pedigreeFileChooser);
		loadPreference(this.pedigreeFileChooser, GATK_PED_PREF);
		
		
		return pane2;
		}*/
	
	public abstract CommandLineGATKPane getCommandLineGATKPane();
	
	public void loadPreference(AbstractFileChooser component,String key)
		{
		String path=getPreferences().get(key,null);
		if(path!=null &&  !path.isEmpty()) component.setText(path);
		}
	
	public void savePreference(AbstractFileChooser component,String key)
		{
		if(component.getFile()==null)
			{
			getPreferences().remove(key);
			}
		else
			{
			getPreferences().put(key,component.getFile().getPath());
			}
		}
	
	
	
	public void loadPreference(MultipleFileChooser component,String key)
		{
		String path=getPreferences().get(key,null);
		if(path==null) return;
		for(String s:path.split("[ \n\t]+"))
			{
			if(s.isEmpty()) continue;
			component.addFiles(new File[]{new File(s)});
			}
	
		}

	public void savePreference(MultipleFileChooser component,String key)
		{
		if(component.getFiles().isEmpty())
			{
			getPreferences().remove(key);
			}
		else
			{
			StringBuilder sb=new StringBuilder();
			for(File f:component.getFiles())
				{
				if(sb.length()!=0) sb.append(" ");
				sb.append(f.getPath());
				}
			getPreferences().put(key,sb.toString());
			}
		}

	public void loadPreference(MultipleStringChooser component,String key)
		{
		String ss=getPreferences().get(key,null);
		if(ss==null) return;
		for(String s:ss.split("[ \n\t]+"))
			{
			if(s.isEmpty()) continue;
			if(!component.acceptString(s)) continue;
			component.addString(s);
			}
	
		}

	public void savePreference(MultipleStringChooser component,String key)
		{
		if(component.getStrings().isEmpty())
			{
			getPreferences().remove(key);
			}
		else
			{
			StringBuilder sb=new StringBuilder();
			for(String s:component.getStrings())
				{
				if(sb.length()!=0) sb.append(" ");
				sb.append(s);
				}
			getPreferences().put(key,sb.toString());
			}
		}

	 public <T extends Enum<T>> void  loadPreference(final EnumSetChooser<T> component,String key)
		{
		final String ss=getPreferences().get(key,null);
		if(ss==null) return;
		java.util.Set<String> sel = new HashSet<>();
		for(final String s:ss.split("[ \n\t]+"))
			{
			if(s.isEmpty()) continue;
			sel.add(s);			
			}
		component.setStrings(sel);
			
	
		}

	 public void savePreference(StringsOrFilesChooser component,String key)
		{
		if(component.getStrings().isEmpty())
			{
			getPreferences().remove(key);
			}
		else
			{
			StringBuilder sb=new StringBuilder();
			for(String s:component.getStrings())
				{
				if(sb.length()!=0) sb.append(" ");
				sb.append(s);
				}
			getPreferences().put(key,sb.toString());
			}
		}

	 public <T extends Enum<T>> void  loadPreference(final StringsOrFilesChooser component,String key)
		{
		final String ss=getPreferences().get(key,null);
		if(ss==null) return;
		for(final String s:ss.split("[ \n\t]+"))
			{
			if(s.isEmpty()) continue;
			component.addStrings(new String[]{s});		
			}			
		} 
	 
	 
public <T extends Enum<T>>  void savePreference(EnumSetChooser<T> component,String key)
	{
	if(component.getStrings().isEmpty())
		{
		getPreferences().remove(key);
		}
	else
		{
		StringBuilder sb=new StringBuilder();
		for(String s:component.getStrings())
			{
			if(sb.length()!=0) sb.append(" ");
			sb.append(s);
			}
		getPreferences().put(key,sb.toString());
		}
	}	
	
	
	public void loadPreference(JCheckBox component,String key)
		{
		component.setSelected(getPreferences().getBoolean(key,false));
		}
	
	public void savePreference(JCheckBox component,String key)
		{
		getPreferences().putBoolean(key,component.isSelected());
		}

	public void loadPreference(JTextComponent component,String key)
		{
		String v = getPreferences().get(key, null);
		if(v==null) return;
		component.setText(v);
		component.setCaretPosition(0);
		}
	
	public void savePreference(JTextComponent component,String key)
		{
		getPreferences().put(key,component.getText());
		}

	public void loadPreference(JComboBox<String> component,String key)
		{
		String v = getPreferences().get(key, null);
		if(v==null) return;
		component.setSelectedItem(v);
		}
	
	public void savePreference(JComboBox<String> component,String key)
		{
		if(component.getSelectedItem()==null) return;
		if(component.getSelectedItem().toString().trim().isEmpty()) return;
		getPreferences().put(key,component.getSelectedItem().toString().trim());
		}

	
	
	protected void savePreferences()
		{
		//savePreference(this.REFFileChooser, GATK_REF_PATH_PREF);
		//savePreference(this.pedigreeFileChooser, GATK_PED_PREF);
		//savePreference(this.captureFileOrRegionChooser, GATK_REGFILE_PREF);
		
		try {
			LOG.debug("flush pref");
			getPreferences().flush();
			} catch(Exception err){LOG.warn(err);err.printStackTrace();}
		try {
			LOG.debug("flush pref");
			getPreferences().sync();
			} catch(Exception err){LOG.warn(err);err.printStackTrace();}
		}
	
	private void doMenuClose()
		{
		LOG.info("exiting");
		this.setVisible(false);
		this.dispose();
		savePreferences();
		}
	
	public Preferences getPreferences()
		{
		return this._preferences;
		}
	
	/**
	 * AbstracCommandPane
	 *
	 */
	protected abstract static class AbstracCommandPane  extends JPanel
		{
		protected static final Logger LOG = AbstractGatkUi.LOG;
		JPanel bottomPane;
		AbstractAction runAction;
		AbstractAction cancelAction;
		AbstractGatkUi owner;
		AbstracCommandPane(AbstractGatkUi ownerui)
			{
			super(new BorderLayout());
			this.owner=ownerui;
			setBorder(new EmptyBorder(5, 5, 5, 5));
			this.bottomPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			if(!getCommandName().equals("CommandLineGATK"))
				{
				this.add(bottomPane,BorderLayout.SOUTH);
				}
			
			this.cancelAction = new AbstractAction("Cancel")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					if(owner.runningThread==null) return;
					try {
					owner.runningThread.interrupt();
						}
					catch (Exception e2) {
						}
					owner.runningThread=null;
					}
				};
			runAction = new AbstractAction(getCommandName())
				{
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(owner.runningThread!=null)
						{
						JOptionPane.showMessageDialog(AbstracCommandPane.this, "Command already running");
						return;
						}
					String msg = canBuildCommandLine();
					if(msg!=null)
						{
						JOptionPane.showMessageDialog(
								AbstracCommandPane.this,
								msg);
						return;
						}
					List<String> cmd = buildCommandLine();
					owner.runningThread = new GATKRunner(owner,cmd);
					owner.runningThread.start();
					}
				};
			Font font = new Font("Dialog", Font.BOLD, 18);
			JButton button = new JButton(cancelAction);
			button.setContentAreaFilled(true);
			button.setBackground(Color.ORANGE);
			button.setForeground(Color.WHITE);
			button.setFont(font);
			bottomPane.add(button);
			button = new JButton(runAction);
			button.setContentAreaFilled(true);
			button.setBackground(Color.GREEN);
			button.setForeground(Color.WHITE);
			button.setFont(font);
			bottomPane.add(button);
			}
		public abstract String getCommandName();
		public abstract String getDescription();
		
		/** get online  broad GATK url for this document */
		public String getOnlineUrl()
			{
			return null;
			}
		public abstract void savePreferences();
		
		public boolean requiresIndexedReference()
			{
			return true;
			}
		/** this command  requires a pedigree */
		public boolean requiresPedigree()
			{
			return false;
			}
		
		/** this command  requires a region -L ? */
		public boolean requiresRegion()
			{
			return false;
			}
	
		/** returns an error message or null */
		public String canBuildCommandLine()
			{
			/*
			File f = owner.REFFileChooser.getFile(); 
			if(f==null)
				{
				return "REF not defined";
				}
			if(this.requiresIndexedReference())
				{
				File fai =new File(f.getParentFile(), f.getName()+".fai");
				if(!fai.exists()) return "REF needs to be indexed but cannot find "+fai;
				}
			if(this.requiresPedigree() && owner.pedigreeFileChooser.getFile()==null)
				{
				return getCommandName()+" requires a pedigree. See tab 'GATK'";
				}
			if(this.requiresRegion() && 
				owner.captureFileOrRegionChooser.getText().trim().isEmpty())
				{
				return getCommandName()+" requires a region. See tab 'GATK'";				
				}
			*/
			
			if( isNonEmptyIntervalRequired() &&
				owner.getCommandLineGATKPane().getIntervalsOption().getStrings().isEmpty() )
				{
				return "The tool "+getCommandName()+" requires an interval defined in the CommandLineGATKPane tab.";	
				}
			return null;
			}
		
		
		public List<String> buildCommandLine()
			{
			List<String> L = new ArrayList<>();
			

			if( owner.getCommandLineGATKPane()!=this)
				{
				/** add command name */
				L.add("-T");
				L.add(getCommandName());

				L.addAll(this.owner.getCommandLineGATKPane().buildCommandLine());
				}
			/*
			if(owner.REFFileChooser.getFile()==null)
				{
				LOG.error("REF not defined");
				}
			else
				{
				L.add("-R");
				L.add(owner.REFFileChooser.getFile().getPath());
				}
			if(!owner.captureFileOrRegionChooser.getText().isEmpty())
				{
				L.add("-L");
				L.add(owner.captureFileOrRegionChooser.getText());
				}

			if(owner.pedigreeFileChooser.getFile()!=null)
				{
				L.add("-ped");
				L.add(owner.pedigreeFileChooser.getFile().getPath());
				}
				*/
			return L;
			}
		
		public String makeLabel(String s)
			{
			while(s.startsWith("-")) s=s.substring(1);
			for(int i=0;i+1<s.length();++i)
				{
				if( Character.isLetter(s.charAt(i)) &&
					Character.isLetter(s.charAt(i+1)) &&
					Character.isLowerCase(s.charAt(i)) &&
					Character.isUpperCase(s.charAt(i+1)))
					{
					return makeLabel(s.substring(0,i+1)+" "+
							Character.toLowerCase(s.charAt(i+1))
							+(i+2<s.length()?s.substring(i+2):"")
							);
					}
				}
			return s.replace('_', ' ');
			}
		
		/** tells if interval (-L ) shouldn't be empty */
		public boolean isNonEmptyIntervalRequired()
			{
			return false;
			}
		}
	
	
	/**
	 * GATKRunner
	 *
	 */
	private static class GATKRunner extends Thread
		{
		private AbstractGatkUi owner;
		private String args[];
		public GATKRunner(final AbstractGatkUi ui,final List<String> args)
			{
			this.owner=ui;
			this.args=args.toArray(new String[args.size()]);
			}
		@Override
		public void run()
			{
			LOG.info("starting "+Arrays.toString(args));
			org.broadinstitute.gatk.engine.CommandLineGATK instance= new org.broadinstitute.gatk.engine.CommandLineGATK();

			try
				{
				org.broadinstitute.gatk.engine.CommandLineGATK.start(instance, this.args);
				
				if(org.broadinstitute.gatk.engine.CommandLineGATK.result == 0)
					{
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								if(GATKRunner.this != owner.runningThread) return;
								
								JOptionPane.showMessageDialog(owner,
								new JScrollPane(new JTextArea("Completed:"+Arrays.toString(args),5,20)),
								"Completed",
								JOptionPane.INFORMATION_MESSAGE,
								null);
								owner.runningThread=null;
							}
						});
					} catch (Exception e) {
						LOG.warn(e);
						}
					}
				else
					{
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								if(GATKRunner.this != owner.runningThread) return;
								JOptionPane.showMessageDialog(owner,
								new JScrollPane(new JTextArea("Failure:"+Arrays.toString(args),5,20)),
								"Failure",
								JOptionPane.ERROR_MESSAGE,
								null);
								owner.runningThread=null;
								owner.runningThread=null;
							}
						});
					} catch (Exception e) {
						LOG.warn(e);
						
						}
					}
				}
			catch(final Exception err)
				{
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							if(GATKRunner.this != owner.runningThread) return;
							JOptionPane.showMessageDialog(owner,
								new JScrollPane(new JTextArea("Failure:"+Arrays.toString(args),5,20)),
								"Failure",
								JOptionPane.ERROR_MESSAGE,
								null);
							owner.runningThread=null;
						}
					});
				} catch (Exception e) {
					LOG.warn(e);
					
					}
				}
			}
		}
	
	
	}

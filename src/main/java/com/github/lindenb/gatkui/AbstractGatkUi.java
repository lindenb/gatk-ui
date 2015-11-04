package com.github.lindenb.gatkui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.broadinstitute.gatk.engine.CommandLineGATK;
import org.broadinstitute.gatk.utils.commandline.CommandLineProgram;
import org.broadinstitute.gatk.utils.commandline.CommandLineUtils;

public class AbstractGatkUi extends JFrame
	{
	private static Class<?> _force_static = CommandLineProgram.class ;
	protected static final Logger LOG = CommandLineUtils.getStingLogger();
	protected Preferences preferences = null;
	private JTextComponent logArea;
	protected InputFileChooser REFFileChooser;
	
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
		super("GATK-UI");
		
		LOG.debug("building ui");
		this.preferences = Preferences.userNodeForPackage(AbstractGatkUi.class);
		
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent e) {
				doMenuClose();
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
		 final JPanel contentPane=new JPanel(new BorderLayout(5,5));
		 setContentPane(contentPane);
		 final JTabbedPane tabbedPane = new JTabbedPane();
		 tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		 contentPane.add(tabbedPane,BorderLayout.CENTER);
		 buildTabbedPane(tabbedPane);
		 
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
		}
	
	protected void buildTabbedPane(JTabbedPane tabbedPane)
		{
		tabbedPane.addTab("REF", buildRefTab());
		tabbedPane.addTab("REGION", buildRegionTab());
		}
	
	private static final String GATK_REF_PATH_PREF="gatk.ref.path";
	
	public JPanel buildRefTab()
		{
		JPanel pane = new JPanel(new BorderLayout());
		pane.setBorder(new TitledBorder("Reference"));
		JPanel pane2 = new JPanel(new FlowLayout());
		this.REFFileChooser  = new InputFileChooser("REF");
		this.REFFileChooser.setFilter("FASTA","fa","fasta");
		String path=this.preferences.get(GATK_REF_PATH_PREF,null);
		if(path!=null) this.REFFileChooser.setFile(new File(path));
		pane2.add(this.REFFileChooser);
		pane2.add(new JButton(new AbstractAction("GATK")
			{
			
			@Override
			public void actionPerformed(ActionEvent e)
				{
				try
					{
					org.broadinstitute.gatk.engine.CommandLineGATK instance= new org.broadinstitute.gatk.engine.CommandLineGATK();
					org.broadinstitute.gatk.engine.CommandLineGATK.start(instance, new String[0]);
					}
				catch(Exception err)
					{
					LOG.error("gatk",err);
					
					}
				}
			}));
		pane.add(pane2,BorderLayout.CENTER);
		return pane;
		}
	
	
	//private static final String GATK_REF_PATH_PREF="gatk.ref.path";
	
	public JPanel buildRegionTab()
		{
		JPanel pane = new JPanel();
		pane.setBorder(new TitledBorder("Regions"));
		
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		InputFileChooser bedChooser = new InputFileChooser("BED");
		bedChooser.setFilter("Region", "bed");
		pane.add(bedChooser);
		pane.add(new JLabel("or...."));
		
		JPanel pane2= new JPanel(new FlowLayout(FlowLayout.LEADING));
		pane.add(pane2);
		JLabel lbl =new JLabel("Region:");
		pane2.add(lbl);
		JTextField tf=new JTextField(50);
		lbl.setLabelFor(tf);
		pane2.add(tf);
		
		return pane;
		}
	
	protected void savePreferences()
		{
		if(this.REFFileChooser.getFile()==null)
			{
			this.preferences.remove(GATK_REF_PATH_PREF);
			}
		else
			{
			this.preferences.put(
					GATK_REF_PATH_PREF,
					this.REFFileChooser.getFile().getPath()
					);
			}
		try {
			LOG.debug("flush pref");
			this.preferences.flush();
			} catch(Exception err){LOG.warn(err);}
		}
	
	private void doMenuClose()
		{
		LOG.info("exiting");
		this.setVisible(false);
		this.dispose();
		savePreferences();
		}
	
	
	private abstract class AbstractTab  extends JPanel
		{
		AbstractTab()
			{
			super(new BorderLayout(5,5));
			this.setBorder(new TitledBorder(getLabel()));
			}
		public abstract String getLabel();
		public abstract void loadPrefs(Preferences prefs);
		public abstract void savePrefs(Preferences prefs);
		}
	
	
	
	
	private abstract class AbstractFileChooser extends JPanel
		{
		private JTextField textField;
		private File file;
		private FileFilter filter=null;
		AbstractFileChooser(String label)
			{
			super(new BorderLayout(5,5));
			JLabel lbl=new JLabel(label);
			this.add(lbl,BorderLayout.WEST);
			this.textField = new JTextField(50);
			this.textField.setEditable(false);
			lbl.setLabelFor(this.textField);
			this.add(this.textField,BorderLayout.CENTER);
			JPanel p = new JPanel(new FlowLayout());
			this.add(p,BorderLayout.EAST);
			p.add(new JButton(new AbstractAction("Set...")
					{
					@Override
					public void actionPerformed(ActionEvent e)
						{
						File dir=(file!=null?file.getParentFile():null);
						JFileChooser chooser = new JFileChooser(dir);
						if(filter!=null) chooser.setFileFilter(filter);
						if(select(chooser)!=JFileChooser.APPROVE_OPTION) return;
						if(chooser.getSelectedFile()==null) return;
						setFile(chooser.getSelectedFile());
						}
					}));
			p.add(new JButton(new AbstractAction("Clear")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					setFile(null);
				}
				}));
			}
		public void setFilter(final String description,final String...extensions)
			{
			this.filter=new FileFilter() {
				
				@Override
				public String getDescription() {
					return description;
				}
				
				@Override
				public boolean accept(File f)
					{
					if(f.isDirectory()) return true;
					if(!f.isFile()) return false;
					for(String ext:extensions)
						{
						if(f.getName().toLowerCase().endsWith(("."+ext).toLowerCase())) return true;
						}
					return false;
					}
				};
			}
		protected abstract int select(final JFileChooser c);
		
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
			if(file==null)
				{
				this.textField.setText("");	
				this.textField.setToolTipText("");
				}
			else
				{
				this.textField.setText(file.getPath());
				this.textField.setToolTipText(file.getPath());
				this.textField.setCaretPosition(0);
				}
			}
		}
	private class InputFileChooser extends AbstractFileChooser
		{
		InputFileChooser(String name)
			{
			super(name);
			}
		@Override
		protected int select(JFileChooser c) {
			return c.showOpenDialog(this);
			}
		}

	}

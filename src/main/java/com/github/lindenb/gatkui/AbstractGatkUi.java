package com.github.lindenb.gatkui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.broadinstitute.gatk.utils.commandline.CommandLineProgram;
import org.broadinstitute.gatk.utils.commandline.CommandLineUtils;

@SuppressWarnings("serial")
public class AbstractGatkUi extends JFrame
	{
	protected Thread runningThread = null;
	@SuppressWarnings("unused")
	private static Class<?> _force_static = CommandLineProgram.class ;
	protected static final Logger LOG = CommandLineUtils.getStingLogger();
	protected Preferences _preferences = null;
	private JTextComponent logArea;
	protected InputFileChooser REFFileChooser;
	protected InputFileChooser captureFileChooser;
	protected InputFileChooser pedigreeFileChooser;
	private JTextField captureRegionField;
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
		super("GATK-UI");
		
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
		 this.tabbedPane = new JTabbedPane();
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
		}
	
	protected void buildTabbedPane(JTabbedPane tabbedPane)
		{
		tabbedPane.addTab("GATK", builGatkTab());
		}
	
	private static final String GATK_REF_PATH_PREF="gatk.ref.path";
	private static final String GATK_REGFILE_PREF="gatk.regfile";
	private static final String GATK_REG_PREF="gatk.reg";
	private static final String GATK_PED_PREF="gatk.ped";
	
	
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
				File fai =new File(f.getParentFile(), f.getName()+".fai");
				if(fai.exists()) return true;
				return false;
				}
			});
		loadPreference(this.REFFileChooser, GATK_REF_PATH_PREF);
		pane2.add(this.REFFileChooser);
		
		JLabel lbl = new JLabel("Region BED:",JLabel.TRAILING);
		lbl.setToolTipText(" One or more genomic intervals over which to operate");
		pane2.add(lbl);
				
		this.captureFileChooser = new InputFileChooser();
		this.captureFileChooser.setFilter("Region", "bed");
		loadPreference(this.captureFileChooser, GATK_REGFILE_PREF);
		pane2.add(this.captureFileChooser);
		
		
		pane2.add(new JLabel("or ...",JLabel.CENTER));
		pane2.add(new JLabel());
		
		lbl =new JLabel("Region:",JLabel.TRAILING);
		lbl.setToolTipText(" One or more genomic intervals over which to operate");
		pane2.add(lbl);
		this.captureRegionField=new JTextField(50);
		loadPreference(this.captureFileChooser, GATK_REG_PREF);
		lbl.setLabelFor(this.captureRegionField);
		pane2.add(this.captureRegionField);
		
		
		lbl=new JLabel("Pedigree:",JLabel.TRAILING);
		lbl.setToolTipText("Pedigree files for samples");
		pane2.add(lbl);
		this.pedigreeFileChooser=new InputFileChooser();
		this.pedigreeFileChooser.setFilter("Pedigree", "ped");
		pane2.add(this.pedigreeFileChooser);
		loadPreference(this.pedigreeFileChooser, GATK_PED_PREF);
		
		
		return pane2;
		}
	
	public void loadPreference(AbstractFileChooser component,String key)
		{
		String path=getPreferences().get(key,null);
		if(path!=null) component.setFile(new File(path));
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
		for(String s:path.split(""))
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
		savePreference(this.REFFileChooser, GATK_REF_PATH_PREF);
		savePreference(this.pedigreeFileChooser, GATK_PED_PREF);
		savePreference(this.captureFileChooser, GATK_REGFILE_PREF);
		savePreference(this.captureRegionField, GATK_REG_PREF);
		
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
	
	protected abstract static class AbstracCommandPane  extends JPanel
		{
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
			this.add(bottomPane,BorderLayout.SOUTH);
			cancelAction = new AbstractAction("Cancel")
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
		public abstract void savePreferences();
		
		public String canBuildCommandLine()
			{
			if(owner.REFFileChooser.getFile()==null)
				{
				return "REF not defined";
				}
			return null;
			}
		
		public List<String> buildCommandLine()
			{
			List<String> L = new ArrayList<>();
			L.add("-T");
			L.add(getCommandName());
			if(owner.REFFileChooser.getFile()==null)
				{
				LOG.error("REF not defined");
				}
			else
				{
				L.add("-R");
				L.add(owner.REFFileChooser.getFile().getPath());
				}
			if(owner.captureFileChooser.getFile()!=null)
				{
				L.add("-L");
				L.add(owner.captureFileChooser.getFile().getPath());
				}
			else if(!owner.captureRegionField.getText().trim().isEmpty())
				{
				L.add("-L");
				L.add(owner.captureRegionField.getText().trim());
				}
			return L;
			}
		}
	
	
	
	static abstract class AbstractFilterChooser extends JPanel
		{
		private FileFilter filter=null;
		
		public AbstractFilterChooser() {
			super(new BorderLayout(5,5));
			}
		public void setFilter(FileFilter filter)
			{
			this.filter=filter;
			}
		public FileFilter getFilter() {
			return filter;
			}
		public void setFilter(final String description,final String...extensions)
			{
			setFilter(new FileFilter() {
				
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
				});
			}
		}
	
	static abstract class AbstractFileChooser extends AbstractFilterChooser
		{
		private JTextField textField;
		private File file;
		AbstractFileChooser()
			{
			this.textField = new JTextField(50);
			this.textField.setEditable(false);
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
						if(getFilter()!=null) chooser.setFileFilter(getFilter());
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
	static class InputFileChooser extends AbstractFileChooser
		{
		@Override
		protected int select(JFileChooser c) {
			return c.showOpenDialog(this);
			}
		}
	
	static class OutputFileChooser extends AbstractFileChooser
		{
		@Override
		protected int select(JFileChooser c) {
			int r= c.showOpenDialog(this);
			if( r!=JFileChooser.APPROVE_OPTION) return r;
			File f= c.getSelectedFile();
			if(f.exists() && JOptionPane.showConfirmDialog(this, f.getName()+" exist. Overwrite?", "File exists", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null)!=JOptionPane.OK_OPTION)
				{
				return JFileChooser.CANCEL_OPTION;
				}
			return JFileChooser.APPROVE_OPTION;
			}
		}


	static class MultipleFileChooser extends AbstractFilterChooser
		{
		private JList<File> fileList;
		private AbstractAction addAction;
		private AbstractAction rmAction;
		MultipleFileChooser()
			{
			this.fileList = new JList<>(new DefaultListModel<File>());
			JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.add(top,BorderLayout.NORTH);
			this.addAction = new AbstractAction("[+]")
				{
				@Override
				public void actionPerformed(ActionEvent e)
					{
					Set<File> f= getFiles();
					File first=(f.isEmpty()?null:f.iterator().next());
					File dir=(first!=null?first.getParentFile():null);
					JFileChooser chooser = new JFileChooser(dir);
					chooser.setMultiSelectionEnabled(true);
					if(getFilter()!=null) chooser.setFileFilter(getFilter());
					if(chooser.showOpenDialog(MultipleFileChooser.this)!=JFileChooser.APPROVE_OPTION) return;
					if(chooser.getSelectedFiles()==null) return;
					addFiles(chooser.getSelectedFiles());
					}
				};
			this.addAction.putValue(AbstractAction.LONG_DESCRIPTION, "Add a File");
			this.rmAction = new AbstractAction("[-]")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					int i[] = fileList.getSelectedIndices();
					DefaultListModel<File> m = (DefaultListModel<File>)fileList.getModel();
					for(int a=0;a< i.length;++a)
						{
						m.remove(i[(i.length-1)-a]);
						}
					}	
				};
			this.rmAction.putValue(AbstractAction.LONG_DESCRIPTION, "Remove selected File");
			this.rmAction.setEnabled(false);
			this.fileList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
					@Override
					public void valueChanged(ListSelectionEvent e) {
						rmAction.setEnabled(!fileList.isSelectionEmpty());
					}
				});
			top.add(new JButton(addAction));
			top.add(new JButton(rmAction));
			
			JScrollPane scroll = new JScrollPane(this.fileList);
			this.add(scroll,BorderLayout.CENTER);
			}
		
		public Set<File> getFiles()
			{
			 Set<File> f= new HashSet<>(fileList.getModel().getSize());
			 for(int i=0;i< fileList.getModel().getSize();++i)
			 	{
				f.add(fileList.getModel().getElementAt(i)); 
			 	}
			return f;
			}
		public void addFiles(File files[])
			{
			if(files==null || files.length==0) return;
			DefaultListModel<File> m = (DefaultListModel<File>)fileList.getModel();
			Set<File> set=getFiles();
			for(File f:files)
				{	
				if(set.contains(f)) continue;
				set.add(f);
				m.addElement(f);
				}
			}
		}

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
								JOptionPane.showMessageDialog(owner,"Completed:"+Arrays.toString(args));
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
								JOptionPane.showMessageDialog(owner,"FAILURE:"+Arrays.toString(args));
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
									"FAILURE:"+err.getMessage());
							owner.runningThread=null;
						}
					});
				} catch (Exception e) {
					LOG.warn(e);
					
					}
				}
			}
		}
	
	public static class MyLayout implements LayoutManager
		{
		int marginLeft=200;
		int spacingx=5;
		int spacingy=spacingx*2;
		@Override
		public void addLayoutComponent(String name, Component comp) {
			//ignore
			}
		
		@Override
		public void layoutContainer(Container parent)
			{
			synchronized (parent.getTreeLock())
			    {
				 Insets insets = parent.getInsets();
				 int y=insets.top;
				 final int n= parent.getComponentCount();
				 int i=0;
				 while(i<n)
				 	{
					Component c= parent.getComponent(i); 
					Dimension d = c.getPreferredSize();
					int rowHeight=  d.height;
					c.setBounds(
							insets.left,
							y,
							marginLeft,
							d.height
							);
					
					if(i+1<n)
						{
						i++;
						c= parent.getComponent(i); 
						d = c.getPreferredSize();
						int x= insets.left+marginLeft+spacingx;
						int width = parent.getWidth()-(x+insets.right);
						if(width<=marginLeft) width=marginLeft;
						rowHeight= Math.max(rowHeight, d.height);
						c.setBounds(
								x,
								y,
								width,
								d.height
								);
						
						if(i+1<n) y += spacingy;
						}
					y+= rowHeight;
					++i;
				 	}
				 y+=insets.bottom;
			    }
			}
		@Override
		public Dimension minimumLayoutSize(Container parent)
			{
			synchronized (parent.getTreeLock())
		    {
			 int width=marginLeft;
			 Insets insets = parent.getInsets();
			 int y=insets.top;
			 final int n= parent.getComponentCount();
			 int i=0;
			 while(i<n)
			 	{
				Component c= parent.getComponent(i); 
				Dimension d = c.getPreferredSize();
				int rowHeight=  d.height;
				
				if(i+1<n)
					{
					i++;
					c= parent.getComponent(i); 
					d = c.getPreferredSize();
					rowHeight= Math.max(rowHeight, d.height);
					width = Math.max(width, marginLeft+spacingx+d.width);
					if(i+1<n) y += spacingy;
					}
				y+= rowHeight;
				++i;
			 	}
			 y+=insets.bottom;
			 return new Dimension(width+insets.left+insets.right, y);
		    }
			}
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return minimumLayoutSize(parent);
			}
		@Override
		public void removeLayoutComponent(Component parent) {
			//ignore
			}
		}
	
	
	}

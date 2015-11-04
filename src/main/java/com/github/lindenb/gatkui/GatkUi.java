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


@SuppressWarnings("serial")
public class GatkUi extends AbstractGatkPrograms
	{
	
	private GatkUi()
		{
		}
	
	
	@Override
	protected void buildTabbedPane(JTabbedPane tabbedPane) {
		
		super.buildTabbedPane(tabbedPane);
		JPanel pane=new JPanel(new MyLayout());
		for(int i=0;i< 10;++i)
		{
			pane.add(new JLabel(""+i));
			if(i%2==0)
				{
				pane.add(new JTextField(""+i));
				}
			else
				{
				JScrollPane p=new JScrollPane(new JTextArea(""+i));
				p.setPreferredSize(new Dimension(300,100));
				pane.add(p);
				}
		}
		tabbedPane.addTab("test", pane);
	}
	
	public static void main(String[] args)
		{
		final GatkUi app = new GatkUi();
		try
			{
			SwingUtilities.invokeAndWait(new Runnable()
				{
				@Override
				public void run()
					{
					app.pack();
					Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
					Dimension dim = app.getPreferredSize();
					
					app.setBounds(
							(screen.width-dim.width)/2,
							(screen.height-dim.height)/2,
							dim.width, dim.height);
					app.setVisible(true);
					}
				});
			}
		catch(Exception err)
			{
			LOG.error(err);
			}
		}
	
	
	
	

	}

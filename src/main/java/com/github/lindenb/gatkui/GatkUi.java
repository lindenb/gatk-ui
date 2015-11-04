package com.github.lindenb.gatkui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


@SuppressWarnings("serial")
public class GatkUi extends AbstractGatkPrograms
	{
	
	private GatkUi()
		{
		}
	
		
	public static void main(String[] args)
		{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
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

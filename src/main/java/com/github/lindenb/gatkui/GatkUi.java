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

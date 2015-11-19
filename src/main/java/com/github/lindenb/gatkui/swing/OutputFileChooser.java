package com.github.lindenb.gatkui.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


@SuppressWarnings("serial")
public class OutputFileChooser extends AbstractFileChooser
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
		PreferredDirectory.update(f.getParentFile());
		return JFileChooser.APPROVE_OPTION;
		}
	}
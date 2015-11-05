package com.github.lindenb.gatkui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public abstract class AbstractFileChooser extends AbstractFilterChooser
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
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
	AbstractFileChooser()
		{
		this.textField = new JTextField(50);
		this.textField.setEditable(isTextFieldEditable());
		this.add(this.textField,BorderLayout.CENTER);
		JPanel p = new JPanel(new FlowLayout());
		this.add(p,BorderLayout.EAST);
		p.add(new JButton(new AbstractAction("Set...")
				{
				@Override
				public void actionPerformed(ActionEvent e)
					{
					File file = getFile();
					File dir=(file!=null?file.getParentFile():null);
					JFileChooser chooser = new JFileChooser(dir);
					if(getFilter()!=null) chooser.setFileFilter(getFilter());
					if(select(chooser)!=JFileChooser.APPROVE_OPTION) return;
					if(chooser.getSelectedFile()==null) return;
					setText(chooser.getSelectedFile().getPath());
					}
				}));
		p.add(new JButton(new AbstractAction("Clear")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				setText(null);
			}
			}));
		}
	protected abstract int select(final JFileChooser c);
	
	public String getText()
		{
		return this.textField.getText().trim();
		}
	
	public boolean isTextFieldEditable()
		{
		return false;
		}
	
	public File getFile()
		{
		String s= this.getText();
		try
			{
			return (s==null || s.trim().isEmpty()?null:new File(s));
			}
		catch(Exception err)
			{
			return null;
			}
		}
	
	public void setText(String file) {
		if(file==null)
			{
			this.textField.setText("");	
			this.textField.setToolTipText("");
			}
		else
			{
			this.textField.setText(file);
			this.textField.setToolTipText(file);
			this.textField.setCaretPosition(0);
			}
		}
	
	@Override
	public void setToolTipText(String arg0)
		{
		this.textField.setToolTipText(arg0);
		super.setToolTipText(arg0);
		}

}
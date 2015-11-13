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
package com.github.lindenb.gatkui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class StringsOrFilesChooser extends AbstractFilterChooser
	{
	private DefaultListModel<String> model;
	private JList<String> fileList;
	private AbstractAction addAction;
	private AbstractAction rmAction;
	private AbstractAction saveListAction;
	private JTextField textField;
	
	public StringsOrFilesChooser()
		{
		this.model = new DefaultListModel<String>();
		this.fileList = new JList<String>(this.model);
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.add(top,BorderLayout.NORTH);
		
		this.textField = new JTextField(20);
		this.textField.setFont(new Font("Dialog",Font.PLAIN,9));
		top.add(this.textField);
		this.textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s=textField.getText().trim();
				if(s.isEmpty()) return;
				if(!accept(s)) return;
				addStrings(new String[]{s});
				textField.setText("");
				}
			});
		
		this.addAction = new AbstractAction("[+]")
			{
			@Override
			public void actionPerformed(ActionEvent e)
				{
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				if(getFilter()!=null) chooser.setFileFilter(getFilter());
				if(chooser.showOpenDialog(StringsOrFilesChooser.this)!=JFileChooser.APPROVE_OPTION) return;
				if(chooser.getSelectedFiles()==null) return;
				for(File f: chooser.getSelectedFiles())
					{
					addStrings(new String[]{f.getPath()});
					}
				
				}
			};
		this.addAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Add a File");
		this.rmAction = new AbstractAction("[-]")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				int i[] = fileList.getSelectedIndices();
				for(int a=0;a< i.length;++a)
					{
					model.remove(i[(i.length-1)-a]);
					}
				}	
			};
		this.rmAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Remove selected File");
		this.rmAction.setEnabled(false);
		
		
		this.saveListAction = new AbstractAction("[!]")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				try
					{
					JFileChooser fc=new JFileChooser();
					if(fc.showSaveDialog(StringsOrFilesChooser.this)!=JFileChooser.APPROVE_OPTION)
						{
						return;
						}
					final File fs = fc.getSelectedFile();
					if(fs.exists() && JOptionPane.showConfirmDialog(StringsOrFilesChooser.this, fc.toString()+" exits. Overwrite ?",
							"Overwrite",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.CANCEL_OPTION)
						{
						return;
						}
					PrintWriter pw= new PrintWriter(fs);
					for(int i=0;i< fileList.getModel().getSize();++i)
						{
						pw.write(fileList.getModel().getElementAt(i));
						}
					pw.flush();
					pw.close();
					}
				catch (Exception e2)
					{
					e2.printStackTrace();
					}
				}	
			};
			this.saveListAction.putValue(AbstractAction.LONG_DESCRIPTION, "Save Current List As...");
			this.saveListAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Save Current List As...");
		this.saveListAction.setEnabled(false);
		
		this.fileList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e) {
				rmAction.setEnabled(!fileList.isSelectionEmpty());
				}
			});
		
		this.fileList.getModel().addListDataListener(new ListDataListener()
			{
			@Override
			public void intervalRemoved(ListDataEvent arg0)
				{	
				contentsChanged(arg0);
				}
			
			@Override
			public void intervalAdded(ListDataEvent arg0)
				{
				contentsChanged(arg0);
				}
			
			@Override
			public void contentsChanged(ListDataEvent arg0)
				{
				saveListAction.setEnabled(fileList.getModel().getSize()>0);
				}
			});
		
		top.add(new JButton(addAction));
		top.add(new JButton(rmAction));
		top.add(new JButton(saveListAction));
		
		JScrollPane scroll = new JScrollPane(this.fileList);
		this.add(scroll,BorderLayout.CENTER);
		}
	
	public Set<String> getStrings()
		{
		 Set<String> f= new HashSet<>(model.getSize());
		 for(int i=0;i< fileList.getModel().getSize();++i)
		 	{
			f.add(fileList.getModel().getElementAt(i)); 
		 	}
		return f;
		}
	public void addStrings(String files[])
		{
		if(files==null || files.length==0) return;
		Set<String> set = getStrings();
		for(String f:files)
			{	
			if(set.contains(f)) continue;
			set.add(f);
			model.addElement(f);
			}
		}
	
	protected boolean accept(String s)
		{
		return !s.trim().isEmpty();
		}
	
}
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
import java.awt.event.ActionEvent;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class MultipleFileChooser extends AbstractFilterChooser
	{
	private JList<File> fileList;
	private AbstractAction addAction;
	private AbstractAction rmAction;
	private AbstractAction saveListAction;
	public MultipleFileChooser()
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
		this.addAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Add a File");
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
		this.rmAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Remove selected File");
		this.rmAction.setEnabled(false);
		
		
		this.saveListAction = new AbstractAction("[!]")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				try
					{
					JFileChooser fc=new JFileChooser(PreferredDirectory.get(null));
					if(fc.showSaveDialog(MultipleFileChooser.this)!=JFileChooser.APPROVE_OPTION)
						{
						return;
						}
					final File fs = fc.getSelectedFile();
					if(fs.exists() && JOptionPane.showConfirmDialog(MultipleFileChooser.this, fc.toString()+" exits. Overwrite ?",
							"Overwrite",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.CANCEL_OPTION)
						{
						return;
						}
					PrintWriter pw= new PrintWriter(fs);
					for(int i=0;i< fileList.getModel().getSize();++i)
						{
						pw.write(fileList.getModel().getElementAt(i).getPath());
						}
					pw.flush();
					pw.close();
					PreferredDirectory.update(fs.getParentFile());
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
	
	@Override
	public void setStrings(final Set<String> set) {
		DefaultListModel<File> m = (DefaultListModel<File>)fileList.getModel();
		m.clear();
		for(String s:set)
			{
			if(s.trim().isEmpty()) continue;
			m.addElement(new File(s.trim()));
			}
		}
	
	@Override
	public Set<String> getStrings() {
		final Set<String> S= new HashSet<>(fileList.getModel().getSize());
		for(int i=0;i< fileList.getModel().getSize();++i)
		 	{
			S.add(fileList.getModel().getElementAt(i).getPath()); 
		 	}
		return S;
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
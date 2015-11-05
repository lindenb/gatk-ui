package com.github.lindenb.gatkui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class MultipleFileChooser extends AbstractFilterChooser
{
private JList<File> fileList;
private AbstractAction addAction;
private AbstractAction rmAction;
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
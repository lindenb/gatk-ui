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
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.github.lindenb.gatkui.RodFile;


@SuppressWarnings("serial")
public class MultipleRODChooser extends AbstractFilterChooser
	{
	private RodTableModel<RodFile> rodTableModel;
	private JTable rodTable;
	private AbstractAction addAction;
	private AbstractAction rmAction;
	private AbstractAction saveListAction;
	private JTextField rodPrefixTextField;
	public MultipleRODChooser()
		{
		this.rodTableModel = new RodTableModel<RodFile>();
		this.rodTable = new JTable(this.rodTableModel);
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.add(top,BorderLayout.NORTH);
		top.add(new JLabel("Prefix:",JLabel.TRAILING));
		this.rodPrefixTextField= new JTextField(15);
		top.add(this.rodPrefixTextField);
		
		this.addAction = new AbstractAction("[+]")
			{
			@Override
			public void actionPerformed(ActionEvent e)
				{
				JFileChooser chooser = new JFileChooser(PreferredDirectory.get(null));
				if(getFilter()!=null) chooser.setFileFilter(getFilter());
				if(chooser.showOpenDialog(MultipleRODChooser.this)!=JFileChooser.APPROVE_OPTION) return;
				if(chooser.getSelectedFile()==null) return;
				PreferredDirectory.update(chooser.getSelectedFile());
				String prefix = rodPrefixTextField.getText().trim();
				if(prefix.split("[ \t\n\r]").length!=1)
					{
					JOptionPane.showMessageDialog(MultipleRODChooser.this,
							"No whitespace in prefix please");
					return ;
					}
				addRodFile(new RodFile(prefix,chooser.getSelectedFile()));
				rodPrefixTextField.setText("");
				}
			};
		this.addAction.putValue(AbstractAction.SHORT_DESCRIPTION, "Add a File");
		this.rmAction = new AbstractAction("[-]")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				int i[] = rodTable.getSelectedRows();
			
				for(int a=0;a< i.length;++a)
					{
					rodTableModel.removeElement(i[(i.length-1)-a]);
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
					if(fc.showSaveDialog(MultipleRODChooser.this)!=JFileChooser.APPROVE_OPTION)
						{
						return;
						}
					final File fs = fc.getSelectedFile();
					if(fs.exists() && JOptionPane.showConfirmDialog(MultipleRODChooser.this, fc.toString()+" exits. Overwrite ?",
							"Overwrite",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null)!=JOptionPane.CANCEL_OPTION)
						{
						return;
						}
					PrintWriter pw= new PrintWriter(fs);
					for(int i=0;i< rodTable.getModel().getRowCount();++i)
						{
						pw.write(rodTableModel.rows.get(i).toString());
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
		
		this.rodTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e) {
				rmAction.setEnabled(rodTableModel.getRowCount()!=0);
				}
			});
		
		this.rodTable.getModel().addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				saveListAction.setEnabled(rodTableModel.getRowCount()>0);
				}
			});
		
		
		top.add(new JButton(addAction));
		top.add(new JButton(rmAction));
		top.add(new JButton(saveListAction));
		
		JScrollPane scroll = new JScrollPane(this.rodTable);
		this.add(scroll,BorderLayout.CENTER);
		}
	
	@Override
	public void setStrings(final Set<String> set) {
		rodTableModel.clear();
		for(String s:set)
			{
			if(s.trim().isEmpty()) continue;
			rodTableModel.addElement(new RodFile(s.trim()));
			}
		}
	
	@Override
	public Set<String> getStrings() {
		final Set<String> S= new HashSet<>(rodTableModel.getRowCount());
		for(RodFile r:rodTableModel.rows)
		 	{
			S.add(r.toString()); 
		 	}
		return S;
		}
	
	
	public Set<RodFile> getFiles()
		{
		return new HashSet<>(rodTableModel.rows);
		}
	public void addRodFile(RodFile file)
		{
		if(file==null) return;
		if(rodTableModel.rows.contains(file)) return;
		rodTableModel.addElement(file);
		}
	protected class RodTableModel<T extends RodFile>
		extends AbstractTableModel
		{
		Vector<T> rows = new Vector<>();
		@Override
		public int getColumnCount() {
			return 2;
			}
		@Override
		public int getRowCount() {
			return rows.size();
			}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return getValueOf(rows.get(rowIndex),columnIndex);
			}
		public Object getValueOf(final T rod, int columnIndex)
			{
			switch(columnIndex)
				{
				case 0: return rod.getPrefix();
				case 1: return rod.getFile();
				}
			return null;
			}
		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex)
			{
			case 0: return "Prefix";
			case 1: return "File";
			default: return null;
			}
		}
		
		void removeElement(int i)
			{
			this.rows.remove(i);
			fireTableRowsDeleted(i, i);
			}
		void addElement(T t)
			{
			int n=this.rows.size();
			this.rows.add(t);
			fireTableRowsInserted(n, n);
			}
		void clear()
			{
			while(!rows.isEmpty())
				{
				removeElement(0);
				}
			}
		}
}
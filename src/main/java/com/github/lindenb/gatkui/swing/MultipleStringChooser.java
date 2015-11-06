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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class MultipleStringChooser extends JPanel
	{
	private JTextField inputField;
	private JList<String> stringList;
	private AbstractAction addAction;
	private AbstractAction rmAction;
	public MultipleStringChooser()
		{
		super(new BorderLayout(5,5));
		this.stringList = new JList<>(new DefaultListModel<String>());
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.add(top,BorderLayout.NORTH);
		this.addAction = new AbstractAction("[+]")
			{
			@Override
			public void actionPerformed(ActionEvent e)
				{
				if(addString(inputField.getText().trim()))
					{
					inputField.setText("");
					}
				}
			};
		this.addAction.putValue(AbstractAction.LONG_DESCRIPTION,
				"Add a Text");

		this.inputField=new JTextField("",10);
		this.inputField.setFont(new Font("Dialog",Font.PLAIN,10));
		this.inputField.addActionListener(this.addAction);
		top.add(this.inputField);
		
			
		this.rmAction = new AbstractAction("[-]")
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				int i[] = stringList.getSelectedIndices();
				DefaultListModel<String> m = (DefaultListModel<String>)stringList.getModel();
				for(int a=0;a< i.length;++a)
					{
					m.remove(i[(i.length-1)-a]);
					}
				}	
			};
		this.rmAction.putValue(AbstractAction.LONG_DESCRIPTION,
				"Remove selected.");
		this.rmAction.setEnabled(false);
		this.stringList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e) {
					rmAction.setEnabled(!stringList.isSelectionEmpty());
				}
			});
		top.add(new JButton(addAction));
		top.add(new JButton(rmAction));
		
		JScrollPane scroll = new JScrollPane(this.stringList);
		this.add(scroll,BorderLayout.CENTER);
		}
	
	public boolean acceptString(final String s)
		{
		return true;
		}
	
	public boolean requiresUnique()
		{
		return false;
		}
	
	public List<String> getStrings()
		{
		 List<String> L= new ArrayList<String>(this.stringList.getModel().getSize());
		 for(int i=0;i< this.stringList.getModel().getSize();++i)
		 	{
			L.add(stringList.getModel().getElementAt(i)); 
		 	}
		if(requiresUnique())
			{
			Set<String> set = new LinkedHashSet<>(L);
			L= new ArrayList<>(set);
			}
		return L;
		}
	public boolean addString(String s)
		{
		if(s==null) return false;
		s=s.trim();
		if(s.trim().isEmpty()) return false;
		if(!acceptString(s)) return false;
		DefaultListModel<String> m = (DefaultListModel<String>)this.stringList.getModel();
		if(requiresUnique() && getStrings().contains(s)) return false;
		m.addElement(s);
		return true;
		}
}
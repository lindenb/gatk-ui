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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

@SuppressWarnings("serial")
public class EnumListChooser<T extends Enum<T>> extends JPanel
	implements StringSet
	{
	private JList<T> itemList;
	private DefaultListModel<T> model;
	public EnumListChooser(final Class<T> clazz)
		{
		super(new BorderLayout(5,5));
		
		final T  items[]=clazz.getEnumConstants();
		this.model = new DefaultListModel<>();
		for(final T item:items)
			{
			this.model.addElement(item);
			}
		this.itemList = new JList<>(this.model);
		this.itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JScrollPane scroll = new JScrollPane(this.itemList);
		this.add(scroll,BorderLayout.CENTER);
		}

	@Override
	public Set<String> getStrings()
		{
		Set<String> L= new HashSet<String>();
		for(final T item:this.itemList.getSelectedValuesList())
			{
			L.add(item.name());
			}
		return L;
		}
	
	@Override
	public void setStrings(final Set<String> set) {
		this.itemList.clearSelection();
		final List<Integer> idx = new ArrayList<>();
		for(final String s:set)
			{
			for(int i=0;i< itemList.getModel().getSize();++i)
				{
				if(itemList.getModel().getElementAt(i).name().equals(s))
					{
					idx.add(i);
					break;
					}
				}
			}
		final int indices[]= new int[idx.size()];
		for(int i=0;i< idx.size();++i)
			indices[i]=idx.get(i);
		this.itemList.setSelectedIndices(indices);
		}
	}
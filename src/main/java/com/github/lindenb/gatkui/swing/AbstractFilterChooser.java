package com.github.lindenb.gatkui.swing;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;


@SuppressWarnings("serial")
public abstract class AbstractFilterChooser
	extends JPanel
	implements StringSet
	{
	private FileFilter filter=null;
	
	public AbstractFilterChooser() {
		super(new BorderLayout(5,5));
		}
	public void setFilter(FileFilter filter)
		{
		this.filter=filter;
		}
	public FileFilter getFilter() {
		return filter;
		}
	public void setFilter(final String description,final String...extensions)
		{
		setFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return description;
			}
			
			@Override
			public boolean accept(File f)
				{
				if(f.isDirectory()) return true;
				if(!f.isFile()) return false;
				for(String ext:extensions)
					{
					if(f.getName().toLowerCase().endsWith(("."+ext).toLowerCase())) return true;
					}
				return false;
				}
			});
		}
	}
package com.github.lindenb.gatkui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class MyLayout implements LayoutManager
{
int marginLeft=200;
int spacingx=5;
int spacingy=spacingx*2;
@Override
public void addLayoutComponent(String name, Component comp) {
	//ignore
	}

@Override
public void layoutContainer(Container parent)
	{
	synchronized (parent.getTreeLock())
	    {
		 Insets insets = parent.getInsets();
		 int y=insets.top;
		 final int n= parent.getComponentCount();
		 int i=0;
		 while(i<n)
		 	{
			Component c= parent.getComponent(i); 
			Dimension d = c.getPreferredSize();
			int rowHeight=  d.height;
			c.setBounds(
					insets.left,
					y,
					marginLeft,
					d.height
					);
			
			if(i+1<n)
				{
				i++;
				c= parent.getComponent(i); 
				d = c.getPreferredSize();
				int x= insets.left+marginLeft+spacingx;
				int width = parent.getWidth()-(x+insets.right);
				if(width<=marginLeft) width=marginLeft;
				rowHeight= Math.max(rowHeight, d.height);
				c.setBounds(
						x,
						y,
						width,
						d.height
						);
				
				if(i+1<n) y += spacingy;
				}
			y+= rowHeight;
			++i;
		 	}
		 y+=insets.bottom;
	    }
	}
@Override
public Dimension minimumLayoutSize(Container parent)
	{
	synchronized (parent.getTreeLock())
    {
	 int width=marginLeft;
	 Insets insets = parent.getInsets();
	 int y=insets.top;
	 final int n= parent.getComponentCount();
	 int i=0;
	 while(i<n)
	 	{
		Component c= parent.getComponent(i); 
		Dimension d = c.getPreferredSize();
		int rowHeight=  d.height;
		
		if(i+1<n)
			{
			i++;
			c= parent.getComponent(i); 
			d = c.getPreferredSize();
			rowHeight= Math.max(rowHeight, d.height);
			width = Math.max(width, marginLeft+spacingx+d.width);
			if(i+1<n) y += spacingy;
			}
		y+= rowHeight;
		++i;
	 	}
	 y+=insets.bottom;
	 return new Dimension(width+insets.left+insets.right, y);
    }
	}
@Override
public Dimension preferredLayoutSize(Container parent) {
	return minimumLayoutSize(parent);
	}
@Override
public void removeLayoutComponent(Component parent) {
	//ignore
	}
}
package com.github.lindenb.gatkui.swing;

import javax.swing.JFileChooser;

@SuppressWarnings("serial")
public class InputFileChooser extends AbstractFileChooser
	{
	@Override
	protected int select(JFileChooser c) {
		return c.showOpenDialog(this);
		}
	}
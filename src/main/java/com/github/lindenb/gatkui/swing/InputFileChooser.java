package com.github.lindenb.gatkui.swing;

import javax.swing.JFileChooser;


public class InputFileChooser extends AbstractFileChooser
{
@Override
protected int select(JFileChooser c) {
	return c.showOpenDialog(this);
	}
}
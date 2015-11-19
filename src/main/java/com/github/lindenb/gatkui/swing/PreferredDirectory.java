package com.github.lindenb.gatkui.swing;

import java.io.File;
import java.util.prefs.Preferences;

public class PreferredDirectory {

	private PreferredDirectory() {
		}

	public static File get(File defaultDir)
		{
		try {
			Preferences prefs = Preferences.userNodeForPackage(PreferredDirectory.class);
			String s=prefs.get("file", null);
			if(s==null || s.trim().isEmpty()) return defaultDir;
			File dir= new File(s);
			if(dir.exists() && dir.isFile()) return dir.getParentFile();
			if(!dir.exists()) return defaultDir;
			if(!dir.isDirectory()) return defaultDir;
			return dir;
		} catch (Exception e) {
			return defaultDir;
			}
		}


	
	public static void update(File defaultDir)
		{
		if(defaultDir==null) return;
		if(defaultDir.isFile()) defaultDir=defaultDir.getParentFile();
		if(defaultDir==null) return;
		
		
		try {
			Preferences prefs = Preferences.userNodeForPackage(PreferredDirectory.class);
			prefs.put("file", defaultDir.getPath());
			prefs.flush();
		} catch (Exception e) {
			}
		}
	
}

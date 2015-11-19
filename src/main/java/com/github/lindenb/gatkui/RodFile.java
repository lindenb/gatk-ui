package com.github.lindenb.gatkui;

import java.io.File;

public class RodFile {
	private final static String DELIMITER="``````";
	private String prefix;
	private File  file;
	public RodFile(String prefix,File  file)
		{
		this.prefix=(prefix==null?"":prefix);
		this.file=file;
		}
	
	public RodFile(String concat)
		{
		int i = concat.indexOf(DELIMITER);
		if(i==-1)
			{
			prefix="";
			file = new File(concat);
			}
		else
			{
			prefix=concat.substring(0,i);
			file = new File(concat.substring(i+DELIMITER.length()));
			}
		}
	
	public File getFile() {
		return file;
	}
	
	public String getPrefix() {
		return prefix==null?"":prefix.trim();
		}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RodFile other = (RodFile) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		if (prefix == null) {
			if (other.prefix != null) {
				return false;
			}
		} else if (!prefix.equals(other.prefix)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return getPrefix()+DELIMITER+getFile();
		}
	

}

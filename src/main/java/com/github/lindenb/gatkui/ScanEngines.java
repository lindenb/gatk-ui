package com.github.lindenb.gatkui;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.broadinstitute.gatk.utils.commandline.*;

public class ScanEngines
	{
	public static Logger LOG=Logger.getLogger("gatk.ScanEngines");
	private XMLStreamWriter out;
	private String programName=null;
	private boolean program_element_printed=false;
	
	private String makeLabel(String s)
		{
		for(int i=0;i+1<s.length();++i)
			{
			if( Character.isLetter(s.charAt(i)) &&
				Character.isLetter(s.charAt(i+1)) &&
				Character.isLowerCase(s.charAt(i)) &&
				Character.isUpperCase(s.charAt(i+1)))
				{
				return makeLabel(s.substring(0,i+1)+" "+
						Character.toLowerCase(s.charAt(i+1))
						+(i+2<s.length()?s.substring(i+2):"")
						);
				}
			}
		return s.replace('_', ' ');
		}
	
	private void startProgram() throws Exception
		{
		if(this.program_element_printed) return;
		this.program_element_printed=true;
		this.out.writeStartElement("program");
		this.out.writeAttribute("name", this.programName);
		this.out.writeAttribute("disabled","true");
		this.out.writeStartElement("description");
		this.out.writeCharacters(makeLabel(this.programName));
		this.out.writeEndElement();
		this.out.writeStartElement("options");
		}
	
	private void scanClass(String className) throws Exception
		{
		this.program_element_printed=false;
		try	{
			Class<?> clazz = Class.forName(className);
			if(Modifier.isInterface( clazz.getModifiers()))
				return;

			if(Modifier.isAbstract( clazz.getModifiers()))
				return;
			if(clazz.getDeclaredFields().length==0) return;
			
			Object theinstance = null;
			try
				{
				theinstance = clazz.newInstance();
				}
			catch(java.lang.InstantiationException err)
				{
				theinstance=null;
				}
			catch(java.lang.IllegalAccessException err)
				{
				
				}
			this.programName = clazz.getSimpleName();
			if(this.programName==null) return ;
			for(Field field :clazz.getFields())
				{
				Input input = field.getAnnotation(Input.class);
				if(input==null) continue;
				String type = field.getType().getSimpleName();
				LOG.info(type);
				startProgram();
				
				this.out.writeStartElement("option");
				
				this.out.writeAttribute("type",type);
				
				this.out.writeStartElement("description");
				this.out.writeCharacters(input.doc());
				this.out.writeEndElement();
				
				this.out.writeEndElement();
				}
			
			
			for(Field field :clazz.getFields())
				{
				if(field.getAnnotation(Hidden.class)!=null) continue;
				Argument argument = field.getAnnotation(Argument.class);
				if(argument==null) continue;

				String enumClass=null;
				Object defaultValue=null;
				String opt = field.getName();
				String type = field.getType().getSimpleName();
				String required=null;
				if(type==null) continue;
				if(field.isEnumConstant())
					{
					type="enum";
					defaultValue=(theinstance==null?null:field.get(theinstance));
					enumClass=field.getType().getName();
					}
				else if(type.equals("String"))
					{
					 type=type.toLowerCase();
					 defaultValue=(theinstance==null?null:field.get(theinstance));
					}
				else if(type.equals("byte"))
					{
					type="int";
					defaultValue=(theinstance==null?null:field.get(theinstance));
					}
				else if(type.equals("int") || type.equals("double") || type.equals("boolean"))
					{
					defaultValue=(theinstance==null?null:field.get(theinstance));
					}
				else if(type.equals("Integer"))
					{
					type="int";
					required="false";
					}
				else if(type.equals("Double") || type.equals("Boolean"))
					{
					type=type.toLowerCase();
					required="false";
					}
				else
					{
					System.err.println(type+"############");
					}
				
				startProgram();
				
				this.out.writeStartElement("option");
				
				this.out.writeAttribute("type",type);

				
				if(argument.fullName()!=null)
					{
					this.out.writeAttribute("opt","-"+argument.fullName());
					this.out.writeAttribute("label",makeLabel(argument.fullName()));
					}
				if(defaultValue!=null) 
					this.out.writeAttribute("default",String.valueOf(defaultValue));
				if(enumClass!=null) 
					this.out.writeAttribute("enum-class",String.valueOf(enumClass));
				if(required!=null)
					this.out.writeAttribute("required",required);
				
				this.out.writeStartElement("description");
				this.out.writeCharacters(argument.doc());
				this.out.writeEndElement();
				
				this.out.writeEndElement();
				}
			
			for(Field field :clazz.getFields())
				{
				Output output = field.getAnnotation(Output.class);
				if(output==null) continue;
				String type = field.getType().getSimpleName();
				
				if(field.getType().equals(RodBinding.class))
					{
					type+=" "+((ParameterizedType)field.getType().getGenericSuperclass()).getActualTypeArguments()[0];
					}
				
				startProgram();
				
				this.out.writeStartElement("option");
				
				this.out.writeAttribute("type",type);
				this.out.writeAttribute("opt","-out");

				this.out.writeStartElement("description");
				this.out.writeCharacters(output.doc());
				this.out.writeEndElement();
				
				this.out.writeEndElement();
				}

			
			}
		catch(java.lang.UnsatisfiedLinkError err) {}
		catch(java.lang.IllegalAccessError err) {}
		catch(java.lang.ClassNotFoundException err) {}
		catch(java.lang.NoClassDefFoundError err) {}
		catch(Throwable err)
			{
			err.printStackTrace();
			//LOG.warn("Cannot analyse "+className,err);
			}
		finally
			{
			if(this.program_element_printed)
				{
				this.out.writeEndElement();
				this.out.writeEndElement();
				}
			}
		}
	
	
	private void execute(String[] args)
		{		
		if(args.length!=1)
			{
			System.err.println("Usage:\n\tjava -jar gatk-scanengines.jar GenomeAnalysisTK.jar ");
			return ;
			}
		
		try
			{
			XMLOutputFactory xof=XMLOutputFactory.newFactory();
			this.out=xof.createXMLStreamWriter(System.out);
			this.out.writeStartDocument("UTF-8", "1.0");
			this.out.writeStartElement("programs");
			/* http://stackoverflow.com/questions/15720822 */
			ZipInputStream zip = new ZipInputStream(new FileInputStream(args[0]));
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
				{
			    if (!entry.isDirectory() && entry.getName().endsWith(".class"))
			    	{
			        String className = entry.getName().replace('/', '.'); // including ".class"
			        className = className.substring(0, className.length() - ".class".length());
			        scanClass(className);
			    	}
				}
			this.out.writeEndElement();
			this.out.writeEndDocument();
			this.out.flush();
			this.out.close();
			}
		catch(Exception err)
			{
			LOG.info("Failed");
			}
		}

	
	public static void main(String[] args)
		{
		new ScanEngines().execute(args);
		}
	}

/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import net.jot.utils.JOTUtilities;

/**
 * This contains the version release (hardcoded) and build stamp, generated by the Ant build.
 * @author tcolar
 */
public class Version 
{
	public static final String STAMP="02/05/2009 12:15";
	
	public static final String VERSION="0.1.4";

        /**
         * return the version string
         * @return
         */
	public static String getVersion()
	{
		return VERSION+" ("+STAMP+")";
	}
	
	/**
	 * Writes Stamp right in here.
	 * To be called by ANT. 
	 */
	public static void main(String[] args) throws Exception
	{
		String sourceFile=args[0];
		GregorianCalendar cal=new GregorianCalendar();
		String stamp=JOTUtilities.formatDate(new Date(), false);
		File f=new File(sourceFile);
		byte[] buf=new byte[(int)f.length()];
		FileInputStream fis=new FileInputStream(f);
		fis.read(buf);
		fis.close();
		String s=new String(buf);
		s=s.replaceFirst("STAMP=\"[^\"]*\"", "STAMP=\""+stamp+"\"");
		FileOutputStream fos=new FileOutputStream(f);
		fos.write(s.getBytes());
		fos.flush();
		fos.close();
                
                //also write to VERSION.txt
                File f2=new File("VERSION.txt");
                FileOutputStream fos2=new FileOutputStream(f2);
		fos2.write((VERSION+" "+stamp).getBytes());
		fos2.close();
	}
	
}

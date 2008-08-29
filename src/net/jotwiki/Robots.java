/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This object defines what should be returned when a request fro robots.txt is received
 * @author tcolar
 */
public class Robots
{
	private static String robotsString=null;
	
        /**
         * List of pages/url's that should be marqued as dissalowed in robots.txt
         */
	private static String[] disallowed={
		"/setup.do",
		"/login.do",
		"/logout.do",
		"/edit.do",
		"/error.do",
		"/print.do",
		"/forbidden.do",
		"/new.do",
	};

        /**
         * Returns the generated robots.txt content as a byte array
         * @return
         * @throws java.io.IOException
         */
	public static byte[] getBytes() throws IOException
	{
		if(robotsString==null)
		{
			synchronized(Robots.class)
			{
				StringWriter s=new StringWriter();
				BufferedWriter b=new BufferedWriter(s);
				b.write("User-agent: *");
				b.newLine();
				for(int i=0;i!=disallowed.length;i++)
				{
					b.write("Disallow: "+disallowed[i]);
					b.newLine();
				}
				b.flush();
				b.close();
				robotsString=s.toString();
			}
		}
		return robotsString.getBytes();
	}
}

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
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

import net.jot.logger.JOTLogger;
import net.jot.scheduler.JOTScheduledItem;
import net.jot.utils.JOTUtilities;

/**
 * Scheduled job to generates Google sitemaps for all the namespaces (if any pages changed)
 * @author tcolar
 */
public class SitemapGenerator implements JOTScheduledItem
{
	public static final String SITEMAP="sitemap.xml";
	public static final String SITEMAP_GZ="sitemap.xml.gz";
	public static boolean running=false;

	private static boolean dirty=true;

	public void run()
	{
		running=true;
		dirty=false;

		// finding the namespaces
		String dataFolder=WikiPreferences.getInstance().getDataFolder();
		File folder=new File(dataFolder);
		String[] ns=folder.list();

		// generating one sitemap for each one
		for(int i=0;i!=ns.length;i++)
		{
			if(new File(folder,ns[i]).isDirectory())
			{
			String webRoot=JOTUtilities.endWithForwardSlash(WikiPreferences.getInstance().getDefaultedString(ns[i]+"."+WikiPreferences.NS_WEBROOT, ""));
			String nsFolder=JOTUtilities.endWithSlash(WikiPreferences.getInstance().getDataFolder())+ns[i];
			File map=new File(nsFolder,SITEMAP);
			JOTLogger.log(JOTLogger.INFO_LEVEL,this,"Starting to write Sitemap for ns:"+ns[i]+" at:"+map.getAbsolutePath());
			PrintWriter p=null;
			try
			{
				p=new PrintWriter(new FileOutputStream(map));		
				String home=WikiPreferences.getInstance().getDefaultedString(ns[i]+"."+WikiPreferences.NS_HOMEPAGE, "home");
				String sidebar=WikiPreferences.getInstance().getDefaultedString(ns[i]+"."+WikiPreferences.NS_SIDEBAR, "sidebar");
				//testHomePageConnection(ns[i],home);
				// Header
				p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				p.println("<urlset xmlns=\"http://www.google.com/schemas/sitemap/0.84\">");
				//			Homepage
				String stamp=getTimeStamp(ns[i],home);
				if(stamp!=null)
				{
					p.println("<url>");
					p.println("  <loc>"+webRoot+"</loc>");
					p.println("  <lastmod>"+stamp+"</lastmod>");
					p.println("  <changefreq>daily</changefreq>");
					p.println("  <priority>0.9</priority>");
					p.println("</url>");
				}

				File pagesFolder=new File(WikiPreferences.getInstance().getPagesFolder(ns[i]));
				String[] pages=pagesFolder.list();

				for(int j=0;j!=pages.length;j++)
				{
                                  //starting with ~ are conflicted files
                                    if( ! pages[i].startsWith("~"))
                                    {
					//Other pages
					String page=pages[j];
					if(page.endsWith(".txt"))
						page=page.substring(0,page.lastIndexOf(".txt"));
					stamp=getTimeStamp(ns[i],page);
					String url=webRoot+page;
					if(stamp!=null)
					{
						p.println("<url>");
						p.println("  <loc>"+url+"</loc>");
						p.println("  <lastmod>"+stamp+"</lastmod>");
						if(page.equalsIgnoreCase(sidebar))
						{
							p.println("  <priority>0.1</priority>");
						}
						p.println("  <changefreq>daily</changefreq>");
						p.println("</url>");
					}
                                    }
				}
				//Footer
				p.println("</urlset>");
				p.flush();
				p.close();
				//GZIP it !
				JOTLogger.log(JOTLogger.INFO_LEVEL,this,"Compressing the sitemap (gz) for:"+ns[i]+" at:"+map.getAbsolutePath()+".gz");
				GZIPOutputStream gzos=new GZIPOutputStream(new FileOutputStream(new File(nsFolder,SITEMAP_GZ)));
				byte[] buf=new byte[(int)map.length()];
				FileInputStream fis=new FileInputStream(map);
				fis.read(buf);
				fis.close();
				gzos.write(buf);
				gzos.flush();
				gzos.close();
			}
			catch(Exception e)
			{
				JOTLogger.logException(JOTLogger.CRITICAL_LEVEL, this, "Failed to write the sitemap for ns:"+ns[i], e);
			}
			finally
			{
				if(p!=null)
					p.close();			
			}
			JOTLogger.log(JOTLogger.INFO_LEVEL,this,"Sitemap Done for ns:"+ns[i]);
			}
		}
		running=false;
	}

        /**
         * Gets a Sitemap-formatted last modification timestamp for a specific page
         * @param ns
         * @param page
         * @return
         */
	private String getTimeStamp(String ns, String page)
	{

		String pageRoot=WikiPreferences.getInstance().getPagesFolder(ns);
		File file=new File(pageRoot,page+".txt");
		String stamp=null;
		if(file.exists())
		{
			long l=file.lastModified();
			Date d=new Date(l);
			GregorianCalendar c=new GregorianCalendar();
			c.setTime(d);
			stamp=""+c.get(Calendar.YEAR)+"-"+
			JOTUtilities.sizeIt(c.get(Calendar.MONTH)+1,2)+"-"+
			JOTUtilities.sizeIt(c.get(Calendar.DAY_OF_MONTH),2)+"T"+
			JOTUtilities.sizeIt(c.get(Calendar.HOUR_OF_DAY),2)+":"+
			JOTUtilities.sizeIt(c.get(Calendar.MINUTE),2)+":"+
			JOTUtilities.sizeIt(c.get(Calendar.SECOND),2)+"Z";
		}
		return stamp;	

	}

        /**
         * Returns a namespace sitemap as a GZipped array
         * ie: use for requests for sitemap.xml.gz
         * @param namespace
         * @return
         * @throws java.lang.Exception
         */
	public static byte[] getGzippedData(String namespace) throws Exception
	{
		// we don't want to send half baked sitemap
		while(running)
			Thread.sleep(500);
		String nsFolder=JOTUtilities.endWithSlash(WikiPreferences.getInstance().getDataFolder())+namespace;
		File file=new File(nsFolder,SITEMAP_GZ);
		byte[] buf=new byte[(int)file.length()];
		FileInputStream fis=new FileInputStream(file);
		fis.read(buf);
		fis.close();
		return buf;
	}


	public boolean forceRun()
	{
		return false;
	}


	public void runCompleted()
	{
	}


	public boolean skipRun()
	{
		return ! dirty;
	}


	public static void setDirty(boolean dirty)
	{
		SitemapGenerator.dirty = dirty;
	}
}

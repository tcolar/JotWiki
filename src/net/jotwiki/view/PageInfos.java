/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import java.util.Vector;

import net.jotwiki.db.PageOptions;

/**
 * Not an actual 'View' object
 * ShowPageView helper object containing the deatils of a page, such as title and TableOfContent 
 * @author tcolar
 */
public class PageInfos 
{
	private String title="";
	private Vector toc=new Vector();
	// default pageOptions.
	private PageOptions options=new PageOptions();
	
	public void addTocEntry(int level, String text, String link)
	{
		toc.add(new TocEntry(level,text, link));
	}
	
        /**
         * Object representation of a TableOfContents entry
         */
	public class TocEntry
	{
		int level;
		String link;
		String text;
		public String getText()
		{
			return text;
		}
		//String name;
		public TocEntry(int level, String text, String link)
		{
			this.text=text;
			this.level=level;
			this.link=link;
		}
		public int getLevel()
		{
			return level;
		}
		public String getLink()
		{
			return link;
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Vector getToc()
	{
		return toc;
	}

	public void setOptions(PageOptions options)
	{
		this.options=options;
	}

	public PageOptions getOptions()
	{
		return options;
	}
}

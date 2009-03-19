/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 * Edition lock for a page DB object.
 * @author thibautc
 */
public class PageVariable extends JOTModel
{

	public String nameSpace = "";
	public String page = "";
	public String name = "";
	public String value = "";

	public void customize(JOTModelMapping mapping)
	{
		mapping.defineFieldSize("nameSpace", 40);
		mapping.defineFieldSize("name", 40);
		mapping.defineFieldSize("page", 200);
		mapping.defineFieldSize("value", 2000);
	}

	public String defineStorage()
	{
		return DEFAULT_STORAGE;
	}

	public String getNameSpace()
	{
		return nameSpace;
	}

	public void setPage(String dataPage)
	{
		this.page = dataPage;
	}

	public String getPage()
	{
		return page;
	}

	public void setNameSpace(String dataNameSpace)
	{
		this.nameSpace = dataNameSpace;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
}

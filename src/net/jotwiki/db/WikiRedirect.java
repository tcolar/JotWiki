/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jotwiki.db;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 *
 * @author thibautc
 */
public class WikiRedirect extends JOTModel
{
    public String nameSpace = "";
    public String path = "";
    public String url = "";

	protected void customize(JOTModelMapping mapping)
	{
		mapping.defineFieldSize("nameSpace", 40);
        mapping.defineFieldSize("path", 200);
        mapping.defineFieldSize("url", 250);

	}

	public String getNameSpace()
	{
		return nameSpace;
	}

	public void setNameSpace(String nameSpace)
	{
		this.nameSpace = nameSpace;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
	
}

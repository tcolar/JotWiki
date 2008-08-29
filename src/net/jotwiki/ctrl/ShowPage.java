/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import java.util.LinkedList;

import net.jot.web.ctrl.JOTController;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.view.PageInfos;

/**
 * Handles a request to view a page.
 * @author tcolar
 */
public class ShowPage extends JOTController 
{
	String page="Page no found!";

	public String process() throws Exception 
	{
		String ns=WikiUtilities.getNamespace(request);
		String pageName=request.getParameter("page");
		PageInfos infos=new PageInfos();
		//default title
		infos.setTitle(pageName);
		if(pageName==null)
		{
			pageName=(String)session.getAttribute(Constants.SESSION_LAST_PAGE);
			if(pageName==null)
			{
				pageName=WikiPreferences.getInstance().getDefaultedNsString(request,WikiPreferences.NS_HOMEPAGE,"home");
			}
		}
		if(pageName!=null)
		{
			String plainPage=PageReader.getPlainPage(request, ns, pageName);
			infos=PageReader.getPageInfos(plainPage, pageName, ns);
			page=PageReader.getHtmlPage(plainPage);
			session.setAttribute(Constants.SESSION_LAST_PAGE,pageName);
		}
		String key=ns+":"+pageName;
		LinkedList trace=getTrace(ns);
		// Only add if not same as last one.
		if(trace.size()==0 || ! trace.getLast().equals(key))
		{
			trace.addLast(key);
			WikiPreferences.getInstance().getDefaultedNsInt(request,WikiPreferences.NS_NUMBER_OF_BREADCRUMBS, new Integer(5));
			if(trace.size()>5)
				trace.removeFirst();
		}
		request.setParameter(Constants.PAGE_NAME, pageName);
		request.setParameter("pageContent",page);
		request.setAttribute(Constants.PAGE_INFOS,infos);
		return RESULT_SUCCESS;
	}

	public LinkedList getTrace(String currentNs)
	{
		LinkedList trace=(LinkedList)session.getAttribute(Constants.SESSION_TRACE);
		if(trace==null)
		{
			synchronized(this)
			{
				trace=new LinkedList();
				session.setAttribute(Constants.SESSION_TRACE, trace);
			}
		}
		if(! WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_BREADCRUMBS_ACCROSS, Boolean.TRUE).booleanValue())
		{
			// we want to remove the crumbs from other namespaces.
			for(int i=0; i!=trace.size();i++)
			{
				String s=(String)trace.get(i);
				String ns=s.substring(0,s.indexOf(":"));
				if(! ns.equalsIgnoreCase(currentNs))
				{
					trace.remove(i);
					i--;
				}
			}
		}
		return trace;
	}

	public boolean validatePermissions()
	{
          return WikiPermission.hasPermission(request, WikiPermission.VIEW_PAGE);
	}
}

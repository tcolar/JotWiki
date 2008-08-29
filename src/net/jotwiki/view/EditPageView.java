/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.view.JOTView;
import net.jotwiki.Constants;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.forms.EditForm;

/**
 * Page edition form view
 * @author tcolar
 */
public class EditPageView extends JOTView 
{

	public void prepareViewData() throws Exception 
	{
		String page=request.getParameter(Constants.PAGE_CONTENT);
		String md5=(String)request.getAttribute(Constants.PAGE_MD5);
		String ns=WikiUtilities.getNamespace(request);
		addVariable("page", page);
		String pageName=request.getParameter(Constants.PAGE_NAME_SHORT);
		addVariable(EditForm.PAGENAME, pageName);
		addVariable(EditForm.NAMESPACE, ns);
                addVariable("lockLength",WikiPreferences.getInstance().getDefaultedNsInt(request, WikiPreferences.NS_LOCK_LENGTH, new Integer(30)));
		addVariable("mantisEnabled", WikiPreferences.getInstance().isMantisEnabled(request));
		EditForm form=(EditForm)getForm(EditForm.class);
		form.get(EditForm.TEXT).setValue(page);
		form.get(EditForm.PAGENAME).setValue(pageName);
		form.get(EditForm.NAMESPACE).setValue(ns);
                form.get(EditForm.MD5).setValue(md5);
		addForm(form);
	}
	
	public boolean validatePermissions()
	{
          return WikiPermission.hasEditPermission(request);
	}
}

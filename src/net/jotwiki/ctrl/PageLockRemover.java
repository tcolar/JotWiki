/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.web.ctrl.JOTController;
import net.jotwiki.db.PageLock;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;
import net.jotwiki.forms.EditForm;
import net.jotwiki.forms.LoginForm;

/**
 * Handles a pagelock removal request
 * Those request parameters must be provided: EditForm.PAGENAME EditForm.NAMESPACE
 * @author tcolar
 */
public class PageLockRemover extends JOTController
{

	public String process() throws Exception
	{
          String page=request.getParameter(EditForm.PAGENAME);
          String ns=request.getParameter(EditForm.NAMESPACE);
          WikiUser user=(WikiUser)session.getAttribute(LoginForm.LOGGED_USER);
          if(user!=null && page!=null)
          {
            PageLock.removePageLock(ns,page,user.getLogin());
          }
          return RESULT_SUCCESS;
	}

	public boolean validatePermissions()
	{
          return WikiPermission.hasEditPermission(request);
        }

}

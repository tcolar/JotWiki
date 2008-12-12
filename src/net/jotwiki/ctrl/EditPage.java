/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.web.ctrl.JOTController;
import net.jot.web.views.JOTMessageView;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.PageLock;
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;
import net.jotwiki.forms.LoginForm;
import net.jotwiki.forms.PageOptionsForm;

/**
 * Edit Page controller
 * Handle a page edition request
 * @author tcolar
 */
public class EditPage extends JOTController
{
    public final static String RESULT_NEW_PAGE = "isnew";
    public final static String PAGE_LOCKED = "locked";
    public final static String PAGE_LOCK = "page_lock";
    String page = "";

    public String process() throws Exception
    {
        if(session.getAttribute("returnToEdit")!=null)
        {
            String page=(String)session.getAttribute("returnToEdit");
            request.setParameter(Constants.PAGE_NAME_SHORT, page);
            session.removeAttribute("returnToEdit");
        }
        String pageName = request.getParameter(Constants.PAGE_NAME_SHORT);
        String ns = WikiUtilities.getNamespace(request);
        WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);
        if (pageName == null)
        {
            pageName = WikiPreferences.getInstance().getNsString(request, WikiPreferences.NS_HOMEPAGE);
        }
        if (pageName != null)
        {
            JOTSQLCondition cond=new JOTSQLCondition(PageOptionsForm.PAGE_NAME, JOTSQLCondition.IS_EQUAL, pageName);
            if (JOTQueryBuilder.selectQuery(PageOptions.class).where(cond).findOne() == null)
            {
                request.setParameter("pageName", pageName);
                request.setParameter("pageContent", page);
                request.setAttribute("returnToEdit",pageName);
                return RESULT_NEW_PAGE;
            }
            PageLock lock = PageLock.getPageLock(ns, pageName, false);
            if (lock != null && ! lock.getAuthor().equals(user.getLogin()))
            {
                JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Found lock for: "+ns+"/"+pageName+"("+user.getLogin()+")");
                //Setting attributes for the message page.
                request.setAttribute(JOTMessageView.MESSAGE_TITLE,"Page Locked");
                String msg="This page is currently being edited by '"+lock.getAuthor()+"'<br>Please try again later.";
                request.setAttribute(JOTMessageView.MESSAGE_TEXT,msg);
                request.setAttribute(JOTMessageView.MESSAGE_LINK,"last.do");
                
                return PAGE_LOCKED;
            }
            page = PageReader.getPlainPage(request, ns, pageName);
            // lock the page 
            PageLock.updatePageLock(ns, pageName, user.getLogin());
            
            // getting an MD5 for the page, page is now locked so should be safe.
            String md5=PageReader.getPageMD5(ns, pageName);
            request.setAttribute(Constants.PAGE_MD5,md5);
            
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Set lock for: "+ns+"/"+pageName+"("+user.getLogin()+")");
        }
        request.setParameter("pageName", pageName);
        request.setParameter("pageContent", page);
        return RESULT_SUCCESS;
    }

    public boolean validatePermissions()
    {
          return WikiPermission.hasEditPermission(request);
    }
}

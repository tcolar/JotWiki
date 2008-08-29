/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiUser;
import net.jotwiki.forms.CommentForm;

public class CommentFormView extends JOTGeneratedFormView
{
	
	public void prepareViewData() throws Exception 
	{
		CommentForm form=(CommentForm)getForm(CommentForm.class);
		request.setAttribute(JOTGeneratedFormView.GENERATED_FORM,form);
		super.prepareViewData();
	}
	
	public boolean validatePermissions()
	{
            String page = (String)request.getSession().getAttribute(Constants.SESSION_LAST_PAGE);
            try
            {
            PageOptions options = PageReader.getPageOptions(WikiUtilities.getNamespace(request),page);
            if(! options.getCommentsEnabled().booleanValue())
                return false;
            if(WikiUser.isGuest(WikiUser.getCurrentUser(request)) && ! options.getCommentsGuest().booleanValue())
                return false;
            }
            catch(Exception e)
            {
                return false;
            }
            return true;
	}

}

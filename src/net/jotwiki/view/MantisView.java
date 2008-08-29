/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.utils.JOTUtilities;
import net.jot.web.view.JOTView;
import net.jotwiki.WikiPreferences;



/**
 * send to a mantis bug/page
 * @author tcolar
 */
public class MantisView extends JOTView
{

    public void prepareViewData() throws Exception
    {
        String mantisRoot=WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_MANTIS_ROOT, "http://yourmantisserver/");
        String url=JOTUtilities.endWithForwardSlash(mantisRoot);
        
        String id=request.getParameter("id");
        String cat=request.getParameter("cat");
        
        if(id!=null)
            url=url+"view.php?id="+id;
        
        if(cat!=null)
            url=url+"view_all_set.php?type=1&show_category="+cat;
        
        response.sendRedirect(url);
    }

    public boolean validatePermissions()
    {
        // no perms
        return true;
    }
}

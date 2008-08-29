/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.utils.JOTUtilities;
import net.jot.web.ctrl.JOTController;
import net.jotwiki.Constants;
import net.jotwiki.WikiPreferences;

/**
 * Sends back to the last seen page, or "home" if none set.
 * @author thibautc
 *
 */
public class GoToLastPage extends JOTController
{

	public String process() throws Exception
	{
		String last=(String)session.getAttribute(Constants.SESSION_LAST_PAGE);
		if(last==null)
			last=WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_HOMEPAGE, "home");
		JOTUtilities.sendRedirect(response, last, false, true);
		return null;
	}

	public boolean validatePermissions()
	{
                // no permissions needed
		return true;
	}

}

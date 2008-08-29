/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.web.ctrl.JOTController;
import net.jotwiki.forms.LoginForm;

/**
 * Logs a user out.
 * @author tcolar
 */
public class Logout extends JOTController
{

	public String process() throws Exception
	{
		session.removeAttribute(LoginForm.LOGGED_USER);
		return RESULT_SUCCESS;
	}

	public boolean validatePermissions()
	{
		return true;
	}

}

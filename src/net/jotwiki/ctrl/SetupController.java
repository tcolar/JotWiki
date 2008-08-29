/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.web.ctrl.JOTController;
import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.forms.setup.GlobalSetup;

/**
 * Handle setup panel
 * @author tcolar
 */
public class SetupController extends JOTController
{

	public String process() throws Exception
	{
		GlobalSetup form=(GlobalSetup)getForm(GlobalSetup.class);
                if(WikiPermission.hasPermission(request, WikiPermission.SETUP))
                {
                    request.setAttribute(JOTGeneratedFormView.GENERATED_FORM,form);
                }
		return RESULT_SUCCESS;
	}
        
	public boolean validatePermissions()
	{
          return WikiPermission.canAccessSetupPage(request);
	}

}

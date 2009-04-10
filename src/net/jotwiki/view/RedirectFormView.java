/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.forms.setup.RedirectForm;

public class RedirectFormView extends JOTGeneratedFormView
{
	
	public void prepareViewData() throws Exception 
	{
		RedirectForm form=(RedirectForm)getForm(RedirectForm.class);
		request.setAttribute(JOTGeneratedFormView.GENERATED_FORM,form);
		super.prepareViewData();
	}
	
	public boolean validatePermissions()
	{
        return WikiPermission.canAccessSetupPage(request);
	}

}

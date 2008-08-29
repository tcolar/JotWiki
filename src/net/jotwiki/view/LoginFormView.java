/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.forms.LoginForm;

public class LoginFormView extends JOTGeneratedFormView
{
	
	public void prepareViewData() throws Exception 
	{
		LoginForm form=(LoginForm)getForm(LoginForm.class);
		request.setAttribute(JOTGeneratedFormView.GENERATED_FORM,form);
		super.prepareViewData();
	}
	
	public boolean validatePermissions()
	{
		// anybody can try to login !
		return true;
	}

}

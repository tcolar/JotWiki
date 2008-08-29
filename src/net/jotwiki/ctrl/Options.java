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
import net.jotwiki.forms.PageOptionsForm;

/**
 * Handles a Page Options access request
 * @author tcolar
 */
public class Options extends JOTController
{
	public String process() throws Exception
	{

		PageOptionsForm form=(PageOptionsForm)getForm(PageOptionsForm.class);
		request.setAttribute(JOTGeneratedFormView.GENERATED_FORM,form);
		
		return RESULT_SUCCESS;
	}

	public boolean validatePermissions()
	{
          return WikiPermission.hasEditPermission(request);
	}
}

/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.filebrowser.JOTFileBrowserHelper;
import net.jot.web.view.JOTView;
import net.jotwiki.db.WikiPermission;

/**
 * View for testing/displaying  a file manager selection results
 * @author thibautc
 *
 */
public class ShowSelectedFiles extends JOTView
{

	public void prepareViewData() throws Exception 
	{
		addVariable("files",JOTFileBrowserHelper.getChosenFiles(request));
	}

	public boolean validatePermissions() 
        {
          return WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES) || WikiPermission.hasPermission(request, WikiPermission.MANAGE_FILES) ;
	}

}

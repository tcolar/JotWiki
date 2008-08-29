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

public class PageOptionsFormView extends JOTGeneratedFormView
{
	public boolean validatePermissions() 
	{
          return WikiPermission.hasEditPermission(request);
        }
}

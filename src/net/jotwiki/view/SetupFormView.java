/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.WikiPreferences;
import net.jotwiki.db.ProfileCache;
import net.jotwiki.db.WikiPermission;

/**
 * View for the global setup form
 * @author thibautc
 */
public class SetupFormView extends JOTGeneratedFormView
{

    public void prepareViewData() throws Exception
    {
        super.prepareViewData();
        //list of namespaces
        addVariable("namespaces", WikiPreferences.getInstance().getNamespaceList());
        addVariable("profiles", ProfileCache.getInstance().getProfileNames().keySet());
        addVariable("users", ProfileCache.getInstance().getUsers(false).keySet());
        
        if (request.getAttribute("SetupMessage") != null)
        {
            addVariable("SetupMessage", request.getAttribute("SetupMessage"));
        }
        // global setup
        if (WikiPermission.hasPermission(request, WikiPermission.SETUP))
        {
            addVariable("showSetup", Boolean.TRUE);
        }
        //namespaces.
        if (WikiPermission.hasPermission(request, WikiPermission.MANAGE_NAMESPACES))
        {
            addVariable("showNamespaces", Boolean.TRUE);
        }
        //profiles.
        if (WikiPermission.hasPermission(request, WikiPermission.MANAGE_PROFILES))
        {
            addVariable("showProfiles", Boolean.TRUE);
        }
        //users.
        if (WikiPermission.hasPermission(request, WikiPermission.MANAGE_USERS))
        {
            addVariable("showUsers", Boolean.TRUE);
        }
    }

    public boolean validatePermissions()
    {
        return WikiPermission.canAccessSetupPage(request);
    }
}

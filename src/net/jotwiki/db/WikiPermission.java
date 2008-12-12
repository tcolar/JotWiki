/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import net.jot.db.authentication.JOTAuthPermission;
import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowRequest;
import net.jotwiki.WikiUtilities;

/**
 * Note: if adding new permissions, probably need to update ProfileForm !
 * @author tcolar
 */
public class WikiPermission extends JOTAuthPermission
{
    // high level "dangerous" permissions
    // full acess to everything (superuser : admin)
    public transient static final String _FULL_ACCESS_ = "_FULL_ACCESS_";
    // setup access, almost full permissions
    public transient static final String SETUP = "SETUP";
    // access to file system !
    public transient static final String MANAGE_FILES = "MANAGE_FILES";

    // User permissions
    public transient static final String VIEW_PAGE = "VIEW_PAGE";
    public transient static final String SEARCH = "SEARCH";
    public transient static final String CREATE_PAGE = "CREATE_PAGE";
    public transient static final String EDIT_OWN_PAGE = "EDIT_OWN_PAGE";
    public transient static final String EDIT_OTHERS_PAGE = "EDIT_OTHERS_PAGE";
    public transient static final String DELETE_OWN_PAGE = "DELETE_OWN_PAGE";
    public transient static final String DELETE_OTHERS_PAGE = "DELETE_OTHERS_PAGE";
    // files upload.
    public transient static final String UPLOAD_FILES = "UPLOAD_FILES";
    public transient static final String UPLOAD_FILES_CREATE_FOLDERS = "UPLOAD_FILES_CREATE_FOLDERS";
    public transient static final String UPLOAD_FILES_VIEW_FILES = "UPLOAD_FILES_VIEW_FILES";
    public transient static final String UPLOAD_FILES_BROWSING = "UPLOAD_FILES_BROWSING";
    public transient static final String UPLOAD_FILES_UPDATE = "UPLOAD_FILES_UPDATE_FILES";
    public transient static final String UPLOAD_FILES_DELETE = "UPLOAD_FILES_DELETING";
    public transient static final String UPLOAD_FILES_ADD_FILES = "UPLOAD_FILES_ADDING";
    // manage stuff
    public transient static final String MANAGE_USERS = "CREATE_USERS";
    public transient static final String MANAGE_PROFILES = "CREATE_PROFILES";
    public transient static final String MANAGE_NAMESPACES = "CREATE_PROFILES";

    // Note: if adding new permissins, probably need to update ProfileForm !
    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    /**
     * Checks wether the currently logged user is allowed to do something in the given ns.
     * @param permission
     * @return
     */
    public static boolean hasPermission(HttpServletRequest request, String ns, String permission)
    {
        // returns logged user (or guest)
        WikiUser user = WikiUser.getCurrentUser(request);
        boolean result = WikiUser.hasPermission(user, ns, permission);
        String userName = "NULL";
        if (user != null)
        {
            userName = user.getLogin();
        }
        JOTLogger.log(JOTLogger.DEBUG_LEVEL, "WikiPermission", "Checked perm: " + permission + " for:" + userName + " -> " + result);
        return result;
    }

    /**
     * check wether the user can do something in the current ns
     * @param request
     * @param permission
     * @return
     * @throws java.lang.Exception
     */
    public static boolean hasPermission(HttpServletRequest request, String permission)
    {
        String ns = WikiUtilities.getNamespace(request);
        return hasPermission(request, ns, permission);
    }

    public static boolean hasEditPermission(JOTFlowRequest request)
    {
        // TODO: set ownPage to real value
        boolean ownPage = true;
        if (ownPage)
        {
            return hasPermission(request, WikiPermission.EDIT_OWN_PAGE);
        }
        return hasPermission(request, WikiPermission.EDIT_OTHERS_PAGE);
    }

    public static boolean canAccessSetupPage(JOTFlowRequest request)
    {
        return hasPermission(request, WikiPermission.SETUP) ||
                hasPermission(request, WikiPermission.MANAGE_FILES) ||
                hasPermission(request, WikiPermission.MANAGE_PROFILES) ||
                hasPermission(request, WikiPermission.MANAGE_NAMESPACES) ||
                hasPermission(request, WikiPermission.MANAGE_USERS);

    }

    /**
     * Wether the current user can assign a specific subprofile to a user for a specific NS.
     * @param request
     * @param ns
     * @param subProfile
     * @return
     * @throws java.lang.Exception
     */
    public static boolean canAssignSubprofile(JOTFlowRequest request, String ns, String subProfile) throws Exception
    {
        WikiUser user = WikiUser.getCurrentUser(request);
        if(WikiUser.isSuperUser(user))
            return true;
        Long profile=ProfileCache.getInstance().getUserProfileId(user, ns);
        if(profile==null)
            return false;
        Vector subsNames=ProfileCache.getInstance().getSubProfilesNames(profile.longValue());
        Vector subs=ProfileCache.getInstance().getSubProfiles(profile.longValue());
        return subsNames.contains(subProfile) || subs.contains(new Long(WikiSubProfiles.ANY_SUBPROFILE));
    }
    
}

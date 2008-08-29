/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import net.jot.logger.JOTLogger;
import net.jot.web.ctrl.JOTController;
import net.jot.web.views.JOTGeneratedFormView;
import net.jotwiki.db.ProfileCache;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiProfile;
import net.jotwiki.forms.setup.ProfileForm;

/**
 * Handles a profile Edition request
 * @author tcolar
 */
public class ProfileEditController extends JOTController
{
    private static final String RESULT_DELETED="deleted";

    public String process() throws Exception
    {
        boolean isDelete = request.getParameter("actionType") != null && request.getParameter("actionType").equalsIgnoreCase("delete");
        if (isDelete)
        {
            String profile = request.getParameter("profile");
            try
            {
                if (profile.startsWith("~"))
                {
                    throw new Exception("Cannot delete this special profile!");
                }
                if (WikiProfile.getAssignedUsers(profile).length()>0)
                {
                    throw new Exception("Cannot delete the profile, in use by users: "+WikiProfile.getAssignedUsers(profile));
                }
                // all OK
                WikiProfile.removeProfile(profile);
                ProfileCache.getInstance().flushProfileNames();
                ProfileCache.getInstance().flushUserProfileAssignments();
            } catch (Exception e)
            {
                request.setAttribute("SetupMessage", e.getMessage());
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error", e);
            }
            return RESULT_DELETED;
        }

        // Otherwise isEdit:
        ProfileForm form = (ProfileForm) getForm(ProfileForm.class);
        request.setAttribute(JOTGeneratedFormView.GENERATED_FORM, form);

        return RESULT_SUCCESS;
    }

    public boolean validatePermissions()
    {
        boolean result = WikiPermission.hasPermission(request, WikiPermission.MANAGE_PROFILES);
        if (result == false)
        {
            request.setAttribute("SetupMessage", "You cannot modify this profile.");
        }

        return result;
    }
}

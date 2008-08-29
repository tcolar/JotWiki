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
import net.jotwiki.db.ProfileCache;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;
import net.jotwiki.forms.setup.UserForm;

/**
 * Handles a user Edition request
 * @author tcolar
 */
public class UserEditController extends JOTController
{
    private static final String RESULT_DELETED="deleted";

    public String process() throws Exception
    {
        boolean isDelete=request.getParameter("actionType")!=null && request.getParameter("actionType").equalsIgnoreCase("delete");
        if(isDelete)
        {
            String user=request.getParameter("user");
            try
            {
                if(!user.startsWith("~") &&  !user.equalsIgnoreCase("admin"))
                {
                    WikiUser.removeUser(user);
                    ProfileCache.getInstance().flushUserList();
                }
                else
                {
                    throw new Exception("Cannot delete this special user!");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("SetupMessage", e.getMessage());
            }
            return RESULT_DELETED;
        }
        // Otherwise isEdit:
        UserForm form = (UserForm) getForm(UserForm.class);
        request.setAttribute(JOTGeneratedFormView.GENERATED_FORM, form);
        return RESULT_SUCCESS;
    }

    public boolean validatePermissions()
    {
        return WikiPermission.hasPermission(request, WikiPermission.MANAGE_USERS);
    }
}

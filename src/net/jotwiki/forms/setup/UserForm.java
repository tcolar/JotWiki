/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms.setup;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTDBItemForm;
import net.jot.web.forms.ui.JOTFormCategory;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormPasswordField;
import net.jot.web.forms.ui.JOTFormSelectField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.ProfileCache;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiProfileSet;
import net.jotwiki.db.WikiUser;

/**
 * For editing / creating users
 * @author thibautc
 */
public class UserForm extends JOTDBItemForm
{

    private final static String LOGIN = "login";
    private final static String PASSWORD = "password";
    private final static String PASSWORD2 = "pasword2";
    private final static String FIRST = "firstName";
    private final static String LAST = "lastName";
    private final static String DESCRIPTION = "description";
    private final static String DEFAULT_PROFILE = "defaultProfile";

    public void layoutForm(JOTFlowRequest request)
    {
        setFormTitle("Edit User");
        setFormAction("submituser.do");
        //DB Fields
        addFormField(new JOTFormTextField(LOGIN, "Login", 20, ""));

        addFormField(new JOTFormPasswordField(PASSWORD, "Password", 10, ""));
        addFormField(new JOTFormPasswordField(PASSWORD2, "Password Confirmation", 10, (String) model.getFieldValue("password")));
        addFormField(new JOTFormTextField(FIRST, "First Name", 20, ""));
        addFormField(new JOTFormTextField(LAST, "Last Name", 20, ""));
        addFormField(new JOTFormTextField(DESCRIPTION, "Description", 30, ""));

        String[] defaultProfileSelection = null;
        if (!model.isNew())
        {
            WikiUser user = (WikiUser) model;
            // load default values
            try
            {
                Long profId = ProfileCache.getInstance().getUserProfileId(user, WikiUser.__ANY_NS__);
                if (profId != null)
                {
                    String profName=ProfileCache.getInstance().getProfile(profId.longValue()).getName();
                    defaultProfileSelection = new String[1];
                    defaultProfileSelection[0] = profName;
                }
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error reading existing use profile infos: ", e);
            }
        }

        //Custom fields
        Vector profileNames = getAssignableProfileNames(request, WikiUser.__ANY_NS__);
        String[] names = (String[]) profileNames.toArray(new String[0]);

        Vector namespaces = WikiPreferences.getInstance().getNamespaceList();

        addCategory(new JOTFormCategory("DEFAULT Profile"));
        JOTFormSelectField defaultProfile = new JOTFormSelectField(DEFAULT_PROFILE, "DEFAULT:", 1, names, defaultProfileSelection);
        defaultProfile.setHelp("A profile is a set or permissions to be assigned to a user.<br><br>You should assign a 'Default' profile to each user, and that will be used, unless you override it by specifying a namespace specific profile.");
        addFormField(defaultProfile);

        addCategory(new JOTFormCategory("Namespace Specific Profiles"));
        for (int i = 0; i != namespaces.size(); i++)
        {
            profileNames = getAssignableProfileNames(request, WikiUser.__ANY_NS__);
            profileNames.add(0, "-DEFAULT-");
            names = (String[]) profileNames.toArray(new String[0]);
            String ns = (String) namespaces.get(i);
            // finding current selection
            String[] profileSelection = null;
            if (!model.isNew())
            {
                WikiUser user = (WikiUser) model;
                // load default values
                try
                {
                    Long profId = ProfileCache.getInstance().getUserProfileId(user, ns, true);
                    String profName="-DEFAULT-";
                    if(profId!=null)
                        profName=ProfileCache.getInstance().getProfile(profId.longValue()).getName();
                    profileSelection = new String[1];
                    profileSelection[0] = profName;
                } catch (Exception e)
                {
                    JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error reading existing use profile infos: ", e);
                }
            }
            addFormField(new JOTFormSelectField("_NS_" + ns, ns, 1, names, profileSelection));
        }

        addSubmitButton(new JOTFormSubmitButton("Save User"));
    }

    // Generates form help icon and message HTML 
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        boolean isNew = model.isNew();
        String login = get(LOGIN).getValue();
        String oldLogin = (String) model.getFieldValue("login");
        String password = get(PASSWORD).getValue();
        String password2 = get(PASSWORD2).getValue();
        Hashtable results = new Hashtable();
        boolean isGuest = login.equals(WikiUser.__GUEST_USER__);

        // check user does not already exists (if new or edited)
        if ((isNew || !login.equals(oldLogin)) && !WikiUser.isNewUser(WikiUser.class, login))
        {
            results.put("1", "A user with that login already exists.");
        }
        // check not "admin"
        if (login.equalsIgnoreCase("admin"))
        {
            results.put("1", "'Admin' is a restricted name.");
        }
        // check login syntax valid (no special chars , >4 length)
        if (!isGuest && !WikiUtilities.isValidUrlSubtring(login))
        {
            results.put("1", "Login can only contain a-z A-Z 0-9 _");
        }
        if (!isNew && oldLogin.startsWith("~") && !oldLogin.equals(login))
        {
            results.put("1", "You cannot change this user login.");
        }
        // login length
        if (login.length() < 5)
        {
            results.put("1", "Login too short (<5)");
        }
        // check password > 4 length
        if (!isGuest && password.length() < 5)
        {
            results.put("1", "Password too short (<5)");
        }
        // check password and password2 match
        if (!isGuest && ! password.equals(password2))
        {
            results.put("1", "Passwords don't match");
        }
        return results;
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        return WikiPermission.hasPermission(request, WikiPermission.MANAGE_USERS);
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        String oldLogin = (String) model.getFieldValue("login");
        boolean isNew = model.isNew();
        String login = get(LOGIN).getValue();
        boolean isGuest = login.equals(WikiUser.__GUEST_USER__);
        if (isGuest)
        {
            get(PASSWORD).setValue("");
            get(PASSWORD2).setValue("");
        }

        // save the db fields using standard parent code
        super.save(request);

        long userId = model.getId();
        // save custom fields (profile assignment) (do only the ones allowed to)
        // for each ns, get requested profile assignment, chek wether user profile allow this subprofile.
        TreeMap profileNames = ProfileCache.getInstance().getProfileNames();
        String defaultProfile = request.getParameter(DEFAULT_PROFILE);
        if (defaultProfile != null && profileNames.containsKey(defaultProfile) && WikiPermission.canAssignSubprofile(request, WikiUser.__ANY_NS__, defaultProfile))
        {
            long profileId = ProfileCache.getInstance().getProfileId(defaultProfile).longValue();
            assignProfile(userId, WikiUser.__ANY_NS__, profileId);
        }
        Vector namespaces = WikiPreferences.getInstance().getNamespaceList();
        for (int i = 0; i != namespaces.size(); i++)
        {
            String ns = (String) namespaces.get(i);
            String profile = request.getParameter("_NS_" + ns);
            if (profile != null && profileNames.containsKey(profile) && WikiPermission.canAssignSubprofile(request, ns, profile))
            {
                long profileId = ProfileCache.getInstance().getProfileId(profile).longValue();
                assignProfile(userId, ns, profileId);
            }
            if(profile.equals("-DEFAULT-"))
            {
                removeProfileAssignment(userId, ns);
            }
        }
        // update cached stuff
        ProfileCache.getInstance().flushUserList();
        if (oldLogin != null && oldLogin.length() > 0)
        {
            ProfileCache.getInstance().flushUserProfileAssignment(oldLogin);
        }
        if (!isNew)
        {
            ProfileCache.getInstance().flushUserProfileAssignment(login);
        }
    }

    public void updateModel(JOTFlowRequest request) throws Exception
    {
        String login = request.getParameter("user");
        if (login != null)
        {
            model = WikiUser.getUserByLogin(WikiUser.class, login);
            // protect from changing the "Admin" user
            if (model == null || ((WikiUser) model).getLogin().equalsIgnoreCase("admin"))
            {
                model = new WikiUser();
            }
        } else
        {
            model = new WikiUser();
        }
    }

    /**
     * Save  a user profile assignment
     * @param login
     * @param ns
     * @param profile
     */
    private void assignProfile(long userId, String ns, long profileId)
    {
        JOTSQLCondition cond=new JOTSQLCondition("user", JOTSQLCondition.IS_EQUAL, new Long(userId));
        JOTSQLCondition cond2=new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
        try
        {
            // updates existing assignmenet or create new one if none yet.
            WikiProfileSet set = (WikiProfileSet) JOTQueryBuilder.selectQuery(WikiProfileSet.class).where(cond).where(cond2).findOrCreateOne();
            set.setUser(userId);
            set.setNameSpace(ns);
            set.setProfile(profileId);
            set.save();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error assigning profile", e);
        }
    }

    private void removeProfileAssignment(long userId, String ns)
    {
        JOTSQLCondition cond=new JOTSQLCondition("user", JOTSQLCondition.IS_EQUAL, new Long(userId));
        JOTSQLCondition cond2=new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
        try
        {
            WikiProfileSet set = (WikiProfileSet) JOTQueryBuilder.selectQuery(WikiProfileSet.class).where(cond).where(cond2).findOne();
            if(set!=null)
                set.delete();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error removing an assigned profile", e);
        }
    }

    /**
     * Which namespaces can be assigned by user for a given namespace
     * @param request
     * @param ns
     * @return
     */
    private Vector getAssignableProfileNames(JOTFlowRequest request, String ns)
    {
        Vector results = new Vector();
        Iterator profiles = ProfileCache.getInstance().getProfileNames().keySet().iterator();
        while (profiles.hasNext())
        {
            String profile = (String) profiles.next();
            try
            {
                if (WikiPermission.canAssignSubprofile(request, ns, profile))
                {
                    results.add(profile);
                }
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error checking assignments", e);
            }
        }
        return results;
    }
}

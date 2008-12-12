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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTFormConst;
import net.jot.web.forms.JOTGeneratedForm;
import net.jot.web.forms.ui.JOTFormCategory;
import net.jot.web.forms.ui.JOTFormCheckboxField;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormHiddenField;
import net.jot.web.forms.ui.JOTFormSelectField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.ProfileCache;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiProfile;
import net.jotwiki.db.WikiProfile;
import net.jotwiki.db.WikiSubProfiles;

/**
 * Form for editing/creating a profile
 * @author thibautc
 */
public class ProfileForm extends JOTGeneratedForm
{

    private final static String PROFILE_NAME = "profile";
    private final static String OLD_PROFILE_NAME = "oldProfileName";
    private final static String PROFILES_SELECT = "profilesSelect";
    private static Vector superPerms;
    private static Vector specialPerms;
    private static Vector contentPerms;
    private static Vector userPerms;

    // define the fields to show in the form
    static
    {
        superPerms = new Vector();
        superPerms.add(new ProfileEntry(WikiPermission._FULL_ACCESS_, "<font color='#ff0000'><b>!! All Permissions !!</b></font>", "Gives access to everything, current and future permissions."));
        specialPerms = new Vector();
        specialPerms.add(new ProfileEntry(WikiPermission.SETUP, "<font color='#ffaa00'><b>Setup permission</b></font>", "Gives access to the global setup and setting of the admin password."));
        specialPerms.add(new ProfileEntry(WikiPermission.MANAGE_FILES, "<font color='#ffaa00'><b>Manage the filesystem</b></font>", "Allow to remotely manage all jotwiki files."));
        contentPerms = new Vector();
        contentPerms.add(new ProfileEntry(WikiPermission.VIEW_PAGE, "View pages"));
        contentPerms.add(new ProfileEntry(WikiPermission.SEARCH, "Use search feature"));
        contentPerms.add(new ProfileEntry(WikiPermission.EDIT_OWN_PAGE, "Edit own pages"));
        contentPerms.add(new ProfileEntry(WikiPermission.EDIT_OTHERS_PAGE, "Edit others pages"));
        contentPerms.add(new ProfileEntry(WikiPermission.DELETE_OWN_PAGE, "Delete own pages"));
        contentPerms.add(new ProfileEntry(WikiPermission.DELETE_OTHERS_PAGE, "Delete others pages"));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES, "Attach files/images", "Allow to add existing images/files from the jotwiki files folder."));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_ADD_FILES, "Upload new files/images"));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_BROWSING, "Browse for files/images", "Wether to allow browsing around to pick a file/image or just stick to the main directory only."));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_CREATE_FOLDERS, "Creating subfolders when adding images"));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_DELETE, "Delete existing images/files."));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_UPDATE, "Update/replace existing files/images"));
        contentPerms.add(new ProfileEntry(WikiPermission.UPLOAD_FILES_VIEW_FILES, "View/Download existing files/images", "Allows to open/download the existing files."));
        userPerms = new Vector();
        userPerms.add(new ProfileEntry(WikiPermission.MANAGE_PROFILES, "<font color='#ffaa00'><b>Manage profiles</b></font>", "Profiles are 'Permissions Set' to be assigned to a user."));
        userPerms.add(new ProfileEntry(WikiPermission.MANAGE_USERS, "<font color='#ffaa00'><b>Manage users</b></font>"));
    }

    public void layoutForm(JOTFlowRequest request)
    {
        setFormTitle("Edit Profile");
        setFormAction("submitprofile.do");
        String profName = "";
        long profileId = -1;
        Map debug=request.getParameterMap();
        if (request.getParameter("profile") != null)
        {
            profName = request.getParameter("profile");

            JOTSQLCondition cond=new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, profName);
            try
            {
                WikiProfile profile = (WikiProfile) JOTQueryBuilder.selectQuery(WikiProfile.class).where(cond).findOne();
                if (profile != null)
                {
                    profileId = profile.getId();
                }
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error trying to get profile " + profName, e);
            }
        }
        JOTFormTextField prf = new JOTFormTextField(PROFILE_NAME, "Profile Name", 20, profName);
        addFormField(prf);
        if (request.getParameter("profile") != null)
        {
            addFormField(new JOTFormHiddenField(OLD_PROFILE_NAME, profName));
        }
        
        addCategory(new JOTFormCategory("Super User Permission"));
        addFields(profileId, superPerms);
        addCategory(new JOTFormCategory("High level Permissions"));
        addFields(profileId, specialPerms);
        addCategory(new JOTFormCategory("User Management Permissions"));
        addFields(profileId, userPerms);
        // user profiles assignment perms.
        Vector v = new Vector();
        try
        {
            v = new Vector(ProfileCache.getInstance().getProfileNames().keySet());
        } catch (Exception e)
        {
        }
        v.add(0, "-NONE-");
        v.add(1, "-ANY-");
        String[] values = (String[]) v.toArray(new String[0]);
        String[] selected = getSelectedProfiles(profileId);
        JOTFormSelectField select = new JOTFormSelectField(PROFILES_SELECT, "Can assign those Profiles to users:", 5, values, selected);
        select.setHelp("If allowed to manage/create users, which profiles can be given to the added/edited users.<br>Be careful with this, for example if you select 'ANY', that means the user could create another user with full permissions to everyting.<br><b>Note:</b>You can select mutliple entrie by clicking while holding the 'CTRL' key.");
        select.setAllowMultiples(true);
        addFormField(select);
        addCategory(new JOTFormCategory("Content Permissions(Namespace specific)"));
        addFields(profileId, contentPerms);

        addSubmitButton(new JOTFormSubmitButton("Save"));
    }

    // Generates form help icon and message HTML 
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        Hashtable errors = new Hashtable();
        // validate the name is ok (ie: not starting with ~)
        String oldName = request.getParameter(OLD_PROFILE_NAME);
        boolean isNew = oldName == null;
        String newName = get(PROFILE_NAME).getValue();
        if (isNew)
        {
            if (newName.startsWith("~") || !WikiUtilities.isValidUrlSubtring(newName) || newName.length() < 3)
            {
                errors.put("1", "Invalid Profile Name.");
            }
            if (ProfileCache.getInstance().getProfileNames().keySet().contains(newName))
            {
                errors.put("1", "This Namespace name is already in use !");
            }
        } else
        {
            // not new, but edited name
            if (!newName.equals(oldName))
            {
                if (ProfileCache.getInstance().getProfileNames().keySet().contains(newName))
                {
                    errors.put("1", "This Namespace name is already in use !");
                }
                if (oldName.startsWith("~"))
                {
                    errors.put("1", "This Namespace cannot be renamed.");
                }
            }
        }
        return errors;
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        return WikiPermission.hasPermission(request, WikiPermission.MANAGE_PROFILES);
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        String oldName = request.getParameter(OLD_PROFILE_NAME);
        boolean isNew = request.getParameter(OLD_PROFILE_NAME) == null;
        String newName = get(PROFILE_NAME).getValue();
        long id = -1;
        if (isNew)
        {
            // save new one
            WikiProfile profile = new WikiProfile();
            profile.setName(newName);
            profile.setDescription(newName);
            profile.setRemovable(true);
            profile.save();
            id = profile.getId();
        } else
        {
            //update existing one
            JOTSQLCondition cond=new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, request.getParameter(OLD_PROFILE_NAME));
            WikiProfile profile = (WikiProfile) JOTQueryBuilder.selectQuery(WikiProfile.class).where(cond).findOne();
            if (profile != null)
            {
                if (isRemovable(OLD_PROFILE_NAME))
                {
                    // notremovable = can't be renamed
                    profile.setName(newName);
                    profile.setDescription(newName);
                }

                profile.save();
                id = profile.getId();
            }
        }
        if (id != -1)
        {
            // deal with the permissions
            // remove existing entries.
            JOTSQLCondition cond=new JOTSQLCondition("profile", JOTSQLCondition.IS_EQUAL, new Long(id));
            Vector perms = JOTQueryBuilder.selectQuery(WikiPermission.class).where(cond).find().getAllResults();
            for (int i = 0; i != perms.size(); i++)
            {
                WikiPermission perm = (WikiPermission) perms.get(i);
                perm.delete();
            }
            //adding new ones
            String perm = WikiPermission._FULL_ACCESS_;
            if (get(perm) != null && get(perm).getValue() != null && get(perm).getValue().equals(JOTFormConst.VALUE_CHECKED))
            {
                // if the user has the super perm, then we add that one only
                WikiPermission sub = new WikiPermission();
                sub.setProfile(id);
                sub.setPermission(WikiPermission._FULL_ACCESS_);
                sub.save();
            } else
            {
                // otherwise add independant perms
                addPerms(id, specialPerms);
                addPerms(id, contentPerms);
                addPerms(id, userPerms);
            }

            // deal with subprofiles
            // remove current ones
            JOTSQLCondition cond2=new JOTSQLCondition("profile", JOTSQLCondition.IS_EQUAL, new Long(id));
            Vector subs = JOTQueryBuilder.selectQuery(WikiSubProfiles.class).where(cond2).find().getAllResults();
            for (int i = 0; i != subs.size(); i++)
            {
                WikiSubProfiles sub = (WikiSubProfiles) subs.get(i);
                sub.delete();
            }
            // adding new ones
            String[] profs = request.getParameterValues("profilesSelect");
            if (profs != null)
            {
                List prfs = java.util.Arrays.asList(profs);
                if (!prfs.contains("-NONE-") && prfs.contains("-ANY-"))
                {
                    // creating a special "any" entry
                    WikiSubProfiles sub = new WikiSubProfiles();
                    sub.setProfile(id);
                    sub.setSubProfile(WikiSubProfiles.ANY_SUBPROFILE);
                    sub.save();
                } else if (!prfs.contains("-NONE-"))
                {
                    // creating individual entries
                    TreeMap map = ProfileCache.getInstance().getProfileNames();
                    Iterator profiles = map.keySet().iterator();
                    while (profiles.hasNext())
                    {
                        String profile = (String) profiles.next();
                        Long profileId = (Long) map.get(profile);
                        if (profileId != null)
                        {
                            long prfId = profileId.longValue();
                            if (prfs.contains(profile))
                            {
                                // adding the entry
                                WikiSubProfiles sub = new WikiSubProfiles();
                                sub.setProfile(id);
                                sub.setSubProfile(prfId);
                                sub.save();
                            }
                        }
                    }
                }
            }
            // update the cache to pick up new/updated profile
            ProfileCache.getInstance().flushProfile(id);
            ProfileCache.getInstance().flushProfilePermissions(new Long(id));
            ProfileCache.getInstance().flushProfileNames();
        }
    }

    private void addFields(long profileId, Vector entries)
    {
        for (int i = 0; i != entries.size(); i++)
        {
            ProfileEntry entry = (ProfileEntry) entries.get(i);
            //TODO: if edit: set value.
            boolean value = false;
            if (profileId > -1)
            {
                try
                {
                    value = ProfileCache.getInstance().getProfilePerms(new Long(profileId)).contains(entry.getName());
                } catch (Exception e)
                {
                    JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error trying to get profile perm " + profileId + "/" + entry, e);
                }
            }
            JOTFormField field = new JOTFormCheckboxField(entry.getName(), entry.getDescription(), value);
            if (entry.getHelp() != null)
            {
                field.setHelp(entry.getHelp());
            }
            addFormField(field);
        }
    }

    private void addPerms(long id, Vector perms) throws Exception
    {
        for (int i = 0; i != perms.size(); i++)
        {
            ProfileEntry entry = (ProfileEntry) perms.get(i);
            String perm = entry.getName();
            if (get(perm) != null && get(perm).getValue() != null && get(perm).getValue().equals(JOTFormConst.VALUE_CHECKED))
            {
                WikiPermission sub = new WikiPermission();
                sub.setProfile(id);
                sub.setPermission(perm);
                sub.save();
            }
        }
    }

    private String[] getSelectedProfiles(long profileId)
    {
        String[] result = {};
        try
        {
            Vector subsIds = ProfileCache.getInstance().getSubProfiles(profileId);
            Vector subsNames = ProfileCache.getInstance().getSubProfilesNames(profileId);
            if (subsIds.contains(new Long(WikiSubProfiles.ANY_SUBPROFILE)))
            {
                result = new String[1];
                result[0] = "-ANY-";
            } else
            {
                result = (String[]) subsNames.toArray(new String[0]);
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error trying to get selected subprofiles " + profileId, e);
        }
        return result;
    }

    private boolean isRemovable(String profName)
    {
        boolean result = true;
        try
        {
            JOTSQLCondition cond=new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, profName);
            WikiProfile profile = (WikiProfile) JOTQueryBuilder.selectQuery(WikiProfile.class).where(cond).findOne();
            if (profile != null)
            {
                result = profile.isRemovable();
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error trying to get profile " + profName, e);
        }
        return result;
    }

    /**
     * Internal class, data holder
     */
    static class ProfileEntry
    {

        private String name;
        private String description;
        private String help = null;

        public String getDescription()
        {
            return description;
        }

        public String getHelp()
        {
            return help;
        }

        public String getName()
        {
            return name;
        }

        public ProfileEntry(String name, String description)
        {
            this(name, description, null);
        }

        public ProfileEntry(String name, String description, String help)
        {
            this.name = name;
            this.description = description;
            this.help = help;
        }
    }
}

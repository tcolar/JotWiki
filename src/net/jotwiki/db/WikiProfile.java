/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import java.util.Vector;
import net.jot.db.authentication.JOTAuthProfile;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.query.JOTQueryManager;
import net.jotwiki.db.WikiSubProfiles;

/**
 * Defines the permissions composing a profile.
 * 
 * User profile DB object
 * @author tcolar
 */
public class WikiProfile extends JOTAuthProfile
{

    public boolean dataRemovable = true;
    public static final String STANDARD_GUEST_PROFILE = "~~GUEST_PROFILE~~";
    // vector of strings (profileName)
    public static Vector profilesList = null;

    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    public boolean isRemovable()
    {
        return dataRemovable;
    }

    public void setRemovable(boolean b)
    {
        dataRemovable = b;
    }

    /**
     * Delete a profile and associated subprofile entries.
     * Be careful that no user should be using this profile or it will be an issue
     * @param profile
     * @throws java.lang.Exception
     */
    public static void removeProfile(String profile) throws Exception
    {
        WikiProfile prof = (WikiProfile) getByName(profile);
        Long profId = new Long(prof.getId());
        // delete the profile itself.
        prof.delete();
        // delete this profile subprofile entries.
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataProfile", JOTSQLCondition.IS_EQUAL, profId));
        Vector entries = JOTQueryManager.find(WikiSubProfiles.class, params);
        for (int i = 0; i != entries.size(); i++)
        {
            WikiSubProfiles prfEntry = (WikiSubProfiles) entries.get(i);
            prfEntry.delete();
        }
        // delete entries from profiles that have this as a subprofile.
        JOTSQLQueryParams params2 = new JOTSQLQueryParams();
        params2.addCondition(new JOTSQLCondition("dataSubProfile", JOTSQLCondition.IS_EQUAL, profId));
        Vector entries2 = JOTQueryManager.find(WikiSubProfiles.class, params2);
        for (int i = 0; i != entries2.size(); i++)
        {
            WikiSubProfiles prfEntry = (WikiSubProfiles) entries2.get(i);
            prfEntry.delete();
        }

    }

    /**
     * Cpmma separated list of user logins, of users which uses the profile.
     * @param profile
     * @return
     */
    public static String getAssignedUsers(String profile) throws Exception
    {
        WikiProfile prof = (WikiProfile) getByName(profile);
        Long profileId = new Long(prof.getId());
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataProfile", JOTSQLCondition.IS_EQUAL, profileId));
        Vector entries = JOTQueryManager.find(WikiProfileSet.class, params);
        String results = "";
        for (int i = 0; i != entries.size(); i++)
        {
            WikiProfileSet set = (WikiProfileSet) entries.get(i);
            long user = set.getUser();
            WikiUser usr = (WikiUser) JOTQueryManager.findByID(WikiUser.class, user);
            results += usr.getLogin() + ", ";
        }
        return results;
    }

    /**
     * Wether the lprofile is available (name not already in use)
     */
    public static boolean isNewProfile(String profileName) throws Exception
    {
        return getByName(profileName) == null;
    }

    public static WikiProfile getByName(String profileName) throws Exception
    {
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataName", JOTSQLCondition.IS_EQUAL, profileName));
        return (WikiProfile) JOTQueryManager.findOne(WikiProfile.class, params);
    }
    
    public static boolean findPermission(WikiProfile profile, String permission) throws Exception
    {
        Vector perms=ProfileCache.getInstance().getProfilePerms(new Long(profile.getId()));
        return perms.contains(permission);
    }
 
}

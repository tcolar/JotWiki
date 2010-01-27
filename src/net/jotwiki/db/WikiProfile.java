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
import net.jot.persistance.builders.JOTQueryBuilder;

/**
 * Defines the permissions composing a profile.
 * 
 * User profile DB object
 * @author tcolar
 */
public class WikiProfile extends JOTAuthProfile
{

    public boolean removable = true;
    public transient static final String STANDARD_GUEST_PROFILE = "~~GUEST_PROFILE~~";
    // vector of strings (profileName)
    public transient static Vector profilesList = null;

    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    public boolean isRemovable()
    {
        return removable;
    }

    public void setRemovable(boolean b)
    {
        removable = b;
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
        JOTSQLCondition cond=new JOTSQLCondition("profile", JOTSQLCondition.IS_EQUAL, profId);
        Vector entries = JOTQueryBuilder.selectQuery(null, WikiSubProfiles.class).where(cond).find().getAllResults();
        for (int i = 0; i != entries.size(); i++)
        {
            WikiSubProfiles prfEntry = (WikiSubProfiles) entries.get(i);
            prfEntry.delete();
        }
        // delete entries from profiles that have this as a subprofile.
        JOTSQLCondition cond2=new JOTSQLCondition("subProfile", JOTSQLCondition.IS_EQUAL, profId);
        Vector entries2 = JOTQueryBuilder.selectQuery(null, WikiSubProfiles.class).where(cond2).find().getAllResults();
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
        JOTSQLCondition cond=new JOTSQLCondition("profile", JOTSQLCondition.IS_EQUAL, profileId);
        Vector entries = JOTQueryBuilder.selectQuery(null, WikiProfileSet.class).where(cond).find().getAllResults();
        String results = "";
        for (int i = 0; i != entries.size(); i++)
        {
            WikiProfileSet set = (WikiProfileSet) entries.get(i);
            long user = set.getUser();
            WikiUser usr = (WikiUser) JOTQueryBuilder.findByID(null, WikiUser.class, user);
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
        JOTSQLCondition cond=new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, profileName);
        return (WikiProfile) JOTQueryBuilder.selectQuery(null, WikiProfile.class).where(cond).findOne();
    }
    
    public static boolean findPermission(WikiProfile profile, String permission) throws Exception
    {
        Vector perms=ProfileCache.getInstance().getProfilePerms(new Long(profile.getId()));
        return perms.contains(permission);
    }
 
}

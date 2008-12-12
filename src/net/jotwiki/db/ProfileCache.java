/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;

/**
 * Caches users, profiles/permissions and such for performance reasons
 * @author thibautc
 */
public class ProfileCache
{

    // singleton
    private final static ProfileCache cache = new ProfileCache();
    /** hashtable of profileData objects*/
    private TreeMap profiles = new TreeMap();
    /** cache profile name to id map (String name -> Long id)*/
    private Hashtable nameToId = new Hashtable();
    private TreeMap profileNames = null;
    private TreeMap users = null;
    /**
     * Stores a hash:           login -> assignment(Hashtable)
     * Assignment is a hash     namespace -> profileId(Long)  
     */
    private TreeMap profileAssignments = new TreeMap();
    /**
     * Hashtable of Vector of strings containing the permissions assigned to a profile.
     * profile ID -> Vector(permissions)
     * @return
     */
    private TreeMap profilePerms = new TreeMap();

    public final static ProfileCache getInstance()
    {
        return cache;
    }
    private ProfileCache(){}

    public WikiProfile getProfile(long profileId) throws Exception
    {
        Long id = new Long(profileId);
        if (profiles.get(id) == null)
        {
            synchronized (this)
            {
                // get the profile
                WikiProfile profile = (WikiProfile) JOTQueryBuilder.findByID(WikiProfile.class, profileId);
                ProfileData data = new ProfileData(profile);
                // get the sub profiles
                if (profile != null)
                {
                    JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Caching profile(and subs): " + profile.getDescription());
                    Vector subs = getSubProfiles(profile.getId());
                    data.setSubProfiles(subs);
                }
                // store in cache
                profiles.put(id, data);
                nameToId.put(profile.getName(), new Long(profile.getId()));
            }
        }

        return (WikiProfile) ((ProfileData) profiles.get(id)).getProfile();
    }

    /**
     * Vector of long(subprofiles ID's) (not cached)
     * @return
     */
    public Vector getSubProfiles(long profileId) throws Exception
    {
        Vector results = new Vector();
        JOTSQLCondition cond=new JOTSQLCondition("dataProfile", JOTSQLCondition.IS_EQUAL, new Long(profileId));
        Vector subs = JOTQueryBuilder.selectQuery(WikiSubProfiles.class).where(cond).find().getAllResults();
        // read and store them
        for (int i = 0; subs != null && i != subs.size(); i++)
        {
            WikiSubProfiles prof = (WikiSubProfiles) subs.get(i);
            Long sub = new Long(prof.getSubProfile());
            results.add(sub);
        }
        return results;
    }

    /**
     * Return the subprofile list as names rather than ids.
     * @param profileId
     * @return
     * @throws java.lang.Exception
     */
    public Vector getSubProfilesNames(long profileId) throws Exception
    {
        Vector results = new Vector();
        Vector ids = getSubProfiles(profileId);
        for (int i = 0; i != ids.size(); i++)
        {
            Long idL = (Long) ids.get(i);
            if (idL != null)
            {
                long id = idL.longValue();
                WikiProfile profile = getProfile(id);
                if (profile != null)
                {
                    results.add(profile.getName());
                }
            }
        }
        return results;
    }

    /**
     * Return a treemap of all the existing profiles (name->id), sorted by name alphabetically
     * @return
     * @throws java.lang.Exception
     */
    public synchronized TreeMap getProfileNames()
    {
        if (profileNames == null)
        {
            synchronized (this)
            {
                JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Caching profile names");
                profileNames = new TreeMap();
                try
                {
                    Vector v = JOTQueryBuilder.findAll(WikiProfile.class).getAllResults();
                    for (int i = 0; i != v.size(); i++)
                    {
                        WikiProfile profile = (WikiProfile) v.get(i);
                        profileNames.put(profile.getName(), new Long(profile.getId()));
                    }
                } catch (Exception e)
                {
                    JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to read profile names from DB, not initialized yet ?", e);

                }
            }
        }
        return profileNames;
    }

    /**
     *  flush whole cache
     *  and force rereading all from db
     *  including profile names
     */
    public synchronized void flushAll()
    {
        profiles.clear();
        flushProfileNames();
        flushUserProfileAssignments();
        profilePerms.clear();
        nameToId.clear();
    }

    /**
     * flush a particular profile from profile cache and profilename
     * Note: you might want to call flushProfileNames as well
     * @param profileId
     */
    public synchronized void flushProfile(long profileId)
    {
        profiles.remove(new Long(profileId));
        try
        {
            WikiProfile prof = getProfile(profileId);
            if (prof != null)
            {
                nameToId.remove(prof.getName());
            }
        } catch (Exception e)
        {
        }
    }

    /**
     * Flushes the profile names cache
     */
    public synchronized void flushProfileNames()
    {
        profileNames = null;
    }

    /**
     * Flushes which profile is assigned to a user/namespace combo 
     */
    public synchronized void flushUserProfileAssignments()
    {
        profileAssignments.clear();
    }

    /**
     * Flushes which profile is assigned to a user/namespace combo 
     */
    public synchronized void flushUserProfileAssignment(String login)
    {
        profileAssignments.remove(login);
    }

    /**
     * Flush permission for a profile from the cache 
     */
    public synchronized void flushProfilePermissions(Long profileId)
    {
        profilePerms.remove(profileId);
    }

    public Long getUserProfileId(WikiUser user, String ns) throws Exception
    {
        Long result = getUserProfileId(user, ns, false);
        return result;
    }

    /**
     * return the assigned profile for a given user/namespace (lazilly cached) or null if none.
     * @param user
     * @param ns
     */
    public Long getUserProfileId(WikiUser user, String ns, boolean nullIfUnset) throws Exception
    {
        String login = user.getLogin();
        if (!profileAssignments.containsKey(login))
        {
            synchronized (this)
            {
                JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Caching profile assignments for: " + user.getLogin());

                TreeMap assignments = new TreeMap();
                JOTSQLCondition cond=new JOTSQLCondition("dataUser", JOTSQLCondition.IS_EQUAL, new Long(user.getId()));
                Vector userProfiles = JOTQueryBuilder.selectQuery(WikiProfileSet.class).where(cond).find().getAllResults();
                if (userProfiles != null)
                {
                    for (int i = 0; i != userProfiles.size(); i++)
                    {
                        WikiProfileSet set = (WikiProfileSet) userProfiles.get(i);
                        assignments.put(set.getNameSpace(), new Long(set.getProfile()));
                    }
                }
                profileAssignments.put(login, assignments);
            }
        }
        TreeMap assignment = (TreeMap) profileAssignments.get(login);
        Long result = (Long) assignment.get(ns);
        // if no assignment for this NS, then try the defaultNS assignment
        if (!nullIfUnset && result == null)
        {
            result = (Long) assignment.get(WikiUser.__ANY_NS__);
        }
        return result;
    }

    /**
     * Return a vector of permissions strings defined in the profile (lazilly cached)
     * @param longValue
     */
    public Vector getProfilePerms(Long profileId) throws Exception
    {
        if (!profilePerms.containsKey(profileId))
        {
            synchronized (this)
            {
                JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Caching perms for profile: " + profileId);
                Vector perms = new Vector();
                JOTSQLCondition cond=new JOTSQLCondition("dataProfile", JOTSQLCondition.IS_EQUAL, profileId);
                Vector profPerms = JOTQueryBuilder.selectQuery(WikiPermission.class).where(cond).find().getAllResults();
                if (profPerms != null)
                {
                    for (int i = 0; i != profPerms.size(); i++)
                    {
                        WikiPermission perm = (WikiPermission) profPerms.get(i);
                        perms.add(perm.getPermission());
                    }
                }
                profilePerms.put(profileId, perms);
            }
        }
        return (Vector) profilePerms.get(profileId);
    }

    /**
     * Gets a list of all the users (lazilly cached)
     * TreeMap login -> ID
     * @return
     */
    public TreeMap getUsers(boolean includeAdmin) throws Exception
    {
        if (users == null)
        {
            synchronized (this)
            {
                try
                {
                    users = new TreeMap();
                    JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Caching user list");
                    Vector userV = JOTQueryBuilder.selectQuery(WikiUser.class).orderBy("dataLogin").find().getAllResults();
                    if (userV != null)
                    {
                        for (int i = 0; i != userV.size(); i++)
                        {
                            WikiUser user = (WikiUser) userV.get(i);
                            // exclude special admin user if necessary
                            if (!user.getLogin().equalsIgnoreCase("admin") || includeAdmin)
                            {
                                users.put(user.getLogin(), new Long(user.getId()));
                            }
                        }
                    }
                } catch (Exception e)
                {
                    JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to read user list from DB, not initialized yet ?", e);
                }
            }
        }
        return users;
    }

    /**
     * Return which profile the user has for a given namespace
     * @param login
     * @param namespace
     */
    /**
     * Force rereading the user list from DB
     */
    public void flushUserList()
    {
        users = null;
    }

    /**
     * Gets a profile ID from it's name
     * @param profileName
     */
    public Long getProfileId(String profileName)
    {
        // initilaizes the profile names if necessary
        TreeMap names = getProfileNames();
        return (Long) names.get(profileName);
    }

    /**
     * Holder object, storing a profile, and it's subProfiles(Vector)
     */
    protected class ProfileData
    {

        private WikiProfile profile;
        /**
         * Vector of sub profiles ID's (Long)
         */
        private Vector subProfiles = new Vector();

        public ProfileData(WikiProfile profile)
        {
            this.profile = profile;
            subProfiles = new Vector();
        }

        /**
         * vector of subprofs ID(Long)
         * @param subs
         */
        private void setSubProfiles(Vector subs)
        {
            subProfiles = subs;
        }

        public WikiProfile getProfile()
        {
            return profile;
        }

        public Vector getSubProfiles()
        {
            return subProfiles;
        }
    }
}

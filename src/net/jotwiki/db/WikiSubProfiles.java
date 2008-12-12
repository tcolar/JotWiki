/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 * If a user has the permissions to create other users and assign them profiles, this tables stores the list of profiles he can assign.
 * So in other word it stores which (sub)profiles can be assigned by a particualr profile.
 * Example: parent profile "manager" can assign users the subProfiles "editor" and "viewer".
 * @author thibautc
 */
public class WikiSubProfiles extends JOTModel
{
    /** The "parent" profile ID*/
    public long profile;
    /** The sub profile that can be assigned by the owner of the "parent" profile.*/
    public long subProfile;
    
    /** If we have one with sub=0, it means ANY subs*/
    public transient static final long ANY_SUBPROFILE=0;
    
    public void customize(JOTModelMapping mapping)
    {
    }

    public String defineStorage()
    {
		return DEFAULT_STORAGE;
    }

    public long getSubProfile()
    {
        return subProfile;
    }
    
    public long getProfile()
    {
        return profile;
    }

    public void setProfile(long dataProfile)
    {
        this.profile = dataProfile;
    }

    public void setSubProfile(long dataSubProfile)
    {
        this.subProfile = dataSubProfile;
    }
    
}

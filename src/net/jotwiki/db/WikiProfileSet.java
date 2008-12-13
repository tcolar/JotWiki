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
 * Each user can have a different profile assignement for each namespace.
 * @author thibautc
 */
public class WikiProfileSet extends JOTModel
{
    public long user;
    public String nameSpace="";
    public long profile;
    
    public void customize(JOTModelMapping mapping)
    {
        mapping.defineFieldSize("nameSpace", 30);
    }

    public String defineStorage()
    {
		return DEFAULT_STORAGE;
    }

    public String getNameSpace()
    {
        return nameSpace;
    }

    public long getProfile()
    {
        return profile;
    }

    public long getUser()
    {
        return user;
    }

    public void setNameSpace(String dataNameSpace)
    {
        this.nameSpace = dataNameSpace;
    }

    public void setProfile(long dataProfile)
    {
        this.profile = dataProfile;
    }

    public void setUser(long dataUser)
    {
        this.user = dataUser;
    }

}

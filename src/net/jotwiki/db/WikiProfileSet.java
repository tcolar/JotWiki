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
    public long dataUser;
    public String dataNameSpace="";
    public long dataProfile;
    
    public void customize(JOTModelMapping mapping)
    {
        mapping.defineFieldSize("dataNameSpace", 30);
    }

    public String defineStorage()
    {
		return DEFAULT_STORAGE;
    }

    public String getNameSpace()
    {
        return dataNameSpace;
    }

    public long getProfile()
    {
        return dataProfile;
    }

    public long getUser()
    {
        return dataUser;
    }

    public void setNameSpace(String dataNameSpace)
    {
        this.dataNameSpace = dataNameSpace;
    }

    public void setProfile(long dataProfile)
    {
        this.dataProfile = dataProfile;
    }

    public void setUser(long dataUser)
    {
        this.dataUser = dataUser;
    }

}

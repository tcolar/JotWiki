/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import net.jot.persistance.JOTDBUpgrader;

/**
 * Handles DB Upgrades
 * @author thibautc
 */
public class DBUpgrader extends JOTDBUpgrader
{

    public static final int CURRENT_VERSION = 4;

    public int getLatestVersion()
    {
        return CURRENT_VERSION;
    }

    public void upgradeDb(int version) throws Exception
    {
        /*if(version<2)
        {
        addTableColumn(WikiUser.class, "dataRemovable", Boolean.TRUE);
        addTableColumn(WikiProfile.class, "dataRemovable", Boolean.TRUE);
        // adding new guest profile (not used by any real user, profile for 'anonymous' users)
        WikiProfile guestProfile=new WikiProfile();
        guestProfile.setName(WikiProfile.STANDARD_GUEST_PROFILE);
        guestProfile.setDescription("Standard jotwiki profile for a guest(unlogged) user.");
        guestProfile.setRemovable(false);
        guestProfile.save();
        }// end of version 1 to version 2
        if(version<3)
        {
        addTableColumn(PageOptions.class, "dataCommentsEnabled", Boolean.TRUE);
        addTableColumn(PageOptions.class, "dataCommentsNb", new Integer(5));
        addTableColumn(PageOptions.class, "dataCommentsGuest", Boolean.TRUE);
        addTableColumn(PageOptions.class, "dataCommentsEmail", "");
        }*/
    }
}

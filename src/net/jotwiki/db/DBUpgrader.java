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
    }
}

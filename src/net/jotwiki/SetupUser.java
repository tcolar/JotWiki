/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import net.jotwiki.db.WikiUser;

/**
 * Special "fake" user for running the initial setup.
 * @author thibautc
 *
 */
public class SetupUser extends WikiUser
{

    public SetupUser()
    {
        dataLogin = "admin";
    }

    public final void save() throws Exception
    {
    // not savable of course (fake user).
    }
}

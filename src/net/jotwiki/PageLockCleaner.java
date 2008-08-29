/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */

package net.jotwiki;

import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.scheduler.JOTScheduledItem;
import net.jotwiki.db.PageLock;

/**
 * Scheduled Job that cleans the expired DB page locks.
 * @author thibautc
 */
public class PageLockCleaner implements JOTScheduledItem
{
    
  public void run()
  {
    Vector namespaces=WikiPreferences.getInstance().getNamespaceList();
    // going through namespaces
    for(int j=0;j!=namespaces.size();j++)
    {
      String namespace=(String)namespaces.get(j);
      int length=WikiPreferences.getInstance().getDefaultedInt(namespace+"."+WikiPreferences.NS_LOCK_LENGTH, new Integer(30)).intValue();
      int nbLocks=PageLock.removeExpiredNsLocks(namespace,length);
      if(nbLocks>0)
        JOTLogger.log(JOTLogger.INFO_LEVEL, PageLock.class, "Removed "+nbLocks+" expired lock for namespace "+namespace);
    }
  }

  public boolean skipRun()
  {
    return false;
  }

  public boolean forceRun()
  {
    return false;
  }

  public void runCompleted()
  {
  }
  
}

/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import java.util.Date;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Edition lock for a page DB object.
 * @author thibautc
 */
public class PageLock extends JOTModel
{

  public String dataNameSpace = "";
  public String dataPage = "";
  public String dataAuthor = "";
  public long dataLockTime = -1;

  public void customize(JOTModelMapping mapping)
  {
    mapping.defineFieldSize("dataNameSpace", 40);
    mapping.defineFieldSize("dataPage", 200);
    mapping.defineFieldSize("dataAuthor", 80);
  }

  public String defineStorage()
  {
    return DEFAULT_STORAGE;
  }

    /**
   * Returns a page lock for given ns,page
   * or null if does not exist, unless createIfmissing=true in which case it will create a new lock and return it
   * @param ns
   * @param page
   * @return
   */
  public static PageLock getPageLock(String ns, String page, boolean createIfMising) throws Exception
  {
    JOTSQLQueryParams params = new JOTSQLQueryParams();
    params.addCondition(new JOTSQLCondition("dataNameSpace", JOTSQLCondition.IS_EQUAL, ns));
    params.addCondition(new JOTSQLCondition("dataPage", JOTSQLCondition.IS_EQUAL, page));
    PageLock lock = null;
    if (createIfMising)
    {
      lock = (PageLock) JOTQueryManager.findOrCreateOne(PageLock.class, params);
    }
    else
    {
      lock = (PageLock) JOTQueryManager.findOne(PageLock.class, params);
    }
    return lock;
  }

  /**
   *  update(and creates if missing) a page lock
   * @param implClass
   * @param ns
   * @param page
   * @param author
   * @throws java.lang.Exception
   */
  public static synchronized void updatePageLock(String ns, String page, String author) throws Exception
  {
    PageLock lock = getPageLock(ns, page, true);
    lock.setNameSpace(ns);
    lock.setPage(page);
    lock.setAuthor(author);
    lock.setLockTime(new Date().getTime());
    lock.save();
    JOTLogger.log(JOTLogger.INFO_LEVEL, PageLock.class, "Set/Update page lock for "+ns+":"+page+" ("+author+")");
  }

  /**
   * Removes a page lock in given namespace
   * @param ns
   * @param page
   * @param author - if not null, then the lock will only be removed if it was set by 'author'.
   * @throws java.lang.Exception
   */
  public static synchronized void removePageLock(String ns, String page, String author) throws Exception
  {
    PageLock lock = getPageLock(ns, page, false);
    if (lock != null && (author!=null && lock.getAuthor().equals(author)))
    {
     JOTLogger.log(JOTLogger.INFO_LEVEL, PageLock.class, "Removing page lock for "+ns+":"+page+" ("+lock.getAuthor()+")");
     lock.delete();
    }
  }

  /**
   * Removes all the expired lock for a given ns
   * Return the number of locks that where removed.
   * @param namespace
   * @param length
   * @return
   */
    public static int removeExpiredNsLocks(String namespace, int length)
    {
        int cpt=0;
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        Long cuttofTime=new Long(new Date().getTime()-(1000*60*length));
        params.addCondition(new JOTSQLCondition("dataNameSpace", JOTSQLCondition.IS_EQUAL, namespace));
        params.addCondition(new JOTSQLCondition("dataLockTime", JOTSQLCondition.IS_LOWER, cuttofTime));
        try
        {
            Vector locks =  JOTQueryManager.find(PageLock.class, params);
            for(int i=0;i!=locks.size();i++)
            {
                PageLock lock=(PageLock)locks.get(i);
                JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageLock.class, "Removed expired lock: "+namespace+":"+lock.getPage()+" ("+lock.getAuthor()+")");
                lock.delete();
                cpt++;
            }
        }
        catch(Exception e){}
        return cpt;
    }


  public String getAuthor()
  {
    return dataAuthor;
  }

  public void setAuthor(String dataAuthor)
  {
    this.dataAuthor = dataAuthor;
  }

  public String getNameSpace()
  {
    return dataNameSpace;
  }

  public void setPage(String dataPage)
  {
    this.dataPage = dataPage;
  }

  public void setLockTime(long lockTime)
  {
    this.dataLockTime = lockTime;
  }

  public String getPage()
  {
    return dataPage;
  }

  public long getLockTime()
  {
    return dataLockTime;
  }

  public void setNameSpace(String dataNameSpace)
  {
    this.dataNameSpace = dataNameSpace;
  }

}

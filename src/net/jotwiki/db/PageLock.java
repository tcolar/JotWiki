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
import net.jot.persistance.builders.JOTQueryBuilder;

/**
 * Edition lock for a page DB object.
 * @author thibautc
 */
public class PageLock extends JOTModel
{

  public String nameSpace = "";
  public String page = "";
  public String author = "";
  public long lockTime = -1;

  public void customize(JOTModelMapping mapping)
  {
    mapping.defineFieldSize("nameSpace", 40);
    mapping.defineFieldSize("page", 200);
    mapping.defineFieldSize("author", 80);
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
    JOTSQLCondition cond=new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
    JOTSQLCondition cond2=new JOTSQLCondition("page", JOTSQLCondition.IS_EQUAL, page);
    PageLock lock = null;
    if (createIfMising)
    {
      lock = (PageLock) JOTQueryBuilder.selectQuery(null, PageLock.class).where(cond).where(cond2).findOrCreateOne();
    }
    else
    {
      lock = (PageLock) JOTQueryBuilder.selectQuery(null, PageLock.class).where(cond).where(cond2).findOne();
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
        Long cuttofTime=new Long(new Date().getTime()-(1000*60*length));
        JOTSQLCondition cond=new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, namespace);
        JOTSQLCondition cond2=new JOTSQLCondition("lockTime", JOTSQLCondition.IS_LOWER, cuttofTime);
        try
        {
            Vector locks =  JOTQueryBuilder.selectQuery(null, PageLock.class).where(cond).where(cond2).find().getAllResults();
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
    return author;
  }

  public void setAuthor(String dataAuthor)
  {
    this.author = dataAuthor;
  }

  public String getNameSpace()
  {
    return nameSpace;
  }

  public void setPage(String dataPage)
  {
    this.page = dataPage;
  }

  public void setLockTime(long lockTime)
  {
    this.lockTime = lockTime;
  }

  public String getPage()
  {
    return page;
  }

  public long getLockTime()
  {
    return lockTime;
  }

  public void setNameSpace(String dataNameSpace)
  {
    this.nameSpace = dataNameSpace;
  }

}

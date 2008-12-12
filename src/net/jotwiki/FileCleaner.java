/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */

package net.jotwiki;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTPersistanceManager;
import net.jot.scheduler.JOTClock;
import net.jot.scheduler.JOTScheduledItem;
import net.jot.utils.JOTUtilities;

/**
 * Scheduled Task that finds old(expired) files and delete them to release disk space
 * For example old log files etc...
 * @author thibautc
 */
public class FileCleaner implements JOTScheduledItem
{
    public static final int KEEP_CONFLICT_FILES_FOR=7;
    
    
  //TODO: remove logs and db backups jobs should be part of JOT rather tha JOTwiki.  
    
  public void run()
  {
    // Do the old log files
    int keepLogsFor=WikiPreferences.getInstance().getDefaultedInt(WikiPreferences.GLOBAL_KEEP_LOGS_FOR,new Integer(7)).intValue();
    FilenameFilter oldLogsFilter=new WikiTempFileFilter(keepLogsFor,"jotwiki.log_",null);
    File logFolder=new File(WikiPreferences.getInstance().getLogsFolder());
    if(logFolder.exists() && logFolder.isDirectory())
    {
      File[] files=logFolder.listFiles(oldLogsFilter);
      int cpt=0;
      for(int i=0;i!=files.length;i++)
      {
          JOTLogger.log(JOTLogger.DEBUG_LEVEL,this,"Removing old log file: "+files[i].getAbsolutePath());
          files[i].delete();
          cpt++;
      }
      JOTLogger.log(JOTLogger.INFO_LEVEL,this,"Removed "+cpt+" old(over "+keepLogsFor+" days) log files.");
    }
    
    Vector namespaces=WikiPreferences.getInstance().getNamespaceList();
    // going through namespaces
    for(int j=0;j!=namespaces.size();j++)
    {
      String namespace=(String)namespaces.get(j);
      // do the conflicted pages
      FilenameFilter oldConflictsFilter=new WikiTempFileFilter(KEEP_CONFLICT_FILES_FOR,"~",null);
    
      File conflictFolder=new File(WikiPreferences.getInstance().getPagesFolder(namespace));
      if(conflictFolder.exists() && conflictFolder.isDirectory())
      {
        File[] files=conflictFolder.listFiles(oldConflictsFilter);
        int cpt=0;
        for(int i=0;i!=files.length;i++)
        {
          JOTLogger.log(JOTLogger.DEBUG_LEVEL,this,"["+namespace+"] Removing old conflict backup file: "+files[i].getAbsolutePath());
          files[i].delete();
          cpt++;
        }
        JOTLogger.log(JOTLogger.INFO_LEVEL,this,"["+namespace+"] Removed "+cpt+" old(over "+KEEP_CONFLICT_FILES_FOR+" days) conflict backup files.");
      }
      // do the page backups (oldpages)
      int keepPagesFor=WikiPreferences.getInstance().getDefaultedInt(namespace+"."+WikiPreferences.NS_KEEP_OLD_PAGES_FOR,new Integer(45)).intValue();
      FilenameFilter oldPagesFilter=new WikiTempFileFilter(keepPagesFor,null,null);
    
      File pagesFolder=new File(WikiPreferences.getInstance().getArchiveFolder(namespace));
      if(pagesFolder.exists() && pagesFolder.isDirectory())
      {
        File[] files=pagesFolder.listFiles(oldPagesFilter);
        int cpt=0;
        for(int i=0;i!=files.length;i++)
        {
          JOTLogger.log(JOTLogger.DEBUG_LEVEL,this,"["+namespace+"] Removing old page backup file: "+files[i].getAbsolutePath());
          files[i].delete();
          cpt++;
        }
        JOTLogger.log(JOTLogger.INFO_LEVEL,this,"["+namespace+"] Removed "+cpt+" old(over "+keepPagesFor+" days) page backup files.");
      }
    }
    // do the old DB Backups 
    int keepDbBackupsFor=WikiPreferences.getInstance().getDefaultedInt(WikiPreferences.GLOBAL_KEEP_DB_BACKUPS_FOR,new Integer(15)).intValue();
    FilenameFilter oldDbBackupsFilter=new WikiTempFileFilter(keepDbBackupsFor,null,null);
    Hashtable dbs=JOTPersistanceManager.getInstance().getDatabases();
    Enumeration e=dbs.keys();
    while(e.hasMoreElements())
    {
      String db=(String)e.nextElement();
      String backupFolder=JOTPersistanceManager.getInstance().getDbBackupFolder(db);
      File f=new File(backupFolder);
      if(f.exists() && f.isDirectory())
      {
        File[] folders=f.listFiles(oldDbBackupsFilter);
        int cpt=0;
        for(int i=0;i!=folders.length;i++)
        {
          JOTLogger.log(JOTLogger.DEBUG_LEVEL,this,"Removing old db Backup folder: "+folders[i].getAbsolutePath());
          JOTUtilities.deleteFolderContent(folders[i]);
          folders[i].delete();
          cpt++;
        }        
        JOTLogger.log(JOTLogger.INFO_LEVEL,this,"Removed "+cpt+" old DB backup folders.");
     }
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
  
  /**
   * Internal filenameFilter that recognized jotwiki temp files, such as log files etc...
   */
  static class WikiTempFileFilter implements FilenameFilter
  {
    long cuttofTime;
    String head;
    String tail;
    
    public WikiTempFileFilter(int olderThanInDays, String head, String tail)
    {
        cuttofTime=new Date().getTime()-(olderThanInDays*1000*3600*24);
        this.head=head;
        this.tail=tail;
    }
      
    public boolean accept(File dir, String name)
    {
            try
            {
              if(head!=null && !name.startsWith(head))
                return false;
              
              if(tail!=null && !name.endsWith(tail))
                return false;
              
              Matcher m=JOTClock.TIMESTAMP_PATTERN.matcher(name);
              if(m.matches())
              {
                String match=m.group(1);
                Date fileDate=JOTClock.parseDateString(match);
                
                if(fileDate.getTime()<cuttofTime)
                {
                  JOTLogger.log(JOTLogger.TRACE_LEVEL,this,"Match: "+match+" date:"+fileDate);
                  return true;
                }
              }
            }
            catch(Exception e)
            {
                  JOTLogger.log(JOTLogger.DEBUG_LEVEL,this,"Error parsing temp file date: "+e);
            }
            return false;
    }
      
  }
}

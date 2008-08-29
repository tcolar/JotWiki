/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.File;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.scheduler.JOTScheduledItem;
import net.jot.search.simpleindexer.JOTSimpleSearchEngine;

/**
 *
 * @author thibautc
 */
public class SearchIndexer implements JOTScheduledItem
{

    private static SearchIndexer index = null;
    private JOTSimpleSearchEngine engine = null;

    public static SearchIndexer getInstance()
    {
        if (index == null)
        {
            synchronized (SearchIndexer.class)
            {
                try
                {
                    index = new SearchIndexer();
                } catch (Exception e)
                {
                    JOTLogger.logException(JOTLogger.ERROR_LEVEL, SearchIndexer.class, "Failed creating instance !! : ", e);
                }
            }
        }
        return index;
    }

    private SearchIndexer() throws Exception
    {
        File indexRoot = new File(WikiPreferences.getInstance().getSearchIndexFolder());
        engine = new JOTSimpleSearchEngine(indexRoot);
    }

    public JOTSimpleSearchEngine getEngine()
    {
        return engine;
    }

    /**
     * 
     * @param ns namespace
     * @param pageName page name (without the .txt)
     */
    public static String getUinqueId(String ns, String pageName)
    {
        return ns + "/" + pageName;
    }

    // ********* USUAL scheduler mmethods ************
    public void run()
    {
        Vector namespaces = WikiPreferences.getInstance().getNamespaceList();
        for (int i = 0; i != namespaces.size(); i++)
        {
            String ns = (String) namespaces.get(i);
            if (WikiPreferences.getInstance().isIndexingEnabled(ns))
            {
                JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Starting indexing for NS: " + ns);
                String folder = WikiPreferences.getInstance().getPagesFolder(ns);
                File dir = new File(folder);
                if (dir.exists())
                {
                    File[] files = dir.listFiles();
                    for (int j = 0; j != files.length; j++)
                    {
                        String page = files[j].getName();
                        if (page.endsWith(".txt"))
                        {
                            page = page.substring(0, page.lastIndexOf(".txt"));
                            String id = getUinqueId(ns, page);
                            // reindex the file only if modified 
                            try
                            {
                                getEngine().indexFile(files[j], id, true);
                            } catch (Exception e)
                            {
                                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed indexing: " + id, e);
                            }
                        }
                    }
                }
                JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Done indexing NS: " + ns);
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
}

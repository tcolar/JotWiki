/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import java.util.Vector;
import net.jot.search.simpleindexer.JOTSearchResult;
import net.jot.search.simpleindexer.JOTSimpleSearchEngine;
import net.jot.web.ctrl.JOTController;
import net.jotwiki.Constants;
import net.jotwiki.SearchIndexer;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 *
 * @author thibautc
 */
public class Search extends JOTController
{

    public String process() throws Exception
    {
        String ns=WikiUtilities.getNamespace(request);
                
        String text = request.getParameter("text");
        String page = request.getParameter("page");
        // if page!=null then we just want a new page of an existing query, doing nothing here, handlinr in searchResults.java
        if (page == null && text != null)
        {
            // new query
            JOTSimpleSearchEngine engine = SearchIndexer.getInstance().getEngine();
            String[] keywords = JOTSimpleSearchEngine.parseQueryIntoKeywords(text);
            JOTSearchResult[] results = engine.performSearch(keywords, null);

            boolean acrossNs = WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_SEARCH_ACROSS, Boolean.FALSE).booleanValue();
            int maxResults = WikiPreferences.getInstance().getDefaultedNsInt(request, WikiPreferences.NS_SEARCH_MAX_RESULTS, new Integer(100)).intValue();

            Vector v = new Vector();
            // limit nb of results
            for (int i = 0; i != results.length && v.size()<maxResults; i++)
            {
                // filter for namespace if necessary
                if (acrossNs || results[i].getID().startsWith(ns+"/"))
                {
                    v.add(results[i]);
                }
            }

            session.setAttribute(Constants.SESSION_SEARCH_STRING, text);
            session.setAttribute(Constants.SESSION_SEARCH_RESULTS, v);
        }
        return RESULT_SUCCESS;
    }

    public boolean validatePermissions()
    {
        String ns = WikiUtilities.getNamespace(request);
        return WikiPreferences.getInstance().isIndexingEnabled(ns) && WikiPermission.hasPermission(request, WikiPermission.SEARCH);
    }
}

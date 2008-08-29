/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.search.simpleindexer.JOTSearchResult;
import net.jot.search.simpleindexer.JOTSimpleSearchEngine;
import net.jot.utils.JOTHTMLUtilities;
import net.jot.utils.JOTUtilities;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 *
 * @author thibautc
 */
public class SearchResults extends ShowPageView
{

    public void prepareViewData() throws Exception
    {
        String pageStr = request.getParameter("page");
        if(pageStr==null)
            pageStr="1";
        
        int pageSize = WikiPreferences.getInstance().getDefaultedNsInt(request, WikiPreferences.NS_SEARCH_RESULTS_PER_PAGE, new Integer(10)).intValue();
        addVariable("query", session.getAttribute(Constants.SESSION_SEARCH_STRING));
        Vector results=(Vector)session.getAttribute(Constants.SESSION_SEARCH_RESULTS);
        
        // pagination
        int page=1;
        int nbPages=1+(results.size()-1)/pageSize;
        if(pageStr!=null)
            page=new Integer(pageStr).intValue();
        if(page<1 || page>nbPages)
           page=nbPages;
        
        int end=page*pageSize;
        if(end>results.size())
            end=results.size();
        
        List pagedResults=results.subList((page-1)*pageSize, end);
        
        if(pagedResults.size()>0)
        {
            addVariable("results", pagedResults);
        }
        
        addVariable("lastPage", new Integer(nbPages+1));
        addVariable("currentPage", new Integer(page));
    }

    public String getPageLinks()
    {
        String result="";
        Integer last=(Integer)getVariables().get("lastPage");
        Integer cur=(Integer)getVariables().get("currentPage");
        for(int i=1;i!=last.intValue();i++)
        {
            boolean bold=i==cur.intValue();
            result+="<a href='search.do?page="+i+"'>"+(bold?"<b>":"")+i+(bold?"</b>":"")+"</a>&nbsp;&nbsp;";
        }
        return result;
    }
    
    public boolean validatePermissions()
    {
        String ns=WikiUtilities.getNamespace(request);
        return WikiPreferences.getInstance().isIndexingEnabled(ns) && WikiPermission.hasPermission(request,WikiPermission.SEARCH);
    }

    /**
     * Return total nb of results.
     * @return
     */
    public String getNbResults()
    {
        Vector results=(Vector)session.getAttribute(Constants.SESSION_SEARCH_RESULTS);
        return ""+results.size();
    }
    
    /**
     * Return the link to the result's matching page.
     * @param result
     * @return
     */
    public String getLink(JOTSearchResult result)
    {
        String id=result.getID();
        String ns=id.substring(0,id.indexOf("/"));
        String page=id.substring(id.indexOf("/")+1, id.length());
        if( ! ns.equals(WikiUtilities.getNamespace(request)))
        {
            // return link to other ns
            String link=JOTUtilities.endWithForwardSlash(WikiPreferences.getInstance().getDefaultedString(ns + "." + WikiPreferences.NS_WEBROOT, ns)) + page;
            return "<img src='images/link_icon.gif'/>" + 
                   "<a href='"+link+"' class='searchLink'>"+link+"</a>";
        }
        // same ns link
        return "<a href='"+page+"' class='searchLink'>"+page+"</a>";
    }
    
    /**
     * returns 5 stars total, some yello, some grey and possibly one half yello/grey
     * @param result
     * @return
     */
    public String getStars(JOTSearchResult result)
    {
        int score=result.getScore();
        int nbFull=score/2;
        boolean half=score%2!=0;
        int nbEmpty=5-nbFull-(half?1:0);
        String s="";
        for(int i=0;i!=nbFull;i++)
            s+="<img src='images/star_full.gif'/>";
        if(half)
            s+="<img src='images/star_half.gif'/>";
        for(int i=0;i!=nbEmpty;i++)
            s+="<img src='images/star_empty.gif'/>";
        
        return s;
    }
    
    /**
     * gets a fiew lines(3) of preview of the best matching lines in the file to display as an abstract
     * @param result
     * @return
     */
    public String getAbstract(JOTSearchResult result)
    {
        String query=(String)session.getAttribute(Constants.SESSION_SEARCH_STRING);
        String[] keywords=JOTSimpleSearchEngine.parseQueryIntoKeywords(query);
        int bestLine=result.getBestLine();
        String extract = "...<br/>";
        int start=bestLine-1;
        if(start<=1)
        {
            start=1;
            extract+="";
        }
            
        String id=result.getID();
        String ns=id.substring(0,id.indexOf("/"));
        String pageName=id.substring(id.indexOf("/")+1, id.length());
        
        BufferedReader reader = null;
        try
        {
            File file = PageReader.getPageFile(ns, pageName);
            if (file.exists())
            {
                reader = new BufferedReader(new FileReader(file));
                String s = null;
                // skip lines.
                for(int i=1;i!=start;i++)
                {
                    reader.readLine();
                }
                // read 3 lines of extract
                for(int i=0;i!=3;i++)
                {
                    if((s=reader.readLine())!=null && s.length()>0)
                    {
                        s=JOTHTMLUtilities.textToHtml(s);
                        for(int j=0;j!=keywords.length;j++)
                        {
                            // highlight keywords
                            Matcher m=Pattern.compile(keywords[j],Pattern.CASE_INSENSITIVE).matcher(s);
                            s=m.replaceAll("<span class='searchHighlight'>$0</span>");
                        }
                        extract += s + "<br/>";
                    }
                }
                if(reader.readLine()!=null)
                    extract+="...<br/>";
                reader.close();
            }
        } catch (FileNotFoundException f)
        {
        } catch (Exception e)
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch(Exception e2){}
            }
        }
        return extract;
    }
    
}

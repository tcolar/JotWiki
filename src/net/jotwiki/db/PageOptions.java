/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;

/**
 * Page Options DB object.
 * @author tcolar
 */
public class PageOptions extends JOTModel
{
    public String nameSpace = "";
    public String author = "";
    public String docType = "Wiki";
    public Integer blogEntries = new Integer(10);
    public Boolean blogCalendar = Boolean.FALSE;
    public Boolean blogRss = Boolean.FALSE;
    public String blogRssTitle = "";
    public String pageName = "";
    public Boolean commentsEnabled=Boolean.TRUE;
    public Integer commentsNb=new Integer(5);
    public Boolean commentsGuest=Boolean.TRUE;
    public String commentsEmail="";
    

    public void customize(JOTModelMapping mapping)
    {
        mapping.defineFieldSize("nameSpace", 40);
        mapping.defineFieldSize("pageName", 200);
        mapping.defineFieldSize("author", 80);
        mapping.defineFieldSize("docType", 40);
        mapping.defineFieldSize("blogRssTitle", 200);
        mapping.defineFieldSize("commentsEmail", 200);
    }

    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    public String getDataAuthor()
    {
        return author;
    }

    public void setDataAuthor(String dataAuthor)
    {
        this.author = dataAuthor;
    }

    public Boolean getDataBlogCalendar()
    {
        return blogCalendar;
    }

    public void setDataBlogCalendar(Boolean dataBlogCalendar)
    {
        this.blogCalendar = dataBlogCalendar;
    }

    public Integer getDataBlogEntries()
    {
        return blogEntries;
    }

    public void setDataBlogEntries(Integer dataBlogEntries)
    {
        this.blogEntries = dataBlogEntries;
    }

    public Boolean getDataBlogRss()
    {
        return blogRss;
    }

    public void setDataBlogRss(Boolean dataBlogRss)
    {
        this.blogRss = dataBlogRss;
    }

    public String getDataBlogRssTitle()
    {
        return blogRssTitle;
    }

    public void setDataBlogRssTitle(String dataBlogRssTitle)
    {
        this.blogRssTitle = dataBlogRssTitle;
    }

    public String getDataDocType()
    {
        return docType;
    }

    public void setDataDocType(String dataDocType)
    {
        this.docType = dataDocType;
    }

    public String getDataNameSpace()
    {
        return nameSpace;
    }

    public void setDataNameSpace(String dataNameSpace)
    {
        this.nameSpace = dataNameSpace;
    }

    public String getDataPageName()
    {
        return pageName;
    }

    public void setDataPageName(String dataPageName)
    {
        this.pageName = dataPageName;
    }

    /**
     * Returns the PageOptions for a given page
     * @param ns
     * @param page
     * @return
     * @throws java.lang.Exception
     */
    public static PageOptions getPageOptions(String ns, String page) throws Exception
    {
        JOTSQLCondition cond=new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
        JOTSQLCondition cond2=new JOTSQLCondition("pageName", JOTSQLCondition.IS_EQUAL, page);
        PageOptions options =  (PageOptions) JOTQueryBuilder.selectQuery(null, PageOptions.class).where(cond).where(cond2).findOrCreateOne();
        return options;
    }

    public String getCommentsEmail()
    {
        return commentsEmail;
    }

    public Boolean getCommentsEnabled()
    {
        return commentsEnabled;
    }

    public Boolean getCommentsGuest()
    {
        return commentsGuest;
    }

    public Integer getCommentsNb()
    {
        return commentsNb;
    }

    public void setCommentsEmail(String dataCommentsEmail)
    {
        this.commentsEmail = dataCommentsEmail;
    }

    public void setCommentsEnabled(Boolean dataCommentsEnabled)
    {
        this.commentsEnabled = dataCommentsEnabled;
    }

    public void setCommentsGuest(Boolean dataCommentsGuest)
    {
        this.commentsGuest = dataCommentsGuest;
    }

    public void setCommentsNb(Integer dataCommentsNb)
    {
        this.commentsNb = dataCommentsNb;
    }

}

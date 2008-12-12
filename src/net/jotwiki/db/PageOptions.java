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

    public String dataNameSpace = "";
    public String dataAuthor = "";
    public String dataDocType = "Wiki";
    public Integer dataBlogEntries = new Integer(10);
    public Boolean dataBlogCalendar = Boolean.FALSE;
    public Boolean dataBlogRss = Boolean.FALSE;
    public String dataBlogRssTitle = "";
    public String dataPageName = "";
    public Boolean dataCommentsEnabled=Boolean.TRUE;
    public Integer dataCommentsNb=new Integer(5);
    public Boolean dataCommentsGuest=Boolean.TRUE;
    public String dataCommentsEmail="";
    

    public void customize(JOTModelMapping mapping)
    {
        mapping.defineFieldSize("dataNameSpace", 40);
        mapping.defineFieldSize("dataPageName", 200);
        mapping.defineFieldSize("dataAuthor", 80);
        mapping.defineFieldSize("dataDocType", 40);
        mapping.defineFieldSize("dataBlogRssTitle", 200);
        mapping.defineFieldSize("dataCommentsEmail", 200);
    }

    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    public String getDataAuthor()
    {
        return dataAuthor;
    }

    public void setDataAuthor(String dataAuthor)
    {
        this.dataAuthor = dataAuthor;
    }

    public Boolean getDataBlogCalendar()
    {
        return dataBlogCalendar;
    }

    public void setDataBlogCalendar(Boolean dataBlogCalendar)
    {
        this.dataBlogCalendar = dataBlogCalendar;
    }

    public Integer getDataBlogEntries()
    {
        return dataBlogEntries;
    }

    public void setDataBlogEntries(Integer dataBlogEntries)
    {
        this.dataBlogEntries = dataBlogEntries;
    }

    public Boolean getDataBlogRss()
    {
        return dataBlogRss;
    }

    public void setDataBlogRss(Boolean dataBlogRss)
    {
        this.dataBlogRss = dataBlogRss;
    }

    public String getDataBlogRssTitle()
    {
        return dataBlogRssTitle;
    }

    public void setDataBlogRssTitle(String dataBlogRssTitle)
    {
        this.dataBlogRssTitle = dataBlogRssTitle;
    }

    public String getDataDocType()
    {
        return dataDocType;
    }

    public void setDataDocType(String dataDocType)
    {
        this.dataDocType = dataDocType;
    }

    public String getDataNameSpace()
    {
        return dataNameSpace;
    }

    public void setDataNameSpace(String dataNameSpace)
    {
        this.dataNameSpace = dataNameSpace;
    }

    public String getDataPageName()
    {
        return dataPageName;
    }

    public void setDataPageName(String dataPageName)
    {
        this.dataPageName = dataPageName;
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
        JOTSQLCondition cond=new JOTSQLCondition("dataNameSpace", JOTSQLCondition.IS_EQUAL, ns);
        JOTSQLCondition cond2=new JOTSQLCondition("dataPageName", JOTSQLCondition.IS_EQUAL, page);
        PageOptions options =  (PageOptions) JOTQueryBuilder.selectQuery(PageOptions.class).where(cond).where(cond2).findOrCreateOne();
        return options;
    }

    public String getCommentsEmail()
    {
        return dataCommentsEmail;
    }

    public Boolean getCommentsEnabled()
    {
        return dataCommentsEnabled;
    }

    public Boolean getCommentsGuest()
    {
        return dataCommentsGuest;
    }

    public Integer getCommentsNb()
    {
        return dataCommentsNb;
    }

    public void setCommentsEmail(String dataCommentsEmail)
    {
        this.dataCommentsEmail = dataCommentsEmail;
    }

    public void setCommentsEnabled(Boolean dataCommentsEnabled)
    {
        this.dataCommentsEnabled = dataCommentsEnabled;
    }

    public void setCommentsGuest(Boolean dataCommentsGuest)
    {
        this.dataCommentsGuest = dataCommentsGuest;
    }

    public void setCommentsNb(Integer dataCommentsNb)
    {
        this.dataCommentsNb = dataCommentsNb;
    }

}

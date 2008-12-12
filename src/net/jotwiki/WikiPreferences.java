/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPreferences;
import net.jot.prefs.JOTPropertiesPreferences;
import net.jot.utils.JOTUtilities;

/**
 * This gives easy,centralized access to all of the Wiki preferences, stored in wiki.properties
 * @author tcolar
 */
public class WikiPreferences extends JOTPropertiesPreferences
{

    private String rootFolder=null;
    private static WikiPreferences prefs = null;
    public static final String FILENAME = "wiki.properties";
    public static final String MAIN_ROOT_FOLDER_WIN = "wiki.rootfolder.windows";
    public static final String MAIN_ROOT_FOLDER_OTHERS = "wiki.rootfolder.others";

    // Global settings
    public static final String GLOBAL_STACK_TRACE = "_GLOBAL_.wiki.stacktrace.enabled";
    public static final String GLOBAL_LOG_LEVELS = "_GLOBAL_.wiki.log.levels";
    public static final String GLOBAL_PRINT_TO_CONSOLE = "_GLOBAL_.wiki.printToConsole.enabled";
    public static final String GLOBAL_SITEMAP_AT_STARTUP = "_GLOBAL_.wiki.google.sitemap.runatstartup";
    public static final String GLOBAL_SITEMAP_SCHEDULE = "_GLOBAL_.wiki.google.sitemap.schedule";
    public static final String GLOBAL_ADDTHIS_PUBLISHER = "_GLOBAL_.wiki.addthis.publisher";
    public static final String GLOBAL_KEEP_LOGS_FOR = "_GLOBAL_.wiki.log.keep.for";
    public static final String GLOBAL_KEEP_DB_BACKUPS_FOR = "_GLOBAL_.wiki.db.backups.keep.for";
    public static final String GLOBAL_MAIL_HOST = "_GLOBAL_.wiki.mail.host";
    public static final String GLOBAL_MAIL_DOMAIN = "_GLOBAL_.wiki.mail.domain";
    public static final String GLOBAL_MAIL_PORT = "_GLOBAL_.wiki.mail.port";
    public static final String GLOBAL_MAIL_FROM = "_GLOBAL_.wiki.mail.from";
    public static final String GLOBAL_ENCODE_EMAIL = "_GLOBAL_.wiki.spam.encode.mailto";
    public static final String GLOBAL_ENCODE_MAILTO = "_GLOBAL_.wiki.spam.encode.email";
    public static final String GLOBAL_INDEXING_AT_STARTUP = "_GLOBAL_.wiki.indexing.runatstartup";
    public static final String GLOBAL_INDEXING_SCHEDULE = "_GLOBAL_.wiki.indexing.schedule";
    /* Namespace settings
     * those will have the namespace prepended ex: default.wiki.ns.moto
     * Preferably read by using the getStringNs() and similar methods (Ns)
     */
    public static final String NS_TITLE = "wiki.ns.title";
    public static final String NS_MOTO = "wiki.ns.moto";
    public static final String NS_TEMPLATE = "wiki.ns.template";
    public static final String NS_HOMEPAGE = "wiki.ns.homepage";
    public static final String NS_SIDEBAR = "wiki.ns.sidebar";
    public static final String NS_NUMBER_OF_BREADCRUMBS = "wiki.ns.breadcrumbs.howmany";
    public static final String NS_WEBROOT = "wiki.ns.webroot";
    public static final String NS_BREADCRUMBS_ACCROSS = "wiki.ns.breadcrumbs.acrossnamespaces";
    public static final String NS_SITE_TEMPLATE = "wiki.ns.template";
    public static final String NS_GOOGLE_UACCT = "wiki.ns.google.analytics.uacct";
    public static final String NS_LOCK_LENGTH = "wiki.ns.editor.lock.length";
    public static final String NS_KEEP_OLD_PAGES_FOR = "wiki.ns.oldpages.keepfor";
    // comments
    public static final String NS_COMMENTS_ENABLED = "wiki.ns.comments.enabled";
    public static final String NS_COMMENTS_ALLOW_GUEST = "wiki.ns.comments.allowguest";
    public static final String NS_COMMENTS_CAPTCHA_FOR_GUEST = "wiki.ns.comments.captchaforguest";
    public static final String NS_COMMENTS_CAPTCHA_FOR_LOGGED = "wiki.ns.comments.captchaforlogged";
    public static final String NS_COMMENTS_NEWEST_FIRST = "wiki.ns.comments.newestfirst";
    public static final String NS_COMMENTS_HOW_MANY = "wiki.ns.comments.howmany";
    public static final String NS_COMMENTS_EMAIL_TO = "wiki.ns.comments.emailto";
    //search
    public static final String NS_SEARCH_ENABLED = "wiki.ns.search.enabled";
    public static final String NS_SEARCH_ACROSS = "wiki.ns.search.across";
    public static final String NS_SEARCH_MAX_RESULTS = "wiki.ns.search.maxresults";
    public static final String NS_SEARCH_RESULTS_PER_PAGE = "wiki.ns.search.perpage";
    //others
    public static final String NS_MANTIS_ROOT = "wiki.ns.mantis.root";

    public static WikiPreferences getInstance()
    {
        if (prefs == null)
        {
            synchronized (WikiPreferences.class)
            {
                prefs = new WikiPreferences();
            }
        }
        // ceating folders if necessary (first run)
        prefs.createIfNotExists(prefs.getRootFolder() + "conf");
        prefs.createIfNotExists(prefs.getRootFolder() + "logs");
        prefs.createIfNotExists(prefs.getRootFolder() + "templates");
        prefs.createIfNotExists(prefs.getCurrentNamespaceFolder("default") + "files");
        prefs.createIfNotExists(prefs.getCurrentNamespaceFolder("default") + "comments");
        prefs.createIfNotExists(prefs.getCurrentNamespaceFolder("default") + "pages");
        prefs.createIfNotExists(prefs.getCurrentNamespaceFolder("default") + "oldpages");

        return prefs;
    }

    private WikiPreferences(){}
    
    /**
     * Gets the wiki root folder ie: /opt/jotwiki/
     * @return
     */
    public String getRootFolder()
    {
        if (rootFolder == null)
        {
            String prop = System.getProperty("JOTWIKI_HOME");
            if(prop!=null)
                rootFolder=JOTUtilities.endWithSlash(prop);
            else
            {
            if (JOTUtilities.isWindowsOS())
            {
                rootFolder=JOTUtilities.endWithSlash(JOTPreferences.getInstance().getDefaultedString(MAIN_ROOT_FOLDER_WIN, "c:\\jotwiki\\"));
            } else
            {
                rootFolder=JOTUtilities.endWithSlash(JOTPreferences.getInstance().getDefaultedString(MAIN_ROOT_FOLDER_OTHERS, "/opt/jotwiki/"));
            }
            }
        }
        return rootFolder;
    }

    /**
     * Gets the current template root folder ie: /opt/jotwiki/templates/ice/
     * @return
     */
    public String getCurrentTemplateFolder(String nameSpace)
    {
        String template = getString(nameSpace + "." + NS_TEMPLATE);
        if (!new File(getTemplatesFolder() + template).exists())
        {
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Template not found for: " + nameSpace);
            template = Constants.DEFAULT_TEMPLATE;
        }
        return JOTUtilities.endWithSlash(getTemplatesFolder() + template);
    }

    public String getCurrentNamespaceFolder(String nameSpace)
    {
        return JOTUtilities.endWithSlash(getDataFolder() + nameSpace);
    }

    public String getCommentsFolder(String ns)
    {
        return JOTUtilities.endWithSlash(getCurrentNamespaceFolder(ns)) + "comments";
    }

    public String getTemplatesFolder()
    {
        return JOTUtilities.endWithSlash(getRootFolder() + "templates");
    }

    public String getPagesFolder(String nameSpace)
    {
        return JOTUtilities.endWithSlash(getCurrentNamespaceFolder(nameSpace) + "pages");
    }

    public String getConfFolder()
    {
        return JOTUtilities.endWithSlash(getRootFolder() + "conf");
    }

    public String getSearchIndexFolder()
    {
        return JOTUtilities.endWithSlash(getRootFolder() + "searchindex");
    }

    public String getFilesFolder(String nameSpace)
    {
        return JOTUtilities.endWithSlash(getCurrentNamespaceFolder(nameSpace) + "files");
    }

    public String getLogsFolder()
    {
        return JOTUtilities.endWithSlash(getRootFolder() + "logs");
    }

    public String getDataFolder()
    {
        return JOTUtilities.endWithSlash(getRootFolder() + "data");
    }

    /**
     * Saves the preferences into wiki.properties
     */
    public void save()
    {
        FileOutputStream out = null;
        try
        {
            File f = new File(getConfFolder(), FILENAME);
            out = new FileOutputStream(f);
            saveTo(out);
            out.close();
        } catch (Exception e)
        {
            if (out != null)
            {
                try
                {
                    out.close();
                } catch (Exception e2)
                {
                }
            }
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to save " + FILENAME, e);
        }
    }

    public String getArchiveFolder(String nameSpace)
    {
        return JOTUtilities.endWithSlash(getCurrentNamespaceFolder(nameSpace) + "oldpages");
    }

    public String getDefaultedNsString(HttpServletRequest req, String key, String defaultValue)
    {
        String ns = WikiUtilities.getNamespace(req);
        return getDefaultedString(ns + "." + key, defaultValue);
    }

    public String getNsString(HttpServletRequest req, String key)
    {
        String ns = WikiUtilities.getNamespace(req);
        return getString(ns + "." + key);
    }

    public Integer getDefaultedNsInt(HttpServletRequest req, String key, Integer defaultValue)
    {
        String ns = WikiUtilities.getNamespace(req);
        return getDefaultedInt(ns + "." + key, defaultValue);
    }

    public void setNsString(HttpServletRequest req, String key, String value)
    {
        String ns = WikiUtilities.getNamespace(req);
        setString(ns + "." + key, value);
    }

    public Boolean getDefaultedNsBoolean(HttpServletRequest req, String key, Boolean defaultValue)
    {
        String ns = WikiUtilities.getNamespace(req);
        return getDefaultedBoolean(ns + "." + key, defaultValue);
    }

    public Properties getProperties()
    {
        return props;
    }

    /**
     * Vector of template names strings
     * @return
     */
    public Vector getTemplateList()
    {
        return WikiUtilities.getSubfolderList(getTemplatesFolder());
    }

    /**
     * Vector of existing template names(strings)
     * @return
     */
    public Vector getNamespaceList()
    {
        return WikiUtilities.getSubfolderList(getDataFolder());
    }

    public Boolean isMantisEnabled(HttpServletRequest request)
    {
        String mantis = getNsString(request, NS_MANTIS_ROOT);
        return new Boolean(mantis != null && mantis.length() > 0);
    }

    public boolean isIndexingEnabled(String ns)
    {
        Boolean indexing = getDefaultedBoolean(ns + "." + NS_SEARCH_ENABLED, Boolean.TRUE);
        return indexing.booleanValue();
    }

    private void createIfNotExists(String folder)
    {
        new File(folder).mkdirs();
    }
}

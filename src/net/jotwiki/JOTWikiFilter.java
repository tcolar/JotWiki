/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTPersistanceManager;
import net.jot.prefs.JOTPreferences;
import net.jot.prefs.JOTPropertiesPreferences;
import net.jot.scheduler.JOTScheduler;
import net.jot.scheduler.JOTSchedulingOptions;
import net.jot.utils.JOTConstants;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowManager;
import net.jot.web.JOTFlowRequest;
import net.jot.web.JOTMainFilter;
import net.jot.web.JOTRewrittenRequest;
import net.jot.web.view.JOTViewParser;
import net.jot.web.view.JOTViewParserInterface;
import net.jotwiki.db.WikiUser;
import net.jotwiki.forms.LoginForm;

/**
 * Master filter that handles all the requests from the servlet container.
 * Is called according to specification in web.xml, example:
 * 
 *      <filter>
<filter-name>JOTWikiFilter</filter-name>
<filter-class>net.jotwiki.JOTWikiFilter</filter-class>
</filter>
<filter-mapping>
<!--  handles url's like http://server/jotwiki/view.do/home  (aka fancy url's) -->
<filter-name>JOTWikiFilter</filter-name>
<url-pattern>*</url-pattern>
<dispatcher>REQUEST</dispatcher>
<servlet-name>net.jotwiki.JOTWikiFilter</servlet-name>
<!--  VERY IMPORTANT ! -->
<dispatcher>FORWARD</dispatcher>	
</filter-mapping>
 * @author tcolar
 */
public class JOTWikiFilter extends JOTMainFilter implements Filter
{

    private static final long serialVersionUID = 1020265679050704054L;
    private static final String ROBOTS_TXT = "robots.txt";
    Exception initError = null;
    boolean runSetup = false;

    //Handle the passed-in FilterConfig
    // override of JOTMainFilter
    private static boolean inited = false;

    public void init(FilterConfig fConfig)
    {
        synchronized (this)
        {
            //just in case, should not happen.
            if (inited)
            {
                return;
            }
            inited = true;
        }
        JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "#*# Start init");

        //we don't want jot to send to container so we can intercept filesnotfound here !
        sendToContainer = false;

        filterConfig = fConfig;
        runSetup = false;
        initError = null;

        // > mainfilter stuff
        confPath = filterConfig.getServletContext().getRealPath("/jotconf");
        JOTPreferences.setWebConfPath(confPath);
        flowConfig = JOTFlowManager.init(confPath);
        if (flowConfig.getTemplateRoots() == null)
        {
            flowConfig.setTemplateRoot(filterConfig.getServletContext().getRealPath(""));
        }
        flowConfig.setConfigPath(confPath);

        flowConfig.runValidation();

        context = filterConfig.getServletContext().getServletContextName();
        // < mainfilter stuff

        WikiPreferences prefs = WikiPreferences.getInstance();
        String wikiPath = prefs.getRootFolder();
        JOTPropertiesPreferences jotPrefs=JOTPreferences.getInstance();
        // we ovveride the standard jotPreferences values with our custom ones.
        jotPrefs.setString("wiki.rootfolder.windows",wikiPath);
        jotPrefs.setString("wiki.rootfolder.others",wikiPath);
        jotPrefs.setString("db.fs.root_folder.windows", wikiPath+"db"+File.separator);
        jotPrefs.setString("db.fs.root_folder.others", wikiPath+"db"+File.separator);
        try
        {
            createFoldersIfNecessary(wikiPath);
        }
        catch(Exception e)
        {
            initError = new Exception("Failed to create the initial folders: ",e);
        }
        
        JOTLogger.log(JOTLogger.INFO_LEVEL, this, "JOTWiki data/log path: " + wikiPath);
        if (!new File(wikiPath, "VERSION.txt").exists())
        {
            initError = new Exception(wikiPath + " does not appear to be the jotwiki data folder !! ");
        } else
        {
            try
            {
                loadJotPrefs(prefs, wikiPath);
                // logger	
                initLogger(prefs);

                JOTPersistanceManager.getInstance().init(JOTPreferences.getInstance());
            } catch (Exception e)
            {
                initError = new Exception("Initialization error: ", e);
            }

            // setting scheduler jobs
            registerJobs();
            //Starting the scheduler
            JOTScheduler.getInstance().start();
        }

		// setting postprocessor for templates
		JOTViewParserInterface postProcessor=new PostTemplateParser();
		JOTViewParser.setPostProcessor(postProcessor);

        JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Init Completed");
    }

    private void registerJobs()
    {
        // ** Sitemap job, once per hour per default
        Boolean runNow = WikiPreferences.getInstance().getDefaultedBoolean(WikiPreferences.GLOBAL_SITEMAP_AT_STARTUP, Boolean.TRUE);
        String schedule = WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_SITEMAP_SCHEDULE, "* * * 1 0");
        if (runNow.booleanValue() || JOTSchedulingOptions.isValid(schedule))
        {
            JOTSchedulingOptions siteMapOptions = new JOTSchedulingOptions();
            siteMapOptions.setRunNow(runNow.booleanValue());
            siteMapOptions.setRunAt(schedule);
            JOTScheduler.getInstance().registerItem(new SitemapGenerator(), siteMapOptions);
        }

        // ** Page locks remover job, every 5 minutes and right away
        JOTSchedulingOptions locksOptions = new JOTSchedulingOptions();
        locksOptions.setRunEvery(5, true);
        JOTScheduler.getInstance().registerItem(new PageLockCleaner(), locksOptions);

        // ** File Cleanup jobs, run at 3am and 10pm
        JOTSchedulingOptions cleanupOptions = new JOTSchedulingOptions();
        cleanupOptions.setRunNow(false);
        cleanupOptions.setRunAt("* * * 3,22 0");
        JOTScheduler.getInstance().registerItem(new FileCleaner(), cleanupOptions);

        // ** Search engine indexing job, update every 10mn
        Boolean runIndexNow = WikiPreferences.getInstance().getDefaultedBoolean(WikiPreferences.GLOBAL_INDEXING_AT_STARTUP, Boolean.TRUE);
        String indexSchedule = WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_INDEXING_SCHEDULE, "* * * * 0,10,20,30,40,50");
        if (runNow.booleanValue() || JOTSchedulingOptions.isValid(schedule))
        {
            JOTSchedulingOptions indexingOptions = new JOTSchedulingOptions();
            indexingOptions.setRunAt(indexSchedule);
            indexingOptions.setRunNow(runIndexNow.booleanValue());
            JOTScheduler.getInstance().registerItem(SearchIndexer.getInstance(), indexingOptions);
        }
    // ** RSS Job
    //JOTSchedulingOptions rssOptions=new JOTSchedulingOptions();
    //rssOptions.setRunEvery(30, true); /* Every 30mn if needed and at startup*/
    //JOTScheduler.getInstance().registerItem(new RssGenerator(), rssOptions);

    }

    public static void initLogger(WikiPreferences prefs)
    {
        String logPath = prefs.getRootFolder() + "logs" + File.separator + "jotwiki.log";
        String levelsString = prefs.getString(WikiPreferences.GLOBAL_LOG_LEVELS);
        if (levelsString == null)
        {
            levelsString = "0-5";
        }
        String[] levels = levelsString.split(",");
        String catString = null;
        JOTLogger.init(logPath, levels, catString);
        JOTLogger.setPrintStackTrace(prefs.getDefaultedBoolean(WikiPreferences.GLOBAL_STACK_TRACE, Boolean.TRUE).booleanValue());
        JOTLogger.setPrintToConcole(prefs.getDefaultedBoolean(WikiPreferences.GLOBAL_PRINT_TO_CONSOLE, Boolean.TRUE).booleanValue());

        JOTLogger.log(JOTLogger.INFO_LEVEL, JOTWikiFilter.class, "Sarting Jotwiki Version: " + Version.getVersion());
        JOTLogger.log(JOTLogger.INFO_LEVEL, JOTWikiFilter.class, "JOTWiki logs path: " + logPath);
    }

    private void createFoldersIfNecessary(String wikiPath) throws Exception
    {
        String nsPath = wikiPath + "/" + Constants.DEFAULT_NS;
        if (!new File(wikiPath).exists())
        {
            new File(wikiPath).mkdirs();
            new File(nsPath + "/pages").mkdirs();
            new File(nsPath + "/files").mkdirs();
            new File(wikiPath + "/conf").mkdirs();
            new File(wikiPath + "/logs").mkdirs();
            new File(nsPath + "/oldpages").mkdirs();
            new File(wikiPath + "/templates").mkdirs();
            File tplFolder = new File(wikiPath + "/templates/" + Constants.DEFAULT_TEMPLATE + "/");
            if (!tplFolder.exists())
            {
                tplFolder.mkdirs();
                // copy "web" folder content into default template (/webcontent/* /webcontent/images/**)
                File webFolder = new File(filterConfig.getServletContext().getRealPath(""));
                JOTUtilities.copyFolderContent(tplFolder, webFolder, false);
                File tplImgFolder = new File(wikiPath + "/templates/" + Constants.DEFAULT_TEMPLATE + "/images/");
                tplImgFolder.mkdirs();
                File imgFolder = new File(filterConfig.getServletContext().getRealPath("") + "/images");
                JOTUtilities.copyFolderContent(tplImgFolder, imgFolder, true);
            }

        }
        if (!new File(wikiPath).exists() || !new File(wikiPath).isDirectory())
        {
            initError = new Exception("Failed to access/create wiki data folder at: " + wikiPath);
            return;
        }

    }
    
    private void loadJotPrefs(WikiPreferences prefs, String wikiPath) throws Exception
    {
        File f = new File(wikiPath + "/conf", WikiPreferences.FILENAME);
        if (!f.exists())
        {
            // file does not exists/can't be read -> send to setup page ??
            String text = "Config file was not found: " + f.getAbsolutePath();
            text += ", if you need to configure the server (fresh install), please create an empty file in: " + wikiPath + "/runsetup.txt, and then restart jotwiki (or the whole java server).";
            initError = new Exception(text);
            return;
        }
        try
        {
            prefs.loadFrom(f);
        } catch (Exception e)
        {
            initError = new Exception("Failed to read jotwiki.conf at: " + f.getAbsolutePath(), e);
            return;
        }
    }

    /**
     * Main request processing method.
     * Filter implementation
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        Hashtable attributes = new Hashtable();

        if (request instanceof JOTFlowRequest)
        {
            //this happens in case of a forward
            //we don't want to RE-wrap the request
            ServletRequest oldRequest = ((JOTFlowRequest) request).getRequest();

            if (oldRequest instanceof JOTRewrittenRequest)
            {
                JOTRewrittenRequest rewrittenRequest = (JOTRewrittenRequest) oldRequest;
                //saving existing attributes so they will be readded when wrapping again the request later.
                attributes = rewrittenRequest.getCustomParams();
                request = rewrittenRequest.getRequest();

            } else
            {
                attributes = ((JOTFlowRequest) request).getCustomParams();
                request = ((JOTFlowRequest) request).getRequest();
            }
        }


        Hashtable times = new Hashtable();
        times.put("Start", new Long(new Date().getTime()));
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getServletPath();
        JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "#*# Start request: " + path);

        String namespace = getNamespace(path);
        if (namespace == null || !WikiPreferences.getInstance().getNamespaceList().contains(namespace))
        {
            namespace = Constants.DEFAULT_NS;
        }
        req.getSession().setAttribute(Constants.NAMESPACE, namespace);
        String page = getPage(path);
        if (page != null)
        {
            if (page.startsWith("/"))
            {
                page = page.substring(1, page.length());
            }
            attributes.put("page", page);
            if (page.equals(""))
            {
                page = WikiPreferences.getInstance().getDefaultedNsString(req, WikiPreferences.NS_HOMEPAGE, "home");
                JOTUtilities.sendRedirect(res, page, false, false);
            }
            path = Constants.DEFAULT_ACTION;
        }
        else if(path.indexOf("/fetchItem.do/")!=-1)
        {
                String item=path.substring(path.lastIndexOf("/"),path.length());
                attributes.put("item",item);
                path="/fetchItem.do";
        }
        else
        {
            String ns = "/" + namespace + "/";
            if (path.indexOf(ns) > -1 && path.endsWith(".do"))
            {
                path = path.substring(path.indexOf(ns) + ns.length() - 1, path.length());
            }
        }

        JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Request Path: " + path);

        String wikiPath = WikiPreferences.getInstance().getRootFolder();

        if (new File(wikiPath, "runsetup.txt").exists())
        {
            runSetup = true;
        } else
        {
            runSetup = false;
        }

        if (runSetup && path.endsWith(".do") && !path.endsWith("setup.do"))
        {
            // setup needs to be done.
            initError = null;
            // log the user in as "setupuser"
            WikiUser user = new SetupUser();
            req.getSession(true).setAttribute(LoginForm.LOGGED_USER, user);
            // send to setup page
            JOTUtilities.sendRedirect(res, "setup.do", false, false);
            times.put("Stop", new Long(new Date().getTime()));
            debugTiming(path, times);
            return;
        }

        JOTFlowRequest newRequest = new JOTRewrittenRequest(req, path, attributes);

        // set the template root(s), number1 is the current ns template, number 2 is the default template)
        String[] tplRoots = new String[2];
        tplRoots[0] = JOTUtilities.endWithSlash(WikiPreferences.getInstance().getCurrentTemplateFolder(namespace));
        tplRoots[1] = JOTUtilities.endWithSlash(WikiPreferences.getInstance().getTemplatesFolder() + Constants.DEFAULT_TEMPLATE);

        req.getSession().setAttribute(JOTConstants.CUSTOM_TEMPLATE_ROOTS, tplRoots);

        //sitemap
        if (path.endsWith(SitemapGenerator.SITEMAP_GZ))
        {
            try
            {
                response.setContentType("x-application/x-gzip");
                byte[] data = SitemapGenerator.getGzippedData(namespace);
                response.setContentLength(data.length);
                response.getOutputStream().write(data);
                response.flushBuffer();
                times.put("Stop", new Long(new Date().getTime()));
                debugTiming(path, times);
                return;
            } catch (Exception e)
            {
                System.out.println("Failed to send the sitemap !");
                e.printStackTrace();
            }
        }
        // Robots.txt
        if (path.endsWith(ROBOTS_TXT))
        {
            try
            {
                response.setContentType("text/text");
                byte[] data = Robots.getBytes();
                response.setContentLength(data.length);
                response.getOutputStream().write(data);
                response.flushBuffer();
                times.put("Stop", new Long(new Date().getTime()));
                debugTiming(path, times);
                return;
            } catch (Exception e)
            {
                System.out.println("Failed to send robots.txt !");
                e.printStackTrace();
            }
        }

        if (initError != null)
        {
            response.setContentType("text/html");
            response.getOutputStream().println("<b>" + initError.getMessage() + "</b><br><pre>");
            initError.printStackTrace(new PrintStream(response.getOutputStream()));
            response.getOutputStream().println("</pre>");
            response.flushBuffer();
        } else
        {

            if (req.getServletPath().equals("") || req.getServletPath().equals("/"))
            {
                String ns = "";
                if (namespace != null)
                {
                    ns = "/" + namespace;
                }
                JOTUtilities.sendRedirect(res, page, false, false);
                times.put("Stop", new Long(new Date().getTime()));
                debugTiming(req.getRequestURI(), times);
                return;
            }
            // Calling standard JOT action

            times.put("startDoFilter", new Long(new Date().getTime()));
            super.doFilter(newRequest, response, filterChain);
            times.put("endDoFilter", new Long(new Date().getTime()));
        }


        if (!response.isCommitted())
        {
            // try if it's a template file (image, css etc ..)
            //path, namespace
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "******* l for : " + path);

            int index = 0;
            String ns = getNamespace(path);
            if (!WikiPreferences.getInstance().getNamespaceList().contains(ns))
            {
                ns = null;
            }
            if (ns != null)
            {
                index = path.indexOf(ns) + ns.length();
            }
            String item = path.substring(index, path.length());
            fetchTemplateItem(req, res, item);
        }

        if (!response.isCommitted())
        {
            // giving-up, letting appserver try.
            times.put("startChain", new Long(new Date().getTime()));
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "#*# Start dofilter " + path);

            filterChain.doFilter(newRequest, response);
            times.put("endChain", new Long(new Date().getTime()));
        }
        times.put("Stop", new Long(new Date().getTime()));
        debugTiming(req.getRequestURI() + "(ns:" + namespace + " page:" + page + ")", times);
    }

    /**
     *  Used to time queries, for debugging, profiling purposes
     * @param path
     * @param times
     */
    private void debugTiming(String path, Hashtable times)
    {
        if (times != null)
        {
            long start = ((Long) times.get("Start")).longValue();
            long end = ((Long) times.get("Stop")).longValue();
            String debug = "Timing for " + path + " total: " + (end - start) + "ms";
            Enumeration keys = times.keys();
            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                if (!(key.equals("Start") | key.equals("Stop")))
                {
                    long value = ((Long) times.get(key)).longValue();
                    debug += ", " + key + ":" + (value - start) + "ms ";
                }
            }
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, debug);
        }
    }

    /**
     * Figure out the wiki page from the request path
     * @param path
     * @return
     */
    private String getPage(String path)
    {
        int index = path.lastIndexOf("/");
        if (index == -1)
        {
            index = 0;
        }
        String page = path.substring(index, path.length());
        if (page.indexOf(".") != -1)
        {
            page = null;
        }
        return page;
    }

    /**
     * Figures out the namespace from the request path
     * @param path
     * @return
     */
    private String getNamespace(String path)
    {
        String namespace = null;
        int index1 = path.indexOf("/");
        if (index1 > -1 && path.length() >= index1 + 1)
        {
            int index2 = path.indexOf("/", index1 + 1);
            if (index2 > -1)
            {
                namespace = path.substring(index1 + 1, index2);
                /*if(!WikiPreferences.getInstance().getNamespaceList().contains(namespace))
                {
                // not a namespace
                namespace=null;
                }*/
                // namespace cannot have a dot
                if (namespace.indexOf(".") > -1)
                {
                    namespace = null;
                }
            }
        }
        return namespace;
    }

    //Clean up resources
    public void destroy()
    {
        filterConfig = null;
    }

    /**
     * Retrieve a template item such as an image from the template dir.
     * @param request
     * @param response
     * @param item
     */
    public void fetchTemplateItem(HttpServletRequest request, HttpServletResponse response, String item)
    {

        byte[] cbuf = new byte[50000];
        DataInputStream reader = null;
        try
        {
            String nameSpace = WikiUtilities.getNamespace(request);
            String templateRoot = WikiPreferences.getInstance().getCurrentTemplateFolder(nameSpace);
            File f = new File(templateRoot + item);

            if (!f.exists())
            {
                // try the default template
                templateRoot = WikiPreferences.getInstance().getTemplatesFolder() + Constants.DEFAULT_TEMPLATE;
                templateRoot = JOTUtilities.endWithSlash(templateRoot);
                f = new File(templateRoot + item);
            }
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Retrieving template item: " + f.getAbsolutePath());
            reader = new DataInputStream(new FileInputStream(f));
            if (f.getAbsolutePath().startsWith(templateRoot))
            {
                int i = 0;
                while ((i = reader.read(cbuf)) != -1)
                {
                    response.getOutputStream().write(cbuf, 0, i);
                }
                reader.close();
                response.flushBuffer();
            }
        } catch (Exception e)
        {
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "*******: " + e);
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            } catch (Exception e2)
            {
            }
        }
    }
}

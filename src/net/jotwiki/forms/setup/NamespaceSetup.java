/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms.setup;

import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTPropertiesForm;
import net.jot.web.forms.ui.JOTFormCategory;
import net.jot.web.forms.ui.JOTFormCheckboxField;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormHiddenField;
import net.jot.web.forms.ui.JOTFormSelectField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jotwiki.Constants;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 * Form for the configuration of a Namespace.
 * @author tcolar
 */
public class NamespaceSetup extends JOTPropertiesForm
{

    private static final String NS_NAME = "NsName";
    private static final String OLD_NS_NAME = "OldNsName";
    private static final String NEW_NS = "!!NEW!!";
    // the namespace we are working on.
    String ns = "";

    public void updateProperties(JOTFlowRequest request)
    {
        props = new NameSpaceProperties(WikiPreferences.getInstance().getProperties());
        // the namespace to be created/edited will be passed as a parameter or null if new.
        ns = request.getParameter(NS_NAME);
        if (ns == null)
        {
            ns = request.getParameter(Constants.NAMESPACE_SHORT);
        }
        if (ns == null)
        {
            ns = NEW_NS;
        }
        ((NameSpaceProperties) props).setOldNs(ns);
    }

    public void layoutForm(JOTFlowRequest request)
    {
        String[] values = {"default"};

        Vector tpls = WikiPreferences.getInstance().getTemplateList();
        String[] templates = new String[tpls.size()];
        for (int i = 0; i != tpls.size(); i++)
        {
            templates[i] = (String) tpls.get(i);
        }

        setFormTitle("Namespace Settings");
        setFormAction("submitnamespace.do");

        JOTFormHiddenField oldName = new JOTFormHiddenField(OLD_NS_NAME, ns);
        oldName.setSaveAutomatically(false);
        addFormField(oldName);

        JOTFormTextField name = new JOTFormTextField(NS_NAME, "Namespace name", 20, ns.equals(NEW_NS) ? "" : ns);
        name.setHelp("The name of the namespace. <br>You must only use basic characters(a-z - _), NO SPACES!<br>Preferably do not change this once defined as this is a major/risky change.");
        name.setSaveAutomatically(false);
        addFormField(name);

        JOTFormTextField title = new JOTFormTextField(WikiPreferences.NS_TITLE, "Title", 20, "My Great Website");
        title.setHelp("The title usually shows at the top of the template, as well as in the HTML Page Title.");
        addFormField(title);
        JOTFormTextField moto = new JOTFormTextField(WikiPreferences.NS_MOTO, "Moto", 20, "If it's not broken, you are not trying!");
        moto.setHelp("The moto usually shows under the title, depending of the template layout.");
        addFormField(moto);

        addFormField(new JOTFormSelectField(WikiPreferences.NS_SITE_TEMPLATE, "Template", 1, templates, values));
        JOTFormTextField home = new JOTFormTextField(WikiPreferences.NS_HOMEPAGE, "Home Page", 20, "home");
        home.setHelp("The name of the 'default' / home page.<br>for example: home or index .");
        addFormField(home);
        JOTFormTextField webroot = new JOTFormTextField(WikiPreferences.NS_WEBROOT, "Web Root URL", 20, "http://");
        webroot.setHelp("This is the 'External' / 'web' URL to access your site.<br><br><b> This needs to be set correctly for Google sitemaps to work</b><br><br>For example it might be:<br>- http://mysite.com/ (If you used mod_proxy to map your site to a real domain name - virtual host)<br>- http://mysite.com/namespace/ (if the namespace is a subset of the main site)<br>- http://mysite:8080/namespace/ (if you are running the site straight of tomcat)");
        addFormField(webroot);

        JOTFormTextField nbcrumbs = new JOTFormTextField(WikiPreferences.NS_NUMBER_OF_BREADCRUMBS, "How many breadcrumbs?", 2, "5");
        nbcrumbs.setHelp("Breadcrumbs are a list of the X last page links the user visited on the site/namespace.");
        addFormField(nbcrumbs);

        JOTFormCheckboxField crumbs = new JOTFormCheckboxField(WikiPreferences.NS_BREADCRUMBS_ACCROSS, "Show crumbs from other Namespaces?", false);
        crumbs.setHelp("Do you want to only display the breadcrumbs from this namespace or also the ones from the other namespaces?");
        addFormField(crumbs);

        JOTFormTextField googleAnalytics = new JOTFormTextField(WikiPreferences.NS_GOOGLE_UACCT, "Google Aanalytics ID", 20, "");
        googleAnalytics.setHelp("If you want to enable google analytics, enter your ID(_uacct) here.<br>Example: UA-123456-7<br>Otherwise leave empty.");
        addFormField(googleAnalytics);

        JOTFormTextField lockLength = new JOTFormTextField(WikiPreferences.NS_LOCK_LENGTH, "Edition Lock length(mn)", 3, "30");
        lockLength.setHelp("This defines how long(in minutes) a page is locked by a user for editing it.<br>If the page is being edited by the user for over that length of time without being saved, it will be assumed the user 'abandoned' the page, and it will be unlocked so that other users can edit it.");
        addFormField(lockLength);

        JOTFormTextField removePagesAfter = new JOTFormTextField(WikiPreferences.NS_KEEP_OLD_PAGES_FOR, "Keep old page revisions for(days)", 3, "45");
        removePagesAfter.setHelp("When a page is modified, the old version is backedUp in the 'oldpages' folder. You can choose here how long you want to keep them.<br>The default setting is to remove them, and free disk space, after 45 days.<br><b>Requires a restart</b>");
        addFormField(removePagesAfter);

        addCategory(new JOTFormCategory("Comments / Discussions"));
        JOTFormCheckboxField enableComments=new JOTFormCheckboxField(WikiPreferences.NS_COMMENTS_ENABLED, "Enable comments", true);
        enableComments.setHelp("Wether to allow comments by default.<br><b>Can be ovveriden in the page options</b>.");
        addFormField(enableComments);
        JOTFormCheckboxField guestComments=new JOTFormCheckboxField(WikiPreferences.NS_COMMENTS_ALLOW_GUEST, "Allow guests to post comments.", true);
        guestComments.setHelp("<b>Can be ovveriden in the page options</b>.");
        addFormField(guestComments);
        JOTFormCheckboxField guestCaptcha=new JOTFormCheckboxField(WikiPreferences.NS_COMMENTS_CAPTCHA_FOR_GUEST, "Use Captcha for guest users comments.", true);
        guestCaptcha.setHelp("Will require the guest user to answer a captcha (type a code read from an image), when postic a comment.<br>This is used to prevent 'robots' to post spam.<br>Good idea to enable this for guest users.");
        addFormField(guestCaptcha);
        JOTFormCheckboxField loggedCaptcha=new JOTFormCheckboxField(WikiPreferences.NS_COMMENTS_CAPTCHA_FOR_LOGGED, "Use Captcha for logged users comments.", false);
        loggedCaptcha.setHelp("Will require a registered user to answer a captcha (type a code read from an image), when postic a comment.<br>This is used to prevent 'robots' to post spam.<br>Usually not needed for a logged user. ");
        addFormField(loggedCaptcha);
        JOTFormCheckboxField commentsOrder=new JOTFormCheckboxField(WikiPreferences.NS_COMMENTS_NEWEST_FIRST, "Show newest comments first.", true);
        commentsOrder.setHelp("Control comments order. Default is 'newest first'.");
        addFormField(commentsOrder);
        JOTFormTextField howMany=new JOTFormTextField(WikiPreferences.NS_COMMENTS_HOW_MANY, "How many comment to show in the page(max)", 3, "5");
        howMany.setHelp("How many comments should show in the page by default. The user can see more after cliking 'view more comments'.<b>Can be ovveriden in the page options</b>.");        
        addFormField(howMany);
        JOTFormTextField commentsEmails=new JOTFormTextField(WikiPreferences.NS_COMMENTS_EMAIL_TO, "Send copy of comments to:", 30,"");
        commentsEmails.setHelp("Comma separated list of email address(es) that will receive a copy of all comments as they are posted (ie: a moderator).<b>Can be ovveriden in the page options</b>.You must have configured the mail server in the main config options for this to work.");
        addFormField(commentsEmails);
        
        addCategory(new JOTFormCategory("Search feature"));
        JOTFormCheckboxField enableSearch=new JOTFormCheckboxField(WikiPreferences.NS_SEARCH_ENABLED, "Enable search/indexing", true);
        enableSearch.setHelp("Wether to allow the search feature.<br>If 'off' then the serach form feature be available <b>AND</b> this namespace won't be indexed at all. ");
        addFormField(enableSearch);
        JOTFormCheckboxField searchAcross=new JOTFormCheckboxField(WikiPreferences.NS_SEARCH_ACROSS, "Search across namespaces", false);
        searchAcross.setHelp("When searching, should we show results from this Namespace only, or all namespaces ?");
        addFormField(searchAcross);
        JOTFormTextField maxResults=new JOTFormTextField(WikiPreferences.NS_SEARCH_MAX_RESULTS, "Maximum number of results", 3, "100");
        maxResults.setHelp("When searching,how many results (maximum, total) to return.");
        addFormField(maxResults);
        JOTFormTextField perPage=new JOTFormTextField(WikiPreferences.NS_SEARCH_RESULTS_PER_PAGE, "How many results per page(max)", 3, "20");
        addFormField(perPage);
        
        
        addCategory(new JOTFormCategory("Optional Special Features"));
        JOTFormTextField mantis=new JOTFormTextField(WikiPreferences.NS_MANTIS_ROOT, "Mantis root URL", 30,"");
        mantis.setHelp("Leave empty if you don't have mantis or don't want to use it.<br>If you want to enable the (minimal) integration with the mantis bug tracker, please enter here the URL of the root of your mantis site.<br>Ex: http://mantis.mycomp.com/mantis/");
        addFormField(mantis);

        addSubmitButton(new JOTFormSubmitButton("Save Namespace"));
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        Hashtable errors = new Hashtable();
        String oldNs = get(OLD_NS_NAME).getValue();
        String newNs = get(NS_NAME).getValue();
        if (!newNs.equals(oldNs))
        {
            if (WikiPreferences.getInstance().getNamespaceList().contains(newNs))
            {
                errors.put("1", "This Namespace name is already in use !");
            }
        }
        // validate the namespace as valid characters
        if (!WikiUtilities.isValidUrlSubtring(newNs))
        {
            errors.put("2", "The Namespace Name can only use a-z,A-Z,0-9,-,_");
        }
        return errors;
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        return WikiPermission.hasPermission(request, WikiPermission.MANAGE_NAMESPACES);
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        String oldNs = get(OLD_NS_NAME).getValue();
        String newNs = get(NS_NAME).getValue();
        ((NameSpaceProperties) props).setNewNs(newNs);
        ((NameSpaceProperties) props).setOldNs(oldNs);

        String dataFolder = WikiPreferences.getInstance().getDataFolder();
        if (oldNs.equals(NEW_NS))
        {
            //new ns, create folders
            String nsFolder = JOTUtilities.endWithSlash(JOTUtilities.endWithSlash(dataFolder) + newNs);
            new File(nsFolder).mkdirs();
            new File(nsFolder + "pages").mkdirs();
            new File(nsFolder + "oldpages").mkdirs();
            new File(nsFolder + "files").mkdirs();
        }
        if (!oldNs.equals(NEW_NS) && !newNs.equals(oldNs))
        {
            //renamed ns.
            String oldNsFolder = JOTUtilities.endWithSlash(JOTUtilities.endWithSlash(dataFolder) + oldNs);
            String nsFolder = JOTUtilities.endWithSlash(JOTUtilities.endWithSlash(dataFolder) + newNs);
            new File(oldNsFolder).renameTo(new File(nsFolder));
        }

        super.save(request);

    }

    //override
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public void saveProperties() throws Exception
    {
        WikiPreferences prefs = WikiPreferences.getInstance();
        prefs.getProperties().putAll(props);
        prefs.save();
        // force reload of prefs.
        prefs.loadFrom(new File(prefs.getConfFolder(), WikiPreferences.FILENAME));
        int breakpoint=0;
    }

    /**
     * Overload hack of Properties so we can deal with/replace our custom properties
     * which have the namespace prepended
     * @author thibautc
     *
     */
    private class NameSpaceProperties extends Properties
    {

        String newNs = null;
        String oldNs = null;

        public void setOldNs(String ns)
        {
            oldNs = ns;
        }

        public void setNewNs(String ns)
        {
            newNs = ns;
        }

        public NameSpaceProperties(Properties original)
        {
            //copy mapping from original hashtable.
            putAll(original);
        }

        //overload
        public String getProperty(String key)
        {
            key = oldNs + "." + key;
            return super.getProperty(key);
        }
        // overload of standard function
        public Object setProperty(String key, String value)
        {
            if (!oldNs.equals(NEW_NS) && !newNs.equals(oldNs))
            {
                // renamed ns, remove old prop first
                remove(oldNs + "." + key);
            }
            return super.setProperty(newNs + "." + key, value);
        }
    }
}

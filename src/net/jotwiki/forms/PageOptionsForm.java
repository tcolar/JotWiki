/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms;

import java.util.Hashtable;

import javax.servlet.http.HttpSession;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTDBForm;
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
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;

/**
 * Form for a wiki page options
 * @author tcolar
 */
public class PageOptionsForm extends JOTDBForm
{

    public static final String NAMESPACE = "nameSpace";
    public static final String PAGE_NAME = "pageName";
    public static final String AUTHOR = "author";
    public static final String DOC_TYPE = "docType";
    public static final String BLOG_ENTRIES = "blogEntries";
    public static final String BLOG_CAL = "blogCalendar";
    public static final String BLOG_RSS = "blogRss";
    public static final String BLOG_RSS_TITLE = "blogRssTitle";
    public static final String COMMENTS_ENABLED = "commentsEnabled";
    public static final String COMMENTS_NB = "commentsNb";
    public static final String COMMENTS_GUEST = "commentsGuest";
    public static final String COMMENTS_EMAIL = "commentsEmail";

    //private String currentPage=null;
    //private String currentNs=null;
    //override
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public void layoutForm(JOTFlowRequest request)
    {

        HttpSession session = request.getSession();
        String page = request.getParameter(Constants.PAGE_NAME_SHORT);
        //if(page==null) page=(String)session.getAttribute(Constants.TEMP_PAGE);
        if (request.getAttribute("returnToEdit") != null)
        {
            addFormField(new JOTFormHiddenField("returnToEdit", page));
        }
        String ns = (String) session.getAttribute(Constants.NAMESPACE);
        WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);
        String author = "";
        if (user != null)
        {
            author = user.getFirstName() + " " + user.getLastName();
        }

        setFormTitle("Page Options");
        setFormAction("submitpageoptions.do");

        addCategory(new JOTFormCategory("General settings"));
        String[] types = {"Wiki", "Blog"};
        String[] defaults = {"Wiki"};
        // TODO: namespaces select ?
        JOTFormTextField names = new JOTFormTextField(NAMESPACE, "Namespace: ", 20, ns);
        addFormField(names);
        JOTFormTextField name = new JOTFormTextField(PAGE_NAME, "Page Name: ", 20, page);
        name.setHelp("<b>Warning: Changing the page name has lots of implications ... TBD ...</b>");
        addFormField(name);

        // TODO: user list select ?
        addFormField(new JOTFormTextField(AUTHOR, "Author: ", 20, author));
        addFormField(new JOTFormSelectField(DOC_TYPE, "Page Type", 1, types, defaults));
        
        addCategory(new JOTFormCategory("Comments"));
        boolean enabled=WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_ENABLED, Boolean.TRUE).booleanValue();
        JOTFormCheckboxField enableComments = new JOTFormCheckboxField(COMMENTS_ENABLED, "Enable comments", enabled);
        enableComments.setHelp("Wether to allow comments by default.<br><b>Can be ovveriden in the page options</b>.");
        addFormField(enableComments);
        boolean guest=WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_ALLOW_GUEST, Boolean.TRUE).booleanValue();
        JOTFormCheckboxField guestComments = new JOTFormCheckboxField(COMMENTS_GUEST, "Allow guests to post comments.", guest);
        guestComments.setHelp("<b>Can be ovveriden in the page options</b>.");
        addFormField(guestComments);
        String nb=WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_COMMENTS_HOW_MANY, "5");
        JOTFormTextField howMany = new JOTFormTextField(COMMENTS_NB, "How many comment to show in the page(max)", 3, nb);
        howMany.setHelp("How many comments should show in the page by default. The user can see more after cliking 'view more comments'.<b>Can be ovveriden in the page options</b>.");
        addFormField(howMany);
        String emails=WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_COMMENTS_EMAIL_TO, "");
        JOTFormTextField commentsEmails = new JOTFormTextField(COMMENTS_EMAIL, "Send copy of comments to:", 30, emails);
        commentsEmails.setHelp("Comma separated list of email address(es) that will receive a copy of all comments as they are posted (ie: a moderator).<b>Can be ovveriden in the page options</b>.You must have configured the mail server in the main config options for this to work.");
        addFormField(commentsEmails);

        addCategory(new JOTFormCategory("Blog Options (for page type : blog)"));
        addFormField(new JOTFormTextField(BLOG_ENTRIES, "Blog entries per page", 3, "10"));
        addFormField(new JOTFormCheckboxField(BLOG_CAL, "Show Blog Calendar ?", false));
        addFormField(new JOTFormCheckboxField(BLOG_RSS, "Create RSS for this Blog ?", false));
        addFormField(new JOTFormTextField(BLOG_RSS_TITLE, "Blog RSS Title", 20, ""));

        addSubmitButton(new JOTFormSubmitButton("Save Options"));
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        Hashtable errors = new Hashtable();
        // TODO: validate page name does not already exists in namespace and no weird characters
        return errors;
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        super.save(request);
        // TODO: ++ page changed name
        //TODO: ++ page chnaged namespace

        if (request.getParameter("returnToEdit") != null)
        {
            request.getSession().setAttribute("returnToEdit", request.getParameter(PAGE_NAME));
            setResult("toedit");
        }
    }

    public void updateModel(JOTFlowRequest request) throws Exception
    {
        String page = request.getParameter(Constants.PAGE_NAME_SHORT);
        String ns = request.getParameter(Constants.NAMESPACE_SHORT);
        JOTSQLCondition cond=new JOTSQLCondition(PAGE_NAME, JOTSQLCondition.IS_EQUAL, page);
        JOTSQLCondition cond2=new JOTSQLCondition(NAMESPACE, JOTSQLCondition.IS_EQUAL, ns);
        model = JOTQueryBuilder.selectQuery(PageOptions.class).where(cond).where(cond2).findOrCreateOne();
        layoutForm(request);
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        return WikiPermission.hasEditPermission(request);
    }
}

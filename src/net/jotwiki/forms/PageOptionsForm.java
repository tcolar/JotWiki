/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms;

import java.io.File;
import java.util.Hashtable;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.web.JOTFlowRequest;
import net.jot.utils.JOTTextFileCache;
import net.jot.web.forms.ui.JOTFormCategory;
import net.jot.web.forms.ui.JOTFormCheckboxField;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormHiddenField;
import net.jot.web.forms.ui.JOTFormSelectField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jot.web.forms.ui.JOTFormTextareaField;
import net.jot.web.view.JOTView;
import net.jot.web.view.JOTViewParser;
import net.jotwiki.Constants;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;
import net.jot.utils.JOTPair;
import net.jot.web.forms.JOTDBItemForm;
import net.jotwiki.db.PageVariable;

/**
 * Form for a wiki page options
 * @author tcolar
 */
public class PageOptionsForm extends JOTDBItemForm
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
	public static final Pattern WIKI_VAR_PATTERN = Pattern.compile("(<wiki:var\\s+name=\"([^\"]+)\"\\s*>)", JOTViewParser.PATTERN_FLAGS);
	public static final Pattern OPEN_VAR_PATTERN = Pattern.compile("<wiki:var\\s+name=\"[^\"]+\"\\s*>", JOTViewParser.PATTERN_FLAGS);
	public static final String CLOSE_VAR_STRING = "</wiki:var>";
	public static final Pattern CLOSE_VAR_PATTERN = Pattern.compile(CLOSE_VAR_STRING, JOTViewParser.PATTERN_FLAGS);
	public static final String WIKI_VAR_ID = "__WV_";
	public static final String WIKI_VAR_CHECKBOX_ID = "__WVC_";
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
		String ns = (String) session.getAttribute(Constants.NAMESPACE);

		String page = request.getParameter(Constants.PAGE_NAME_SHORT);
		//if(page==null) page=(String)session.getAttribute(Constants.TEMP_PAGE);
		if (request.getAttribute("returnToEdit") != null)
		{
			addFormField(new JOTFormHiddenField("returnToEdit", page));
		}
		WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);
		String author = "";
		if (user != null)
		{
			author = user.getFirstName() + " " + user.getLastName();
		}

		setFormTitle("Page Options");
		setFormAction("submitpageoptions.do");

		addCategory(new JOTFormCategory("General settings"));
		String[] types =
		{
			"Wiki", "Blog"
		};
		String[] defaults =
		{
			"Wiki"
		};
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
		boolean enabled = WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_ENABLED, Boolean.TRUE).booleanValue();
		JOTFormCheckboxField enableComments = new JOTFormCheckboxField(COMMENTS_ENABLED, "Enable comments", enabled);
		enableComments.setHelp("Wether to allow comments by default.<br><b>Can be ovveriden in the page options</b>.");
		addFormField(enableComments);
		boolean guest = WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_ALLOW_GUEST, Boolean.TRUE).booleanValue();
		JOTFormCheckboxField guestComments = new JOTFormCheckboxField(COMMENTS_GUEST, "Allow guests to post comments.", guest);
		guestComments.setHelp("<b>Can be ovveriden in the page options</b>.");
		addFormField(guestComments);
		String nb = WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_COMMENTS_HOW_MANY, "5");
		JOTFormTextField howMany = new JOTFormTextField(COMMENTS_NB, "How many comment to show in the page(max)", 3, nb);
		howMany.setHelp("How many comments should show in the page by default. The user can see more after cliking 'view more comments'.<b>Can be ovveriden in the page options</b>.");
		addFormField(howMany);
		String emails = WikiPreferences.getInstance().getDefaultedNsString(request, WikiPreferences.NS_COMMENTS_EMAIL_TO, "");
		JOTFormTextField commentsEmails = new JOTFormTextField(COMMENTS_EMAIL, "Send copy of comments to:", 30, emails);
		commentsEmails.setHelp("Comma separated list of email address(es) that will receive a copy of all comments as they are posted (ie: a moderator).<b>Can be ovveriden in the page options</b>.You must have configured the mail server in the main config options for this to work.");
		addFormField(commentsEmails);

		addCategory(new JOTFormCategory("Blog Options (for page type : blog)"));
		addFormField(new JOTFormTextField(BLOG_ENTRIES, "Blog entries per page", 3, "10"));
		addFormField(new JOTFormCheckboxField(BLOG_CAL, "Show Blog Calendar ?", false));
		addFormField(new JOTFormCheckboxField(BLOG_RSS, "Create RSS for this Blog ?", false));
		addFormField(new JOTFormTextField(BLOG_RSS_TITLE, "Blog RSS Title", 20, ""));

		// Page customizable tags (<wiki:var name="keywords"></wiki:var>)
		String help = "Check this box to use custom value entered in following field instead of default value.";
		String help2 = "Replace a template placeholder in the template with data<br>Ex: in the template <b>&lt;wiki:var name=\"keywords\">default keyword&lt;/wiki:var></b><br/>and here we could Enter:<br/><i>Cheap Stuff</i><br/>or:<br/><i>&lt;jot:include file=\"kw_page\">&lt;/jot:include></i><br/>To include the wiki page 'kw_page'";

		Vector vars = findCustomTemplateVariables(ns, page);

		addCategory(new JOTFormCategory("Custom Template Variables"));
		for (int i = 0; i != vars.size(); i++)
		{
			WikiCustomVariable var = (WikiCustomVariable) vars.get(i);
			JOTFormCheckboxField chk = new JOTFormCheckboxField(WIKI_VAR_CHECKBOX_ID + var.getName(), "Use custom for " + var.getName(), !var.isDefaulted());
			chk.setHelp(help);
			addFormField(chk);
			JOTFormTextareaField text = new JOTFormTextareaField(WIKI_VAR_ID + var.getName(), var.getName(), 60, 3, var.getValue());
			text.setHelp(help2);
			addFormField(text);
		}

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
		//TODO: ++ page changed namespace

		// custom vars
		HttpSession session = request.getSession();
		String ns = (String) session.getAttribute(Constants.NAMESPACE);
		String page = request.getParameter(Constants.PAGE_NAME);
		Vector vars = findCustomTemplateVariables(ns, page);
		JOTSQLCondition cond = new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
		JOTSQLCondition cond2 = new JOTSQLCondition("page", JOTSQLCondition.IS_EQUAL, page);

		for (int i = 0; i != vars.size(); i++)
		{
			WikiCustomVariable var = (WikiCustomVariable) vars.get(i);
			JOTSQLCondition cond3 = new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, var.getName());
			String chk = request.getParameter(WIKI_VAR_CHECKBOX_ID + var.getName());
			String val = request.getParameter(WIKI_VAR_ID + var.getName());

			PageVariable pageVar = (PageVariable) JOTQueryBuilder.selectQuery(PageVariable.class).where(cond).where(cond2).where(cond3).findOne();
			if (chk != null && (chk.toLowerCase().equals("on") || chk.toLowerCase().equals("selected")))
			{
				if (pageVar == null)
				{
					pageVar = new PageVariable();
				}
				pageVar.setName(var.getName());
				pageVar.setNameSpace(ns);
				pageVar.setPage(page);
				pageVar.setValue(val);
				pageVar.save();
			} else
			{
				if (pageVar != null)
				{
					pageVar.delete();
				}
			}
		}

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
		JOTSQLCondition cond = new JOTSQLCondition(PAGE_NAME, JOTSQLCondition.IS_EQUAL, page);
		JOTSQLCondition cond2 = new JOTSQLCondition(NAMESPACE, JOTSQLCondition.IS_EQUAL, ns);
		model = JOTQueryBuilder.selectQuery(PageOptions.class).where(cond).where(cond2).findOrCreateOne();
		layoutForm(request);
	}

	public boolean validatePermissions(JOTFlowRequest request)
	{
		return WikiPermission.hasEditPermission(request);
	}

	/**
	 * return vector of WikiCustomVariable
	 * @param ns
	 * @return
	 */
	private Vector findCustomTemplateVariables(String ns, String page)
	{
		Vector variables = new Vector();
		// find special "custom" variables in the template.
		File f = new File(WikiPreferences.getInstance().getCurrentTemplateFolder(ns) + File.separator + "view.html");
		if (!f.exists())
		{
			// otherise use the files formthe deafult NS
			f = new File(WikiPreferences.getInstance().getTemplatesFolder() + File.separator + Constants.DEFAULT_NS + File.separator + "view.html");
		}

		String template = "";
		try
		{
			template = JOTTextFileCache.getFileText(f.getAbsolutePath());
		} catch (Exception e)
		{
			JOTLogger.logException(this, "Failed to load template: " + f.getAbsolutePath(), e);
		}

		//TODO: cache ?
		Matcher m = WIKI_VAR_PATTERN.matcher(template);
		while (m.find())
		{
			StringBuffer buf = new StringBuffer();
			String name = m.group(2).trim();
			JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found wiki var " + name);

			JOTPair pair = JOTViewParser.findMatchingClosingTag(m.end(), template, OPEN_VAR_PATTERN, CLOSE_VAR_PATTERN);
			int index = pair.getX();
			if (index != -1)
			{
				String value = template.substring(m.end(), pair.getX());
				JOTSQLCondition cond1 = new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, name);
				JOTSQLCondition cond2 = new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
				JOTSQLCondition cond3 = new JOTSQLCondition("page", JOTSQLCondition.IS_EQUAL, page);
				PageVariable pages = null;
				boolean defaulted = true;
				try
				{
					PageVariable var = (PageVariable) JOTQueryBuilder.selectQuery(PageVariable.class).where(cond1).where(cond2).where(cond3).findOne();
					if (var != null)
					{
						value = var.getValue();
						defaulted = false;
					}
				} catch (Exception e)
				{
					JOTLogger.logException(this, "Failed to load template: " + f.getAbsolutePath(), e);
				}
				variables.add(new WikiCustomVariable(name, value, defaulted));
			}
		}
		return variables;
	}

	// represent a found custom variable in template
	class WikiCustomVariable
	{

		String name = "";
		String value = "";
		boolean defaulted = true;

		public WikiCustomVariable(String name, String value, boolean defaulted)
		{
			this.defaulted = defaulted;
			this.name = name;
			this.value = value;
		}

		public boolean isDefaulted()
		{
			return defaulted;
		}

		public String getName()
		{
			return name;
		}

		public String getValue()
		{
			return value;
		}
	}
}

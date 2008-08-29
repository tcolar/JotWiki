/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.captcha.JOTSendCaptchaView;
import net.jot.web.forms.JOTFormElement;
import net.jot.web.forms.JOTGeneratedForm;
import net.jot.web.forms.ui.JOTFormCaptchaField;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jot.web.forms.ui.JOTFormTextareaField;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiUser;

public class CommentForm extends JOTGeneratedForm
{

    private static final String ERROR_CSS = "form_error";
    public final String TITLE = "title";
    public final String AUTHOR = "author";
    public final String TEXT = "text";
    public final String CAPTCHA = "captcha";
    //public final String PAGE = "page";
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        String page = (String) request.getSession().getAttribute(Constants.SESSION_LAST_PAGE);
        String title = get(TITLE).getValue();
        String text = get(TEXT).getValue();
        String author = WikiUser.getCurrentUser(request).getLogin();
        if (!WikiUser.isLoggedIn(WikiUser.getCurrentUser(request)))
        {
            author = get(AUTHOR).getValue() + " (Guest)";
        }
        //save comment to file
        try
        {
            String ns = WikiUtilities.getNamespace(request);
            String folder = JOTUtilities.endWithSlash(WikiPreferences.getInstance().getCommentsFolder(ns)) + page;
            new File(folder).mkdirs();
            String name = "";
            Date now = null;
            synchronized (this)
            {
                now = new Date();
                Thread.sleep(1);
            }
            name = "" + now.getTime() + ".txt";
            File f = new File(folder, name);
            PrintWriter writer = new PrintWriter(new FileOutputStream(f));
            writer.println("TIME: " + JOTUtilities.formatDate(now, false));
            writer.println("AUTHOR: " + author);
            writer.println("TITLE: " + title);
            writer.println("TEXT: " + text);
            writer.flush();
            writer.close();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to write comment to file.", e);
        }
        //send email
        try
        {
            PageOptions options = PageReader.getPageOptions(WikiUtilities.getNamespace(request), page);
            String email = options.getCommentsEmail();
            String[] emails = email.split(",");
            if (emails.length > 0)
            {
                String report="Page: "+page;
                report += "\nFrom: " + author;
                report += "\nTitle:" + title;
                report += "\nText:" + text;
                for (int i = 0; i != emails.length; i++)
                {
                    WikiUtilities.getEmailer().send(emails[i].trim(), "JOTWiki: New comment posted", report);
                }
            }
        } catch (Exception e)
        {
        }
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        String page = (String) request.getSession().getAttribute(Constants.SESSION_LAST_PAGE);
        try
        {
            PageOptions options = PageReader.getPageOptions(WikiUtilities.getNamespace(request), page);
            if (!options.getCommentsEnabled().booleanValue())
            {
                return false;
            }
            if (WikiUser.isGuest(WikiUser.getCurrentUser(request)) && !options.getCommentsGuest().booleanValue())
            {
                return false;
            }
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public void layoutForm(JOTFlowRequest request)
    {
        setFormTitle("Add a Comment");
        setFormAction("submitcomment.do");
        //addFormField(new JOTFormHiddenField(PAGE,request.getParameter("page")));
        if (!WikiUser.isLoggedIn(WikiUser.getCurrentUser(request)))
        {
            addFormField(new JOTFormTextField(AUTHOR, "Author:", 20, "Guest"));
        }
        addFormField(new JOTFormTextField(TITLE, "Title:", 60, "My 2 cents."));
        addFormField(new JOTFormTextareaField(TEXT, "Text:", 60, 5, ""));
        if (needsCaptcha(request))
        {
            addFormField(new JOTFormCaptchaField(CAPTCHA, "Captcha:", "sendCaptcha.do", JOTSendCaptchaView.getGenerator()));
        }
        // captcha code will go in session.setAttribute(CAPTCHA_SESSION_ID,captcha);

        addSubmitButton(new JOTFormSubmitButton("Post Comment"));
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        Hashtable h = new Hashtable();

        if (needsCaptcha(request))
        {
            JOTFormElement captcha = get(CAPTCHA);
            captcha.unsetTagProperty("class");
            String code = (String) request.getSession().getAttribute(JOTSendCaptchaView.CAPTCHA_SESSION_ID);
            if (code == null || captcha.getValue() == null || !captcha.getValue().equalsIgnoreCase(code))
            {
                h.put("Captcha error", "Captcha verification failed.");
                captcha.setTagProperty("class", ERROR_CSS);
            }
        }
        return h;
    }

    private boolean needsCaptcha(JOTFlowRequest request)
    {
        WikiUser user = WikiUser.getCurrentUser(request);
        boolean guestCapctha = WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_CAPTCHA_FOR_GUEST, Boolean.TRUE).booleanValue();
        boolean regCapctha = WikiPreferences.getInstance().getDefaultedNsBoolean(request, WikiPreferences.NS_COMMENTS_CAPTCHA_FOR_LOGGED, Boolean.FALSE).booleanValue();
        return (WikiUser.isGuest(user) && guestCapctha) || (WikiUser.isLoggedIn(user) && regCapctha);
    }
}

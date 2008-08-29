/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.jot.utils.JOTEmailSender;
import net.jot.web.forms.ui.JOTFormField;

/**
 * Collection of utilities for jotwiki
 * @author tcolar
 */
public class WikiUtilities
{

    private static JOTEmailSender emailer = null;
    private static Pattern simpleChars = Pattern.compile("[a-zA-Z0-9-_]+");

    /**
     * Returns the current namespace the user in it
     * @param req
     * @return
     */
    public static String getNamespace(HttpServletRequest req)
    {
        String nameSpace = (String) req.getSession().getAttribute(Constants.NAMESPACE);
        if (nameSpace == null)
        {
            nameSpace = Constants.DEFAULT_NS;
        }
        return nameSpace;
    }

    public static void setNamespace(HttpServletRequest req, String ns)
    {
        req.getSession().setAttribute(Constants.NAMESPACE, ns);
    }

    /**
     * Return generated html code for a Form description/help
     * This creates HTML with the form title, foolowed by an "help icon", which when
     * clicked shows the help message.
     * @param field
     * @param spanCpt
     * @return
     */
    public static String getCustomFormDescription(JOTFormField field, int spanCpt)
    {
        String desc = "";
        if (field.getHelp() != null)
        {
            String spanId = "_help_span_" + spanCpt;
            desc += field.getDescription() + "<a class='gen_form_help_link' href='#' onClick=\"gen_form_toggle('" + spanId + "');\">&nbsp;<img src='images/help.png' border=0></a>";
            desc += "<br><div id='" + spanId + "' class='gen_form_help'>" + field.getHelp() + "</div>";
        } else
        {
            desc = field.getDescription();
        }
        return desc;
    }

    /**
     * Check wether a page URL is valid (ie: only numbers,letters and underscores)
     * @param str
     * @return
     */
    public static boolean isValidUrlSubtring(String str)
    {
        Matcher m = simpleChars.matcher(str);
        return m.matches();
    }

    /**
     * Vector of subfolder name(strings) in a specific folder.
     * @return
     */
    public static Vector getSubfolderList(String parentFolder)
    {
        Vector v = new Vector();
        File f = new File(parentFolder);
        File[] files = f.listFiles();
        if (files != null && files.length > 0)
        {
            for (int i = 0; i != files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    v.add(files[i].getName());
                }
            }
        }
        return v;
    }

    public static JOTEmailSender getEmailer()
    {
        if (emailer == null)
        {
            synchronized (WikiUtilities.class)
            {
                emailer = new JOTEmailSender();
                emailer.setHost(WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_MAIL_HOST, null));
                emailer.setPort(WikiPreferences.getInstance().getDefaultedInt(WikiPreferences.GLOBAL_MAIL_PORT, new Integer(25)).intValue());
                emailer.setDomain(WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_MAIL_DOMAIN, "mycompany.com"));
                emailer.setFrom(WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_MAIL_FROM, "postmaster"));
                emailer.setBounceTo(WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_MAIL_FROM, "postmaster"));
                emailer.setReplyTo(WikiPreferences.getInstance().getDefaultedString(WikiPreferences.GLOBAL_MAIL_FROM, "postmaster"));
            }
        }
        return emailer;
    }
}

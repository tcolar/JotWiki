/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.forms;

import java.util.Date;
import java.util.Hashtable;

import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowRequest;
import net.jot.web.JOTRequestCounter;
import net.jot.web.forms.JOTFormElement;
import net.jot.web.forms.JOTGeneratedForm;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormPasswordField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiUser;

public class LoginForm extends JOTGeneratedForm
{

    public static final String LOGGED_USER = "loggedUser";
    private static final String ERROR_CSS = "form_error";
    public final String LOGIN = "login";
    public final String PASSWORD = "password";
    //
    public static JOTRequestCounter counter = new JOTRequestCounter(10);
    public static int maxRequestPerIPPer10Mn = 10;
    public static int blockIPForMn = 60;
    Vector blockedIps = new Vector();
    long blockedTime = new Date().getTime();

//	override
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        return WikiUtilities.getCustomFormDescription(field, spanCpt);
    }

    public void save(JOTFlowRequest request) throws Exception
    {
        JOTFormElement login = get(LOGIN);
        JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Loggin in: " + login.getValue());
        WikiUser user = (WikiUser) WikiUser.getUserByLogin(WikiUser.class, login.getValue());
        request.getSession(true).setAttribute(LOGGED_USER, user);
    }

    public boolean validatePermissions(JOTFlowRequest request)
    {
        //all open
        return true;
    }

    public void layoutForm(JOTFlowRequest request)
    {
        setFormTitle("Login");
        setFormAction("submitlogin.do");

        addFormField(new JOTFormTextField(LOGIN, "Login:", 20, ""));
        addFormField(new JOTFormPasswordField(PASSWORD, "Password:", 20, ""));

        //addFormField(new JOTFormCaptchaField("Captcha:", "Captcha", "sendCaptcha.do",SendCaptchaView.getGenerator()));

        addSubmitButton(new JOTFormSubmitButton("Login"));
    }

    public Hashtable validateForm(JOTFlowRequest request) throws Exception
    {
        Hashtable h = new Hashtable();

        String ip = request.getRemoteAddr();
        int value = counter.countRequest(request);
        if (blockedTime > new Date().getTime() + blockIPForMn * 60000)
        {
            blockedIps.clear();
        }
        if (value > maxRequestPerIPPer10Mn)
        {
            blockedIps.add(ip);
            JOTLogger.log(JOTLogger.WARNING_LEVEL, this, "Blocking Login request for: " + ip);
        }
        if (blockedIps.contains(ip))
        {
            h.put("LOGIN_ERROR", "Too many tries.");
            return h;
        }



        JOTFormElement login = get(LOGIN);
        JOTFormElement password = get(PASSWORD);
        login.unsetTagProperty("class");
        password.unsetTagProperty("class");
        String l = login.getValue();
        String p = password.getValue();
        if (l == null || p == null)
        {
            h.put("LOGIN_ERROR", "Please enter your Login and Password.");
            login.setTagProperty("class", ERROR_CSS);
            password.setTagProperty("class", ERROR_CSS);
        }
        if (!WikiUser.isUserValid(WikiUser.class, l, p))
        {
            h.put("LOGIN_ERROR", "Invalid Login/Password.");
            login.setTagProperty("class", ERROR_CSS);
            password.setTagProperty("class", ERROR_CSS);
            // Lame delay to slow down brute forced Password cracking.
            Thread.sleep(5000);
            JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Failed Login for: " + l);
        }
        return h;
    }
}

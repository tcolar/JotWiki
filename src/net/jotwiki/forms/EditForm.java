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
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.servlet.http.HttpSession;

import net.jot.logger.JOTLogger;
import net.jot.scheduler.JOTClock;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTForm;
import net.jot.web.forms.JOTFormConst;
import net.jot.web.views.JOTMessageView;
import net.jotwiki.Constants;
import net.jotwiki.PageReader;
import net.jotwiki.SitemapGenerator;
import net.jotwiki.WikiPreferences;
import net.jotwiki.db.PageLock;
import net.jotwiki.db.PageOptions;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiUser;

/**
 * The Page edition Megaform
 * @author tcolar
 */
public class EditForm extends JOTForm
{

  public static final String TEXT = "text";
  public static final String PAGENAME = Constants.PAGE_NAME;
  public static final String NAMESPACE = Constants.NAMESPACE;
  public static final String MD5 = Constants.PAGE_MD5;

  public void init(JOTFlowRequest request) throws Exception
  {
    defineField(TEXT, JOTFormConst.TEXTAREA);
    defineField(PAGENAME, JOTFormConst.INPUT_HIDDEN);
    defineField(NAMESPACE, JOTFormConst.INPUT_HIDDEN);
    defineField(MD5, JOTFormConst.INPUT_HIDDEN);
  }

  public void save(JOTFlowRequest request) throws Exception
  {
    HttpSession session = request.getSession();
    WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);
    if (!WikiPermission.hasEditPermission(request))
    {
      throw new Exception("Not allowed to edit page!");
    }

    String text = get(TEXT).getValue();
    String pageName = get(PAGENAME).getValue();
    String nameSpace = get(NAMESPACE).getValue();

    boolean conflicted = isPageConflicted(user.getLogin());

    synchronized (this)
    {
      if (!conflicted)
      {
        archivePage(nameSpace, pageName);
        //savePageInfos(pageName.getValue(), );
        SitemapGenerator.setDirty(true);
      }
      else
      {
        // save under a special "conflict" filename.
        pageName = "~" + pageName + "~_" + JOTClock.getDateStringWithMs();
        JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Page as changed while edited (MD5/Lock)! will save under a temp name: " + nameSpace + ":" + pageName);
        // set the message
        request.setAttribute(JOTMessageView.MESSAGE_TITLE, "Page has changed while you edited !");
        String msg = "<font color='red'><b>This probably happened because you let your lock expire, another author edited/locked this page since.<br>We cannot save the page, because we could loose the other author change.<br>We have saved your page under a TEMPORARY name here:<br><a href='"+pageName+"'>"+pageName+"</a><br>SAVE THIS LINK !<br>Once the other author is done with his edit, you should manually merge your changes into the 'real' page.</b></font>";
        request.setAttribute(JOTMessageView.MESSAGE_TEXT, msg);
        request.setAttribute(JOTMessageView.MESSAGE_LINK, "last.do");

        // will redirect to message page.
        setResult("conflicted");
      }
      // save the new text to the file.
      savePage(nameSpace, pageName, text);
      PageOptions options=PageOptions.getPageOptions(nameSpace, pageName);
      options.setDataAuthor(user.getLogin());
      options.save();
    }

  }

  /**
   * Check wether a page is comflicted
   * ie: is locked by somebody else or has changed on file(MD5 hash changed)
   * @param author
   * @return
   * @throws java.lang.Exception
   */
  private boolean isPageConflicted(String author) throws Exception
  {
    String pageName = get(PAGENAME).getValue();
    String nameSpace = get(NAMESPACE).getValue();
    String oldMD5 = get(MD5).getValue();

    String newMD5 = PageReader.getPageMD5(nameSpace, pageName);
    
    PageLock lock=PageLock.getPageLock(nameSpace, pageName, false);
// if file changed (MD5) : conflict
    if (!oldMD5.equals(newMD5))
      return true;
// if file is now locked by somebody else  : conflict    
    if(lock!=null && ! lock.getAuthor().equals(author))
      return true;
    
    return false;
  }
  
  public Hashtable validate(JOTFlowRequest request) throws Exception
  {
    return new Hashtable();
  }

  /**
   * Daves the edited page to file
   * @param ns
   * @param pageName
   * @param text
   * @throws java.lang.Exception
   */
  private synchronized void savePage(String ns, String pageName, String text) throws Exception
  {
    String pageFolder = WikiPreferences.getInstance().getPagesFolder(ns);
    File file = new File(pageFolder, pageName + ".txt");
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(text.getBytes());
    fos.close();
  }

  /**
   * Backs-up the previous version of the page
   * @param ns
   * @param pageName
   * @throws java.lang.Exception
   */
  private synchronized void archivePage(String ns, String pageName) throws Exception
  {
    String pageFolder = WikiPreferences.getInstance().getPagesFolder(ns);
    File page = new File(pageFolder, pageName + ".txt");
    if (page.exists())
    {
      GregorianCalendar c = new GregorianCalendar();
      String stamp = "_" + JOTClock.getDateStringWithMs();
      File archive = new File(WikiPreferences.getInstance().getArchiveFolder(ns), pageName + stamp + ".txt");
      JOTUtilities.copyFile(archive, page);
    }
  }

  public boolean validatePermissions(JOTFlowRequest request)
  {
          return WikiPermission.hasEditPermission(request);
  }
}

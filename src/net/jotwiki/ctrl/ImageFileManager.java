/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.ctrl;

import java.io.File;

import net.jot.web.filebrowser.JOTFileBrowserController;
import net.jot.web.filebrowser.JOTFileBrowserHelper;
import net.jot.web.filebrowser.JOTFileBrowserSession;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 *  This is an extension to the standard JOTFileBrowserController
 *  Cstomized to give permissions to a page author to upload/pick images(and other files)
 * @author tcolar
 */
public class ImageFileManager extends JOTFileBrowserController
{

  public JOTFileBrowserSession createFbSession()
  {
    //WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);

    String rootFolder = WikiPreferences.getInstance().getFilesFolder(WikiUtilities.getNamespace(request));
    String startFolder = rootFolder;

    JOTFileBrowserSession fbSession = new JOTFileBrowserSession(new File(rootFolder), new File(startFolder), JOTFileBrowserHelper.TYPE_CHOOSE_1PLUS_FILE);
    fbSession.setTitle("Please pick at least 1 file.");
    fbSession.setNbOfUploadFields(3);
    fbSession.setAllowListFiles(true);
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_CREATE_FOLDERS))
    {
      fbSession.setAllowCreateFolders(true);
    }
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_VIEW_FILES))
    {
      fbSession.setAllowDownloadFile(true);
    }
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_BROWSING))
    {
      fbSession.setAllowBrowsing(true);
    }
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_ADD_FILES))
    {
      fbSession.setAllowUploadFile(true);
    }
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_UPDATE))
    {
      fbSession.setAllowUpdateFile(true);
    }
    if(WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES_DELETE))
    {
      fbSession.setAllowDelete(true);
    }
    return fbSession;
  }

  public boolean validatePermissions()
  {
          return WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES);
  }
}

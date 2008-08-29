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
import net.jotwiki.db.WikiPermission;

/**
 * This is an extension to the standard JOTFileBrowserController
 * Customize for the Wiki Admin user with extra functionnality
 * such as browse all files, delete, view all etc...
 * @author tcolar
 */
public class AdminFileManager extends JOTFileBrowserController
{

        /**
         * Provides all the 'Admin' permissions
         * @return
         */
	public JOTFileBrowserSession createFbSession()
	{
		String rootFolder=WikiPreferences.getInstance().getDataFolder();
		String startFolder=rootFolder;
		
		JOTFileBrowserSession fbSession=new JOTFileBrowserSession(new File(rootFolder), new File(startFolder),JOTFileBrowserHelper.TYPE_BROWSE);
		fbSession.setTitle("Administrator File Manager");
		fbSession.setNbOfUploadFields(1);
		fbSession.setAllowCreateFolders(true);
		fbSession.setAllowUploadFile(true);
		fbSession.setAllowDelete(true);
		fbSession.setAllowDeleteFilledFolders(true);
		fbSession.setAllowListHiddenFiles(true);
		fbSession.setAllowPickRootFolder(true);
		fbSession.setAllowRenaming(true);
		fbSession.setAllowUpdateFile(true);
		fbSession.setAllowDownloadFile(true);
		fbSession.setAllowBrowsing(true);
		fbSession.setAllowListFiles(true);
		return fbSession;
	}

	public boolean validatePermissions()
	{
          return WikiPermission.hasPermission(request, WikiPermission.MANAGE_FILES);
	}

}

/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import java.io.File;

import net.jot.web.filebrowser.JOTFileBrowserHelper;
import net.jot.web.view.JOTView;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 * Pass along which images/files where selected by the filemanager to the view, so we can then insert them in the edition form,
 * using javascript in this view's html.
 * @author thibautc
 *
 */
public class FilesSelected extends JOTView 
{

	public void prepareViewData() throws Exception 
	{
		String rootFolder=WikiPreferences.getInstance().getFilesFolder(WikiUtilities.getNamespace(request));
		File [] files=JOTFileBrowserHelper.getChosenFiles(request);
		String str="";
		if(files!=null)
		{
			for(int i=0;i!=files.length;i++)
			{
				String path=files[i].getAbsolutePath();
				if(path.indexOf(rootFolder)==0)
				{
					path=path.substring(rootFolder.length(),path.length());
					str+="{{"+path+"}} ";
				}
			}
		}
		addVariable("files",str);
	}

	public boolean validatePermissions() 
	{
          return WikiPermission.hasPermission(request, WikiPermission.UPLOAD_FILES) || WikiPermission.hasPermission(request, WikiPermission.MANAGE_FILES) ;
        }
}

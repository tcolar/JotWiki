/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import net.jot.logger.JOTLogger;
import net.jot.web.view.JOTView;
import net.jotwiki.PageReader;
import net.jotwiki.WikiPreferences;
import net.jotwiki.WikiUtilities;
import net.jotwiki.db.WikiPermission;

/**
 * Special View object that returns an image/file data
 * Note that it directly returns the data and ends the request.
 * @author tcolar
 */
public class FetchImage extends JOTView
{

    public void prepareViewData() throws Exception
    {

        String image = request.getParameter("img");
        if (image != null && PageReader.isImage(image))
        {
            // image
            {
                writeContentType(image.toLowerCase());
            }
            if (image != null)
            {
                writeImageFromFile(image);
            }
        } else
        {
            // file
            writeImageFromFile(image);
        }

        response.flushBuffer();
    // we are done.
    }

    private void writeContentType(String image)
    {
        String type = "image/jpeg";
        if (image.endsWith(".gif"))
        {
            type = "image/gif";
        }
        if (image.endsWith(".png"))
        {
            type = "image/png";
        }
        if (image.endsWith(".bmp"))
        {
            type = "image/bmp";
        }
        response.setContentType(type);
    }

    public void writeImageFromFile(String image) throws Exception
    {
        byte[] cbuf = new byte[50000];
        DataInputStream reader =
                null;
        String page =
                "";
        try
        {
            String nameSpace = WikiUtilities.getNamespace(request);
            String imageRoot =
                    WikiPreferences.getInstance().getFilesFolder(nameSpace);
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTViewParser.class, "Caching template: "+templatePath);						
            reader = new DataInputStream(new FileInputStream(new File(imageRoot, image)));

            int i = 0;
            String s =
                    null;
            int totalSize = 0;
            while ((i = reader.read(cbuf)) != -1)
            {
                totalSize += i;
                response.getOutputStream().write(cbuf, 0, i);
            }

            response.setContentLength(totalSize);
            reader.close();
        } catch (Exception e)
        {
            if (reader != null)
            {
                reader.close();
            }
            JOTLogger.log(JOTLogger.INFO_LEVEL, this, "Image not found for:" + image);
        }

    }

    public boolean validatePermissions()
    {
        return WikiPermission.hasPermission(request, WikiPermission.VIEW_PAGE);
    }
}

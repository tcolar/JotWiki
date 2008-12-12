/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.tmp;

import java.io.File;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTPersistanceManager;
import net.jot.prefs.JOTPreferences;
import net.jot.captcha.generators.JOTSTDCaptchaGenerator;
import net.jot.image.writers.JOTBMPImageWriter;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jotwiki.db.WikiProfile;
import net.jotwiki.db.WikiProfileSet;
import net.jotwiki.db.WikiUser;

/**
 * For quickly testing pieces of code.
 * and cleaning up temporary/test data
 * @author tcolar
 */
public class Test
{

    public static void main(String[] args)
    {
        try
        {
            testDumpCSV();
            //testCaptcha();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void testCaptcha() throws Exception
    {
        /*CaptchaImage image=new CaptchaImage(100, 100);
        AbstractImagePixel pixel0=new AbstractImagePixel(240, 240, 240, 0);
        AbstractImagePixel pixel1=new AbstractImagePixel(255, 0, 0, 0);
        AbstractImagePixel pixel2=new AbstractImagePixel(0,255, 0, 0);
        AbstractImagePixel pixel3=new AbstractImagePixel(0,0,255,0);
        AbstractImagePixel pixel4=new AbstractImagePixel(255, 0, 255, 0);
        AbstractImagePixel pixel5=new AbstractImagePixel(200, 200, 200, 0);
        
        image.fillImage(pixel0);
        image.drawRectangle(3,1,25,99,25, pixel2);
        image.drawWavyLine(4,5,5,50,5, pixel3,true);
        image.drawWavyLine(4,5,5,5,85, pixel3,true);
        image.drawWavyLine(4,50,5,50,85, pixel3,false);
        image.drawWavyLine(4,5,40,50,40, pixel3,false);
        image.drawLine(3,25,25,25,85, pixel2);
        image.drawLine(3,25,5,85,45, pixel4);
        */
        JOTBMPImageWriter writer=new JOTBMPImageWriter();
        JOTSTDCaptchaGenerator gen= new JOTSTDCaptchaGenerator();
        for(int i=0;i!=9;i++)
        {
            String str=gen.writeToFile(writer, new File("/tmp/test"+i+".bmp"));
        }
    }
    
    private static void testDumpCSV() throws Exception
    {
        System.out.println("**************");
        System.setProperty("jot.prefs", "web/jotconf/jot.properties");
        JOTPreferences.getInstance().initPrefs();
        JOTPersistanceManager.getInstance().init(JOTPreferences.getInstance());
        
        /*
        deleteTableEntry(WikiProfile.class, 20);
        deleteTableEntry(WikiProfile.class, 21);
        deleteTableEntry(WikiProfileSet.class, 5);
        deleteTableEntry(WikiProfileSet.class, 20);
        deleteTableEntry(WikiProfileSet.class, 2);
        deleteTableEntry(WikiProfileSet.class, 3);
        */
        
        
        
        //JOTQueryManager.dumpToCSV(System.out, WikiUser.class);
        System.out.println("**************");
        JOTQueryBuilder.dumpToCSV(System.out, WikiUser.class);
        System.out.println("**************");
        JOTQueryBuilder.dumpToCSV(System.out, WikiProfile.class);
        System.out.println("**************");
        JOTQueryBuilder.dumpToCSV(System.out, WikiProfileSet.class);
        System.out.println("**************");
    }
    
    /** use to manually remove a broken/obsolete table entry */
    private static void deleteTableEntry(Class modelClass, long id) throws Exception
    {
        JOTModel model=JOTQueryBuilder.findByID(modelClass, id);
        if(model!=null)
        {
            model.delete();
        }
    }
}

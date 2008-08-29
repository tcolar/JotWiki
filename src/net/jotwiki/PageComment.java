/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Data representation of a page comment
 * @author thibautc
 */
public class PageComment 
{
    private String author="";
    private String title="";
    private String time="";
    private String text="";

    public static PageComment parseFromFile(File f) throws Exception
    {
       PageComment comment=new PageComment();
       BufferedReader reader=new BufferedReader(new FileReader(f));
       String s=null;
       while((s=reader.readLine())!=null)
       {
           if(s.startsWith("TIME: "))
               comment.setTime(s.substring("TIME: ".length(),s.length()));
           if(s.startsWith("AUTHOR: "))
               comment.setAuthor(s.substring("AUTHOR: ".length(),s.length()));
           if(s.startsWith("TITLE: "))
               comment.setTitle(s.substring("TITLE: ".length(),s.length()));
           if(s.startsWith("TEXT: "))
           {
               String text=s.substring("TEXT: ".length(),s.length());
               while((s=reader.readLine())!=null)
               {
                   text+="\n"+s;
               }
               comment.setText(text);
           }
       }
       return comment;
    }
    
    public String getAuthor()
    {
        return author;
    }

    public String getText()
    {
        return text;
    }

    public String getTime()
    {
        return time;
    }

    public String getTitle()
    {
        return title;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
    
    
}

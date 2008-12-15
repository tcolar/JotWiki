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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.utils.JOTHTMLUtilities;
import net.jot.utils.JOTUtilities;
import net.jot.web.util.JOTAntiSpam;
import net.jot.web.view.JOTViewParser;
import net.jotwiki.db.PageOptions;
import net.jotwiki.forms.PageOptionsForm;
import net.jotwiki.view.PageInfos;


/*
 * TODO: user conf overide / edit/add patterns
 * TODO: page cache (after parsing)
 * TODO: -- code color syntax
 * 
 */
/**
 * This is the main class used to read/parse a wiki page
 * @author tcolar
 */
public class PageReader
{

    private static final int PATTERN_FLAGS_MULTI = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE | Pattern.CANON_EQ;
    private static final String PARSER_TAG_HEAD = "!JOT_parser_tag_";
    private static final String PARSER_TAG_TAIL = "!";
    private static final Pattern PARSER_TAG_PATTERN = Pattern.compile(PARSER_TAG_HEAD + "(\\d+)" + PARSER_TAG_TAIL);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("\\{\\{(\\s*)(\\S*)(\\s*)\\}\\}");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("\\@\\@([^@]*)\\@\\@");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^|\\]]*)\\|?([^\\]]*)\\]\\]");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[0-9a-zA-Z_.-]+\\@[0-9a-zA-Z_-]+\\.[0-9a-zA-Z_.-]+");
    private static final Pattern rowPattern = Pattern.compile("\\s*(\\|[^\n]*)");
    private static final Pattern tablePattern = Pattern.compile("^(\\s*\\^.*?)\n\n", PATTERN_FLAGS_MULTI);
    private static final Pattern tableNoHeadPattern = Pattern.compile("^(\\s*\\|.*?)\n\n", PATTERN_FLAGS_MULTI);
    private static final Pattern headerPattern = Pattern.compile("\\s*(\\^[^\n]*)");
    private static final Pattern pHeader = Pattern.compile("(===*)([^=]*)(==+)");
    private static final String STRIP_VAR_HEAD = "!_JOT_STRIP_VAR_";
    private static final String STRIP_VAR_TAIL = "!";

    /**
     * Extract the title and build the TableOfTopics from the plainwiki page
     * Also retrieved in DB the page options specified by the author.
     * @param plainPage
     * @param pageName
     * @param ns
     * @return
     * @throws java.lang.Exception
     */
    public static PageInfos getPageInfos(String plainPage, String pageName, String ns) throws Exception
    {
        PageInfos infos = new PageInfos();
        int titleDepth = 999;
        String title = pageName;
        Matcher m = pHeader.matcher(plainPage);
        while (m.find())
        {
            String prefix = m.group(1);
            String postfix = m.group(3);
            String text = m.group(2).trim();
            if (prefix != null && postfix != null && prefix.length() == postfix.length())
            {
                // h1 is depth 0, h5 is depth 4 
                int depth = 6 - prefix.length();
                // first highest level header found will be title
                if (depth < titleDepth)
                {
                    title = text;
                    titleDepth = depth;
                }
                String link = pageLink(text);
                // save in the TOC
                infos.addTocEntry(depth, text, link);
            }
        }

        infos.setTitle(title);

        PageOptions options=getPageOptions(ns, pageName);
        if(options!=null)
            infos.setOptions(options);

        return infos;
    }

    public static PageOptions getPageOptions(String ns, String pageName) throws Exception
    {
        JOTSQLCondition cond=new JOTSQLCondition(PageOptionsForm.PAGE_NAME, JOTSQLCondition.IS_EQUAL, pageName);
        JOTSQLCondition cond2=new JOTSQLCondition(PageOptionsForm.NAMESPACE, JOTSQLCondition.IS_EQUAL, ns);
        PageOptions options = (PageOptions) JOTQueryBuilder.selectQuery(PageOptions.class).where(cond).where(cond2).findOne();
        return options;
    }
    
    public static boolean isImage(String m2)
    {
        String file=m2.trim().toLowerCase();
        return file.endsWith(".jpg")||
                file.endsWith(".jpeg")||
                file.endsWith(".bmp")||
                file.endsWith(".gif")||
                file.endsWith(".png");
    }

    private static String doSpecial(Hashtable parts, String page)
    {
        Matcher m = SPECIAL_PATTERN.matcher(page);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String content = m.group(1);

            String replacement = content;
            
            if(content.startsWith("MANTIS-ROOT"))
            {
                replacement = "<a target='mantis' class='mantis'  href='mantis.do'>Home&nbsp;</a>";
            }
            else if(content.startsWith("MANTIS-ID:"))
            {
                String id=content.substring("MANTIS-ID:".length(),content.length());
                replacement = "<a target='mantis' class='mantis' href='mantis.do?id="+id+"'>#"+id+"&nbsp;</a>";
            }
            else if(content.startsWith("MANTIS-CAT:"))
            {
                String cat=content.substring("MANTIS-CAT:".length(),content.length());
                replacement = "<a target='mantis' class='mantis' href='mantis.do?cat="+cat+"'>"+cat+"&nbsp;</a>";
            }
            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
            parts.put("" + index, replacement);

            JOTViewParser.safeAppendReplacement(m, buf, tag);
        }
        m.appendTail(buf);
        page = buf.toString();
        return page;
    }

    // encode the amil addresses found in the text, so they can't be easily harvested by robots
    private static String encodeEmails(Hashtable parts, String page)
    {
        Matcher m = EMAIL_PATTERN.matcher(page);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String content = m.group(0);

            String replacement = JOTAntiSpam.encodeEmail(content,false);
            
            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
            parts.put("" + index, replacement);

            JOTViewParser.safeAppendReplacement(m, buf, tag);
        }
        m.appendTail(buf);
        page = buf.toString();
        return page;
    }


    /**
     * format a page link such as it only contains letters and numbers
     * other characters are replaced by undersores.
     * @param text
     * @return
     */
    private static String pageLink(String text)
    {
        return text.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_").replaceAll("_[_]+", "_");
    }

    /**
     * Fully retrieve a plain wiki page, parses it and returns the resulting HTML code.
     * @param plainPage
     * @return
     * @throws java.lang.Exception
     */
    public static String getHtmlPage(String plainPage) throws Exception
    {
        Hashtable parts = new Hashtable();
        String page = processText(parts, plainPage);
        //now we have a page with only "plain" content in it
        // so we can encode all the html left

        page = JOTHTMLUtilities.textToHtml(page, JOTHTMLUtilities.ENCODE_HTML_CHARS | JOTHTMLUtilities.ENCODE_LINE_BREAKS);
        //now restore the found tags with the new content
        page = restore(parts, page);
        return page;
    }

    /**
     * Returns a wiki page associated file.
     * @param nameSpace
     * @param pageName
     * @return
     * @throws java.lang.Exception
     */
    public static File getPageFile(String nameSpace, String pageName) throws Exception
    {
        String pageRoot = WikiPreferences.getInstance().getPagesFolder(nameSpace);
        File file = new File(pageRoot, pageName + ".txt");
        return file;
    }

    /**
     * returns the MD5 Hash of a plain page file.
     * The hash can be used to detect wether the page/file content changed or not..
     */
    // we will use this to see if the page as changed later.
    public static String getPageMD5(String nameSpace, String pageName)
    {
        String md5 = "";
        try
        {
            File f=getPageFile(nameSpace, pageName);
            if(f.exists())
            {
                md5 = JOTUtilities.getFileMd5(f);
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, PageReader.class, "Failed to compute MD5 for page file.", e);
        }
        return md5;
    }

    /**
     * Retuns a plain (non parsed) wiki page content.
     * @param req
     * @param nameSpace
     * @param pageName
     * @return
     * @throws java.lang.Exception
     */
    public static String getPlainPage(HttpServletRequest req, String nameSpace, String pageName) throws Exception
    {
        String pageRoot = WikiPreferences.getInstance().getPagesFolder(nameSpace);
        BufferedReader reader = null;
        String page = "";
        try
        {
            File file = getPageFile(nameSpace, pageName);
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "Loading page: [" + nameSpace + "]: " + pageName + " -> " + file.getAbsolutePath());
            if (file.exists())
            {
                String fileTimestamp = new Date(file.lastModified()).toString();
                req.setAttribute("fileTimestamp", fileTimestamp);
                reader = new BufferedReader(new FileReader(file));
                String s = null;
                while ((s = reader.readLine()) != null)
                {
                    page += s + "\n";
                }
                reader.close();
            } else
            {
                page = "This page does not exist yet.";
                req.setAttribute("fileTimestamp", "");
            }
        } catch (FileNotFoundException f)
        {
            JOTLogger.log(JOTLogger.INFO_LEVEL, PageReader.class, "Did not find: " + pageName);
            throw (f);
        } catch (Exception e)
        {
            if (reader != null)
            {
                reader.close();
            }
            throw (e);
        }
        return page;
    }

    /**
     * Parses a plain page and returns generated HTML.
     * @param parts - empty hashtable on first call (recursion variable) 
     * @param page
     * @return
     * @throws java.lang.Exception
     */
    public static String processText(Hashtable parts, String page) throws Exception
    {
        // replacing stuffs
        // we first do the one for which the content shouldn't be parsed

        Replacer codeReplacer = new Replacer("<code\\s*([^> ]*)\\s*(\\|\\s*([^>]*))?>", "</code>", "<div class='code'><div class='code_title'>" + (STRIP_VAR_HEAD + 3 + STRIP_VAR_TAIL) + "</div><pre>", "</pre></div>");
        codeReplacer.setOpenLength(Replacer.AUTOMATIC);
        codeReplacer.setParseContent(false);
        codeReplacer.setLineBreaks(false);
        codeReplacer.setEncoding(JOTHTMLUtilities.ENCODE_HTML_CHARS);
        page = strip(parts, page, codeReplacer);

        Replacer inlineCodeReplacer = new Replacer("''", "''", "<span class='code'>", "</span>");
        inlineCodeReplacer.setParseContent(false);
        codeReplacer.setEncoding(JOTHTMLUtilities.ENCODE_HTML_CHARS);
        page = strip(parts, page, inlineCodeReplacer);

        Replacer htmlReplacer = new Replacer("<html>", "</html>");
        htmlReplacer.setEncodeContent(false);
        htmlReplacer.setParseContent(false);
        htmlReplacer.setLineBreaks(false);
        page = strip(parts, page, htmlReplacer);

        //do the special ones that are more complex
        page = doLinks(parts, page);
        
        
        // do this after doLinks
        if(WikiPreferences.getInstance().getDefaultedBoolean(WikiPreferences.GLOBAL_ENCODE_EMAIL,Boolean.TRUE).booleanValue())
        {
            page=encodeEmails(parts,page);
        }
        page = doTables(parts, page);
        page = doImages(parts, page);

        page = doSpecial(parts, page);
        
        Replacer italicReplacer = new Replacer("//", "//", "<i>", "</i>");
        page = strip(parts, page, italicReplacer);
        Replacer delReplacer = new Replacer("<del>", "</del>", "<del>", "</del>");
        page = strip(parts, page, delReplacer);
        Replacer subReplacer = new Replacer("<sub>", "</sub>", "<sub>", "</sub>");
        page = strip(parts, page, subReplacer);
        Replacer boldReplacer = new Replacer("\\*\\*", "\\*\\*", "<b>", "</b>");
        boldReplacer.setOpenLength(2);
        boldReplacer.setCloseLength(2);
        page = strip(parts, page, boldReplacer);
        Replacer underlineReplacer = new Replacer("__", "__", "<u>", "</u>");
        page = strip(parts, page, underlineReplacer);
        Replacer hrReplacer = new Replacer("----+\n", "", "<hr/>", "");
        hrReplacer.setRemoveContent(true);
        page = strip(parts, page, hrReplacer);
        Replacer fixmeReplacer = new Replacer("^FIXME:", "\n\n", "<div class='box'><div class='fixme'>", "</div></div>\n");
        page = strip(parts, page, fixmeReplacer);
        Replacer warningReplacer = new Replacer("^WARNING:", "\n\n", "<div class='box'><div class='warning'>", "</div></div>\n");
        page = strip(parts, page, warningReplacer);
        Replacer tipReplacer = new Replacer("^TIP:", "\n\n", "<div class='box'><div class='tip'>", "</div></div>\n");
        page = strip(parts, page, tipReplacer);
        Replacer noteReplacer = new Replacer("^NOTE:", "\n\n", "<div class='box'><div class='note'>", "</div></div>\n");
        page = strip(parts, page, noteReplacer);
        Replacer deletemeReplacer = new Replacer("^DELETEME:", "\n\n", "<div class='box'><div class='deleteme'>", "</div></div>\n");
        page = strip(parts, page, deletemeReplacer);
        Replacer bulletReplacer = new Replacer("  \\*", "\n", "<ul><li>", "</li></ul>\n");
        page = strip(parts, page, bulletReplacer);
        Replacer listReplacer = new Replacer("  -", "\n", "<ul><li>", "</li></ul>\n");
        page = strip(parts, page, listReplacer);
        page = doHrs(parts, page);

        //smileys
        String context = "";
        Replacer smileReplacer = new Replacer(":-\\)", "", "<img src='" + context + "images/smileys/smile.gif' alt='smile'/>", "");
        page = strip(parts, page, smileReplacer);
        Replacer winkReplacer = new Replacer(";-\\)", "", "<img src='" + context + "images/smileys/wink.gif' alt='wink'/>", "");
        page = strip(parts, page, winkReplacer);
        Replacer lolReplacer = new Replacer(":-D", "", "<img src='" + context + "images/smileys/lol.gif' alt='LOL'/>", "");
        page = strip(parts, page, lolReplacer);
        Replacer tongueReplacer = new Replacer(":-P", "", "<img src='" + context + "images/smileys/tongue.gif' alt='tongueOut'/>", "");
        page = strip(parts, page, tongueReplacer);
        Replacer frownReplacer = new Replacer(":-\\(", "", "<img src='" + context + "images/smileys/frown.gif' alt='frown'/>", "");
        page = strip(parts, page, frownReplacer);

        Replacer breaksReplacer = new Replacer("\\\\\\\\$", "", "", "");
        page = strip(parts, page, breaksReplacer);

        return page;
    }

    private static String doHrs(Hashtable parts, String page)
    {
        StringBuffer buf = new StringBuffer();
        Matcher m = pHeader.matcher(page);
        while (m.find())
        {
            String prefix = m.group(1);
            String text = m.group(2);
            String postfix = m.group(3);
            if (prefix != null && postfix != null && prefix.length() == postfix.length())
            {
                int level = 7 - prefix.length();
                String replacement = "<h" + level + "><a name='" + pageLink(text) + "'></a>" + text + "</h" + level + ">";
                int index = parts.size();
                String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
                parts.put("" + index, replacement);
                JOTViewParser.safeAppendReplacement(m, buf, tag);
            }
        }
        m.appendTail(buf);
        page = buf.toString();
        return page;
    }

    private static String doImages(Hashtable parts, String page)
    {
        StringBuffer buf = new StringBuffer();
        Matcher m = IMAGE_PATTERN.matcher(page);
        while (m.find())
        {
            String m1 = m.group(1);
            String m2 = m.group(2);
            String m3 = m.group(3);
            String css = "class='imgLeft'";
            String replacement = "";
            if (isImage(m2))
            {
                // image
                if (m1.length() > 0 && m3.length() > 0)
                {
                    css = "class='imgCenter'";
                } else if (m1.length() > 0)
                {
                    css = "class='imgRight'";
                } 

                replacement = getImageCode(m2, css);
            } else
            {
                //other file attachment
                replacement="<img src='images/files.gif'/><a href='fetchImage.do?img="+ m2.trim() +"'>"+m2.trim()+"</a>";
            }

            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
            parts.put("" + index, replacement);
            JOTViewParser.safeAppendReplacement(m, buf, tag);
        }
        m.appendTail(buf);
        page = buf.toString();
        return page;
    }

    private static String getImageCode(String image, String css)
    {
        return "<div "+css+"><img src='fetchImage.do?img=" + image + "' alt='" + image + "'/></div>";
    }

    private static String doLinks(Hashtable parts, String page)
    {
        boolean encodeMailto=WikiPreferences.getInstance().getDefaultedBoolean(WikiPreferences.GLOBAL_ENCODE_MAILTO,Boolean.TRUE).booleanValue();

        // links
        Matcher m = LINK_PATTERN.matcher(page);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String link = m.group(1);
            String name = m.group(2);
            if (name == null || name.length() < 1)
            {
                name = link;
            }

            page = JOTHTMLUtilities.textToHtml(page, JOTHTMLUtilities.ENCODE_SYMBOLS | JOTHTMLUtilities.ENCODE_CURLEYS);

            String replacement = "";
            String image = null;
            boolean internal = true;
            boolean isMailTo=false;
            //JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "url: "+link);
            //JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "urli: "+link.indexOf("www."));
            if (link.indexOf("://") != -1 || link.startsWith("\\\\") || link.startsWith("www."))
            {
                if (link.startsWith("www."))
                {
                    link = "http://" + link;
                }
                if (link.startsWith("\\\\"))
                {
                    link = "file:///" + link;
                }
                internal = false;
                image = "images/link_icon.gif";
                if (link.startsWith("file://") || link.startsWith("\\\\"))
                {
                    image = "images/windows.gif";
                }
            } else if (link.startsWith("mailto:"))
            {
                internal = false;
                image = "images/mail_icon.gif";
                isMailTo=true;
            }
            if (image != null)
            {
                replacement += "<img src='" + image + "'/>";
            }
            if (!internal)
            {
                if (isMailTo)
                {
                    String l=encodeMailto?JOTAntiSpam.encodeEmail(link, true):link;
                    String n=encodeMailto?JOTAntiSpam.encodeEmail(link, false):name;
                    
                    replacement += "<a href='" + l + "'>" + n + "</a>";
                }
                else
                {
                    replacement += "<a href='" + link + "' target=\"OUT\">" + name + "</a>";
                }
            } else
            {
                // relative link if in same namespace
                link = link.trim().toLowerCase().replaceAll("[ &'./\\~!@^*()+={}\\[\\]<>$]", "_").replaceAll("_[_]+", "_");
                String nameSpace = "";
                if (link.indexOf(":") != -1)
                {
                    replacement += "<img src='images/link_icon.gif'/>";
                    nameSpace = link.substring(0, link.indexOf(":"));
                    link = link.substring(link.indexOf(":") + 1, link.length());
                    name = name.substring(name.indexOf(":") + 1, name.length());
                    // recreates a full link for new namespace
                    link = JOTUtilities.endWithForwardSlash(WikiPreferences.getInstance().getDefaultedString(nameSpace + "." + WikiPreferences.NS_WEBROOT, nameSpace)) + link;
                }
                replacement += "<a href='" + link + "'>" + name + "</a>";
            }
            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
            parts.put("" + index, replacement);

            JOTViewParser.safeAppendReplacement(m, buf, tag);
        }
        m.appendTail(buf);
        page = buf.toString();
        return page;
    }

    /**
     * While parsing the pieces of text that needed to be parsed, where replaced by temporary tags by the strip method
     * The restore method replaces those tags by the actual text once parsing as completed
     * @param parts
     * @param page
     * @return
     */
    private static String restore(Hashtable parts, String page)
    {
        Matcher m;
        while ((m = PARSER_TAG_PATTERN.matcher(page)).find())
        {
            String index = m.group(1);
            String content = (String) parts.get(index);
            //JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "New Content("+index+"):"+content);
            content = restore(parts, content);
            page = page.substring(0, m.start()) + content + page.substring(m.end(), page.length());
            parts.remove(index);
        }
        return page;
    }

    /**
     * Replaces parsed content by temporary tags, that will be replaced later by the restore method
     * @param parts
     * @param page
     * @param replacer
     * @return
     * @throws java.lang.Exception
     */
    public static String strip(Hashtable parts, String page, Replacer replacer) throws Exception
    {
        Pattern openPattern = Pattern.compile(replacer.getOpen(), PATTERN_FLAGS_MULTI);
        Pattern closePattern = Pattern.compile(replacer.getClose(), PATTERN_FLAGS_MULTI);
        Matcher m;
        boolean unclosed = false;
        while (!unclosed && (m = openPattern.matcher(page)).find())
        {
            if (replacer.getOpenLength() == Replacer.AUTOMATIC)
            {
                replacer.setOpenLength(m.group(0).length());
            }
            int start = m.start();
            // if end is empty, then we don't have to look for an end tag.
            int end = m.end();
            if (replacer.getClose().length() > 0)
            {
                end = JOTViewParser.findMatchingClosingTag(start + replacer.getOpenLength(), page, openPattern, closePattern, 1) - closePattern.pattern().length();
            }
            if (end > start)
            {
                String content = page.substring(m.end(), end);
                //JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "Content:"+content);
                if (replacer.isRemoveContent())
                {
                    content = "";
                }
                if (replacer.isParseContent())
                {
                    content = processText(parts, content);
                }
                if (replacer.isEncodeContent())
                {
                    content = JOTHTMLUtilities.textToHtml(content, replacer.getEncoding());
                }
                if (replacer.isLineBreaks())
                {
                    content = JOTHTMLUtilities.textToHtml(content, JOTHTMLUtilities.ENCODE_LINE_BREAKS);
                }
                int index = parts.size();
                String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;

                String head = replacer.getHead();
                String tail = replacer.getTail();
                for (int i = 0; i != 5; i++)
                {
                    String val = "";
                    if (m.groupCount() >= i)
                    {
                        if (m.group(i) != null)
                        {
                            val = m.group(i);
                        }
                    }
                    head = head.replaceAll(STRIP_VAR_HEAD + i + STRIP_VAR_TAIL, val);
                    tail = tail.replaceAll(STRIP_VAR_HEAD + i + STRIP_VAR_TAIL, val);
                }

                parts.put("" + index, head + content + tail);
                //JOTLogger.log(JOTLogger.DEBUG_LEVEL, PageReader.class, "parts added:"+index+" -> "+parts.get(""+index));
                page = page.substring(0, start) + tag + page.substring(end + replacer.getCloseLength(), page.length());
            } else
            {
                //throw(new Exception("Could not find closing tag: "+closePattern));
                unclosed = true;
            }
        }
        return page;
    }

    public static String doTables(Hashtable parts, String page) throws Exception
    {
        // tables with headers
        Matcher tableMatcher = tablePattern.matcher(page);
        StringBuffer buf = new StringBuffer();

        while (tableMatcher.find())
        {
            String newTable = "<table class='table'>\n";
            Matcher headerMatcher = headerPattern.matcher(tableMatcher.group(1));
            if (headerMatcher.find())
            {
                newTable += "<tr>\n";
                String[] headers = headerMatcher.group(1).split("\\^");
                for (int i = 1; i != headers.length; i++)
                {
                    newTable += "<th class='th'>" + processText(parts, headers[i]) + "</th>\n";
                }
                newTable += "</tr>\n";
            }
            newTable += doTableRows(parts, tableMatcher.group(1));
            newTable += "</table>\n";
            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL + "\n";
            parts.put("" + index, newTable);
            JOTViewParser.safeAppendReplacement(tableMatcher, buf, tag);
        }
        tableMatcher.appendTail(buf);
        page = buf.toString();

        //tables without headers
        buf = new StringBuffer();
        tableMatcher = tableNoHeadPattern.matcher(page);
        while (tableMatcher.find())
        {
            String newTable = "<table class='table'>\n";
            newTable += doTableRows(parts, tableMatcher.group(1));
            newTable += "</table>\n";
            int index = parts.size();
            String tag = PARSER_TAG_HEAD + index + PARSER_TAG_TAIL;
            parts.put("" + index, newTable);
            JOTViewParser.safeAppendReplacement(tableMatcher, buf, tag);
        }
        tableMatcher.appendTail(buf);
        page = buf.toString();
        return page;
    }

    public static String doTableRows(Hashtable parts, String tableContent) throws Exception
    {
        String newTable = "";
        Matcher rowMatcher = rowPattern.matcher(tableContent);
        while (rowMatcher.find())
        {
            newTable += "<tr>\n";
            String[] values = rowMatcher.group(1).split("\\|");
            for (int i = 1; i != values.length; i++)
            {
                String value = values[i];
                value = processText(parts, value);
                newTable += "<td class='td'>" + value + "</td>\n";
            }
            newTable += "</tr>\n";
        }
        return newTable;
    }

    /**
     * For testing purposes
     * @param args
     */
    public static void main(String[] args)
    {
        //System.out.println(encodeEmail("fred.flintstone@bedrock.com"),false);
        /*String[] a = {"<code>", "<code  >", "<code java>", "<code my site>", "<code java blah>", "<code | java my great site>", "<code java | my great site>"};
        for (int i = 0; i != a.length; i++)
        {
            String s = a[i];
            System.out.println("*** " + s);
            Pattern p = Pattern.compile("<code\\s*([^> ]*)\\s*(\\|\\s*([^>]*))?>");
            Matcher m = p.matcher(s);
            if (m.find())
            {
                System.out.println("found!");
                for (int j = 0; j != m.groupCount() + 1; j++)
                {
                    System.out.println("" + j + ":" + m.group(j));
                }
            }
        }*/
    }
}

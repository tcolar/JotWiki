/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jotwiki;

import java.util.Hashtable;
import java.util.regex.Matcher;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.utils.JOTPair;
import net.jot.web.view.JOTViewParser;
import net.jot.web.view.JOTViewParserData;
import net.jot.web.view.JOTViewParserInterface;
import net.jotwiki.db.PageVariable;
import net.jotwiki.forms.PageOptionsForm;

/**
 * Deal with custom parse tags (Ex: wiki:var)
 *
 * TODO:  cache those until changed in PageOptionsForm for speed
 * @author thibautc
 */
public class PostTemplateParser implements JOTViewParserInterface
{

	public String process(String template, JOTViewParserData view, String templateRoot) throws Exception
	{
		Matcher m = null;
		while ((m = PageOptionsForm.WIKI_VAR_PATTERN.matcher(template)).find())
		{
			String jotId = m.group(1);
			jotId = jotId.trim();
			JOTPair pair = JOTViewParser.findMatchingClosingTag(m.end(), template, PageOptionsForm.OPEN_VAR_PATTERN, PageOptionsForm.CLOSE_VAR_PATTERN);

	        Hashtable vars=view.getVariables();
			String pageName=(String)vars.get(Constants.PAGE_NAME);
			String ns=(String)vars.get(Constants.NAMESPACE);

			JOTSQLCondition cond = new JOTSQLCondition("page", JOTSQLCondition.IS_EQUAL, pageName);
			JOTSQLCondition cond2 = new JOTSQLCondition("nameSpace", JOTSQLCondition.IS_EQUAL, ns);
			JOTSQLCondition cond3 = new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, jotId);
			PageVariable var = (PageVariable) JOTQueryBuilder.selectQuery(PageVariable.class).where(cond).where(cond2).where(cond3).findOne();

			if (var == null)
			{
				// keeping tag content
				template = template.substring(0, m.start()) +template.substring(m.end(),pair.getX()) + template.substring(pair.getY());
			} else
			{
				// replacing tag by new value
				template = template.substring(0, m.start()) + var.getValue() + template.substring(pair.getY());
			}
		}//end while

		return template;
	}
}

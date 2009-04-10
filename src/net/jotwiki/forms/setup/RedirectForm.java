/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jotwiki.forms.setup;

import java.util.Hashtable;
import net.jot.persistance.JOTModel;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTCRUDForm;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jotwiki.db.WikiPermission;
import net.jotwiki.db.WikiRedirect;

/**
 *
 * @author thibautc
 */
public class RedirectForm extends JOTCRUDForm
{

	public void defineColumns(JOTFlowRequest request)
	{
		title="301 Redirects";
		action="submitRedirects.do";
		JOTFormTextField ns=new JOTFormTextField("NAMESPACE", "Namespace", 20, "");
		ns.setHelp("The namespace where the original page (ex: default)");
		JOTFormTextField path=new JOTFormTextField("PATH", "Path", 30, "");
		path.setHelp("The path to be redirected(within the ns): Ex: /my_page");
		JOTFormTextField url=new JOTFormTextField("NEW_URL", "NEW URL", 40, "http://");
		url.setHelp("The fully qualified URL to redirect to. Ex: http://www.google.com/");
		addColumn(ns);
		addColumn(path);
		addColumn(url);
	}

	public void updateModel(JOTFlowRequest request) throws Exception
	{
		modelClass=WikiRedirect.class;
		//dataEntries= JOTQueryBuilder.selectQuery(modelClass).find().getAllResults();
		dataEntries=null;
	}

	public boolean validatePermissions(JOTFlowRequest request)
	{
		return WikiPermission.hasPermission(request, WikiPermission.SETUP);
	}

	public Hashtable validateForm(JOTFlowRequest request) throws Exception
	{
		return new Hashtable();
	}

	protected boolean isNewEntryValid(JOTModel model)
	{
		WikiRedirect redir=(WikiRedirect)model;
		if(redir.getNameSpace()!=null && redir.getPath()!=null && redir.getNameSpace().length()>0 && redir.getPath().length()>0 && redir.getUrl()!=null && redir.getUrl().length()>4)
		{
			return true;
		}
		return false;
	}
}

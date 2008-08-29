/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.view;

/**
 * Extension of ShowPageView, simply adding a variable telling us we are in print mode
 * The template will use this variable to use the printing CSS rather that regular one.
 * @author tcolar
 */
public class PrintPageView extends ShowPageView
{
	public void prepareViewData() throws Exception 
	{
		super.prepareViewData();
		addVariable("print", Boolean.TRUE);
	}
}

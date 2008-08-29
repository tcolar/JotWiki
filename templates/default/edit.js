/*
Handles the textarea things.
Mostly the nightmerish handling of the textarea selection/insertion.
Mozilla nd IE have completely different ways of doig that, trying to use common code just results
in headaches.
Anyway i think i ended up with a simple solution in the end, compare to other crazy solutions i saw on the net.
*/

var selectionStart=0;
var selectionEnd=0;
var ie_range=null;

/* Generic functions */

// Finds the current selection / caret position
function updateSelection(textArea) 
{
	if( document.selection )
	{
		// Can't have start and end points working well in IE, so use the range thing instead.
		textArea.focus();
 		ie_range = document.selection.createRange(); 
	}
	else
	{
   		selectionStart=textArea.selectionStart;
    	selectionEnd=textArea.selectionEnd; 
	}	
	return;
}

// wrap the currently selection with given head and tail (if nothing selected, uses defaultText)
function wrapText(textArea, head, tail, defaultText)
{
	updateSelection(textArea);
	if(ie_range)
	{
		// internet explorer
		if(ie_range.text)
		{
			ie_range.text=head+ie_range.text+tail;
			// leaves us behind the new addition which is what we want
		}
		else
		{
			ie_range.text=head+defaultText+tail;		
			// select the defaultText for easy change
			ie_range.moveStart('character',-(tail.length+defaultText.length));
			ie_range.moveEnd('character',-tail.length);
		}
		ie_range.select();
	}
	else
	{
		// mozilla
		var text=textArea.value;
		if(selectionEnd > selectionStart)
		{
			var newText=head+text.substring(selectionStart, selectionEnd)+tail;
			textArea.value=text.substring(0,selectionStart)+newText+text.substring(selectionEnd,text.length);
			// go right after the change
			var pos=selectionEnd+head.length+tail.length;
			textArea.setSelectionRange(pos, pos);	
		}
		else
		{
			textArea.value=text.substring(0,selectionStart)+head+defaultText+tail+text.substring(selectionStart,text.length);
			// Select the defaultText so it can be replaced easily.	
			var pos=selectionEnd+head.length;
			textArea.setSelectionRange(pos, pos+defaultText.length);		
		}
	}
	textArea.focus();
	return;
}

function showExpiredLock()
{
    alert('Your edition lock as expired !\nSomebody else could edit the page and you might loose your changes.\nYou should copy/save your changes to a text file to be safe.');
}

function showAlmostExpiredLock()
{
   alert('Your edition lock will expire in less than 5 minutes.\n You should save the page as soon as possible to make sure your changes won\'t be lost.'); 
}

/* functions called by HTML */

function updateText(head, tail, defaultText)
{
	wrapText(document.getElementById('textEditor'),head,tail,defaultText);
	document.getElementById('boxes').style.display='none';
	document.getElementById('smileys').style.display='none';
	document.getElementById('mantis').style.display='none';
	return;
}

function openFileManager()
{
	window.open("fm.do","_blank","width=780,height=580,top=10,left=10,status=yes,scrollbars=yes,toolbar=no,menubar=no,location=no");
	return;
}

// will make the timeout warning/messages appear when edition lock gets low / expires
function setTimers(timeout)
{
    // warning 5 mn before the end.
    setTimeout('showAlmostExpiredLock()',(timeout-5)*60000);
    setTimeout('showExpiredLock()',timeout*60000);    
}

// Handles form cancel button (with confirmation popup)
function confirmCancel(formName,pageName,ns)
{
    if (confirm("Are you sure you want to cancel (and loose your changes) ?")) 
    {   
       var form=document.getElementById(formName);
       form.action="cancelEdit.do?pageName="+pageName+"&nameSpace="+ns;
       form.submit();
    }
}
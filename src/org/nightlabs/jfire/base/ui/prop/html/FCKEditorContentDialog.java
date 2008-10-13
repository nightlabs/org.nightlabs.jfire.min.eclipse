package org.nightlabs.jfire.base.ui.prop.html;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.htmlcontent.IFCKEditorContent;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class FCKEditorContentDialog extends TitleAreaDialog
{
	private FCKEditorContentViewer contentViewer;

	public FCKEditorContentDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent)
	{
		contentViewer = new FCKEditorContentViewer(parent, SWT.NONE);
		contentViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return contentViewer;
	}

	public void setContent(IFCKEditorContent content)
	{
		contentViewer.setContent(content);
	}
}

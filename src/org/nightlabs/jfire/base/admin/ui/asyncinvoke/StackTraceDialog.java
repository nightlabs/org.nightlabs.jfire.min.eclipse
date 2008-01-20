package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.dialog.CenteredDialog;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;

public class StackTraceDialog
		extends CenteredDialog
{
	private AsyncInvokeProblem asyncInvokeProblem;

	public StackTraceDialog(Shell parentShell, AsyncInvokeProblem asyncInvokeProblem)
	{
		super(parentShell);
		this.asyncInvokeProblem = asyncInvokeProblem;
	}

	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Point getInitialSize()
	{
		return new Point(640, 480);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton)
	{
		if (id == CANCEL)
			return null;

		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Text stackTrace = new Text(area, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		stackTrace.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackTrace.setText(asyncInvokeProblem.getLastError().getErrorStackTrace());
		return area;
	}
}

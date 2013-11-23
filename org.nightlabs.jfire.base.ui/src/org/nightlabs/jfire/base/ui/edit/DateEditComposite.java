package org.nightlabs.jfire.base.ui.edit;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.DateTimeControl;

public class DateEditComposite
extends AbstractInlineEditComposite {

	private DateTimeControl dateTimeControl;
	
	public DateEditComposite(Composite parent, long editFlags) {
		super(parent, SWT.NONE);
		
		if (dateTimeControl != null)
			dateTimeControl.dispose();

		dateTimeControl = new DateTimeControl(this, SWT.NONE, editFlags, (Date) null);
		dateTimeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dateTimeControl.addModifyListener(getSwtModifyListener());
		dateTimeControl.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyModificationListeners();
//				getEditor().setChanged(true);
			}
		});
//		dateTimeControl.setDate(getEditor().getDataField().getDate());
	}
	
	public void setInput(Date date) {
		dateTimeControl.setDate(date);
	}

//	@Override
//	public void _refresh() {
//		DateStructField dateStructField = (DateStructField) getEditor().getStructField();
//		if (dateTimeControl != null)
//			dateTimeControl.dispose();
//
//		dateTimeControl = new DateTimeControl(this, SWT.NONE, dateStructField.getDateTimeEditFlags(), (Date) null);
//		dateTimeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		dateTimeControl.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				getEditor().setChanged(true);
//			}
//		});
//		dateTimeControl.addSelectionListener(new SelectionAdapter(){
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				getEditor().setChanged(true);
//			}
//		});
//		dateTimeControl.setDate(getEditor().getDataField().getDate());
//	}
	
	public Date getDate() {
		return dateTimeControl.getDate();
	}
	
	@Override
	public void dispose() {
		dateTimeControl.removeModifyListener(getSwtModifyListener());
		super.dispose();
	}
}
package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.util.RCPUtil;

public class TextEditComposite
extends AbstractInlineEditComposite
{
	private Text fieldText;
	private int lineCount;
	
	public TextEditComposite(Composite parent, int style, int lineCount) {
		super(parent, style);
		
		this.lineCount = lineCount;
		
		fieldText = new Text(this, createTextStyle() | SWT.NONE);
		fieldText.setEnabled(true);
		fieldText.setLayoutData(createTextLayoutData());
		
		fieldText.addModifyListener(getSwtModifyListener());
	}
	
	protected int createTextStyle() {
		if (isMultiLine())  {
			return getTextBorderStyle() | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
		} else {
			 return getTextBorderStyle();
		}

	}

	protected Object createTextLayoutData() {
		if (isMultiLine()) {
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.minimumHeight = RCPUtil.getFontHeight(this) * getLineCount();
			return gd;
		} else {
			GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			return textData;
		}
	}
	
	private boolean isMultiLine() {
		return lineCount > 1;
	}

	private int getLineCount() {
		return lineCount;
	}

	protected int getTextBorderStyle() {
		return getBorderStyle();
	}
	
	protected Text getFieldText() {
		return fieldText;
	}

	public String getContent() {
		return fieldText.getText();
	}

	public void setContent(String content) {
		if (content == null)
			content = "";
		fieldText.setText(content);
	}
	
	@Override
	public void setEnabledState(boolean enabled, String tooltip) {
		fieldText.setEditable(enabled);

		if (!enabled)
			fieldText.setToolTipText(tooltip);
		else
			fieldText.setToolTipText(null);
	}
	
	@Override
	public void dispose() {
		fieldText.removeModifyListener(getSwtModifyListener());
		super.dispose();
	}
}
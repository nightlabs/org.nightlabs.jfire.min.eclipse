/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ScriptValidatorDialog extends ResizableTitleAreaDialog 
{
//	private JSEditorComposite jsEditorComposite;
	private Text text;
	private String script;
	
	/**
	 * @param shell
	 * @param resourceBundle
	 */
	public ScriptValidatorDialog(Shell shell, ResourceBundle resourceBundle) {
		super(shell, resourceBundle);
	}

	public String getScript() 
	{
		return script;
	}
	
//	@Override
//	protected Control createDialogArea(Composite parent) 
//	{
//		setTitle("Validator Script");
//		getShell().setText("Validator Script");
//		setMessage("Edit the validator script");
//		jsEditorComposite = new JSEditorComposite(parent);
//		if (script != null) {
//			jsEditorComposite.setDocumentText(script);
//		}
//		jsEditorComposite.getDocument().addDocumentListener(new IDocumentListener(){
//			@Override
//			public void documentChanged(DocumentEvent event) {
//				script = jsEditorComposite.getDocumentText();
//			}
//			@Override
//			public void documentAboutToBeChanged(DocumentEvent event) {
//				
//			}
//		});
//		return jsEditorComposite;
//	}
//
//	public void setScript(String script) {
//		this.script = script;
//		if (jsEditorComposite != null) {
//			jsEditorComposite.setDocumentText(script);
//		}
//	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{
		setTitle("Validator Script");
		getShell().setText("Validator Script");
		setMessage("Edit the validator script");
		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (script != null) {
			text.setText(script);
		}
		text.addModifyListener(new ModifyListener(){
		
			@Override
			public void modifyText(ModifyEvent e) {
				script = text.getText();
			}
		});
		return text;
	}

	public void setScript(String script) {
		this.script = script;
		if (text != null && !text.isDisposed()) {
			text.setText(script);
		}
	}
	
}

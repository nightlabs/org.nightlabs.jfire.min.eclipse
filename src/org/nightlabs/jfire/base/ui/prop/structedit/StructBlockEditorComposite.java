package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.action.SelectionAction;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ScriptDataBlockValidator;

public class StructBlockEditorComposite extends XComposite
{
	class AddValidatorAction extends Action 
	{
		public AddValidatorAction() {
			super();
			setText("Add Validator");
			setToolTipText("Add an validator to the data block");
			setId(AddValidatorAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
		}
		
		@Override
		public void run() {
			ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null);
			int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				String script = dialog.getScript();
				ScriptDataBlockValidator validator = new ScriptDataBlockValidator(
						ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, script);
				block.addDataBlockValidator(validator);
				validatorTable.setInput(block.getDataBlockValidators());
				markDirty();
			}
		}
	}
	
	class DeleteValidatorAction extends SelectionAction 
	{
		public DeleteValidatorAction() {
			super();
			setText("Delete Validator");
			setToolTipText("Removes the selected validator from the data block");
			setId(DeleteValidatorAction.class.getName());
			setImageDescriptor(SharedImages.DELETE_16x16);
		}
		
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() {
			return !getSelection().isEmpty();
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}

		@Override
		public void run() {
			IDataBlockValidator validator = (IDataBlockValidator) getSelectedObjects().get(0);
			block.removeDataBlockValidator(validator);
			validatorTable.refresh();
			markDirty();
		}
	}
	
	class EditValidatorAction extends SelectionAction 
	{
		public EditValidatorAction() {
			super();
			setText("Edit Validator");
			setToolTipText("Edits the selected validator");
			setId(EditValidatorAction.class.getName());
			setImageDescriptor(SharedImages.EDIT_16x16);
		}
		
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() {
			return !getSelection().isEmpty();
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}

		@Override
		public void run() {
			IDataBlockValidator validator = (IDataBlockValidator) getSelectedObjects().get(0);
			if (validator instanceof IScriptValidator) {
				ScriptDataBlockValidator scriptValidator = (ScriptDataBlockValidator) validator;
				ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null);
				dialog.setScript(scriptValidator.getScript());
				int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					scriptValidator.setScript(dialog.getScript());
					validatorTable.refresh();
					markDirty();
				}				
			}
		}
	}
	
	private I18nTextEditor blockNameEditor;
	private Button uniqueButton;
	private StructBlock block;
	private DataBlockValidatorTable validatorTable;
	private ListenerList listeners;
	
	public StructBlockEditorComposite(Composite parent, int style, LanguageChooser languageChooser) {
		super(parent, style, LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		this.setVisible(false);

		blockNameEditor = new I18nTextEditor(this, languageChooser, Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.blockNameEditor.caption")); //$NON-NLS-1$
		
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		uniqueButton = new Button(this, SWT.CHECK);
		uniqueButton.setLayoutData((new GridData()));
		uniqueButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.unique.text")); //$NON-NLS-1$
		
		ToolBarSectionPart sectionPart = new ToolBarSectionPart(new FormToolkit(getDisplay()), this, Section.TITLE_BAR, "Validators");
		validatorTable = new DataBlockValidatorTable(sectionPart.getSection(), SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		validatorTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		sectionPart.getSection().setClient(validatorTable);
		
		listeners = new ListenerList();
		
		final EditValidatorAction editAction = new EditValidatorAction();
		sectionPart.registerAction(new AddValidatorAction(), true);
		sectionPart.registerAction(new DeleteValidatorAction(), true);
		sectionPart.registerAction(editAction, true);
		sectionPart.setSelectionProvider(validatorTable);
		sectionPart.updateToolBarManager();
		
		validatorTable.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
		
		uniqueButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				markDirty();
			}			
		});
		
		blockNameEditor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				markDirty();
			}
		});		
	}

	public void setStructBlock(StructBlock psb) {
		block = psb;
		blockNameEditor.setI18nText(psb.getName(), EditMode.DIRECT);
		uniqueButton.setSelection(block.isUnique());
		validatorTable.setInput(block.getDataBlockValidators());
		this.setVisible(true);
	}

	public I18nTextEditor getBlockNameEditor() {
		return blockNameEditor;
	}
		
	protected void markDirty() {
		notifyDataChangeListeners();
	}
	
	public void addDataChangeListener(IDataChangeListener listener) {
		listeners.add(listener);
	}

	public void removeDataChangeListener(IDataChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void notifyDataChangeListeners() {
		for (Object listener : listeners.getListeners()) {
			((IDataChangeListener)listener).dataChanged();
		}
	}
}

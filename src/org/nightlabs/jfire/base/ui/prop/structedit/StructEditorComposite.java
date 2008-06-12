package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.LanguageChooserCombo;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.language.LanguageChooserCombo.Mode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;

public class StructEditorComposite extends XComposite {
	private StructTree structTree;
	private StructEditor structEditor;

	private I18nTextEditor structNameEditor;
	private Composite partEditorComposite;
	private LanguageChooserCombo languageChooser;
	
//	private static class StructLocalIDLabelProvider extends org.eclipse.jface.viewers.LabelProvider {
//		@Override
//		public String getText(Object element) {
//			if (element instanceof StructID) {
//				StructID structID = (StructID) element;
//				return structID.organisationID + "#" + structID.linkClass; //$NON-NLS-1$
//			}
//			if (element instanceof StructLocalID) {
//				StructLocalID structLocalID = (StructLocalID) element;
//				return structLocalID.organisationID + '#' + structLocalID.linkClass;
//			}
//			return super.getText(element);
//		}
//	}
//
//	private class StructIDComboSelectionListener implements SelectionListener {
//		public void widgetDefaultSelected(SelectionEvent e) {
//		}
//
//		public void widgetSelected(SelectionEvent e) {
//			if (structEditor.isChanged()) {
//				MessageBox mb = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
//				mb.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditorComposite.messageBoxStructModifiedSaveConfirmation.text")); //$NON-NLS-1$
//				mb.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditorComposite.messageBoxStructModifiedSaveConfirmation.message")); //$NON-NLS-1$
//				switch (mb.open())
//				{
//				case SWT.YES:
//					structEditor.storeStructure();
//					break;
//				case SWT.NO:
//					structEditor.setChanged(false);
//					break;
//				case SWT.CANCEL:
//					return;
//				}
//			}
//			structEditor.setCurrentStructLocalID(structIDComposite.getSelectedElement());
//		}
//	}

	public StructEditorComposite(
			Composite parent, int style,
			final StructEditor structEditor, StructTree structTree
		) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));
		this.structEditor = structEditor;

		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;

//		if (createStructIDCombo) {
//			structIDComposite = new XComboComposite<StructLocalID>(this,
//					AbstractListComposite.getDefaultWidgetStyle(this), (String) null, new StructLocalIDLabelProvider());
//			structIDComposite.setLayoutData(gd);
//			structIDComposite.addSelectionListener(new StructIDComboSelectionListener());
//
//			gd = new GridData(GridData.FILL_HORIZONTAL);
//			gd.horizontalSpan = 2;
//			new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);
//		}
		XComposite topLine = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		topLine.getGridLayout().numColumns = 2;
		topLine.getGridData().horizontalSpan = 2;
		XComposite nameWrapper = new XComposite(topLine, SWT.NONE, LayoutMode.TOTAL_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		gd = new GridData();
//		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;

		languageChooser = new LanguageChooserCombo(topLine, Mode.iconAndText);
		languageChooser.setLayoutData(gd);

		structNameEditor = new I18nTextEditor(nameWrapper, languageChooser);
		structNameEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				structEditor.setChanged(true);
			}
		});


		this.structTree = structTree;
		structTree.createComposite(this, style, languageChooser);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 200;
		structTree.getComposite().setLayoutData(gd);

//		if (createStructIDCombo) {
//			try {
//				structIDComposite.setInput(new LinkedList<StructLocalID>(StructEditorUtil.getAvailableStructLocalIDs()));
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new RuntimeException(e);
//			}
//		}
		partEditorComposite = new XComposite(this, SWT.NONE);
	}

	public void setLoadingText() {
		structTree.setInput(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditorComposite.structTree.input_loadingStructure")); //$NON-NLS-1$
	}

	public void setStruct(IStruct struct) {
		structTree.setInput(struct);
		structTree.triggerSelectionChangeEvent();
		structNameEditor.setI18nText(struct.getName(), EditMode.DIRECT);
	}


	public void setPartEditor(StructPartEditor<?> structPartEditor) {
		if (partEditorComposite != null) {
			partEditorComposite.dispose();
		}

		this.partEditorComposite = structPartEditor.createComposite(this, this.getStyle(), structEditor, languageChooser);
		((GridData)this.partEditorComposite.getLayoutData()).verticalAlignment = SWT.TOP;
		this.partEditorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.layout(true, true);
	}

	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}
}

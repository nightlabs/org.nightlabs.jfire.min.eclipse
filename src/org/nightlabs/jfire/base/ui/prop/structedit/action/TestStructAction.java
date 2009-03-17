package org.nightlabs.jfire.base.ui.prop.structedit.action;

import org.nightlabs.base.ui.action.SelectionAction;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.base.idgenerator.IDGeneratorClient;
import org.nightlabs.jfire.base.ui.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.ui.prop.structedit.TestPropertySetWizard;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class TestStructAction extends SelectionAction 
{
	private StructEditor structEditor;
	
	public TestStructAction(StructEditor structEditor) {
		super();
		this.structEditor = structEditor;
		setText("Test Structure");
		setToolTipText("Test the given structure");
		setImageDescriptor(SharedImages.SEARCH_16x16);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	@Override
	public boolean calculateEnabled() {
		return structEditor.getStruct() instanceof StructLocal;
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
		IStruct struct = structEditor.getStruct();
		StructLocal structLocal = (StructLocal) struct;
		PropertySet propertySet = new PropertySet(IDGeneratorClient.getOrganisationID(),
				IDGeneratorClient.nextID(PropertySet.class), structLocal);
		propertySet.setDisplayName("Test PropertySet");
		propertySet.inflate(struct);
		TestPropertySetWizard wizard = new TestPropertySetWizard(propertySet, null);
		wizard.setWindowTitle(propertySet.getDisplayName());
		DynamicPathWizardDialog dlg = new DynamicPathWizardDialog(wizard);
		dlg.open();
	}
}

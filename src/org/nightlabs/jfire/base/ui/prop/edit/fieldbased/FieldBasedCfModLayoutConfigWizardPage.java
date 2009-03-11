/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.fieldbased;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;

/**
 * A Wizard page that creates a {@link FieldBasedEditorCfModLayoutConfig} PropertySet editor.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class FieldBasedCfModLayoutConfigWizardPage extends WizardHopPage {

	private FieldBasedEditorCfModLayoutConfig editor;
	
	/**
	 * Constructs a new {@link FieldBasedCfModLayoutConfigWizardPage}.
	 */
	public FieldBasedCfModLayoutConfigWizardPage() {
		super(FieldBasedCfModLayoutConfigWizardPage.class.getName());
		init();
	}

	/**
	 * Constructs a new {@link FieldBasedCfModLayoutConfigWizardPage} with the given title.
	 * @param title The title of the new page.
	 */
	public FieldBasedCfModLayoutConfigWizardPage(String pageName, String title) {
		super(FieldBasedCfModLayoutConfigWizardPage.class.getName());
		setTitle(title);
		init();
	}

	/**
	 * Constructs a new {@link FieldBasedCfModLayoutConfigWizardPage} with the given title and title image.
	 * @param title The title of the new page.
	 * @param titleImage The title image of the new page.
	 */
	public FieldBasedCfModLayoutConfigWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(FieldBasedCfModLayoutConfigWizardPage.class.getName());
		setTitle(title);
		setImageDescriptor(titleImage);
		init();
	}
	
	/**
	 * Used internally, creates the editor. 
	 */
	private void init() {
		editor = new FieldBasedEditorCfModLayoutConfig();
	}
	
	/**
	 * Set the {@link PropertySetFieldBasedEditLayoutConfigModule} for the editor of this page,
	 * where the editor can read its ui layout and the fields to display from.
	 * Note, that this has to be set before {@link #createPageContents(Composite)} is invoked.
	 * 
	 * @param configModule The config module to set.
	 */
	public void setLayoutConfigModule(PropertySetFieldBasedEditLayoutConfigModule configModule) {
		editor.setConfigModule(configModule);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		Control c = editor.createControl(parent, true);
		editor.addDataFieldEditorChangedListener(new DataFieldEditorChangedListener() {
			@Override
			public void dataFieldEditorChanged(DataFieldEditorChangedEvent dataFieldEditorChangedEvent) {
				editor.updatePropertySet();
			}
		});
		setControl(c);
		return c;
	}
	
	/**
	 * The {@link PropertySetEditor} created for this page.
	 * This is accessible after the constructor.
	 * 
	 * @return The {@link PropertySetEditor} created for this page. 
	 */
	public FieldBasedEditorCfModLayoutConfig getEditor() {
		return editor;
	}
	
	@Override
	public void onShow() {
		editor.refreshControl();
	}
}

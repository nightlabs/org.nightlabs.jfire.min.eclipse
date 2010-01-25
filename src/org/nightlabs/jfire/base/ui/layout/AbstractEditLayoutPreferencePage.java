package org.nightlabs.jfire.base.ui.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.base.ui.config.AbstractUserConfigModulePreferencePage;

public abstract class AbstractEditLayoutPreferencePage
extends AbstractUserConfigModulePreferencePage {
	
//	private AbstractEditLayoutUseCase useCase;
	private GridLayoutConfigComposite configComposite;
	private IGridLayoutConfig gridLayoutConfig;
	
	protected AbstractEditLayoutPreferencePage() {
//		this.useCase = useCase;
	}

	@Override
	protected void createPreferencePage(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.None, LayoutMode.TIGHT_WRAPPER);
		String description = getUseCaseDescription();
		if (description != null) {
			Label desc = new Label(wrapper, SWT.WRAP);
			desc.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
			desc.setText(description);
			Label sep = new Label(wrapper, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
		}
		configComposite = new GridLayoutConfigComposite(wrapper, SWT.NONE);
		configComposite.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setConfigChanged(true);
			}
		});
		
		createFooterComposite(wrapper, configComposite, this);
	}
	

	@Override
	public void updateConfigModule() {
		getConfigModuleController().getConfigModule();
		configComposite.updateGridLayoutConfig();
	}
	
	@Override
	protected void updatePreferencePage() {
//		configComposite.setGridLayoutConfig(
//				new GridLayoutConfig((PropertySetFieldBasedEditLayoutConfigModule2) getConfigModuleController().getConfigModule()));
		gridLayoutConfig = createConfigModuleGridLayoutConfig();
		configComposite.setGridLayoutConfig(gridLayoutConfig);
	}
	
	protected abstract IGridLayoutConfig createConfigModuleGridLayoutConfig();
	
	protected void createFooterComposite(Composite wrapper, GridLayoutConfigComposite configComposite, AbstractEditLayoutPreferencePage abstractEditLayoutPreferencePage) {}
	
	public IGridLayoutConfig getCurrentGridLayoutConfig() {
		return gridLayoutConfig;
	}
	
//	public AbstractEditLayoutUseCase getUseCase() {
//		return useCase;
//	}
//
//	public EditLayoutUseCaseDescription getUseCaseDescription() {
//		return getUseCase().getDescription();
//	}
//
//	public EditLayoutUseCaseName getUseCaseName() {
//		return getUseCase().getName();
//	}
	
	public abstract String getUseCaseDescription();
}

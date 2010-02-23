package org.nightlabs.jfire.base.ui.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.base.ui.config.AbstractUserConfigModulePreferencePage;

public abstract class AbstractEditLayoutPreferencePage
extends AbstractUserConfigModulePreferencePage {
	
//	private AbstractEditLayoutUseCase useCase;
	private GridLayoutConfigComposite configComposite;
	private IGridLayoutConfig gridLayoutConfig;
	private boolean useTabFolder;
	private TabFolder tabFolder;
	
	protected AbstractEditLayoutPreferencePage() {
//		this.useCase = useCase;
	}
	
	protected AbstractEditLayoutPreferencePage(boolean useTabFolder) {
		this.useTabFolder = useTabFolder;
	}

	@Override
	protected void createPreferencePage(Composite parent) {
		if (useTabFolder) {
			tabFolder = new TabFolder(parent, SWT.BORDER);
			tabFolder.setLayout(XComposite.getLayout(LayoutMode.TIGHT_WRAPPER));
			XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, tabFolder);
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText("Search configuration");
			Composite content = createContent(tabFolder);
			tabItem.setControl(content);
		} else {
			createContent(parent);
		}
	}
	
	private Composite createContent(Composite parent) {
		Composite wrapper = new XComposite(parent, SWT.None, LayoutMode.ORDINARY_WRAPPER);
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
		
		return wrapper;
	}

	@Override
	public void updateConfigModule() {
		getConfigModuleController().getConfigModule();
		configComposite.updateGridLayoutConfig();
	}
	
	@Override
	protected void updatePreferencePage() {
//		configComposite.setGridLayoutConfig(
//				new GridLayoutConfig((PropertySetEditLayoutConfigModule2) getConfigModuleController().getConfigModule()));
		gridLayoutConfig = createConfigModuleGridLayoutConfig();
		configComposite.setGridLayoutConfig(gridLayoutConfig);
	}
	
	protected abstract IGridLayoutConfig createConfigModuleGridLayoutConfig();
	
	protected void createFooterComposite(Composite wrapper, GridLayoutConfigComposite configComposite, AbstractEditLayoutPreferencePage abstractEditLayoutPreferencePage) {}
	
	protected TabFolder getTabFolder() {
		if (useTabFolder) {
			return tabFolder;
		} else {
			throw new IllegalStateException("This method may only be called if the constructor was called with useTabFolder == true.");
		}
	}
	
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

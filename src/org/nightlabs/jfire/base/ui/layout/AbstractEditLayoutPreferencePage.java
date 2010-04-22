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

/**
 * Base-class for creating a preference-page that configures the {@link IGridLayoutConfig} for a
 * certain use-case. The preference-page will operate on a custom implementation of
 * {@link IGridLayoutConfig}, which is required to be provided by subclasses in
 * {@link #createConfigModuleGridLayoutConfig()}. This layout-config will be edited using a
 * {@link GridLayoutConfigComposite}
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractEditLayoutPreferencePage
extends AbstractUserConfigModulePreferencePage {
	
	/** {@link GridLayoutConfigComposite} created to edit {@link #gridLayoutConfig} */
	private GridLayoutConfigComposite configComposite;
	/** {@link IGridLayoutConfig} created in {@link #createConfigModuleGridLayoutConfig()} */
	private IGridLayoutConfig gridLayoutConfig;
	
	/**
	 * Whether or not the ui configuring the IGridLayoutConfig should be created as a TabItem in a
	 * Tab, set in the constructor, default is <code>false</code>.
	 */
	private final boolean useTabFolder;
	/** TabFolder created when useTabFolder == true */
	private TabFolder tabFolder;
	
	/**
	 * Create a new {@link AbstractEditLayoutComposite}.
	 */
	protected AbstractEditLayoutPreferencePage() {
		this(false);
	}

	/**
	 * Create a new {@link AbstractEditLayoutPreferencePage} and define whether the ui configuring
	 * the IGridLayoutConfig should be created as TabItem inside a TabFolder.
	 * 
	 * @param useTabFolder Whether the primary ui of this page (that configures the
	 *            IGridLayoutConfig) should be created as TabItem inside a TabFolder.
	 */
	protected AbstractEditLayoutPreferencePage(boolean useTabFolder) {
		this.useTabFolder = useTabFolder;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates the ui of this page. This method checks if a TabFolder should be created and places
	 * the ui inside a TabItem if so.
	 * </p>
	 */
	@Override
	protected void createPreferencePage(Composite parent) {
		if (useTabFolder) {
			tabFolder = new TabFolder(parent, SWT.BORDER);
			tabFolder.setLayout(XComposite.getLayout(LayoutMode.TIGHT_WRAPPER));
			XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, tabFolder);
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(getTabText());
			Composite content = createContent(tabFolder);
			tabItem.setControl(content);
		} else {
			createContent(parent);
		}
	}

	/**
	 * Creates the actual ui that allows the user to edit the {@link IGridLayoutConfig}. This is
	 * called from {@link #createPreferencePage(Composite)}.
	 * 
	 * @param parent The parent to create the ui for.
	 * @return The created ui.
	 */
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
		
		createFooterComposite(wrapper, configComposite);
		
		return wrapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateConfigModule() {
		getConfigModuleController().getConfigModule();
		configComposite.updateGridLayoutConfig();
	}
	
	@Override
	protected void updatePreferencePage() {
		gridLayoutConfig = createConfigModuleGridLayoutConfig();
		configComposite.setGridLayoutConfig(gridLayoutConfig);
	}

	/**
	 * Create the {@link IGridLayoutConfig} that should be edited by this preference page. Note,
	 * that this method might not be called only once, it is rather called every time the ui needs
	 * to be refreshed with new values from the config-module.
	 * 
	 * @return The {@link IGridLayoutConfig} that should be edited by this preference page.
	 */
	protected abstract IGridLayoutConfig createConfigModuleGridLayoutConfig();

	/**
	 * Create a composite displayed below the ui that configures the IGridLayoutConfig. This
	 * composite is optional and the base implementation will not create it and therefore return
	 * <code>null</code>. Subclasses may override this method and create a custom footer composite
	 * 
	 * @param wrapper The parent to create the footer composite for.
	 * @param configComposite The {@link GridLayoutConfigComposite} of this ui (displayed) above the footer composite.
	 * @return The created footer composite, or <code>null</code> to indicate that no footer-composite should be used.
	 */
	protected Composite createFooterComposite(Composite wrapper, GridLayoutConfigComposite configComposite) {
		return null;
	}

	/**
	 * @return The TabFolder created for this page, if {@link #useTabFolder} is <code>true</code>.
	 *         If {@link #useTabFolder} is <code>false</code> this method will throw an
	 *         IllegalStateException.
	 */
	protected TabFolder getTabFolder() {
		if (useTabFolder) {
			return tabFolder;
		} else {
			throw new IllegalStateException("This method may only be called if the constructor was called with useTabFolder == true.");
		}
	}

	/**
	 * @return The text to use for the TabItem.
	 */
	private String getTabText() {
		if (!useTabFolder) {
			// Who is asking this, when no Tab-Folder is created? Reward him with a simple delegation, no check.
			return getLayoutConfigTabText();
		}
		String layoutConfigTabText = getLayoutConfigTabText();
		if (layoutConfigTabText == null) {
			throw new IllegalStateException(
					"This " + getClass().getSimpleName() + 
					" was created with useTabFolder == true, but the method getLayoutConfigTabText() was not implemented properly for this case.");
		}
		return layoutConfigTabText;
	}

	/**
	 * If an {@link AbstractEditLayoutPreferencePage} was created with {@link #useTabFolder} ==
	 * <code>true</code>, this method must have been overridden and return a non-null value. This
	 * value will be used as text for the created TabItem.
	 * 
	 * @return The text to used for the TabItem created for this page. 
	 */
	protected String getLayoutConfigTabText() {
		return null;
	}
	
	/**
	 * @return The currently edited {@link IGridLayoutConfig}.
	 */
	public IGridLayoutConfig getCurrentGridLayoutConfig() {
		return gridLayoutConfig;
	}

	/**
	 * Return a description for the use-case the {@link IGridLayoutConfig} is currently edited for.
	 * This will be displayed above the ui to edit the IGridLayoutConfig.
	 * 
	 * @return A description for the use-case the {@link IGridLayoutConfig} is currently edited for.
	 */
	public abstract String getUseCaseDescription();
}

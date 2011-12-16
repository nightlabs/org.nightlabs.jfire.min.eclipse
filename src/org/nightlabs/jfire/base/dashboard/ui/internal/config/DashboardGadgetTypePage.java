/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.base.ui.wizard.WizardHop;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.dashboard.ui.DashboardGadgetRegistry;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;

/**
 * @author abieber
 *
 */
public class DashboardGadgetTypePage extends WizardHopPage {

	private static class PageEntry {
		IDashboardGadgetConfigPage<Object> page;
		DashboardGadgetLayoutEntry<Object> layoutEntry;
	}
	
	private DashboardGadgetFactoryTable table;
	private Text typeDescription;
	
	private Map<String, PageEntry> factoryConfigPages = new HashMap<String, PageEntry>();
	
	private DashboardLayoutConfigModule<?> configModule;
	
	private ISelectionChangedListener typeSelectedListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			
			IDashboardGadgetFactory selectedType = table.getFirstSelectedElement();
			if (selectedType != null) {
				typeDescription.setText(selectedType.getDescription());
				
				getWizardHop().removeAllHopPages();
				getWizardHop().addHopPage(getCreateFactoryConfigEntry(selectedType).page);
				getWizard().getContainer().updateButtons();
			}
		}
	};
	
	
	public DashboardGadgetTypePage(DashboardLayoutConfigModule<?> configModule) {
		super(DashboardGadgetTypePage.class.getName());
		this.configModule = configModule;
		setWizardHop(new WizardHop(this));
		setTitle(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.config.DashboardGadgetTypePage.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.config.DashboardGadgetTypePage.message")); //$NON-NLS-1$
	}
	
	@Override
	public Control createPageContents(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		
		table = new DashboardGadgetFactoryTable(wrapper, SWT.NONE);
		table.setInput(DashboardGadgetRegistry.sharedInstance().getFactories());
		table.getTableViewer().setComparator(new ViewerComparator());
		
		table.addSelectionChangedListener(typeSelectedListener);
		table.getTableViewer().addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				IDashboardGadgetFactory selectedType = table.getFirstSelectedElement();
				if (selectedType != null) {
					getContainer().showPage(getNextPage());
				}
			}
		});
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		typeDescription = new Text(wrapper, SWT.WRAP);
		typeDescription.setEditable(false);
		
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		textGD.minimumHeight = RCPUtil.getFontHeight(typeDescription) * 5;
		typeDescription.setLayoutData(textGD);
		
		return wrapper;
	}

	@SuppressWarnings("unchecked")
	private PageEntry getCreateFactoryConfigEntry(IDashboardGadgetFactory factory) {
		PageEntry pageEntry = (PageEntry) factoryConfigPages.get(factory.getDashboardGadgetType());
		if (pageEntry == null) {
			pageEntry = new PageEntry();
			IDashboardGadgetConfigPage<Object> page = factory.createConfigurationWizardPage();
			DashboardGadgetLayoutEntry<Object> layoutEntry = (DashboardGadgetLayoutEntry<Object>) configModule.createEditLayoutEntry(factory.getDashboardGadgetType());
			page.initialize(layoutEntry);
			pageEntry.page = page;
			pageEntry.layoutEntry = layoutEntry;
			factoryConfigPages.put(factory.getDashboardGadgetType(), pageEntry);
		}
		return pageEntry;
	}
	
	
	public DashboardGadgetLayoutEntry<?> getConfiguredLayoutEntry() {
		IDashboardGadgetFactory selectedType = table.getFirstSelectedElement();
		if (selectedType != null) {
			PageEntry pageEntry = getCreateFactoryConfigEntry(selectedType);
			pageEntry.page.configure(pageEntry.layoutEntry);
			return pageEntry.layoutEntry;
		}
		return null;
	}
	
}

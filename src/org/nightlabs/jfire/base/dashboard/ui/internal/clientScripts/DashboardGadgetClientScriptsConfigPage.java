package org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashbardGadgetConfigPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetClientScriptsConfig;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

public class DashboardGadgetClientScriptsConfigPage extends AbstractDashbardGadgetConfigPage<Object> {

	private I18nTextEditor gadgetTitle;
	
	private Button buttonConfirmProcessing;
	
	private Table table;
	
	private List<DashboardGadgetClientScriptsConfig.ClientScript> clientScripts;
	
	private boolean confirmProcessing;
	
	private TableViewer tableViewer;
	
	public DashboardGadgetClientScriptsConfigPage() {
		super(DashboardGadgetClientScriptsConfigPage.class.getName());
		setTitle("Client scripts");
	}

	@Override
	public Control createPageContents(final Composite parent) {
		final XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, 2);
		
		final Label labelDescription1 = new Label(wrapper, SWT.WRAP);
		labelDescription1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		labelDescription1.setText("This gadget will show you all stored client scripts.");
		
		final Label labelTitle = new Label(wrapper, SWT.NONE);
		labelTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		labelTitle.setText("Select the title for this gadget:");

		gadgetTitle = new I18nTextEditor(wrapper);
		gadgetTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		gadgetTitle.setI18nText(!getLayoutEntry().getEntryName().isEmpty() ? getLayoutEntry().getEntryName() : createInitialName());
		
		createUpperWidgets(wrapper);
		createTableWidget(wrapper);
		createTableSpecificWidgets(wrapper);
		
		setTableInput();
		
		return wrapper;
	}
	
	private void createUpperWidgets(final Composite parent) {
		GridData gd;
		
		buttonConfirmProcessing = new Button(parent, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd.verticalIndent = 10;
		buttonConfirmProcessing.setLayoutData(gd);
		buttonConfirmProcessing.setText("Confirm processing");
		buttonConfirmProcessing.setEnabled(confirmProcessing);
		
		final Label labelDescription2 = new Label(parent, SWT.WRAP);
		labelDescription2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		labelDescription2.setText("Select a client script to be edited or create a new one.");

	}
	
	private void createTableWidget(final Composite parent) {
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5);
		gridData.verticalIndent = 5;
		table.setLayoutData(gridData);
		
		tableViewer = new TableViewer(table);
		
		attachContentProvider();
	    attachLabelProvider();
	}
	
	private void createTableSpecificWidgets(final Composite parent) {
		GridData gd;
		
		final Button buttonNew = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 75;
		buttonNew.setLayoutData(gd);
		buttonNew.setText("New");
		
		final Button buttonRemove = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 75;
		buttonRemove.setLayoutData(gd);
		buttonRemove.setText("Remove");
		
		final Button buttonEdit = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 75;
		buttonEdit.setLayoutData(gd);
		buttonEdit.setText("Edit");

		final Button buttonUp = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 75;
		buttonUp.setLayoutData(gd);
		buttonUp.setText("Up");

		final Button buttonDown = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 75;
		buttonDown.setLayoutData(gd);
		buttonDown.setText("Down");
		
	}
	
	private void attachContentProvider() {
		tableViewer.setContentProvider(new ArrayContentProvider());
	}
	
	private void attachLabelProvider() {
		tableViewer.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(final Object element, final int columnIndex) {
				if (element instanceof DashboardGadgetClientScriptsConfig.ClientScript) {
					return ((DashboardGadgetClientScriptsConfig.ClientScript) element).getName();
				}
				return null;
			}
		});
	}
	
	private void setTableInput() {
		tableViewer.setInput(clientScripts);
	}
	
	private I18nText createInitialName() {
		final I18nTextBuffer textBuffer = new I18nTextBuffer();
//		TradeDashboardGadgetsConfigModuleInitialiser.initializeClientScriptsGadgetName(textBuffer);
		return textBuffer;
	}

	@Override
	public void initialize(final DashboardGadgetLayoutEntry<?> layoutEntry) {
		super.initialize(layoutEntry);
		final Object config_ = getLayoutEntry().getConfig();
		if (config_ instanceof DashboardGadgetClientScriptsConfig) {
			final DashboardGadgetClientScriptsConfig config = (DashboardGadgetClientScriptsConfig) config_;
			clientScripts = config.getClientScripts();
			confirmProcessing = config.isConfirmProcessing();
		}
	}
	
	@Override
	public void configure(final DashboardGadgetLayoutEntry<?> layoutEntry) {
	}
	
}

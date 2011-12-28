package org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
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
	
	private List<DashboardGadgetClientScriptsConfig.ClientScript> clientScripts;
	
	private boolean confirmProcessing;
	
	private TableViewer tableViewer;
	
	private Button buttonRemove;
	
	private Button buttonEdit;
	
	private Button buttonMoveUp;
	
	private Button buttonMoveDown;
	
	private DashboardGadgetClientScriptsConfig config;
	
	public DashboardGadgetClientScriptsConfigPage() {
		super(DashboardGadgetClientScriptsConfigPage.class.getName());
		setTitle("Client scripts");
	}

	@Override
	public Control createPageContents(final Composite parent) {
		final XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, 3);
		
		final Label labelDescription1 = new Label(wrapper, SWT.WRAP);
		labelDescription1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		labelDescription1.setText("This gadget will show you all stored client scripts.");
		
		final Label labelTitle = new Label(wrapper, SWT.NONE);
		labelTitle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		labelTitle.setText("Set the title of this gadget:");

		gadgetTitle = new I18nTextEditor(wrapper);
		gadgetTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		gadgetTitle.setI18nText(!getLayoutEntry().getEntryName().isEmpty() ? getLayoutEntry().getEntryName() : createInitialName());
		
		createUpperWidgets(wrapper);
		createTableWidget(wrapper);
		createButtonWidgets(wrapper);
		
		tableViewer.setInput(clientScripts);
		
		return wrapper;
	}
	
	private void createUpperWidgets(final Composite parent) {
		GridData gd;
		
		buttonConfirmProcessing = new Button(parent, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd.verticalIndent = 10;
		buttonConfirmProcessing.setLayoutData(gd);
		buttonConfirmProcessing.setText("Confirm processing");
		buttonConfirmProcessing.setEnabled(confirmProcessing);
		
		final Label labelDescription2 = new Label(parent, SWT.WRAP);
		labelDescription2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		labelDescription2.setText("Select a client script to be edited or create a new one.");

	}
	
	private void createTableWidget(final Composite parent) {
		final Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gridData.verticalIndent = 5;
		table.setLayoutData(gridData);
		
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				updateButtonStates();
				
				
				
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				if (table.getSelectionIndex() > -1) {
					final TableItem item = table.getItem(table.getSelectionIndex());
					
					
					
				}
			}
		});
		
		tableViewer = new TableViewer(table);
		
		attachContentProvider();
	    attachLabelProvider();
	}
	
	private void createButtonWidgets(final Composite parent_) {
		final Composite parent = new Composite(parent_, SWT.NONE);
		parent.setLayout(new GridLayout());
		GridData gd;
		
		final Button buttonNew = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		buttonNew.setLayoutData(gd);
		buttonNew.setText("New");
		buttonNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				createClientScript();
				updateButtonStates();
			}
		});
		
		buttonRemove = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		buttonRemove.setLayoutData(gd);
		buttonRemove.setText("Remove");
		buttonRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeClientScript();
				updateButtonStates();
			}
		});
		
		buttonEdit = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		buttonEdit.setLayoutData(gd);
		buttonEdit.setText("Edit");
		buttonEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				editClientScript();
//				updateButtonStates();
			}
		});

		buttonMoveUp = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		buttonMoveUp.setLayoutData(gd);
		buttonMoveUp.setEnabled(false);
		buttonMoveUp.setText("Up");
		buttonMoveUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateItemOrder();
				updateButtonStates();
			}
		});

		buttonMoveDown = new Button(parent, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		buttonMoveDown.setLayoutData(gd);
		buttonMoveDown.setEnabled(false);
		buttonMoveDown.setText("Down");
		buttonMoveDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateItemOrder();
				updateButtonStates();
			}
		});
	}
	
	static class ClientScriptPropertiesWrapper {
		
		private String clientScriptName;
		private String clientScriptContent;
		
		public ClientScriptPropertiesWrapper() {
			clientScriptName = "";
			clientScriptContent = "";
		}
		
		public ClientScriptPropertiesWrapper(final String clientScriptName, final String clientScriptContent) {
			this.clientScriptName = clientScriptName;
			this.clientScriptContent = clientScriptContent;
		}

		public String getClientScriptName() {
			return clientScriptName;
		}
		public String getClientScriptContent() {
			return clientScriptContent;
		}
		public void setClientScriptName(final String clientScriptName) {
			this.clientScriptName = clientScriptName;
		}
		public void setClientScriptContent(final String clientScriptContent) {
			this.clientScriptContent = clientScriptContent;
		}
	}
	
	private void createClientScript() {
		final ClientScriptPropertiesWrapper data = new ClientScriptPropertiesWrapper();
		final DashboardGadgetClientScriptsNewEditDialog dialog = new DashboardGadgetClientScriptsNewEditDialog(getShell(), data);
		if (dialog.open() == Window.OK) {
			// Create new ClientScript instance and insert it into the table (not persisted yet)
			DashboardGadgetClientScriptsConfig.ClientScript newClientScript = config.createNewClientScript(
				data.getClientScriptName(), data.getClientScriptContent());		// TODO
//			final DashboardGadgetClientScriptsConfig.ClientScript newClientScript = new DashboardGadgetClientScriptsConfig.ClientScript(
//				data.getClientScriptName(), data.getClientScriptContent());
			if (tableViewer.getInput() instanceof List<?>) {
				final List<DashboardGadgetClientScriptsConfig.ClientScript> clientScripts = (List<DashboardGadgetClientScriptsConfig.ClientScript>) tableViewer.getInput();
				clientScripts.add(newClientScript);
				tableViewer.setInput(null);		// TODO
				tableViewer.setInput(clientScripts);
			}
		}
	}
	
	private void editClientScript() {
		TableItem item = tableViewer.getTable().getItem(tableViewer.getTable().getSelectionIndex());
		Object data_ = item.getData();
		if (data_ instanceof DashboardGadgetClientScriptsConfig.ClientScript) {
			DashboardGadgetClientScriptsConfig.ClientScript clientScript = (DashboardGadgetClientScriptsConfig.ClientScript) data_;
			ClientScriptPropertiesWrapper data = new ClientScriptPropertiesWrapper(clientScript.getName(), clientScript.getScript());
			final DashboardGadgetClientScriptsNewEditDialog dialog = new DashboardGadgetClientScriptsNewEditDialog(getShell(), data);
			if (dialog.open() == Window.OK) {
				
			}
		}
	}
	
	private void removeClientScript() {
		
	}
	
	private void updateItemOrder() {
		
	}
	
	private void updateButtonStates() {
		final boolean validIdx = tableViewer.getTable().getSelectionIndex() > 0 ? true : false;
		buttonRemove.setEnabled(validIdx);
		buttonEdit.setEnabled(validIdx);
		buttonMoveUp.setEnabled(validIdx);
		buttonMoveDown.setEnabled(tableViewer.getTable().getSelectionIndex() < tableViewer.getTable().getItemCount() - 1);
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
			config = (DashboardGadgetClientScriptsConfig) config_;
			clientScripts = config.getClientScripts();
		}
	}
	
	@Override
	public void configure(final DashboardGadgetLayoutEntry<?> layoutEntry) {
	}
	
}

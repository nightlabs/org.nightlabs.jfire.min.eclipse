/**
 * 
 */
package org.nightlabs.jfire.base.ui.person.search;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.WizardHop;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonSearchWizardPage extends WizardHopPage {
	private PersonEditorWizardHop editorWizardHop;
	private PersonSearchComposite searchComposite;
	private String quickSearchText;
	private Person newPerson;
	private Button searchButton;
	private Button editButton;
	private IAction editAction;
	private boolean allowNewLegalEntityCreation;
	private boolean allowEditLegalEntity;

	public PersonSearchWizardPage(String quickSearchText) {
		this(quickSearchText, true, false);
	}
	
	public PersonSearchWizardPage(String quickSearchText, boolean allowNewLegalEntityCreation, boolean allowEditLegalEntity) {
		super(
			PersonSearchWizardPage.class.getName(),
			Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.title") //$NON-NLS-1$
		);
		this.quickSearchText = quickSearchText;
		this.allowNewLegalEntityCreation = allowNewLegalEntityCreation;
		this.allowEditLegalEntity = allowEditLegalEntity;
		
		setDescription(Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.description")); //$NON-NLS-1$
		new WizardHop(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
		searchComposite = new PersonSearchComposite(parent, SWT.NONE, quickSearchText);
		Composite buttonBar = searchComposite.getButtonBar();
		GridLayout gl = new GridLayout();
		XComposite.configureLayout(LayoutMode.LEFT_RIGHT_WRAPPER, gl);
		gl.numColumns = 2;
		
		if (allowNewLegalEntityCreation) {
			gl.numColumns++;
			Button createNewButton = new Button(buttonBar, SWT.PUSH);
			createNewButton.setText(getCreateNewButtonText());
			createNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			createNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (newPerson == null) {
						newPerson = new Person(
							SecurityReflector.getUserDescriptor().getOrganisationID(),
							IDGenerator.nextID(PropertySet.class)
						);
						StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
								Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, new NullProgressMonitor());
						newPerson.inflate(structLocal);
						editorWizardHop = new PersonEditorWizardHop();
						editorWizardHop.initialise(newPerson);					
					}
					getWizardHop().addHopPage(editorWizardHop.getEntryPage());
					personSelectionChanged();
					getContainer().showPage(getNextPage());
				}
			});
		}
		
		if (allowEditLegalEntity) {
			gl.numColumns++;
			editButton = new Button(buttonBar, SWT.PUSH);
			editButton.setText(getEditButtonText());
			editButton.setEnabled(false);
			editButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					editorWizardHop = new PersonEditorWizardHop();
					Person selectedPerson = searchComposite.getResultTable().getFirstSelectedElement();
					StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
							Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, new NullProgressMonitor());				
					selectedPerson.inflate(structLocal);
					editorWizardHop.initialise(selectedPerson);					
					getWizardHop().addHopPage(editorWizardHop.getEntryPage());
					personSelectionChanged();
					getContainer().showPage(getNextPage());
				}
			});
		}
		
		buttonBar.setLayout(gl);
		new XComposite(buttonBar, SWT.NONE, LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		searchButton = searchComposite.createSearchButton(buttonBar);
		searchComposite.getResultTable().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				personSelectionChanged();
			}
		});
		searchComposite.getResultTable().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				personDoubleClicked();
			}
		});
		return searchComposite;
	}
	
	protected void personDoubleClicked() {
		if (getContainer() instanceof WizardDialog) {
			if (getWizard().performFinish()) {
				((WizardDialog) getContainer()).close();
			}
		}
	}
	
	protected void personSelectionChanged() {
		getContainer().updateButtons();
		onPersonSelectionChanged();
	}
	
	protected void onPersonSelectionChanged() 
	{
		if (editButton != null && !editButton.isDisposed())
			editButton.setEnabled(searchComposite.getResultTable().getSelectionCount() == 1);
	}

	@Override
	public void onShow() {
		getWizardHop().removeAllHopPages();
		getContainer().updateButtons();
		getShell().setDefaultButton(searchButton);
	}
	
	@Override
	public boolean isPageComplete() {
		return searchComposite != null && (searchComposite.getResultTable().getFirstSelectedElement() != null || !getWizardHop().getHopPages().isEmpty());		
	}
	
	/**
	 * @return Either the Person selected in the table in the first page
	 * or the newly created Person.
	 */
	public Person getSelectedPerson() {
		if (getWizard().getContainer().getCurrentPage() == this) {
			return searchComposite.getResultTable().getFirstSelectedElement();
		} else {
			if (newPerson != null)
				return newPerson;
			else
				return searchComposite.getResultTable().getFirstSelectedElement();
		}
	}
	
	protected String getCreateNewButtonText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.createNewButton.text"); //$NON-NLS-1$
	}
	
	protected String getEditButtonText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.button.editPerson.text"); //$NON-NLS-1$
	}
}

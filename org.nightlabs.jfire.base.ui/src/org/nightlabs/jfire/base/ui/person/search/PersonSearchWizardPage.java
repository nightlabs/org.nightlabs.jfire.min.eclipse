/**
 *
 */
package org.nightlabs.jfire.base.ui.person.search;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.WizardHop;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.DisplayNamePart;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.PropertySetDAO;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * Wizard-page for doing {@link Person}-searches. It will use a {@link PersonSearchComposite} and
 * additionally provides the possibility to enable the creation of a new or the editing of the
 * selected Person inside an own WizardHop.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public class PersonSearchWizardPage extends WizardHopPage
{
	
	/** The search-composite for this entry page */
	private PersonSearchComposite searchComposite;
	private String quickSearchText;

	/** Set in constructor, defines if createNewButton is created */
	private boolean allowNewPersonCreation;
	/** Triggers {@link #newPersonPressed()} */
	private Button createNewButton;
	/** Initialized when *new person* is pressed, won't be nulled afterwards */
	private PersonEditorWizardHop newPersonEditorWizardHop;
	/** Set when the createNewButton */
	private Person newPerson;
	/**
	 * This is set to true when newPerson is pressed and re-set when the page is shown.
	 */
	private boolean editingNewPerson = false;
	
	/** Set in constructor, defines if editButton is created */
	private boolean allowEditPerson;
	/** Triggers {@link #editPersonPressed()} */
	private Button editButton;
	/** Initialized when *edit person* is pressed, won't be nulled afterwards */
	private PersonEditorWizardHop editorWizardHop;
	
	
	private Button searchButton;

	public PersonSearchWizardPage(String quickSearchText) {
		this(quickSearchText, true, false);
	}

	public PersonSearchWizardPage(String quickSearchText, boolean allowNewPersonCreation, boolean allowEditPerson) {
		this(
			quickSearchText,
			allowNewPersonCreation,
			allowEditPerson,
			Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.title"), //$NON-NLS-1$
			Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.description") //$NON-NLS-1$
		);
	}

	/**
	 * Creates a PersonSearchWizardPage.
	 * 
	 * @param quickSearchText the quickSearchText (will be displayed in the default-entry configured
	 *            for the search)
	 * @param allowNewPersonCreation determines if the create new person button will be displayed,
	 *            for creating a new person.
	 * @param allowEditPerson determines if the edit person button will be displayed, for editing a
	 *            selected person.
	 * @param title the title of the wizard page.
	 * @param description the description of the wizard page.
	 */
	public PersonSearchWizardPage(String quickSearchText, boolean allowNewPersonCreation, boolean allowEditPerson, String title,
			String description)
	{
		super(PersonSearchWizardPage.class.getName(), title);
		if (quickSearchText == null)
			quickSearchText = ""; //$NON-NLS-1$

		this.quickSearchText = quickSearchText;
		this.allowNewPersonCreation = allowNewPersonCreation;
		this.allowEditPerson = allowEditPerson;
		setDescription(description);
		new WizardHop(this);
	}

	private boolean controlIsChildOfOrEquals(Composite parent, Widget child)
	{
		if (parent == null)
			throw new IllegalArgumentException("parent == null"); //$NON-NLS-1$

		if (child == null)
			return false;

		if (parent.equals(child))
			return true;

		if (!(child instanceof Control))
			return false;

		Control c = (Control) child;

		while (c.getParent() != null) {
			Control cp = c.getParent();
			if (parent.equals(cp))
				return true;
			c = cp;
		}

		return false;
	}

	private Listener focusListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (controlIsChildOfOrEquals(searchComposite.getTopWrapper(), event.widget))
				makeSearchButtonDefault();

//			switch (event.type) {
//				case SWT.FocusIn:
//
//					break;
//				case SWT.FocusOut:
//					break;
//			}
		}
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
		searchComposite = new PersonSearchComposite(parent, SWT.NONE, quickSearchText, PersonSearchUseCaseConstants.USE_CASE_ID_DEFAULT);
		Composite buttonBar = searchComposite.getButtonBar();
		final Display display = searchComposite.getDisplay();
		display.addFilter(SWT.FocusIn, focusListener);
//		display.addFilter(SWT.FocusOut, focusListener);
		searchComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				display.removeFilter(SWT.FocusIn, focusListener);
//				display.removeFilter(SWT.FocusOut, focusListener);
			}
		});

		// This doesn't seem to be necessary. Tobias.
//		searchComposite.addSearchTextModifyListener(new ModifyListener(){
//			@Override
//			public void modifyText(ModifyEvent e) {
//				String text = searchComposite.getSearchText();
//				if (text != null) {
//					quickSearchText = text;
//					logger.debug("quickSearchText = "+quickSearchText); //$NON-NLS-1$
//				}
//			}
//		});

		GridLayout gl = new GridLayout();
		XComposite.configureLayout(LayoutMode.LEFT_RIGHT_WRAPPER, gl);
		gl.numColumns = 2;

		if (allowNewPersonCreation) {
			gl.numColumns++;
			createNewButton = new Button(buttonBar, SWT.PUSH);
			createNewButton.setText(getCreateNewButtonText());
			createNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			createNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					newPersonPressed();
				}
			});
		}

		if (allowEditPerson) {
			gl.numColumns++;
			editButton = new Button(buttonBar, SWT.PUSH);
			editButton.setText(getEditButtonText());
			editButton.setEnabled(false);
			editButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					editPersonPressed();
				}
			});
		}

		buttonBar.setLayout(gl);
		new XComposite(buttonBar, SWT.NONE, LayoutDataMode.GRID_DATA_HORIZONTAL);

		searchButton = searchComposite.createSearchButton(buttonBar);
		searchComposite.getResultViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				personSelectionChanged();
			}
		});
		searchComposite.getResultViewer().addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent arg0) {
				personDoubleClicked();
			}
		});
		return searchComposite;
	}

	private boolean switchingToNewPerson = false;

	/**
	 * Adds a new WizardHop for editing a new Person and switches to its first page.
	 */
	protected void newPersonPressed()
	{
		// TODO Think about how we can get the search data that was entered by the user to fill the
		// new person with some data for a start. Tobias.
		
		switchingToNewPerson = true;
		try {
			if (newPerson == null) {
				newPerson = new Person(
						IDGenerator.getOrganisationID(),
						IDGenerator.nextID(PropertySet.class)
				);
				StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
						newPerson.getStructLocalObjectID(),
						new NullProgressMonitor()
				);
				newPerson.inflate(structLocal);
				newPerson.setAutoGenerateDisplayName(true);
				newPersonEditorWizardHop = new PersonEditorWizardHop();
				newPersonEditorWizardHop.initialise(newPerson);
			}
			if (quickSearchText != null && newPerson != null) {
				newPerson.setDisplayName(quickSearchText, newPerson.getStructure());
				for (DisplayNamePart part : newPerson.getStructure().getDisplayNameParts()) {
					StructField<?> sf = part.getStructField();
					String suffix = part.getStructFieldSuffix();
					String firstPart = null;
					if (suffix.equals("")) { //$NON-NLS-1$
						firstPart = quickSearchText;
					}
					else {
						int index = quickSearchText.indexOf(suffix);
						if (index != -1) {
							String[] parts = quickSearchText.split(suffix);
							if (parts.length > 0) {
								firstPart = parts[0];
								StringBuilder sb = new StringBuilder();
								sb.append(firstPart);
								sb.append(suffix);
								quickSearchText = quickSearchText.replace(sb.toString(), ""); //$NON-NLS-1$
							}
						}
					}
					if (firstPart != null) {
						try {
							DataField df = newPerson.getDataField(sf.getStructFieldIDObj());
							if (df.supportsInputType(String.class)) {
								df.setData(firstPart);
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				}
			}

			editingNewPerson = true;
			getWizardHop().addHopPage(newPersonEditorWizardHop.getEntryPage());
			personSelectionChanged();
			getContainer().showPage(getNextPage());
		} finally {
			switchingToNewPerson = false;
		}
	}

	/**
	 * Adds a new WizardHop for editing a the selected Person and switches to its first page.
	 */
	protected void editPersonPressed()
	{
		// we have to create a new one, because initialise(...) doesn't overwrite the data
		editorWizardHop = new PersonEditorWizardHop();
		Person selectedPerson = (Person) searchComposite.getResultViewer().getFirstSelectedElement();
		
		// The person might be detached wrongly (trimmed or in another way), so we have to re-obtain
		// it with the correct fetch-groups.
		selectedPerson = (Person) PropertySetDAO.sharedInstance().getPropertySet((PropertySetID) JDOHelper.getObjectId(selectedPerson),
				new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		
		StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
				selectedPerson.getStructLocalObjectID(),
				new NullProgressMonitor()
		);
		selectedPerson.inflate(structLocal);
		editorWizardHop.initialise(selectedPerson);
		getWizardHop().addHopPage(editorWizardHop.getEntryPage());
		personSelectionChanged();
		getContainer().showPage(getNextPage());
	}

	protected void personDoubleClicked() {
		if (getContainer() instanceof WizardDialog) {
			if (isPageComplete() && getWizard().performFinish()) {
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
			editButton.setEnabled(searchComposite.getResultViewer().getSelectedElements().size() == 1 &&
					searchComposite.getResultViewer().getFirstSelectedElement() instanceof Person);

		if (controlIsChildOfOrEquals(searchComposite.getTopWrapper(), searchComposite.getDisplay().getFocusControl()))
			makeSearchButtonDefault();
	}

	@Override
	public void onShow() {
		super.onShow();
		getWizardHop().removeAllHopPages();
		getContainer().updateButtons();
		makeSearchButtonDefault();
		editingNewPerson = false;
	}

	private void makeSearchButtonDefault() {
		getShell().setDefaultButton(searchButton);
	}

	@Override
	public boolean isPageComplete() {
		return searchComposite != null &&
			((searchComposite.getResultViewer().getFirstSelectedElement() != null &&
			  searchComposite.getResultViewer().getFirstSelectedElement() instanceof Person) ||
			  !getWizardHop().getHopPages().isEmpty());
	}

	/**
	 * @return Either the Person selected in the table in the first page, or the edited or newly
	 *         created person.
	 */
	public Person getSelectedPerson() {
		if (getWizard().getContainer().getCurrentPage() == this) {
			if (switchingToNewPerson && newPerson != null)
				return newPerson;
			else
				return (Person) searchComposite.getResultViewer().getFirstSelectedElement();
		} else {
			if (newPerson != null && editingNewPerson)
				return newPerson;
			else if (editorWizardHop != null) {
				// Is the edited person here, because the search-page is not the current one
				// and we are not editing a new person.
				return editorWizardHop.getPerson();
			} else {
				// in this case we are not creating a new person and not editing an existing one,
				// The search page is simply not the current one but was asked for the selected person nevertheless
				return (Person) searchComposite.getResultViewer().getFirstSelectedElement();
			}
		}
	}

	protected String getCreateNewButtonText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.createNewButton.text"); //$NON-NLS-1$
	}

	protected String getEditButtonText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchWizardPage.button.editPerson.text"); //$NON-NLS-1$
	}

	public void setQuickSearchText(String text) {
		this.quickSearchText = text;
		if (searchComposite != null && !searchComposite.isDisposed()) {
			searchComposite.setQuickSearchText(text);
		}
	}
}

package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.UserSecurityGroup;

/**
 * The section containing the user groups controls for the {@link PersonPreferencesPage}.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class UserSecurityGroupsSection extends ToolBarSectionPart {

	/**
	 * The editor model.
	 */
	UserSecurityPreferencesModel model;

	UserSecurityGroupTableViewer userSecurityGroupTableViewer;

	private CheckSelectedAction checkSelectedAction;
	private UncheckSelectedAction uncheckSelectedAction;
	private CheckAllAction checkAllAction;
	private UncheckAllAction uncheckAllAction;
	
	/**
	 * Create an instance of UserSecurityGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserSecurityGroupsSection(FormPage page, Composite parent)
	{
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.sectionTitle")); //$NON-NLS-1$);
		createClient(getSection(), page.getEditor().getToolkit());
	}



	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit)
	{
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);

		userSecurityGroupTableViewer = new UserSecurityGroupTableViewer(createUserSecurityGroupsTable(toolkit, container), UserUtil.getSectionDirtyStateManager(this));
		userSecurityGroupTableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ') { 
					UserSecurityPreferencesModel model = userSecurityGroupTableViewer.getModel();
					IStructuredSelection sel = (IStructuredSelection)userSecurityGroupTableViewer.getSelection();
					if (sel.size() > 1) {
						for (Iterator<UserSecurityGroup> iterator = sel.iterator(); iterator.hasNext();) {
							UserSecurityGroup userSecurityGroup = (UserSecurityGroup)iterator.next();
							model.addElement(userSecurityGroup);
						}
					}
					else {
						UserSecurityGroup userSecurityGroup = (UserSecurityGroup)sel.getFirstElement();
						if (model.contains(userSecurityGroup)) {
							model.removeElement(userSecurityGroup);
						}
						else {
							model.addElement(userSecurityGroup);
						}
					}
					
					userSecurityGroupTableViewer.refresh();
					markDirty();
				}
			}
		});
		
		checkSelectedAction = new CheckSelectedAction();
		uncheckSelectedAction = new UncheckSelectedAction();
		checkAllAction = new CheckAllAction();
		uncheckAllAction = new UncheckAllAction();

		getToolBarManager().add(checkSelectedAction);
		getToolBarManager().add(uncheckSelectedAction);
		getToolBarManager().add(checkAllAction);
		getToolBarManager().add(uncheckAllAction);
		
		updateToolBarManager();
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(checkSelectedAction);
		menuManager.add(uncheckSelectedAction);
		menuManager.add(checkAllAction);
		menuManager.add(uncheckAllAction);
		
		Menu menu = menuManager.createContextMenu(userSecurityGroupTableViewer.getTable());
		userSecurityGroupTableViewer.getTable().setMenu(menu);	
	}
	
	public void setModel(final UserSecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (userSecurityGroupTableViewer != null && !userSecurityGroupTableViewer.getTable().isDisposed())
					userSecurityGroupTableViewer.setModel(model);
			}
		});
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.descriptionText"), true, false); //$NON-NLS-1$
//		text.addHyperlinkListener(new HyperlinkAdapter() {
//			/* (non-Javadoc)
//			 * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
//			 */
//			@Override
//			public void linkActivated(HyperlinkEvent e)
//			{
//				System.err.println("HYPERLINK EVENT! "+e); //$NON-NLS-1$
//			}
//		});
		section.setDescriptionControl(text);
	}

	private Table createUserSecurityGroupsTable(FormToolkit toolkit, Composite container)
	{
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | XComposite.getBorderStyle(container));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		fTable.setLayoutData(gd);
//		TableColumn col1 = new TableColumn(fTable, SWT.NULL);
//		col1.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.col0")); //$NON-NLS-1$
//		TableColumn col2 = new TableColumn(fTable, SWT.NULL);
//		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.col1")); //$NON-NLS-1$
//		TableLayout tlayout = new TableLayout();
//		tlayout.addColumnData(new ColumnWeightData(30, 30));
//		tlayout.addColumnData(new ColumnWeightData(70, 70));
//		fTable.setLayout(tlayout);
		fTable.setLayout(new WeightedTableLayout(new int[] {-1, 30, 70}, new int[] {20, -1, -1}));
		fTable.setHeaderVisible(true);
		toolkit.paintBordersFor(fTable);
		//createContextMenu(fTable);
		return fTable;
	}
	
	public class CheckSelectedAction extends Action {
		public CheckSelectedAction() {
			super();
			setId(CheckSelectedAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					UserSecurityGroupsSection.class,
					"CheckSelected")); //$NON-NLS-1$
			setToolTipText("Check Selected");
			setText("Check Selected");
		}

		@Override
		public void run() {
			UserSecurityPreferencesModel model = userSecurityGroupTableViewer.getModel();
			IStructuredSelection sel = (IStructuredSelection)userSecurityGroupTableViewer.getSelection();
			for (Iterator<UserSecurityGroup> iterator = sel.iterator(); iterator.hasNext();) {
				UserSecurityGroup userSecurityGroup = (UserSecurityGroup)iterator.next();
				model.addElement(userSecurityGroup);
				userSecurityGroupTableViewer.refresh();
				markDirty();
			}
		}
	}
	
	public class UncheckSelectedAction extends Action {
		public UncheckSelectedAction() {
			super();
			setId(UncheckSelectedAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					UserSecurityGroupsSection.class,
					"UncheckSelected")); //$NON-NLS-1$
			setToolTipText("Uncheck Selected");
			setText("Uncheck Selected");
		}

		@Override
		public void run() {
			UserSecurityPreferencesModel model = userSecurityGroupTableViewer.getModel();
			IStructuredSelection sel = (IStructuredSelection)userSecurityGroupTableViewer.getSelection();
			for (Iterator<UserSecurityGroup> iterator = sel.iterator(); iterator.hasNext();) {
				UserSecurityGroup userSecurityGroup = (UserSecurityGroup)iterator.next();
				model.removeElement(userSecurityGroup);
				userSecurityGroupTableViewer.refresh();
				markDirty();
			}
		}
	}
	
	public class CheckAllAction extends Action {
		public CheckAllAction() {
			super();
			setId(CheckAllAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					UserSecurityGroupsSection.class,
					"CheckAll")); //$NON-NLS-1$
			setToolTipText("Check All");
			setText("Check All");
		}

		@Override
		public void run() {
			UserSecurityPreferencesModel model = userSecurityGroupTableViewer.getModel();
			Collection<UserSecurityGroup> userSecurityGroups = model.getAvailableUserSecurityGroups();
			for (UserSecurityGroup userSecurityGroup : userSecurityGroups) {
				model.addElement(userSecurityGroup);
			}
			userSecurityGroupTableViewer.refresh();
			markDirty();
		}
	}
	
	public class UncheckAllAction extends Action {
		public UncheckAllAction() {
			super();
			setId(UncheckAllAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					UserSecurityGroupsSection.class,
					"UncheckAll")); //$NON-NLS-1$
			setToolTipText("Uncheck All");
			setText("Uncheck All");
		}

		@Override
		public void run() {
			UserSecurityPreferencesModel model = userSecurityGroupTableViewer.getModel();
			Collection<UserSecurityGroup> userSecurityGroups = model.getUserSecurityGroups();
			for (UserSecurityGroup userSecurityGroup : userSecurityGroups) {
				model.removeElement(userSecurityGroup);
			}
			userSecurityGroupTableViewer.refresh();
			markDirty();
		}
	}
}
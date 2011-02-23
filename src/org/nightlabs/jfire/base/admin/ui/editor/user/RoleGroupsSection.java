/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.RoleGroup;

/**
 * The section containing the role groups controls.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleGroupsSection extends ToolBarSectionPart
{
	/**
	 * The editor model.
	 */
	RoleGroupSecurityPreferencesModel model;

//	/**
//	 * The viewer for included role groups.
//	 */
//	TableViewer includedRoleGroupsViewer;
//
//	/**
//	 * The viewer for excluded role groups.
//	 */
//	TableViewer excludedRoleGroupsViewer;
	
	RoleGroupTableViewer roleGroupTableViewer;

	private CheckSelectedAction checkSelectedAction;
	private UncheckSelectedAction uncheckSelectedAction;
	private CheckAllAction checkAllAction;
	private UncheckAllAction uncheckAllAction;
	
	/**
	 * Create an instance of RoleGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public RoleGroupsSection(FormPage page, Composite parent, boolean showTotalAvailColumn)
	{
		this(page, parent, showTotalAvailColumn, true);
	}

	public RoleGroupsSection(FormPage page, Composite parent, boolean showTotalAvailColumn, boolean showCheckBoxes)
	{
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.sectionTitle")); //$NON-NLS-1$
		createClient(getSection(), page.getEditor().getToolkit(), showTotalAvailColumn, showCheckBoxes);
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit, boolean showAssignmentSourceColum, boolean showCheckBoxes)
	{
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit, showAssignmentSourceColum, showCheckBoxes);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1); // Why was that "3" instead of "1"? We only need one column! I changed it and hope it has no special reason. If so, please COMMENT IT!!!! Marco.

		ViewerComparator roleGroupComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((RoleGroup)e1).getName().getText().compareTo(((RoleGroup)e2).getName().getText());
			}
		};
		
		section.setExpanded(true);
		
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
		toolkit.paintBordersFor(fTable);
		roleGroupTableViewer = new RoleGroupTableViewer(fTable, UserUtil.getSectionDirtyStateManager(this), showAssignmentSourceColum, showCheckBoxes);
		roleGroupTableViewer.setComparator(roleGroupComparator);
		
//		excludedRoleGroupsViewer = new TableViewer(createRoleGroupsTable(toolkit, container));
//		excludedRoleGroupsViewer.setContentProvider(new RoleGroupsContentProvider());
//		excludedRoleGroupsViewer.setLabelProvider(new RoleGroupsLabelProvider());
//		excludedRoleGroupsViewer.setComparator(roleGroupComparator);
//
////		createRoleGroupButtons(container, toolkit);
//
//		includedRoleGroupsViewer = new TableViewer(createRoleGroupsTable(toolkit, container));
//		includedRoleGroupsViewer.setContentProvider(new RoleGroupsContentProvider());
//		includedRoleGroupsViewer.setLabelProvider(new RoleGroupsLabelProvider());
//		includedRoleGroupsViewer.setComparator(roleGroupComparator);
		
		checkSelectedAction = new CheckSelectedAction();
		uncheckSelectedAction = new UncheckSelectedAction();
		checkAllAction = new CheckAllAction();
		uncheckAllAction = new UncheckAllAction();

		getToolBarManager().add(checkSelectedAction);
		getToolBarManager().add(uncheckSelectedAction);
		getToolBarManager().add(checkAllAction);
		getToolBarManager().add(uncheckAllAction);
		
		updateToolBarManager();
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit, boolean showAssignmentSourceColum, boolean showCheckBoxes)
	{
		FormText text = toolkit.createFormText(section, true);
		if(showAssignmentSourceColum)
			text.setText(
					String.format(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.description"), //$NON-NLS-1$
							Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirect"), //$NON-NLS-1$
							Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceGroup"), //$NON-NLS-1$
							Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirectAndGroup")), false, false); //$NON-NLS-1$
		else
			text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.simpleDescription"), false, false); //$NON-NLS-1$
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
	
	public void setModel(final RoleGroupSecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (roleGroupTableViewer != null && !roleGroupTableViewer.getTable().isDisposed())
					roleGroupTableViewer.setModel(model);
//				if (excludedRoleGroupsViewer == null)
//					return;
//
//				if (excludedRoleGroupsViewer.getTable().isDisposed())
//					return;
//
////				excludedRoleGroupsViewer.setInput(model.getExcludedRoleGroups());
//				includedRoleGroupsViewer.setInput(model.getRoleGroups());
			}
		});
	}
	
//	private void roleGroupsAdd()
//	{
//		ISelection s = excludedRoleGroupsViewer.getSelection();
//		if(s.isEmpty())
//			return;
//		IStructuredSelection selection = (IStructuredSelection)s;
//		Object[] a = selection.toArray();
//		List l = selection.toList();
//		model.getExcludedRoleGroups().removeAll(l);
//		excludedRoleGroupsViewer.remove(a);
//		model.getRoleGroups().addAll(l);
//		includedRoleGroupsViewer.add(a);
//		includedRoleGroupsViewer.setSelection(selection);
//		includedRoleGroupsViewer.reveal(l.get(0));
//		markDirty();
//	}
//
//	private void roleGroupsRemove()
//	{
//		ISelection s = includedRoleGroupsViewer.getSelection();
//		if(s.isEmpty())
//			return;
//		IStructuredSelection selection = (IStructuredSelection)s;
//		Object[] a = selection.toArray();
//		List l = selection.toList();
//		model.getRoleGroups().removeAll(l);
//		includedRoleGroupsViewer.remove(a);
//		model.getExcludedRoleGroups().addAll(l);
//		excludedRoleGroupsViewer.add(a);
//		excludedRoleGroupsViewer.setSelection(selection);
//		excludedRoleGroupsViewer.reveal(l.get(0));
//		markDirty();
//	}
//
//	private void createRoleGroupButtons(Composite client, FormToolkit toolkit)
//	{
//		Composite container = toolkit.createComposite(client);
//		GridLayout layout = new GridLayout();
//		layout.marginHeight = 10;
//		container.setLayout(layout);
//		container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
//
//		Button fAddButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.toRight"), SWT.PUSH); //$NON-NLS-1$
//		fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		fAddButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				roleGroupsAdd();
//			}
//		});
//		//fAddButton.setEnabled(isEditable());
//
//
//		Button fRemoveButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.toLeft"), SWT.PUSH); //$NON-NLS-1$
//		fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		fRemoveButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				roleGroupsRemove();
//			}
//		});
//
//		toolkit.paintBordersFor(container);
//	}

	public class CheckSelectedAction extends Action {
		public CheckSelectedAction() {
			super();
			setId(CheckSelectedAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					RoleGroupsSection.class,
					"CheckSelected")); //$NON-NLS-1$
			setToolTipText("Check Selected");
			setText("Check Selected");
		}

		@Override
		public void run() {
			RoleGroupSecurityPreferencesModel model = roleGroupTableViewer.getModel();
			IStructuredSelection sel = (IStructuredSelection)roleGroupTableViewer.getSelection();
			for (Iterator<RoleGroup> iterator = sel.iterator(); iterator.hasNext();) {
				RoleGroup roleGroup = (RoleGroup)iterator.next();
				model.addRoleGroup(roleGroup);
				roleGroupTableViewer.refresh();
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
					RoleGroupsSection.class,
					"UncheckSelected")); //$NON-NLS-1$
			setToolTipText("Uncheck Selected");
			setText("Uncheck Selected");
		}

		@Override
		public void run() {
			RoleGroupSecurityPreferencesModel model = roleGroupTableViewer.getModel();
			IStructuredSelection sel = (IStructuredSelection)roleGroupTableViewer.getSelection();
			for (Iterator<RoleGroup> iterator = sel.iterator(); iterator.hasNext();) {
				RoleGroup roleGroup = (RoleGroup)iterator.next();
				model.removeRoleGroup(roleGroup);
				roleGroupTableViewer.refresh();
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
					RoleGroupsSection.class,
					"CheckAll")); //$NON-NLS-1$
			setToolTipText("Check All");
			setText("Check All");
		}

		@Override
		public void run() {
			RoleGroupSecurityPreferencesModel model = roleGroupTableViewer.getModel();
			Collection<RoleGroup> roleGroups = model.getAllRoleGroupsInAuthority();
			for (RoleGroup roleGroup : roleGroups) {
				model.addRoleGroup(roleGroup);
			}
			roleGroupTableViewer.refresh();
			markDirty();
		}
	}
	
	public class UncheckAllAction extends Action {
		public UncheckAllAction() {
			super();
			setId(UncheckAllAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					RoleGroupsSection.class,
					"UncheckAll")); //$NON-NLS-1$
			setToolTipText("Uncheck All");
			setText("Uncheck All");
		}

		@Override
		public void run() {
			RoleGroupSecurityPreferencesModel model = roleGroupTableViewer.getModel();
			Collection<RoleGroup> roleGroups = model.getAllRoleGroupsInAuthority();
			for (RoleGroup roleGroup : roleGroups) {
				model.removeRoleGroup(roleGroup);
			}
			roleGroupTableViewer.refresh();
			markDirty();
		}
	}
}

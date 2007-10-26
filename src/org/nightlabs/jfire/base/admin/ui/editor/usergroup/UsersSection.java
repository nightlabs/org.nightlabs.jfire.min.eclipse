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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.usergroup;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;

/**
 * The section containing the users controls.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */

public class UsersSection extends RestorableSectionPart
{
	/**
	 * The viewer for included users.
	 */
	TableViewer includedUsersViewer;

	/**
	 * The viewer for excluded users.
	 */
	TableViewer excludedUsersViewer;

	/**
	 * The model for the usergroup
	 */
	private GroupSecurityPreferencesModel model;

	/**
	 * Create an instance of RoleGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UsersSection(FormPage page, Composite parent)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit());
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit) 
	{
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);
		
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);
		
		ViewerComparator userComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((User)e1).getName().compareTo(((User)e2).getName());
			}
		};

		excludedUsersViewer = new TableViewer(createUsersTable(toolkit, container));
		excludedUsersViewer.setContentProvider(new UsersContentProvider());
		excludedUsersViewer.setLabelProvider(new UsersLabelProvider());
		excludedUsersViewer.setComparator(userComparator);

		createUserButtons(container, toolkit);

		includedUsersViewer = new TableViewer(createUsersTable(toolkit, container));
		includedUsersViewer.setContentProvider(new UsersContentProvider());
		includedUsersViewer.setLabelProvider(new UsersLabelProvider());
		includedUsersViewer.setComparator(userComparator);
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.description"), false, false); //$NON-NLS-1$
		text.addHyperlinkListener(new HyperlinkAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
			 */
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				System.err.println("HYPERLINK EVENT! "+e); //$NON-NLS-1$
			}
		});
		section.setDescriptionControl(text);
	}	
	private void createUserButtons(Composite client, FormToolkit toolkit)
	{
		Composite container = toolkit.createComposite(client);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button fAddButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.toRight"), SWT.PUSH); //$NON-NLS-1$
		fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				usersAdd();
			}
		});

		Button fRemoveButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.toLeft"), SWT.PUSH); //$NON-NLS-1$
		fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				usersRemove();
			}
		});

		toolkit.paintBordersFor(container);
	}

	private Table createUsersTable(FormToolkit toolkit, Composite container)
	{
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		fTable.setLayoutData(gd);
		TableColumn col1 = new TableColumn(fTable, SWT.NULL);
		col1.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.user")); //$NON-NLS-1$
//		TableColumn col2 = new TableColumn(fTable, SWT.NULL);
//		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.description")); //$NON-NLS-1$
		TableLayout tlayout = new TableLayout();
		tlayout.addColumnData(new ColumnWeightData(100, 100));
//		tlayout.addColumnData(new ColumnWeightData(30, 30));
//		tlayout.addColumnData(new ColumnWeightData(70, 70));
		fTable.setLayout(tlayout);
		fTable.setHeaderVisible(true);
		toolkit.paintBordersFor(fTable);
		return fTable;
	}

	/**
	 * Content provider for users.
	 */
	private final class UsersContentProvider extends TableContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement)
		{
			Collection<User> users = (Collection<User>)inputElement;
			return users.toArray();
		}
	}

	/**
	 * Label provider for users. 
	 */
	private static class UsersLabelProvider extends TableLabelProvider
	{
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if(columnIndex == 0)
				return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UsersLabelProvider.class);
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
			if(!(element instanceof User))
				throw new RuntimeException("Invalid object type, expected User"); //$NON-NLS-1$
			User u = (User)element;
			switch(columnIndex) {
			case 0: return u.getName();
			case 1: return u.getDescription();
			}
			return null;
		}
	}

	public void setModel(final GroupSecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				excludedUsersViewer.setInput(model.getExcludedUsers());
				includedUsersViewer.setInput(model.getIncludedUsers());
			}
		});
	}

	private void usersAdd()
	{
		ISelection s = excludedUsersViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getExcludedUsers().removeAll(l);
		excludedUsersViewer.remove(a);
		model.getIncludedUsers().addAll(l);
		includedUsersViewer.add(a);
		refreshUsersDirtyState();
		includedUsersViewer.setSelection(selection);
		includedUsersViewer.reveal(l.get(0));
	}

	private void usersRemove()
	{
		ISelection s = includedUsersViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getIncludedUsers().removeAll(l);
		includedUsersViewer.remove(a);
		model.getExcludedUsers().addAll(l);
		excludedUsersViewer.add(a);
		refreshUsersDirtyState();
		excludedUsersViewer.setSelection(selection);
		excludedUsersViewer.reveal(l.get(0));
	}

	private void refreshUsersDirtyState()
	{
		Collection users = model.getIncludedUsersUnchanged();
		if(model.getIncludedUsers().size() == users.size() && model.getIncludedUsers().containsAll(users))
			markUndirty();
		else
			markDirty();
	}

}

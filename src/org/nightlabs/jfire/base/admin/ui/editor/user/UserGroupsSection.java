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
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;

/**
 * The section containing the user groups controls
 * for the {@link PersonPreferencesPage}.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserGroupsSection extends RestorableSectionPart
{
	/**
	 * The editor model.
	 */
	SecurityPreferencesModel model;

	/**
	 * Included user groups viewer.
	 */
	TableViewer includedUserGroupsViewer;

	/**
	 * Excluded user groups viewer.
	 */
	TableViewer excludedUserGroupsViewer;

	/**
	 * Create an instance of UserGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserGroupsSection(FormPage page, Composite parent)
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
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);

		excludedUserGroupsViewer = new TableViewer(createUserGroupsTable(toolkit, container));
		excludedUserGroupsViewer.setContentProvider(new UserGroupsContentProvider());
		excludedUserGroupsViewer.setLabelProvider(new UserGroupsLabelProvider());

		createUserGroupButtons(container, toolkit);

		includedUserGroupsViewer = new TableViewer(createUserGroupsTable(toolkit, container));
		includedUserGroupsViewer.setContentProvider(new UserGroupsContentProvider());
		includedUserGroupsViewer.setLabelProvider(new UserGroupsLabelProvider());
	}

	public void setModel(final SecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				excludedUserGroupsViewer.setInput(model.getExcludedUserGroups());
				includedUserGroupsViewer.setInput(model.getIncludedUserGroups());
			}
		});
	}
	
	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.descriptionText"), true, false); //$NON-NLS-1$
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

	/**
	 * Content provider for user groups. 
	 */
	private final class UserGroupsContentProvider extends TableContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement)
		{
			Collection<UserGroup> userGroups = (Collection<UserGroup>)inputElement;
			return userGroups.toArray();
		}
	}

	/**
	 * Label provider for user groups. 
	 */
	private class UserGroupsLabelProvider extends TableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex)
		{
			if(columnIndex == 0)
				return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UserGroupsLabelProvider.class);
//				return ImageDescriptor.createFromURL(BaseAdminPlugin.getDefault().getBundle().getEntry(BaseAdminPlugin.getResourceString("icon.usergroup"))).createImage(); //$NON-NLS-1$
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
//			if(!(element instanceof UserGroup))
//				throw new RuntimeException("Invalid object type, expected UserGroup");
			
			User g = (User)element;
			switch(columnIndex) {
				case 0: return g.getName();
				case 1: return g.getDescription();
			}
			return null;
		}
	}

	private Table createUserGroupsTable(FormToolkit toolkit, Composite container)
	{
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		fTable.setLayoutData(gd);
		TableColumn col1 = new TableColumn(fTable, SWT.NULL);
		col1.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.col0")); //$NON-NLS-1$
		TableColumn col2 = new TableColumn(fTable, SWT.NULL);
		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.col1")); //$NON-NLS-1$
//		TableLayout tlayout = new TableLayout();
//		tlayout.addColumnData(new ColumnWeightData(30, 30));
//		tlayout.addColumnData(new ColumnWeightData(70, 70));		
//		fTable.setLayout(tlayout);
		fTable.setLayout(new WeightedTableLayout(new int[] {30, 70}));
		fTable.setHeaderVisible(true);
		toolkit.paintBordersFor(fTable);
		//createContextMenu(fTable);
		return fTable;
	}

	private void userGroupsAdd()
	{
		ISelection s = excludedUserGroupsViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getExcludedUserGroups().removeAll(l);
		excludedUserGroupsViewer.remove(a);
		model.getIncludedUserGroups().addAll(l);
		includedUserGroupsViewer.add(a);
		refreshUserGroupDirtyState();
	}

	private void userGroupsRemove()
	{
		ISelection s = includedUserGroupsViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getIncludedUserGroups().removeAll(l);
		includedUserGroupsViewer.remove(a);
		model.getExcludedUserGroups().addAll(l);
		excludedUserGroupsViewer.add(a);
		refreshUserGroupDirtyState();
	}

	private void refreshUserGroupDirtyState()
	{
		Collection userUserGroups = model.getUser().getUserGroups();
		if(model.getIncludedUserGroups().size() == userUserGroups.size() && model.getIncludedUserGroups().containsAll(userUserGroups))
			markUndirty();
		else
			markDirty();
	}

	private void createUserGroupButtons(Composite client, FormToolkit toolkit)
	{
		Composite container = toolkit.createComposite(client);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button fAddButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.toRight"), SWT.PUSH); //$NON-NLS-1$
		fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				userGroupsAdd();
			}
		});

		Button fRemoveButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupsSection.toLeft"), SWT.PUSH); //$NON-NLS-1$
		fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				userGroupsRemove();
			}
		});

		toolkit.paintBordersFor(container);
	}
}

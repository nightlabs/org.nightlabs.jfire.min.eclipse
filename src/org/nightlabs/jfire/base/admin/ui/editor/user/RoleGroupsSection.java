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
import java.util.Locale;

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
import org.eclipse.swt.widgets.Label;
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
import org.nightlabs.jfire.security.RoleGroup;

/**
 * The section containing the role groups controls.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleGroupsSection extends RestorableSectionPart
{
	/**
	 * The editor model.
	 */
	RoleGroupSecurityPreferencesModel model;

	/**
	 * The viewer for included role groups.
	 */
	TableViewer includedRoleGroupsViewer;

	/**
	 * The viewer for excluded role groups.
	 */
	TableViewer excludedRoleGroupsViewer;

	/**
	 * Create an instance of RoleGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public RoleGroupsSection(FormPage page, Composite parent)
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
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);
		
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);

		ViewerComparator roleGroupComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((RoleGroup)e1).getName().getText().compareTo(((RoleGroup)e2).getName().getText());
			}
		};
		
		Label l = toolkit.createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.notAssigned")); //$NON-NLS-1$
		l = toolkit.createLabel(container, ""); //$NON-NLS-1$
		l = toolkit.createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.assigned")); //$NON-NLS-1$

		section.setExpanded(true);
		
		excludedRoleGroupsViewer = new TableViewer(createRoleGroupsTable(toolkit, container));
		excludedRoleGroupsViewer.setContentProvider(new RoleGroupsContentProvider());
		excludedRoleGroupsViewer.setLabelProvider(new RoleGroupsLabelProvider());
		excludedRoleGroupsViewer.setComparator(roleGroupComparator);

		createRoleGroupButtons(container, toolkit);

		includedRoleGroupsViewer = new TableViewer(createRoleGroupsTable(toolkit, container));
		includedRoleGroupsViewer.setContentProvider(new RoleGroupsContentProvider());
		includedRoleGroupsViewer.setLabelProvider(new RoleGroupsLabelProvider());
		includedRoleGroupsViewer.setComparator(roleGroupComparator);
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.description"), false, false); //$NON-NLS-1$
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
	
	public void setModel(final RoleGroupSecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (excludedRoleGroupsViewer == null)
					return;

				if (excludedRoleGroupsViewer.getTable().isDisposed())
					return;

				excludedRoleGroupsViewer.setInput(model.getExcludedRoleGroups());
				includedRoleGroupsViewer.setInput(model.getIncludedRoleGroups());
			}
		});
	}
	
	/**
	 * Content provider for role groups.
	 */
	private final class RoleGroupsContentProvider extends TableContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement)
		{
			Collection<RoleGroup> roleGroups = (Collection<RoleGroup>)inputElement;
			return roleGroups.toArray();
		}
	}

	/**
	 * Label provider for role groups. 
	 */
	private static class RoleGroupsLabelProvider extends TableLabelProvider
	{
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if(columnIndex == 0)
				return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), RoleGroupsLabelProvider.class);
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
			if(!(element instanceof RoleGroup))
				throw new RuntimeException("Invalid object type, expected RoleGroup"); //$NON-NLS-1$
			RoleGroup g = (RoleGroup)element;
			switch(columnIndex) {
			case 0: return g.getName().getText(Locale.getDefault().getLanguage());
			case 1: return g.getDescription().getText(Locale.getDefault().getLanguage());
			}
			return null;
		}
	}

	private Table createRoleGroupsTable(FormToolkit toolkit, Composite container)
	{
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		fTable.setLayoutData(gd);
		TableColumn col1 = new TableColumn(fTable, SWT.NULL);
		col1.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.roleGroup")); //$NON-NLS-1$
		TableColumn col2 = new TableColumn(fTable, SWT.NULL);
		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.description")); //$NON-NLS-1$
		TableLayout tlayout = new TableLayout();
		tlayout.addColumnData(new ColumnWeightData(30, 30));
		tlayout.addColumnData(new ColumnWeightData(70, 70));
		fTable.setLayout(tlayout);
		fTable.setHeaderVisible(true);
		toolkit.paintBordersFor(fTable);
		//createContextMenu(fTable);
		return fTable;
	}

	private void roleGroupsAdd()
	{
		ISelection s = excludedRoleGroupsViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getExcludedRoleGroups().removeAll(l);
		excludedRoleGroupsViewer.remove(a);
		model.getIncludedRoleGroups().addAll(l);
		includedRoleGroupsViewer.add(a);
		includedRoleGroupsViewer.setSelection(selection);
		includedRoleGroupsViewer.reveal(l.get(0));
		markDirty();
	}

	private void roleGroupsRemove()
	{
		ISelection s = includedRoleGroupsViewer.getSelection();
		if(s.isEmpty())
			return;
		IStructuredSelection selection = (IStructuredSelection)s;
		Object[] a = selection.toArray();
		List l = selection.toList();
		model.getIncludedRoleGroups().removeAll(l);
		includedRoleGroupsViewer.remove(a);
		model.getExcludedRoleGroups().addAll(l);
		excludedRoleGroupsViewer.add(a);
		excludedRoleGroupsViewer.setSelection(selection);
		excludedRoleGroupsViewer.reveal(l.get(0));
		markDirty();
	}

	private void createRoleGroupButtons(Composite client, FormToolkit toolkit)
	{
		Composite container = toolkit.createComposite(client);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		Button fAddButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.toRight"), SWT.PUSH); //$NON-NLS-1$
		fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roleGroupsAdd();
			}
		});
		//fAddButton.setEnabled(isEditable());


		Button fRemoveButton = toolkit.createButton(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.toLeft"), SWT.PUSH); //$NON-NLS-1$
		fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roleGroupsRemove();
			}
		});

		toolkit.paintBordersFor(container);
	}

}

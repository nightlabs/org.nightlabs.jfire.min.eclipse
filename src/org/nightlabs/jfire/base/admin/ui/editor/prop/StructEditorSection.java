package org.nightlabs.jfire.base.admin.ui.editor.prop;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.ui.prop.structedit.StructureChangedListener;
import org.nightlabs.jfire.base.ui.prop.structedit.action.AddStructBlockAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.AddStructFieldAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.RemoveStructElementAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann marius[at]NightLabs[dot]de
 */
public class StructEditorSection 
extends ToolBarSectionPart
implements StructureChangedListener
{
	public StructEditorSection(IFormPage page, Composite parent) 
	{
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE,
					Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructEditorSection.title")); //$NON-NLS-1$

		structEditor = new StructEditor();

		structEditor.createComposite(getContainer(), SWT.NONE, false);
		structEditor.addStructureChangedListener(this);
		
		List<ContributionItem> actionList = new LinkedList<ContributionItem>();
		actionList.add( new ActionContributionItem(new MoveStructElementAction(structEditor, true, this)) );		
		actionList.add( new ActionContributionItem(new MoveStructElementAction(structEditor, false, this)) );
		actionList.add( new Separator() );
		actionList.add( new ActionContributionItem(new AddStructBlockAction(structEditor)) );
		actionList.add( new ActionContributionItem(new AddStructFieldAction(structEditor)) );
		actionList.add( new ActionContributionItem(new RemoveStructElementAction(structEditor)) );

		ToolBarManager toolBarManager = getToolBarManager();
		final MenuManager menuManager = new MenuManager("Actions"); //$NON-NLS-1$
		addActionsToContributionManager(toolBarManager, actionList);

		TreeViewer structTreeViewer = structEditor.getStructTree().getTreeViewer();
		menuManager.createContextMenu(structTreeViewer.getControl());
		addActionsToContributionManager(menuManager, actionList);
		
		updateToolBarManager();
		menuManager.update(true);
		
		Menu popupMenu = menuManager.getMenu();
		structTreeViewer.getTree().setMenu(popupMenu);
		// TODO if this identifier for registerContextMenu is really meaningful, it should be a constant! And it should be documented! Marco.
		page.getEditorSite().registerContextMenu("StructEditorPage.PropertyActions", menuManager, structTreeViewer); //$NON-NLS-1$
//		((EntityEditorPageWithProgress)page).setMenu(popupMenu);
	}
	
	private void addActionsToContributionManager(IContributionManager contributionManager, List<ContributionItem> actionList) 
	{
		if (actionList == null)
			return;
		
		for (ContributionItem item : actionList) {
			if (item instanceof ActionContributionItem)
//			 add only action, so that the manager wraps it in a new ActionContributionItem.
//			 otherwise this item is used in the toolbar creating Toolbaritem, which is not usable in the MenuManager
				contributionManager.add( ((ActionContributionItem) item).getAction() );  
			else
				contributionManager.add( item );
		}
	}
	
	private StructEditor structEditor;
	
	public StructEditor getStructEditor() {
		return structEditor;
	}
	
	public void structureChanged() {
		markDirty();
	}
	
}

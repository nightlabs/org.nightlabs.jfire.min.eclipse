package org.nightlabs.jfire.base.ui.querystore;

import java.util.Collection;
import java.util.Locale;

import javax.jdo.FetchPlan;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.dialog.CenteredTitleDialog;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * 
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class SaveQueryStoreDialog extends CenteredTitleDialog
{
	private BaseQueryStore<?, ?> selectedQueryConfiguration;
	private BaseQueryStoreInActiveTableComposite storeTable; 
	private Collection<BaseQueryStore<?, ?>> existingQueries;
	private boolean errorMsgSet = false;
	
	private static final int CREATE_NEW_QUERY = IDialogConstants.CLIENT_ID + 1;
	
	public SaveQueryStoreDialog(Shell parentShell, Collection<BaseQueryStore<?, ?>> existingQueries)
	{
		super(parentShell);
		assert existingQueries != null;
		this.existingQueries = existingQueries;
	}
	
	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}
	
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Query Saving");
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		storeTable = new BaseQueryStoreInActiveTableComposite(wrapper, SWT.NONE, true,
			AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
		storeTable.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				okPressed();
			}
			
		});
		storeTable.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (errorMsgSet)
					setErrorMessage(null);
			}
		});
		storeTable.setInput(existingQueries);
		
		setTitle("Save Query");
		setMessage("Create a new Query name to store the current query with or select an existing one.");
		return wrapper;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, CREATE_NEW_QUERY, "&Create new query", false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,	false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}
	
	@Override
	protected void buttonPressed(int buttonId)
	{
		super.buttonPressed(buttonId);
		if (CREATE_NEW_QUERY == buttonId)
		{
			BaseQueryStore<?, ?> createdStore = createNewQueryStore();
			if (createdStore == null)
				return;
			
			okPressed();
		}
	}
	
	private BaseQueryStore<?, ?> createNewQueryStore()
	{
		final User owner = UserDAO.sharedInstance().getUser(
			SecurityReflector.getUserDescriptor().getUserObjectID(),
			new String[] { FetchPlan.DEFAULT, User.FETCH_GROUP_NAME }, 
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
			);
		
		final BaseQueryStore<?, ?> queryStore = new BaseQueryStore(owner, 
			IDGenerator.nextID(BaseQueryStore.class), null);
		
		queryStore.getName().setText(Locale.getDefault().getLanguage(), "change this name");
		
		existingQueries.add(queryStore);
		storeTable.setInput(existingQueries);
		storeTable.setSelection(new StructuredSelection(queryStore));
		return queryStore;
	}

	private boolean checkForRights(BaseQueryStore<?, ?> selectedStore)
	{
		if (! selectedStore.getOwnerID().equals(SecurityReflector.getUserDescriptor().getUserObjectID()) )
		{
			errorMsgSet = true;
			setErrorMessage("You cannot change or override the selected query since you are " +
			"not the owner!");
			return false;
		}
		return true;
	}
	
	private boolean changeName(BaseQueryStore<?, ?> selectedStore)
	{
		final QueryStoreEditDialog inputDialog = new QueryStoreEditDialog(
			getShell(), selectedStore);
		
		if (inputDialog.open() != Window.OK)
			return false;
		
		selectedStore.setPubliclyAvailable(inputDialog.isPubliclyAvailable());
		return true;
	}
	
	@Override
	protected void okPressed()
	{
		selectedQueryConfiguration = storeTable.getFirstSelectedElement();
		if (! checkForRights(selectedQueryConfiguration))
			return;
		
		if (! changeName(selectedQueryConfiguration))
			return;
		
		super.okPressed();
	}
	
	public BaseQueryStore<?, ?> getSelectedQueryStore()
	{
		return selectedQueryConfiguration;
	}
}
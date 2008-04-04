package org.nightlabs.jfire.base.ui.querystore;

import java.util.Collection;
import java.util.Locale;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.action.WorkbenchPartAction;
import org.nightlabs.base.ui.dialog.CenteredTitleDialog;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.overview.OverviewEntryEditor;
import org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class SaveQueryCollectionAction
	extends WorkbenchPartAction
{
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(SaveQueryCollectionAction.class);
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	@Override
	public boolean calculateEnabled()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	@Override
	public boolean calculateVisible()
	{
		return true;
	}

	private static final String[] FETCH_GROUPS_QUERYSTORES = new String[] {
		FetchPlan.DEFAULT, BaseQueryStore.FETCH_GROUP_OWNER  
	};

	@Override
	public void run()
	{
		if (! (getActivePart() instanceof OverviewEntryEditor))
		{
			logger.warn("The load QueryCollection action is called from outside an OverviewEntryEditor." +
					"This is not intended! ActivePart=" + getActivePart().getClass().getName(),
					new Exception()
					);
			return;
		}

		final OverviewEntryEditor editor = (OverviewEntryEditor) getActivePart();
		
		if (! (editor.getEntryViewer() instanceof SearchEntryViewer))
		{
			logger.error("This Action will only work with subclasses of SearchEntryViewer, since they" +
					"know what kind of objects their existingQueries will return!", new Exception());
			
			return;
		}
		
		final SearchEntryViewer<?, ?> viewer = (SearchEntryViewer<?, ?>) editor.getEntryViewer();
		
		final Class<?> resultType = viewer.getResultType();
		
		Job fetchStoredQueries = new Job("Fetching stored Query configurations...")
		{
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				final Collection<BaseQueryStore<?, ?>> storedQueryCollections = 
					QueryStoreDAO.sharedInstance().getQueryStoresByReturnType(
						resultType, FETCH_GROUPS_QUERYSTORES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				
				viewer.getComposite().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						chooseQueryCollection(storedQueryCollections, viewer);
					}
				});
				return Status.OK_STATUS;
			}
		};
		fetchStoredQueries.setUser(true);
		fetchStoredQueries.schedule();
	}
	
	protected void chooseQueryCollection(Collection<BaseQueryStore<?, ?>> storedQueries,
		SearchEntryViewer<?, ?> viewer)
	{
		SaveQueryStoreDialog dialog = new SaveQueryStoreDialog(viewer.getComposite().getShell(), storedQueries);
		
		if (dialog.open() != Window.OK)
			return;
		
		final BaseQueryStore queryToSave = dialog.getSelectedQueryStore();
		queryToSave.setQueryCollection(viewer.getManagedQueries());
		
		Job saveQueryJob = new Job("Saving Query:" + queryToSave.getName().getText())
		{
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				QueryStoreDAO.sharedInstance().storeQueryStore(
					queryToSave, FETCH_GROUPS_QUERYSTORES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, false, monitor);
				
				return Status.OK_STATUS;
			}
		};
		saveQueryJob.schedule();
	}
	
	public static class SaveQueryStoreDialog extends CenteredTitleDialog
	{
		private BaseQueryStore<?, ?> selectedQueryConfiguration;
		private BaseQueryStoreTableComposite storeTable; 
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
		protected void configureShell(Shell newShell)
		{
			super.configureShell(newShell);
			newShell.setText("Query Saving");
		}
		
		@Override
		protected Control createDialogArea(Composite parent)
		{
			Composite wrapper = (Composite) super.createDialogArea(parent);
			storeTable = new BaseQueryStoreTableComposite(wrapper, AbstractTableComposite.DEFAULT_STYLE_MULTI_BORDER);
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
			createButton(parent, CREATE_NEW_QUERY, "Create new query name", false);
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
//				storeTable.setSelection(new StructuredSelection(createdStore));
				
//				selectedQueryConfiguration = createdStore;
				
//				 return code is already OK
//				close();
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
			
//			// if the user changed the name -> new store is valid
//			if (changeName(queryStore))
//				return queryStore;
			
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
			final InputDialog inputDialog = new InputDialog(getShell(), "Change query name?", 
				"Enter new query name.", selectedStore.getName().getText(), null)
			{
				@Override
				protected Control createDialogArea(Composite parent)
				{
					// TODO create an I18NText editor instead!
					return super.createDialogArea(parent);
				}
			};
			
			if (inputDialog.open() != Window.OK)
				return false;
			
			if (! selectedStore.getName().getText().equals(inputDialog.getValue()))
			{
				selectedStore.getName().setText(Locale.getDefault().getLanguage(), inputDialog.getValue());						
				return true; 
			}
			return false;
		}
		
		@Override
		protected void okPressed()
		{
			selectedQueryConfiguration = storeTable.getFirstSelectedElement();
			if (! checkForRights(selectedQueryConfiguration))
				return;
			
			changeName(selectedQueryConfiguration);
			super.okPressed();
		}
		
		public BaseQueryStore<?, ?> getSelectedQueryStore()
		{
			return selectedQueryConfiguration;
		}
	}

}

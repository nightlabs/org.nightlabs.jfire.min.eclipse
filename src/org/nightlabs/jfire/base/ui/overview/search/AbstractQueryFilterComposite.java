package org.nightlabs.jfire.base.ui.overview.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryEvent;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * Abstract base class for a Composite which returns {@link AbstractJDOQuery}s for searching. It
 * uses {@link JDOQueryComposite}s which are displayed in a {@link Section}.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryFilterComposite<R, Q extends AbstractSearchQuery<R>>
	extends XComposite
	implements QueryFilter<R, Q>
{
	/**
	 * The active state manager that will handle the presentation of my active state. 
	 */
	private ActiveStateManager sectionButtonActiveStateManager;

	/**
	 * Flag indicating that a <code>null</code> value is being set to the query.
	 * This needs to be done so the {@link #updateUI(QueryEvent)} method can distinguish an 
	 * intentionally set <code>null</code> value from deactivation (nulling the query field) of a
	 * query aspect.
	 */
	protected boolean initialValue;

	/**
	 * Creates a new {@link AbstractQueryFilterComposite}.
	 * <p><b>Note</b>: The caller has to call {@link #createComposite(Composite)} to create the UI! <br />
	 * 	This is not done in this constructor to omit problems with fields that are not only declared,
	 * 	but also initialised. If these fields are used inside {@link #createComposite(Composite)}
	 * 	or new values are assigned to them, one of the following two things may happen:
	 *  <ul>
	 *  	<li>The value assigned to that field is overridden by the initialisation value that is
	 *  			assigned after this constructor is finished</li>
	 *  	<li>The referenced value is not yet properly initialised, because the initialisation is
	 *  			done after the constructor finishes, and hence results in an unexpected exception.</li>
	 *  </ul>
	 * </p>
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param layoutMode
	 *          The layout mode to use. See {@link XComposite.LayoutMode}.
	 * @param layoutDataMode
	 *          The layout data mode to use. See {@link XComposite.LayoutDataMode}.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractQueryFilterComposite(Composite parent, int style, LayoutMode layoutMode,
		LayoutDataMode layoutDataMode, QueryProvider<R, ? super Q> queryProvider)
	{

		super(parent, style, layoutMode, layoutDataMode);
		this.queryProvider = queryProvider;
		if (queryProvider != null)
		{
			queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
		}
		// remove listener from QueryProvider on disposal
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				if (getQueryProvider() != null)
				{
					getQueryProvider().removeModifyListener(getQueryClass(), queryChangeListener);
				}
			}
		});
	}

	/**
	 * Creates a new {@link AbstractQueryFilterComposite} with default layout mode and layout data
	 * mode. Delegate to
	 * {@link AbstractQueryFilterComposite#AbstractQueryFilterComposite(Composite, int, LayoutMode, LayoutDataMode, QueryProvider)}.
	 * 
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractQueryFilterComposite(Composite parent, int style,
		QueryProvider<R, ? super Q> queryProvider)
	{
		this(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA, queryProvider);
	}

	/**
	 * Sets the active state of the given <code>button</code> to <code>active</code> if necessary and
	 * propagates the changes to the ActiveStateManager assigned to the active button in the section
	 * this composite is contained in.
	 *
	 * @param button
	 * 					The button whose active state might have changed, and shall be set to
	 * 					<code>active</code>
	 * @param active
	 *          whether one of the filters contained in has gone <code>active</code>.
	 */
	protected void setSearchSectionActive(Button button, boolean active)
	{
		if (button.getSelection() == active)
			return;
		
		button.setSelection(active);
		getSectionButtonActiveStateManager().setActive(active);
	}

	protected void setSearchSectionActive(boolean active)
	{
		getSectionButtonActiveStateManager().setActive(active);
	}
	
	protected void resetSearchSectionActiveState()
	{
		while (sectionButtonActiveStateManager.isActive())
		{
			sectionButtonActiveStateManager.setActive(false);
		}
	}
	
	/**
	 * This is has to be called by the constructor of the subclass.
	 * 
	 * @param parent
	 *          The parent to use.
	 */
	protected abstract void createComposite(Composite parent);

	@Override
	public void setActive(boolean active)
	{
		if (active)
		{
			resetSearchQueryValues(getQuery());
		}
		else
		{
			unsetSearchQueryValues(getQuery());
		}
	}

	protected abstract void unsetSearchQueryValues(Q query);

	protected abstract void resetSearchQueryValues(Q query);

	/**
	 * Returns the {@link Class} of the type of object which should queried.
	 * 
	 * @return the {@link Class} of the type of object which should queried.
	 */
	public abstract Class<Q> getQueryClass();

	private QueryProvider<R, ? super Q> queryProvider;
	private PropertyChangeListener queryChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			updateUI((QueryEvent) evt);
		}
	};

	protected abstract void updateUI(QueryEvent event);

	/**
	 * @return the Query needed by this query filter and hence all JDOQueryComposites as well.
	 */
	public Q getQuery()
	{
		assert queryProvider != null : "No query provider has been set before!";
		return queryProvider.getQueryOfType(getQueryClass());
	}

	/**
	 * @param queryProvider
	 *          the queryProvider to set that must not be <code>null</code>!
	 */
	@Override
	public void setQueryProvider(QueryProvider<R, Q> queryProvider)
	{
		assert queryProvider != null;
		if (this.queryProvider != null)
		{
			this.queryProvider.removeModifyListener(getQueryClass(), queryChangeListener);
		}
		this.queryProvider = queryProvider;
		this.queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
	}

	/**
	 * Returns the query provider
	 * 
	 * @return the query provider
	 */
	public QueryProvider<R, ? super Q> getQueryProvider()
	{
		return queryProvider;
	}

	/**
	 * @return the sectionButtonActiveStateManager
	 */
	public ActiveStateManager getSectionButtonActiveStateManager()
	{
		return sectionButtonActiveStateManager;
	}

	/**
	 * @param sectionButtonActiveStateManager
	 *          the sectionButtonActiveStateManager to set
	 */
	public void setSectionButtonActiveStateManager(ActiveStateManager sectionButtonActiveStateManager)
	{
		assert sectionButtonActiveStateManager != null;
		this.sectionButtonActiveStateManager = sectionButtonActiveStateManager;
	}
	
	protected void notifyActiveStateChangeListener(Button button)
	{
		final Event event = new Event();
		event.item = event.widget = button;
		event.display = button.getDisplay();
		event.type = SWT.Selection;
		button.notifyListeners(SWT.Selection, event);
	}
	
	/**
	 * Helper class that tries to set the ActiveStateManager if it wasn't available at creation time.
	 * Needed since my ActiveStateManager will be set after construction. 
	 * 
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
	protected abstract class ButtonSelectionListener 
		extends ButtonSelectionStateAdapter
	{
		public ButtonSelectionListener()
		{
			super(getSectionButtonActiveStateManager());
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (activeStateManager == null)
			{
				activeStateManager = getSectionButtonActiveStateManager();				
			}
			
			super.widgetSelected(e);
		}
	}
}

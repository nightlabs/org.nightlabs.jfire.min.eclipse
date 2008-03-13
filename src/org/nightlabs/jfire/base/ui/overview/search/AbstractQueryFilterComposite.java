package org.nightlabs.jfire.base.ui.overview.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryEvent;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.search.JDOQueryComposite;

/**
 * Abstract base class for a Composite which returns {@link AbstractJDOQuery}s for searching.
 * It uses {@link JDOQueryComposite}s which are displayed in a {@link Section}.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryFilterComposite<R, Q extends AbstractSearchQuery<R>>
	extends XComposite
	implements QueryFilter<R, Q>
{
	/**
	 * Creates a new {@link AbstractQueryFilterComposite}.
	 * <p>
	 * Note that subclasses are responsible for defining the layout.
	 * See {@link #createContents()}.
	 * </p>
	 * @param parent The parent to use.
	 * @param style The style to apply.
	 * @param layoutMode The layout mode to use.
	 * @param layoutDataMode The layout data mode to use.
	 */
	public AbstractQueryFilterComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode, QueryProvider<R, ? super Q> queryProvider)
	{
		super(parent, style, layoutMode, layoutDataMode);
		this.queryProvider = queryProvider;
		if (queryProvider != null)
		{
			queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
		}
		createComposite(this);
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
	 * Creates a new {@link AbstractQueryFilterComposite}
	 * with default layout mode and layout data mode.
	 * <p>
	 * Note that subclasses are responsible for defining the layout.
	 * See {@link #createContents()}.
	 * </p>
	 * @param parent The parent to use.
	 * @param style The style to apply.
	 */
	public AbstractQueryFilterComposite(Composite parent, int style, QueryProvider<R, ? super Q> queryProvider)
	{
		super(parent, style);
		this.queryProvider = queryProvider;
		createComposite(this);
		
		queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
		
		// remove QueryChangeListener
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				getQueryProvider().removeModifyListener(queryChangeListener);
			}
		});
	}

	private List<JDOQueryComposite<R, Q>> queryComposites;
	
	/**
	 * This is called by the constructor and itself will call
	 * {@link #createContents()} and {@link #registerJDOQueryComposites()}.
	 * 
	 * @param parent The parent to use.
	 */
	protected void createComposite(Composite parent) {
		createContents();
		queryComposites = registerJDOQueryComposites();
	}
	
	@Override
	public void setActive(boolean active)
	{
		assert queryComposites != null;
		for (JDOQueryComposite<R, Q> queryComposite : queryComposites)
		{
			queryComposite.setActive(active);
		}
	}
	
	/**
	 * Returns the List of {@link JDOQueryComposite}s which should be displayed.
	 * This should match the clients of the Sections that are created in {@link #createContents()}.
	 * 
	 * @param queryProvider the query Provider that will create the query type needed  
	 * @return The List of {@link JDOQueryComposite}s which should be displayed.
	 */
	protected abstract List<JDOQueryComposite<R, Q>> registerJDOQueryComposites();
	
	/**
	 * Returns the {@link Class} of the type of object which should queried.
	 * @return the {@link Class} of the type of object which should queried.
	 */
	public abstract Class<Q> getQueryClass();
	
	/**
	 * Creates the contents, usually the same Composites like
	 * returned in {@link #registerJDOQueryComposites()}.
	 * Here these Composites have to be placed in the layout.
	 */
	protected abstract void createContents();

	private QueryProvider<R, ? super Q> queryProvider;
	private PropertyChangeListener queryChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			for (JDOQueryComposite<R, Q> queryComposite : queryComposites)
			{
				queryComposite.updateUI( (QueryEvent) evt );
			}
		}
	};
	
	/**
	 * @return the Query needed by this query filter and hence all JDOQueryComposites as well.
	 */
	public Q getQuery()
	{
		assert queryProvider != null : "No query provider has been set before!";
		return queryProvider.getQueryOfType(getQueryClass());
	}

	/**
	 * @param queryProvider the queryProvider to set
	 */
	@Override
	public void setQueryProvider(QueryProvider<R, Q> queryProvider)
	{
		assert queryProvider != null;
		this.queryProvider.removeModifyListener(getQueryClass(), queryChangeListener);
		this.queryProvider = queryProvider;
		this.queryProvider.addModifyListener(getQueryClass(), queryChangeListener);
	}
	
	/**
	 * Returns the query provider
	 * @return the query provider
	 */
	public QueryProvider<R, ? super Q> getQueryProvider()
	{
		return queryProvider;
	}
}

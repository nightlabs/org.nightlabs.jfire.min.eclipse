package org.nightlabs.jfire.base.ui.overview.search;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
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
		createComposite(this);
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
	}

	private List<JDOQueryComposite<R, Q>> queryComposites;
//	private Map<Button, Section> button2Section;
//	private Map<Button, JDOQueryComposite> button2Composite = null;
	// TODO: What was that for?
//	private List<QuickSearchEntry> quickSearchEntries = null;
//
//	public List<QuickSearchEntry> getQuickSearchEntryTypes() {
//		return quickSearchEntries;
//	}
//	/**
//	 * TODO: remove!
//	 * @return The mapping of active-buttons to sections.
//	 */
//	protected Map<Button, Section> getButton2Section() {
//		if (button2Section == null)
//			button2Section = new HashMap<Button, Section>();
//		return button2Section;
//	}
//	/**
//	 * TODO: remove!
//	 * @return The mapping of active-button to {@link JDOQueryComposite}.
//	 */
//	protected Map<Button, JDOQueryComposite> getButton2QueryComposite() {
//		if (button2Composite == null)
//			button2Composite = new HashMap<Button, JDOQueryComposite>();
//		return button2Composite;
//	}
	
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
	
//  TODO: What was that for?
//	/**
//	 * This method can be used wihtin {@link #createContents(Composite)}
//	 * to configure a section for a given JDOQueryComposite.
//	 * It will create an active-button for the section and register it
//	 * in the mapping.
//	 */
//	protected void configureSection(Section section, JDOQueryComposite comp)
//	{
//		Button activeButton = new Button(section, SWT.CHECK);
//		activeButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
//		activeButton.setSelection(comp.isActive());
//		activeButton.addSelectionListener(new SelectionListener(){
//			public void widgetSelected(SelectionEvent e) {
//				Button b = (Button) e.getSource();
//				JDOQueryComposite comp = getButton2QueryComposite().get(b);
//				if (comp != null)
//					comp.setActive(b.getSelection());
//				Section section = getButton2Section().get(b);
//				if (section != null)
//					section.setExpanded(b.getSelection());
//			}
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		section.setTextClient(activeButton);
//		getButton2QueryComposite().put(activeButton, comp);
//		getButton2Section().put(activeButton, section);
//	}
//	/**
//	 * @return All active {@link AbstractJDOQuery}s obtained by the {@link JDOQueryComposite}.
//	 */
//	public List<AbstractJDOQuery<?>> getJDOQueries()
//	{
//		if (queryComposites != null)
//		{
//			List<AbstractJDOQuery<?>> queries = new ArrayList<AbstractJDOQuery<?>>(queryComposites.size());
//			for (JDOQueryComposite comp : queryComposites) {
//				if (comp.isActive())
//					queries.add(comp.getJDOQuery());
//			}
//			return queries;
//		}
//		return null;
//	}
		
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
	protected abstract Class<Q> getQueryClass();
	
	/**
	 * Creates the contents, usually the same Composites like
	 * returned in {@link #registerJDOQueryComposites()}.
	 * Here these Composites have to be placed in the layout.
	 */
	protected abstract void createContents();

	private QueryProvider<R, ? super Q> queryProvider;
	
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
		this.queryProvider = queryProvider;
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

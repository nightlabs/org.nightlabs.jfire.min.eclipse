package org.nightlabs.jfire.base.ui.prop.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutComposite;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.util.CollectionUtil;

/**
 * Composite that takes a List of {@link StructFieldSearchEditLayoutEntry}s and visualizes them
 * using the {@link IStructFieldSearchFilterItemEditor}s found for the struct-fields of each entry
 * in the {@link StructFieldSearchFilterEditorRegistry}. The
 * {@link IStructFieldSearchFilterItemEditor}s are searched using the set of struct-fields of each
 * entry, so there has to be an editor registered that can handle all types of fields in that list.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public class PropertySetSearchEditLayoutComposite extends AbstractEditLayoutComposite<StructFieldSearchEditLayoutEntry> {
	
	private static final Logger logger = Logger.getLogger(PropertySetSearchEditLayoutComposite.class);
	
	private List<IStructFieldSearchFilterItemEditor> searchFilterItemEditors;
	private StructFieldSearchEditLayoutEntry defaultEntry;
	private String quickSearchText;

	/**
	 * Create a new {@link PropertySetSearchEditLayoutComposite}.
	 * 
	 * @param parent The parent to add the composite to.
	 * @param style The style to apply to the composite.
	 * @param gridLayout The {@link GridLayout} whose SWT-representation should be applied to the
	 *            composite.
	 * @param editLayoutEntries The entries whose controls should be placed inside this composite.
	 * @param defaultEntry The default entry (which will be applied the quickSearchText)
	 * @param quickSearchText An optional text that will be set for the default entry and trigger an
	 *            early search.
	 */
	public PropertySetSearchEditLayoutComposite(Composite parent, int style, GridLayout gridLayout,
			List<StructFieldSearchEditLayoutEntry> editLayoutEntries, StructFieldSearchEditLayoutEntry defaultEntry, String quickSearchText) {
		super(parent, style, gridLayout, editLayoutEntries, false);
		this.defaultEntry = defaultEntry;
		this.quickSearchText = quickSearchText;
		
		createEntries();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses the {@link StructFieldSearchFilterEditorRegistry} to created
	 * {@link IStructFieldSearchFilterItemEditor}s that will createt the entry-controls.
	 * </p>
	 */
	@Override
	protected Control createEntryControl(StructFieldSearchEditLayoutEntry entry, Composite parent) {
//		IStructFieldSearchFilterItemEditor editor = StructFieldSearchFilterEditorRegistry.sharedInstance().createSearchFilterItemEditor(entry.getObject(), entry.getMatchType());
		if (entry.getObject() == null) {
			return new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		} else {
			Set<StructField<DataField>> set = CollectionUtil.castSet(entry.getObject());
			IStructFieldSearchFilterItemEditor editor = StructFieldSearchFilterEditorRegistry.sharedInstance().createSearchFilterItemEditor(set, entry.getMatchType());
			
			if (editor != null) {
				addSearchFilterItemEditor(editor);
				Control control = editor.createControl(parent, true);
				if (entry.equals(defaultEntry) && quickSearchText != null && !"".equals(quickSearchText)) {
					editor.setInput(quickSearchText);
				}
				
				return control;
			} else {
				logger.warn("No StructFieldSearchFilterEditor was found for the following entry:\n" + entry.toString());
				return null;
			}
		}
	}

	/**
	 * Registers an {@link IStructFieldSearchFilterItemEditor} used in this composite.
	 * 
	 * @param editor The editor to register.
	 */
	protected void addSearchFilterItemEditor(IStructFieldSearchFilterItemEditor editor) {
		if (searchFilterItemEditors == null) {
			searchFilterItemEditors = new LinkedList<IStructFieldSearchFilterItemEditor>();
		}
		
		searchFilterItemEditors.add(editor);
	}
	
	/**
	 * @return All {@link IStructFieldSearchFilterItemEditor}s used in this composite.
	 */
	public List<IStructFieldSearchFilterItemEditor> getSearchFilterItemEditors() {
		return searchFilterItemEditors;
	}
}

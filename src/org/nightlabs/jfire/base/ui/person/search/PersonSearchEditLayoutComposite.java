package org.nightlabs.jfire.base.ui.person.search;

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
import org.nightlabs.jfire.base.ui.prop.search.IStructFieldSearchFilterItemEditor;
import org.nightlabs.jfire.base.ui.prop.search.StructFieldSearchFilterEditorRegistry;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.util.CollectionUtil;

public class PersonSearchEditLayoutComposite extends AbstractEditLayoutComposite<StructFieldSearchEditLayoutEntry> {
	
	private static final Logger logger = Logger.getLogger(PersonSearchEditLayoutComposite.class);
	
	private List<IStructFieldSearchFilterItemEditor> searchFilterItemEditors;
	private StructFieldSearchEditLayoutEntry defaultEntry;
	private String quickSearchText;

	public PersonSearchEditLayoutComposite(Composite parent, int style, GridLayout gridLayout, List<StructFieldSearchEditLayoutEntry> editLayoutEntries, StructFieldSearchEditLayoutEntry defaultEntry, String quickSearchText) {
		super(parent, style, gridLayout, editLayoutEntries, false);
		this.defaultEntry = defaultEntry;
		this.quickSearchText = quickSearchText;
		
		createEntries();
	}

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
	
	protected void addSearchFilterItemEditor(IStructFieldSearchFilterItemEditor editor) {
		if (searchFilterItemEditors == null) {
			searchFilterItemEditors = new LinkedList<IStructFieldSearchFilterItemEditor>();
		}
		
		searchFilterItemEditors.add(editor);
	}
	
	public List<IStructFieldSearchFilterItemEditor> getSearchFilterItemEditors() {
		return searchFilterItemEditors;
	}
}

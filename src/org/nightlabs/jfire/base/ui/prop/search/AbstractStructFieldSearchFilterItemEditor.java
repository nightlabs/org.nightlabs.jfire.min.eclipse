package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.ComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

public abstract class AbstractStructFieldSearchFilterItemEditor<SF extends StructField<?>> implements IStructFieldSearchFilterItemEditor {

	private Collection<SF> structFields;
	private MatchType matchType;
	private ComboComposite<MatchType> matchTypeCombo;
	private boolean showTitle;
	
	public AbstractStructFieldSearchFilterItemEditor(Collection<SF> structFields, MatchType matchType) {
		this.structFields = structFields;
		
		if (matchType != null && !getSupportedMatchTypes().contains(matchType))
			throw new IllegalArgumentException("This Editor does not support the given MatchType.");
		
		this.matchType = matchType;
	}
	
//	public AbstractStructFieldSearchFilterItemEditor(Collection<SF> structFields) {
//		this(structFields, null);
//	}
//
//	public AbstractStructFieldSearchFilterItemEditor(SF structField, MatchType matchType) {
//		this(Collections.singleton(structField), matchType);
//	}
//
//	public AbstractStructFieldSearchFilterItemEditor(SF structField) {
//		this(structField, null);
//	}
	
	@Override
	public Control createControl(Composite par, boolean showTitle) {
		Composite parent = par;
		if (showTitle) {
			parent = new XComposite(par, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
			Label titleLabel = new Label(parent, SWT.NONE);
			titleLabel.setText(getFirstStructField().getName().getText());
		}
		
		// No MatchType specified in constructor, thus create combo to select it
		if (matchType == null) {
			XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
			matchTypeCombo = new ComboComposite<MatchType>(wrapper, SWT.NONE);
			matchTypeCombo.setInput(getSupportedMatchTypes());
			matchTypeCombo.selectElement(getDefaultMatchType());
			
			createEditControl(wrapper);
			return wrapper;
		} else {
			return createEditControl(parent);
		}
	}
	
	/**
	 * Creates the control to edit the search value for the {@link StructField} of this editor.
	 * @param parent The parent of the control.
	 * @return the control to edit the search value.
	 */
	protected abstract Control createEditControl(Composite parent);
	
	/**
	 * Returns the {@link MatchType} that should be selected by default in the {@link MatchType} combo
	 * if no fixed {@link MatchType} is given in the constructor.
	 * @return the {@link MatchType} that should be selected by default in the {@link MatchType} combo
	 */
	protected abstract MatchType getDefaultMatchType();
	
	/**
	 * Returns the {@link MatchType} that has either been specified through the constructor
	 * or selected in the MatchType-combo box.
	 * 
	 * @return the selected {@link MatchType}.
	 */
	protected MatchType getMatchType() {
		if (matchType != null)
			return matchType;
		
		return matchTypeCombo.getSelectedElement();
	}
	
	protected SF getFirstStructField() {
		return structFields.iterator().next();
	}
	
	
	protected Collection<SF> getStructFields() {
		return Collections.unmodifiableCollection(structFields);
	}
	
	protected Collection<StructFieldID> getStructFieldIDs() {
		return NLJDOHelper.getObjectIDList(getStructFields());
	}
}

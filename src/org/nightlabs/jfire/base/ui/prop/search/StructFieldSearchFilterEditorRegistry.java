package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.datastructure.Pair;
import org.nightlabs.eclipse.extension.AbstractEPProcessor;
import org.nightlabs.eclipse.extension.EPProcessorException;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Registry that processes the
 * <code>org.nightlabs.jfire.base.ui.structFieldSearchFilterItemEditor</code> extension-point and
 * registers {@link IStructFieldSearchFilterItemEditorFactory}s. The registry however serves
 * {@link IStructFieldSearchFilterItemEditor}s created by the registered factories, see the methods
 * {@link #createSearchFilterItemEditor(Set, MatchType)} and
 * {@link #createSearchFilterItemEditor(StructField, MatchType)}.
 * 
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class StructFieldSearchFilterEditorRegistry
extends AbstractEPProcessor
{
	private static StructFieldSearchFilterEditorRegistry sharedInstance;
	
	private static final String EXTENSION_POINT_ID = JFireBasePlugin.class.getPackage().getName() + ".structFieldSearchFilterItemEditor"; //$NON-NLS-1$
	private static final String GENERAL_EXTENSION_POINT_ELEMENT_NAME = "structFieldSearchFilterItemEditor"; //$NON-NLS-1$
	private static final String SPECIALISED_EXTENSION_POINT_ELEMENT_NAME = "specialisedStructFieldSearchFilterItemEditor"; //$NON-NLS-1$
	
	private Map<Class<? extends StructField<?>>,Pair<IStructFieldSearchFilterItemEditorFactory,Integer>> generalStructFieldSearchItemEditors =
		new HashMap<Class<? extends StructField<?>>, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>>();
	
	private Map<StructFieldID, Pair<IStructFieldSearchFilterItemEditorFactory,Integer>> specialisedStructFieldSearchItemEditors =
		new HashMap<StructFieldID, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>>();
	
	protected StructFieldSearchFilterEditorRegistry() {
	}
	
	public static StructFieldSearchFilterEditorRegistry sharedInstance() {
		if (sharedInstance == null)	{
			synchronized(StructFieldSearchFilterEditorRegistry.class)	{
				if (sharedInstance == null)	{
					sharedInstance = new StructFieldSearchFilterEditorRegistry();
				}
			}
		}

		return sharedInstance;
	}

	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		try {
			if (element.getName().equals(GENERAL_EXTENSION_POINT_ELEMENT_NAME)) {
				
				Class<? extends StructField<?>> structFieldClass = (Class<? extends StructField<?>>) Class.forName(element.getAttribute("structFieldClass"));
				
				IStructFieldSearchFilterItemEditorFactory factory =
					(IStructFieldSearchFilterItemEditorFactory) element.createExecutableExtension("structFieldSearchFilterItemEditorFactory");
				
				int priority = Integer.parseInt(element.getAttribute("priority"));
				
				Pair<IStructFieldSearchFilterItemEditorFactory, Integer> pair =
					new Pair<IStructFieldSearchFilterItemEditorFactory, Integer>(factory, priority);

				// Only replace existing entry if the new priority is lower (and thus "better")
				Pair<IStructFieldSearchFilterItemEditorFactory, Integer> existingItem = generalStructFieldSearchItemEditors.get(structFieldClass);
				if (existingItem == null || priority < existingItem.getSecond()) // getSecond() returns priority
					generalStructFieldSearchItemEditors.put(structFieldClass, pair);
				
			} else if (element.getName().equals(SPECIALISED_EXTENSION_POINT_ELEMENT_NAME)) {
				
				StructFieldID structFieldID = (StructFieldID) ObjectIDUtil.createObjectID(element.getAttribute("structFieldID"));
				
				IStructFieldSearchFilterItemEditorFactory factory =
					(IStructFieldSearchFilterItemEditorFactory) element.createExecutableExtension("structFieldSearchFilterItemEditorFactory");
				
				int priority = Integer.parseInt(element.getAttribute("priority"));
				
				Pair<IStructFieldSearchFilterItemEditorFactory, Integer> pair =
					new Pair<IStructFieldSearchFilterItemEditorFactory, Integer>(factory, priority);
				
				// Only replace existing entry if the new priority is lower (and thus "better")
				Pair<IStructFieldSearchFilterItemEditorFactory, Integer> existingItem = specialisedStructFieldSearchItemEditors.get(structFieldID);
				if (existingItem == null || priority < existingItem.getSecond()) // getSecond() returns priority
					specialisedStructFieldSearchItemEditors.put(structFieldID, pair);
				
			}
		} catch (Exception e) {
			throw new EPProcessorException(e);
		}
	}
	
	/**
	 * @return The factories registered as 'structFieldSearchFilterItemEditor', i.e. the general ones.
	 */
	protected Map<Class<? extends StructField<?>>, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>> getGeneralStructFieldSearchItemEditors() {
		checkProcessing();
		return generalStructFieldSearchItemEditors;
	}
	
	/**
	 * @return The factories registered as 'specialisedStructFieldSearchFilterItemEditor' and are therefore registered to a special {@link StructFieldID}.
	 */
	protected Map<StructFieldID, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>> getSpecialisedStructFieldSearchItemEditors() {
		checkProcessing();
		return specialisedStructFieldSearchItemEditors;
	}

	/**
	 * Searches and creates an appropriate {@link IStructFieldSearchFilterItemEditor} for searches
	 * of the given structField. This method first searches editors registered as
	 * <code>specialisedStructFieldSearchFilterItemEditor</code>s for the exact
	 * {@link StructFieldID} of the given structField. If no specialized editor can be found a
	 * search in the general registrations based on the type of structField is started.
	 * 
	 * @param <T> The type of data-field of the struct-field.
	 * @param structField The {@link StructField} to search an search-editor for.
	 * @param matchType The match-type to create the editor for.
	 * @return A newly created {@link IStructFieldSearchFilterItemEditor} for the given
	 *         {@link StructField}, or <code>null</code> if no appropriate factory could be found.
	 */
	public <T extends DataField> IStructFieldSearchFilterItemEditor createSearchFilterItemEditor(StructField<T> structField, MatchType matchType) {
		StructFieldID structFieldID = structField.getStructFieldIDObj();
		
		Pair<IStructFieldSearchFilterItemEditorFactory, Integer> pair = getSpecialisedStructFieldSearchItemEditors().get(structFieldID);
				
		if (pair != null) {
			IStructFieldSearchFilterItemEditorFactory factory = pair.getFirst();
			if (factory != null) {
				return factory.createEditorInstance(Collections.singleton(structField), matchType);
			}
		}
		return createSearchFilterItemEditor(Collections.singleton(structField), matchType, false);
	}

	/**
	 * Searches and creates <b>one</b> appropriate {@link IStructFieldSearchFilterItemEditor} that
	 * applies for a combined search for the types of all given {@link StructField}s. If only one
	 * StructField is given also the <code>specialisedStructFieldSearchFilterItemEditor</code>
	 * registrations are searched, otherwise only the general ones.
	 * 
	 * @param <T> The type of data-field of the struct-field.
	 * @param structFields The {@link StructField}s to search an search-editor for.
	 * @param matchType The match-type to create the editor for.
	 * @return A newly created {@link IStructFieldSearchFilterItemEditor} for the given
	 *         {@link StructField}, or <code>null</code> if no appropriate factory could be found.
	 */
	public <T extends DataField> IStructFieldSearchFilterItemEditor createSearchFilterItemEditor(Set<StructField<T>> structFields,
			MatchType matchType) {
		return createSearchFilterItemEditor(structFields, matchType, true);
	}
	
	/**
	 * Used internally and does the work for {@link #createSearchFilterItemEditor(Set, MatchType)}.
	 * The switch doSpcialisedSearch is to avoid a stack-overflows when this is called from the specialised
	 * search in {@link #createSearchFilterItemEditor(StructField, MatchType)}.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends DataField> IStructFieldSearchFilterItemEditor createSearchFilterItemEditor(Set<StructField<T>> structFields,
			MatchType matchType, boolean doSpecialisedSearch) {
		
		if (structFields.isEmpty())
			throw new IllegalArgumentException("Parameter structFields must contain at least one element.");
		
		if (doSpecialisedSearch && structFields.size() == 1) {
			// if there was only one field passed we can also do the specialised search
			return createSearchFilterItemEditor(structFields.iterator().next(), matchType);
		}
		
		Class<? extends StructField<?>> structFieldClass = (Class<? extends StructField<?>>) structFields.iterator().next().getClass();
		
		for (StructField<?> structField : structFields) {
			if (!structField.getClass().equals(structFieldClass))
				throw new IllegalArgumentException("The given structFields must all have exactly the same class.");
		}
		
		Pair<IStructFieldSearchFilterItemEditorFactory, Integer> pair = getGeneralStructFieldSearchItemEditors().get(structFieldClass);
		if (pair != null) {
			IStructFieldSearchFilterItemEditorFactory factory = pair.getFirst();
			if (factory != null) {
				return factory.createEditorInstance(structFields, matchType);
			}
		}
		return null;
	}

	/**
	 * Checks if this registry has a registration that matches the given type of StructField.
	 * 
	 * @param structFieldClass The type of StructField to check.
	 * @return <code>true</code> if there is a regisration matching the given type,
	 *         <code>false</code> otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean hasEditor(Class<? extends StructField> structFieldClass) {
		return getGeneralStructFieldSearchItemEditors().get(structFieldClass) != null;
	}
}

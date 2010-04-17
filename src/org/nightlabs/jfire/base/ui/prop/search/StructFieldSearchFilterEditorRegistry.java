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
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
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
	
	protected Map<Class<? extends StructField<?>>, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>> getGeneralStructFieldSearchItemEditors() {
		checkProcessing();
		return generalStructFieldSearchItemEditors;
	}
	
	protected Map<StructFieldID, Pair<IStructFieldSearchFilterItemEditorFactory, Integer>> getSpecialisedStructFieldSearchItemEditors() {
		checkProcessing();
		return specialisedStructFieldSearchItemEditors;
	}
	
	public <T extends DataField> IStructFieldSearchFilterItemEditor createSearchFilterItemEditor(StructField<T> structField, MatchType matchType) {
		StructFieldID structFieldID = structField.getStructFieldIDObj();
		
		Pair<IStructFieldSearchFilterItemEditorFactory, Integer> pair = getSpecialisedStructFieldSearchItemEditors().get(structFieldID);
				
		if (pair != null) {
			IStructFieldSearchFilterItemEditorFactory factory = pair.getFirst();
			if (factory != null) {
				return factory.createEditorInstance(Collections.singleton(structField), matchType);
			}
		}
		return createSearchFilterItemEditor(Collections.singleton(structField), matchType);
	}
	
	public <T extends DataField> IStructFieldSearchFilterItemEditor createSearchFilterItemEditor(Set<StructField<T>> structFields, MatchType matchType) {
		if (structFields.isEmpty())
			throw new IllegalArgumentException("Parameter structFields must contain at least one element.");
		
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
	
	@SuppressWarnings("unchecked")
	public boolean hasEditor(Class<? extends StructField> structFieldClass) {
		return getGeneralStructFieldSearchItemEditors().get(structFieldClass) != null;
	}
}

/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockGroupEditor.Factory;

/**
 * Registry for the creation of {@link IDataBlockGroupEditor}s.
 * <p>
 * Note currently this will only create {@link IDataBlockGroupEditor}s based on a default factory set.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DataBlockGroupEditorFactoryRegistry {

	/** Default factory, defaults to {@link Factory} */
	public IDataBlockGroupEditorFactory defaultDataBlockGroupFactory = new DataBlockGroupEditor.Factory();

	/**
	 * Creates a new {@link IDataBlockGroupEditor} for the given DataBlockGroup.
	 * If no special editor was registered for that group the default factory (
	 * {@link #setDefaultDataBlockGroupFactory(IDataBlockGroupEditorFactory)})
	 * is used to create the editor.
	 * 
	 * @param struct The structure the DataBlockGroup is in.
	 * @param dataBlockGroup The DataBlockGroup to create an editor for.
	 * @return A new instance of {@link IDataBlockGroupEditor} to edit the given {@link DataBlockGroup}.
	 */
	public IDataBlockGroupEditor createDataBlockGroupEditor() {
		
		return defaultDataBlockGroupFactory.createDataBlockGroupEditor();
	}
	
	/**
	 * Set the {@link IDataBlockGroupEditorFactory} to use when no special one is registered for a certain {@link DataBlockGroup}.
	 * 
	 * @param defaultDataBlockGroupFactory The new default factory.
	 */
	public void setDefaultDataBlockGroupFactory(IDataBlockGroupEditorFactory defaultDataBlockGroupFactory) {
		this.defaultDataBlockGroupFactory = defaultDataBlockGroupFactory;
	}
	
	
	private static DataBlockGroupEditorFactoryRegistry sharedInstance;

	public static DataBlockGroupEditorFactoryRegistry sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new DataBlockGroupEditorFactoryRegistry();
		return sharedInstance;
	}

}

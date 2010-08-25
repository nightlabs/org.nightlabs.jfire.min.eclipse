/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

/**
 * @author Alexander Bieber <!-- alex [at] nightlabs [dot] de -->
 */
public interface IDataBlockGroupEditorFactory {
	
	/**
	 * Create a new {@link IDataBlockGroupEditor}.
	 * 
	 * @return A new instance of an {@link IDataBlockGroupEditor}.
	 */
	IDataBlockGroupEditor createDataBlockGroupEditor();
}

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;


/**
 * Listener that can be added to {@link DataBlockGroupEditor}s in order to be notified in the case a DataBlock
 * has been removed from the associated DataBlockGroup.
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public interface IDataBlockRemovalListener {

	/** Called in the case a DataBlock has been removed from the associated DataBlockGroup. */
	void removedDataBlock();
	/**
	 * Adds the given listener to the list of listeners that will be notified in the case a DataBlock
	 * has been removed from the associated DataBlockGroup.
	 * @param changeListener The listener to be added.
	 */
	void addDataBlockRemovalListener(final IDataBlockRemovalListener changeListener);
	/**
	 * Removes the given listener from the list of listeners that will be notified in the case a DataBlock
	 * has been removed from the associated DataBlockGroup.
	 * @param changeListener The listener to be removed.
	 */
	void removeDataBlockRemovalListener(final IDataBlockRemovalListener changeListener);
}

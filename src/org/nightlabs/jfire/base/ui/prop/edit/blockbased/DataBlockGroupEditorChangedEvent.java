/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Collection;
import java.util.LinkedList;

import org.nightlabs.jfire.prop.DataBlock;

/**
 * Event object used to notify {@link IDataBlockGroupEditorChangedListener}s of a changed
 * {@link DataBlockGroupEditor} and the {@link DataBlock}s changed in it.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DataBlockGroupEditorChangedEvent {

	private IDataBlockGroupEditor dataBlockGroupEditor;
	private DataBlock removedDataBlock;
	private DataBlock addedDataBlock;
	private Collection<DataBlock> changedDataBlocks = new LinkedList<DataBlock>(); 
	
	/**
	 * Create a new {@link DataBlockGroupEditorChangedEvent}.
	 */
	protected DataBlockGroupEditorChangedEvent(IDataBlockGroupEditor dataBlockGroupEditor, DataBlock addedDataBlock, DataBlock removedBlock) {
		this.dataBlockGroupEditor = dataBlockGroupEditor;
		this.addedDataBlock = addedDataBlock;
		if (addedDataBlock != null) {
			changedDataBlocks.add(addedDataBlock);
		}
		this.removedDataBlock = removedBlock;
		if (removedDataBlock != null) {
			changedDataBlocks.add(removedBlock);
		}
	}

	/**
	 * Used internally
	 */
	protected void addChangedDataBlocks(DataBlock... changedDataBlocks) {
		for (DataBlock dataBlock : changedDataBlocks) {
			this.changedDataBlocks.add(dataBlock);
		}
	}

	/**
	 * @return The changed {@link IDataBlockGroupEditor}.
	 */
	public IDataBlockGroupEditor getDataBlockGroupEditor() {
		return dataBlockGroupEditor;
	}
	
	/**
	 * @return The DataBlock that was added to the DataBlockGroup, or <code>null</code> if this was not an added-event.
	 */
	public DataBlock getAddedDataBlock() {
		return addedDataBlock;
	}
	
	/**
	 * @return The DataBlock that was removed from the DataBlockGroup, or <code>null</code> if this was not an removed-event.
	 */
	public DataBlock getRemovedDataBlock() {
		return removedDataBlock;
	}
	
	/**
	 * @return All changed DataBlocks in the DataBlockGroup.
	 */
	public Collection<DataBlock> getChangedDataBlocks() {
		return changedDataBlocks;
	}
	
	public static DataBlockGroupEditorChangedEvent createAddedRemoved(IDataBlockGroupEditor dataBlockGroupEditor, DataBlock addedDataBlock, DataBlock removedDataBlock) {
		return new DataBlockGroupEditorChangedEvent(dataBlockGroupEditor, addedDataBlock, removedDataBlock);
	}
	
	public static DataBlockGroupEditorChangedEvent createChanged(IDataBlockGroupEditor dataBlockGroupEditor, DataBlock... changeDataBlocks) {
		DataBlockGroupEditorChangedEvent event = new DataBlockGroupEditorChangedEvent(dataBlockGroupEditor, null, null);
		event.addChangedDataBlocks(changeDataBlocks);
		return event;
	}
}

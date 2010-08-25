package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.DataBlockRemovalException;
import org.nightlabs.jfire.prop.exception.DataBlockUniqueException;

/**
 * A Composite that manages the editing of a {@link DataBlockGroup}.
 * It therefore retrieves and uses {@link DataBlockEditor}s from the
 * {@link DataBlockEditorFactoryRegistry} for each {@link DataBlock}
 * in the edited {@link DataBlockGroup}.
 *  
 * @author Alexander Bieber <!-- alex [at] nightlabs [dot] de -->
 */
public class DataBlockGroupEditorComposite extends XComposite {
	private DataBlockGroup dataBlockGroup;

	private IStruct struct;

	private List<Composite> blockComposites = new LinkedList<Composite>();

	private IValidationResultHandler validationResultHandler;
	
	private IDataBlockGroupEditor dataBlockGroupEditor;
	
	public DataBlockGroupEditorComposite(Composite parent, IDataBlockGroupEditor dataBlockGroupEditor) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		this.dataBlockGroupEditor = dataBlockGroupEditor;
		
		scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		content = new XComposite(scrolledComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
	}

	private ScrolledComposite scrolledComposite;
	private XComposite content;

	private List<DataBlockEditor> dataBlockEditors = new LinkedList<DataBlockEditor>();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#refresh(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlockGroup)
	 */
	public void refresh(IStruct struct, DataBlockGroup blockGroup) {
		this.dataBlockGroup = blockGroup;
		this.struct = struct;
		createDataBlockEditors(struct, content);
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		assert(dataBlockEditors.size() == blockGroup.getDataBlocks().size());

		content.layout(true, true);
	}

	
	protected void createDataBlockEditors(final IStruct _struct, Composite wrapperComp) {
		if (dataBlockGroup.getDataBlocks().size() == dataBlockEditors.size()) {
			updateBlockEditors(_struct, wrapperComp);
		} else {
			reCreateDataBlockEditors(_struct, wrapperComp);
		}
	}
	
	protected void reCreateDataBlockEditors(final IStruct _struct, Composite wrapperComp) {
		for (Composite comp : blockComposites) {
			comp.dispose();
		}
		dataBlockEditors.clear();
		blockComposites.clear();

		List<DataBlock> dataBlocks = dataBlockGroup.getDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			DataBlock dataBlock = dataBlocks.get(i);
			Composite wrapper = createBlockEditorWrapper(wrapperComp);
			configureBlockEditorWrapper(wrapper);
			blockComposites.add(wrapper);

			if (i > 0) {
				Control sep = createBlockCompositeSeparator(wrapper);
				if (sep != null) {
					GridData gd = new GridData(SWT.HORIZONTAL | SWT.FILL);
					gd.horizontalSpan = 2;
					sep.setLayoutData(gd);
				}
			}

			DataBlockEditor blockEditor = DataBlockEditorFactoryRegistry.sharedInstance().createDataBlockEditor(
					_struct,
					dataBlock
			);
			blockEditor.setData(_struct, dataBlock);
			Control blockEditorControl = blockEditor.createControl(wrapper);
			blockEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
			
			blockEditor.addDataBlockEditorChangedListener(new DataBlockEditorChangedListener() {
				@Override
				public void dataBlockEditorChanged(DataBlockEditorChangedEvent dataBlockEditorChangedEvent) {
					notifyChangeListeners(dataBlockEditorChangedEvent);
				}
			});
			blockEditor.setValidationResultManager(validationResultHandler);
			dataBlockEditors.add(blockEditor);

			if (! _struct.getStructBlock(dataBlockGroup).isUnique()) {
				AddOrRemoveDataBlockGroupComposite manager = new AddOrRemoveDataBlockGroupComposite(wrapper, dataBlock, i);
				manager.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
				manager.setListener(new AddOrRemoveDataBlockGroupComposite.Listener() {
					public void addDataBlock(int index) {
						try {
							for (DataBlockEditor editor : dataBlockEditors)
								editor.updatePropertySet();

							DataBlock addedBlock = dataBlockGroup.addDataBlock(_struct.getStructBlock(dataBlockGroup), index);
							notifyDataBlockGroupChangeListeners(DataBlockGroupEditorChangedEvent.createAddedRemoved(dataBlockGroupEditor, addedBlock, null));
							refresh(struct, dataBlockGroup);
						} catch (DataBlockUniqueException e) {
							e.printStackTrace();
						}
					}
					public void removeDataBlock(DataBlock block) {
						try {
							for (DataBlockEditor editor : dataBlockEditors)
								editor.updatePropertySet();

							dataBlockGroup.removeDataBlock(block);
							notifyDataBlockGroupChangeListeners(DataBlockGroupEditorChangedEvent.createAddedRemoved(dataBlockGroupEditor, null, block));
							refresh(struct, dataBlockGroup);
						} catch (DataBlockRemovalException e) {
							e.printStackTrace();
						}
					}
				});

				if (dataBlockGroup.getDataBlocks().size() == 1) {
					manager.getRemoveButton().setEnabled(false);
				}
			}
		}
	}

	/**
	 * Create a composite that should be used to wrap the BlockEditors of a
	 * DataBlock.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @return A composite that should be used to wrap the BlockEditors of a
	 *         DataBlock
	 */
	protected Composite createBlockEditorWrapper(Composite parent) {
		return new XComposite(parent, SWT.NONE, LayoutMode.NONE, LayoutDataMode.NONE, 2);
	}
	
	/**
	 * Configure the Layout and LayoutData of a composite that wraps all BlockEditors of a DataBlock.
	 * 
	 * @param wrapperComp The composite to configure.
	 */
	protected void configureBlockEditorWrapper(Composite wrapperComp) {
		wrapperComp.setLayout(XComposite.getLayout(LayoutMode.TIGHT_WRAPPER, null, 2));
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, wrapperComp);
	}
	
	/**
	 * Create a Control that is used to separate two Composites of different DataBlocks from each other.
	 * Return <code>null</code> to have no separator.
	 * <p>
	 * Note that this is ment to be a horizontal separator and its LayoutData will be overwritten.
	 * </p>
	 * 
	 * @param parent The parent Composite.
	 * @return A Control used to separate two Composites of different DataBlocks or <code>null</code>.
	 */
	protected Control createBlockCompositeSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	protected void updateBlockEditors(final IStruct _struct, Composite wrapperComp) {
		List<DataBlock> dataBlocks = dataBlockGroup.getDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			DataBlock dataBlock = dataBlocks.get(i);
			dataBlockEditors.get(i).setData(_struct, dataBlock);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#updatePropertySet()
	 */
	public void updatePropertySet() {
		for (DataBlockEditor blockEditor : dataBlockEditors) {
			blockEditor.updatePropertySet();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#getStruct()
	 */
	public IStruct getStruct() {
		return struct;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#getDataBlockGroup()
	 */
	public DataBlockGroup getDataBlockGroup() {
		return dataBlockGroup;
	}
	
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
	}
	
	private ListenerList changeListener = new ListenerList();
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#addDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockGroupEditor#removeDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	
	private ListenerList groupChangedListener = new ListenerList();
	
	public synchronized void addDataBlockEditorGroupChangedListener(IDataBlockGroupEditorChangedListener listener) {
		groupChangedListener.add(listener);
	}

	public synchronized void removeDataBlockEditorGroupChangedListener(IDataBlockGroupEditorChangedListener listener) {
		groupChangedListener.add(listener);
	}
	
	protected synchronized void notifyDataBlockGroupChangeListeners(DataBlockGroupEditorChangedEvent changedEvent) {
		Object[] listeners = groupChangedListener.getListeners();
		for (Object listener : listeners) {
			((IDataBlockGroupEditorChangedListener) listener).dataBlockGroupEditorChanged(changedEvent);
		}
	}

	protected synchronized void notifyChangeListeners(DataBlockEditorChangedEvent changedEvent) {
		DataBlockEditor dataBlockEditor = changedEvent.getDataBlockEditor();
		DataFieldEditor<? extends DataField> dataFieldEditor = changedEvent.getDataFieldEditor();
//		Collection<DisplayNamePart> parts = dataBlockEditor.getStruct().getDisplayNameParts();
		StructBlock structBlock = dataBlockEditor.getStruct().getStructBlock(dataBlockEditor.getDataBlock().getDataBlockGroup());
		if (structBlock.getDataBlockValidators().size() > 0) {
			// if there are validators for the block we have to update the propertySet
			// i.e. write the data from the editor to the property set
			dataFieldEditor.updatePropertySet();
		}
//		else {
//			for (DisplayNamePart part : parts) {
//				if (dataFieldEditor.getStructField().equals(part.getStructField())) {
//					dataFieldEditor.updatePropertySet();
//					break;
//				}
//			}
				
		Object[] listeners = changeListener.getListeners();
		for (Object listener : listeners) {
			((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(changedEvent);
		}
	}
}

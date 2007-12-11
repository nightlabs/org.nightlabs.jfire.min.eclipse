/**
 * 
 */
package org.nightlabs.jfire.base.ui.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedEvent;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedListener;

/**
 * A base class for active tables. 
 * Subclasses must provide a {@link ActiveJDOObjectController} that provides the data.
 * <p>
 * You should not use the {@link #setInput(Object)} method directly for this class,
 * neither the one of the contained {@link TableViewer}. Use the {@link #load()} method instead. 
 * </p> 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de --> 
 */
public abstract class ActiveJDOObjectTableComposite<JDOObjectID, JDOObject> extends
		AbstractTableComposite<JDOObject> {

	/**
	 * Used internally
	 */
	class ContentProvider extends TableContentProvider {
		private List<JDOObject> input; 
		@Override
		public Object[] getElements(Object inputElement) {
			if (input == null) {
				return new Object[0];
			}
			return input.toArray();
		}
		public void setInput(Collection<JDOObject> inp) {
			this.input = new ArrayList<JDOObject>(inp);
		}
		public boolean isInputSet() {
			return input != null;
		}
		public boolean containsElement(JDOObject element) {
			return input != null ? input.contains(element) : false;
		}
		public void add(JDOObject element) {
			if (input == null) {
				input = new ArrayList<JDOObject>();
			}
			input.add(element);
		}
	}
	
	private ActiveJDOObjectController<JDOObjectID, JDOObject> controller;
	
	public ActiveJDOObjectTableComposite(Composite parent, int style) {
		super(parent, style);
		initController();
	}

	public ActiveJDOObjectTableComposite(Composite parent, int style, int viewerStyle) {
		super(parent, style, true, viewerStyle);
		initController();
	}

	protected abstract ActiveJDOObjectController<JDOObjectID, JDOObject> createActiveJDOObjectController();
	
	protected ActiveJDOObjectController<JDOObjectID, JDOObject> getActiveJDOObjectController() {
		if (controller == null) {
			controller = createActiveJDOObjectController();
		}
		return controller;
	}
	
	protected abstract ITableLabelProvider createLabelProvider();

	/**
	 * Sets the {@link ContentProvider} and the LabelProvider {@link #createLabelProvider()}. 
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(createLabelProvider());
	}

	private ContentProvider getContentProvider() {
		return (ContentProvider) getTableViewer().getContentProvider();
	}
	
	/**
	 * Initializes the controller for this table and adds a listener to react to 
	 * changed and deleted objects.
	 */
	private void initController() {
		ActiveJDOObjectController<JDOObjectID, JDOObject> controller = getActiveJDOObjectController();
		// add the listener.
		controller.addJDOObjectsChangedListener(new JDOObjectsChangedListener<JDOObjectID, JDOObject>() {
			public void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event) {
				// refresh changed and new objects				
				final Collection<JDOObject> loadedObjects = event.getLoadedJDOObjects();
				if (loadedObjects != null) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!getContentProvider().isInputSet()) {
								getContentProvider().setInput(loadedObjects);
								getTableViewer().setInput(loadedObjects);
							} else {
								for (JDOObject loadedObject : loadedObjects) {
									if (getContentProvider().containsElement(loadedObject)) {
										getTableViewer().refresh(loadedObject, true);
									} else {
										getTableViewer().add(loadedObject);
										getContentProvider().add(loadedObject);
									}
								}
							}
						}
					});
				}
				
				// remove deleted objects.
				Map<JDOObjectID, JDOObject> deletedObjects = event.getDeletedJDOObjects();
				if (deletedObjects != null) {
					Collection<JDOObject> delObjects = new HashSet<JDOObject>();
					for (JDOObject delObject : delObjects) {
						if (delObject != null) {
							delObjects.add(delObject);
						}
					}
					final Object[] removeElements = delObjects.toArray();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							getTableViewer().remove(removeElements);
						}
					});
				}
			}
		});
		controller.getJDOObjects();
	}
	
	/**
	 * Will cause this table to load its contents
	 * via the {@link ActiveJDOObjectController}.
	 */
	public void load() {
		getActiveJDOObjectController().getJDOObjects();
	}
}

package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public abstract class AbstractStructFieldEditorFactory implements StructFieldEditorFactory {
	protected String structFieldClass = null;

	public String getStructFieldClass() {
		return structFieldClass;
	}

	public void setStructFieldClass(String theClass) {
		structFieldClass = theClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement element, String propertyName, Object data) throws CoreException
	{
		structFieldClass = element.getAttribute("class"); //$NON-NLS-1$
	}
}
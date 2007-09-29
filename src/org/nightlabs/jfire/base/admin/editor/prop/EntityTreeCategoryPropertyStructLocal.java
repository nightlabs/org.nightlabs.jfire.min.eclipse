/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.editor.prop;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.tree.EntityTreeCategory;
import org.nightlabs.base.ui.entity.tree.IEntityTreeCategoryContentConsumer;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.jfire.base.prop.structedit.StructEditorUtil;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.security.User;

/**
 * Entity tree category for {@link User}s.
 * 
 * @version $Revision: 5032 $ - $Date: 2006-11-20 18:46:17 +0100 (Mo, 20 Nov 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class EntityTreeCategoryPropertyStructLocal
extends EntityTreeCategory
{
	protected class LabelProvider extends TableLabelProvider {

		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when dsplaying a simple string
			if(o instanceof String) {
				return (String)o;
			} else if(o instanceof StructLocalID) {
				StructLocalID structID = (StructLocalID) o;
				return structID.linkClass.substring(structID.linkClass.lastIndexOf(".")+1); //$NON-NLS-1$
			} else {
				return ""; //$NON-NLS-1$
			}
		}
		
	}

	public IEditorInput createEditorInput(Object o)
	{
		StructLocalID structLocalID = (StructLocalID)o;
		return new JDOObjectEditorInput<StructLocalID>(structLocalID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected ITreeContentProvider _createContentProvider(IEntityTreeCategoryContentConsumer contentConsumer) {
		return new TreeContentProvider() {
			public Object[] getElements(Object inputElement) {
				return StructEditorUtil.getAvailableStructLocalIDs().toArray();
			}
		};
	}
}

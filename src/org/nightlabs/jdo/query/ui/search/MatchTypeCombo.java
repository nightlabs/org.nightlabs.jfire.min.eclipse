/* *****************************************************************************
 * org.nightlabs.jdo.query.ui - NightLabs Eclipse utilities for JDO            *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jdo.query.ui.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.ComboComposite;
import org.nightlabs.jdo.search.MatchType;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public abstract class MatchTypeCombo extends ComboComposite<MatchType> {

	private List<MatchType> displayingTypes = new LinkedList<MatchType>();

	/**
	 * @param parent
	 * @param wrapperStyle
	 * @param comboStyle
	 */
	public MatchTypeCombo(Composite parent, int comboStyle) {
		super(parent, comboStyle, new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MatchType) element).getLocalisedName();
			}
		});
		
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (MatchType mt : MatchType.values()) {
			if (supportsMatchType(mt))
				displayingTypes.add(mt);
		}
		
		setInput(displayingTypes);
		
		if (displayingTypes.size() > 0)
			setSelectedMatchType(displayingTypes.get(0));
	}

	public abstract boolean supportsMatchType(MatchType matchType);

	public abstract String getMatchTypeDescription(MatchType matchType);

	public MatchType getSelectedMatchType() {
		return getSelectedElement();
	}

	public void setSelectedMatchType(MatchType matchType) {
		if (displayingTypes.contains(matchType))
			selectElement(matchType);
	}
}

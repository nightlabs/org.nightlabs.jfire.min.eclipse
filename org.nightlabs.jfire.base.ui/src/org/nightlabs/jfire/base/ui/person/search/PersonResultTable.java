/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.person.search;

import java.text.Collator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.DefaultPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * {@link PropertySetTable} that displays {@link StructField}s values for a {@link Person}. It comes
 * with a default column-configuration showing some StructFields like company, name, first name or
 * the address. The column configuration however can be changed by overriding
 * {@link #getPropertySetTableConfig()}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonResultTable extends PropertySetTable<Person, Person> {

	public PersonResultTable(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Creates a new instance of the PersonResultTable with an option for viewerStyle.
	 */
	public PersonResultTable(Composite parent, int style, int viewerStyle) {
		super(parent, style, viewerStyle);
	}

	@Override
	protected IPropertySetTableConfig getPropertySetTableConfig() {
		return new PersonResultTableConfig();
	}

	class PersonResultTableConfig extends DefaultPropertySetTableConfig {
		public PersonResultTableConfig() {
			addDefaultColumnDescriptor(PersonStruct.PERSONALDATA_COMPANY);
			addDefaultColumnDescriptor(PersonStruct.PERSONALDATA_NAME, PersonStruct.PERSONALDATA_FIRSTNAME);
			addDefaultColumnDescriptor(PersonStruct.POSTADDRESS_CITY);
			addDefaultColumnDescriptor(PersonStruct.POSTADDRESS_ADDRESS, PersonStruct.POSTADDRESS_POSTCODE);
			addDefaultColumnDescriptor(PersonStruct.PHONE_PRIMARY);
			addDefaultColumnDescriptor(PersonStruct.INTERNET_EMAIL);
			setStruct(StructLocalDAO.sharedInstance().getStructLocal(
					StructLocalID.create(
							Organisation.DEV_ORGANISATION_ID,
							Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE
					),
					new NullProgressMonitor()
			));
		}
	}
	
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		super.setTableProvider(tableViewer);
		tableViewer.setComparator(new ViewerComparator() {
			private Collator collator = Collator.getInstance();

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				String s1 = ((PropertySet)e1).getDisplayName();
				String s2 = ((PropertySet)e2).getDisplayName();
				
				if (s1 == null && s2 != null) {
					return -1;
				} else if (s1 == null && s2 == null) {
					return 0;
				} else if (s1 != null && s2 == null) {
					return 1;
				}
				
				return collator.compare(s1, s2);
			}
		});
	}

	@Override
	protected Person convertInputElement(Person inputElement) {
		if (inputElement instanceof Person) {
			return inputElement;
		}
		return null;
	}
}

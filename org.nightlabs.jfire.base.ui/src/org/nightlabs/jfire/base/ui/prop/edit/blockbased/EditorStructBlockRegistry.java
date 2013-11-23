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

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * Temporal registry for PropStructBlocks to be displayed by block-based PropEditors.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class EditorStructBlockRegistry
{
//	private String organisationID;
//	/**
//	 * The class whose property's structBlock is to be edited.
//	 */
//	private String linkClass;
//	/**
//	 * The {@link Struct} scope to use.
//	 */
//	private String structScope;
//	/**
//	 * The {@link StructLocal} scope to use.
//	 */
//	private String structLocalScope;

	private StructLocalID structLocalID;


	/**
	 * key:   String editorName
	 * value: List propStructBlockIDs
	 */
	private Map<String, List<StructBlockID>> editorsStructBlocks;

//	public EditorStructBlockRegistry(String organisationID, Class<?> linkClass, String structScope, String stuctLocalScope)
//	{
//		this(organisationID, linkClass.getName(), structScope, stuctLocalScope);
//	}

	public EditorStructBlockRegistry(StructLocalID structLocalID)
	{
		this.structLocalID = structLocalID;
		editorsStructBlocks = new HashMap<String, List<StructBlockID>>();
//		this(
//				structLocalID.organisationID,
//				structLocalID.linkClass,
//				structLocalID.structScope,
//				structLocalID.structLocalScope
//		);
	}

//	private EditorStructBlockRegistry(String organisationID, String linkClass, String structScope, String structLocalScope)
//	{
//		this.organisationID = organisationID;
//		this.linkClass = linkClass;
//		this.structScope = structScope;
//		this.structLocalScope = structLocalScope;
//		editorsStructBlocks = new HashMap<String, List<StructBlockID>>();
//	}

	/**
	 * Registers the given structBlockIDs for the editor with the given name.
	 *
	 * @param editorName The name of the editor, whose structBlockIDs are to be registered.
	 * @param propStructBlockKeys Array of structBlockIDs to be registered.
	 */
	public void addEditorStructBlocks(String editorName, StructBlockID[] propStructBlockIDs)
	{
		if(!editorsStructBlocks.containsKey(editorName))
			editorsStructBlocks.put(editorName, new LinkedList<StructBlockID>());

		List<StructBlockID> propStructBlockKeyList = editorsStructBlocks.get(editorName);
		for (int i = 0; i < propStructBlockIDs.length; i++)
		{
			propStructBlockKeyList.add(propStructBlockIDs[i]);
		}
	}

	/**
	 * Returns a {@link List} of the structBlockIDs for the given <code>editorName</code>.
	 * @param editorName The name of the editor whose registered structBlockIDs are to be returned.
	 * @return a {@link List} of the structBlockIDs for the given <code>editorName</code> or an empty unmodifiable List if none.
	 */
	public List<StructBlockID> getEditorStructBlocks(String editorName)
	{
		List<StructBlockID> toReturn = editorsStructBlocks.get(editorName);
		if (toReturn != null)
			return toReturn;
		else
			return Collections.emptyList();
	}

	/**
	 * @see #getEditorStructBlocks(String)
	 */
	public Iterator<StructBlockID> getEditorStructBlocksIterator(String editorName)
	{
		return getEditorStructBlocks(editorName).iterator();
	}

	/**
	 * Checks wether the given StructBlock is registered in at least one editor within the given scope.
	 *
	 * @param editorScope
	 * @param structBlock
	 * @return
	 */
	public boolean hasEditorCoverage(StructBlock structBlock)
	{
		for (List<StructBlockID> idList : editorsStructBlocks.values())
		{
			if (idList.contains(StructBlockID.create(structBlock.getStructBlockOrganisationID(), structBlock.getStructBlockID())))
				return true;
		}
		return false;
	}

	/**
	 * Returns a list of {@link StructBlockID}s that are not covered by any editor.
	 * @return a list of {@link StructBlockID}s that are not covered by any editor.
	 */
	public List<StructBlockID> getUnassignedBlockKeyList()
	{
		List<StructBlockID> toReturn = new LinkedList<StructBlockID>();
		IStruct struct = StructLocalDAO.sharedInstance().getStructLocal(
				structLocalID, new NullProgressMonitor()
		);
		for (StructBlock structBlock : struct.getStructBlocks())
		{
			if (!hasEditorCoverage(structBlock))
				toReturn.add(StructBlockID.create(structBlock.getStructBlockOrganisationID(), structBlock.getStructBlockID()));
		}
		return toReturn;
	}

	/**
	 * Returns an array of {@link StructBlockID}s that are not covered by any editor.
	 * @return an arry of {@link StructBlockID}s that are not covered by any editor.
	 */
	public StructBlockID[] getUnassignedBlockKeyArray()
	{
		List<StructBlockID> keys = getUnassignedBlockKeyList();
		StructBlockID[] toReturn = new StructBlockID[keys.size()];

		for (int i=0; i < keys.size(); i++)
		{
			toReturn[i] = keys.get(i);
		}
		return toReturn;
	}

}

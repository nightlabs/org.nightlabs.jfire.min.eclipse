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

package org.nightlabs.jfire.base.ui.language;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UnknownLanguageException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnknownLanguageException()
	{
	}

	/**
	 * @param message
	 */
	public UnknownLanguageException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownLanguageException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public UnknownLanguageException(Throwable cause)
	{
		super(cause);
	}

}

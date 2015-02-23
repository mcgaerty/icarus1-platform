/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.types;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UnsupportedValueTypeException extends RuntimeException {

	private static final long serialVersionUID = 3427046321201797935L;

	private final ValueType valueType;

	private static final String DEFAULT_MESSAGE = "Value type not supported: "; //$NON-NLS-1$

	public UnsupportedValueTypeException(ValueType valueType) {
		this(DEFAULT_MESSAGE+valueType, valueType, null);
	}

	public UnsupportedValueTypeException(String message, ValueType valueType, Throwable cause) {
		super(message, cause);

		this.valueType = valueType;
	}

	public UnsupportedValueTypeException(String message, ValueType valueType) {
		this(message, valueType, null);
	}

	public UnsupportedValueTypeException(ValueType valueType, Throwable cause) {
		this(DEFAULT_MESSAGE+valueType, valueType, cause);
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return valueType;
	}
}
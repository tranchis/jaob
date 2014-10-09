/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.yoshtec.owl.marshall;

import com.yoshtec.owl.JaobException;

/**
 * Represents Errors that can occur during unmarshaling
 * 
 * @author Jonas von Malottki
 * @see com.yoshtec.owl.marshall.UnMarshaller
 */
public class UnmarshalException extends JaobException {

	private static final long serialVersionUID = 8386389953011734357L;

	public UnmarshalException() {
		super();
	}

	public UnmarshalException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnmarshalException(String message) {
		super(message);
	}

	public UnmarshalException(Throwable cause) {
		super(cause);
	}

}

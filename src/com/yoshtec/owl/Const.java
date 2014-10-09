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
package com.yoshtec.owl;

/**
 * 
 * Constants for some of the aspects of the Components.
 * There supposed to be seldomly changed or only changed if you 
 * know what you're doing. Also they should not be mistaken as 
 * runtime parameters.
 * 
 * @author Jonas von Malottki
 *
 */
public interface Const {

	/** Constant for the Prefix of create Methods in the ObjectFactory */ 
	static public final String CREATE_PREFIX = "create"; 
	
	/** Default String for Annotation values */
	static public final String DEFAULT_ANNOTATION_STRING = "##default";
	
}

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
package com.yoshtec.owl.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Provides the Marshaller with the Information how to build
 * the individual URI.
 * </br>
 * Before creating the Individual the Marshaller will look for this
 * Annotation on a Field or Method. The Method may not have any Arguments 
 * and has to return a value (String is usually the best choice).
 * <br>
 * Consider the following Examples:
 * <h5>Example 1</h5>
 * <pre>
 * {@literal @}OwlClass(uri="some:uri#MyClass")
 * public class MyClass{
 * 		
 *	{@literal @}OwlIndividualId
 *	private String id;
 * 	
 *	// other stuff
 * }
 * </pre>
 * Here the marshaller will read the value from the field {@code id} and will
 * build an individual URI like {@code ontologyUri + "#" + id}.
 * 
 * <h5>Example 2</h5>
 * <pre>
 * {@literal @}OwlClass(uri="some:uri#MyClass")
 * public class MyClass{
 * 
 *	{@literal @}OwlIndividualId
 *	private Object getInfo(){
 * 	
 *	}		
 *	// other stuff
 *}
 * </pre>
 * Here the marshaller will call the Method {@code getInfo()} and will create an 
 * Individual with the URI {@code ontologyUri + "#" + getInfo().toString()}.
 * 
 * 
 * @author Jonas von Malottki
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface OwlIndividualId {

}

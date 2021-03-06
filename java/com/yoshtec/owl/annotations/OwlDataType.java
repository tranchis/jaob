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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * <pre>
 * public class Food{
 * 		{@literal @}OwlDataType("http://www.co-ode.org/ontologies/pizza/pizza.owl#Food")
 * 		private List<Food> hasTopping = null;
 * 		
 * 		{@literal @}OwlDataProperty("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasIngredient")
 * 		{@literal @}OwlDataType("http://www.w3.org/2001/XMLSchema#String")
 * 		{@literal @}OwlFunctionalDataProperty
 * 		private String hasName = "Name";
 * }
 * </pre>
 * 
 * @author Jonas von Malottki
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface OwlDataType {
	/** OWL data type URI */
	String uri();
}

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
package com.yoshtec.owl.util;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some helping methods.
 * 
 * @author Jonas von Malottki
 *
 */
public class OntologyUtil {
	
	private final static Logger log = LoggerFactory.getLogger(OntologyUtil.class);
	
	public final static URI OWL_THING_URI = URI.create("http://www.w3.org/2002/07/owl#Thing");
	
	/**
	 * gets all classes somehow described in this OWLDescription
	 * that is:
	 * <ul>
	 *  <li>Single OWLClasses</li> 
	 *  <li>Through ObjectUnionOf Nested OWLClasses</li>
	 * </ul>
	 * 
	 * Currently lacks to handle OWLObjectIntersectionOf
	 * 
	 * @param odes
	 * @return Owlclasses
	 */
	public static Set<OWLClass> getOWLClasses(OWLDescription odes){
		Set<OWLClass> result = new HashSet<OWLClass>();
		
//		if(odes.isAnonymous()){
//		LogUtil.logObjectInfo(odes, log);
//	}
		Stack<OWLDescription> desstack = new Stack<OWLDescription>();
		
		desstack.push(odes);
		
		while(!desstack.isEmpty()){
			OWLDescription des = desstack.pop();
			
			if(!des.isAnonymous()){
				try {
					// make it a class
					OWLClass ocls = des.asOWLClass();
					
					result.add(ocls);
					
				} catch (Exception e) {
					log.error("Error casting to OWLClass",e);
				}
			} else {
				if(des instanceof OWLNaryBooleanDescription){
					if(des instanceof OWLObjectUnionOf){
						OWLObjectUnionOf ounion = (OWLObjectUnionOf)des;
						for( OWLDescription ldes : ounion.getOperands() ){
							desstack.push(ldes);
						}
					} else {
						log.warn("Unable to Handle this Class construct: {}", des);
					}
				} else {
					log.warn("Unable to handle this Class construct: {}", des);
				}
			}
		}
		return result;
	}
	
	/**
	 * Convenience method for retrieving a set of classes from a
	 * Collection of OWLDescription.
	 * @param odesc
	 * @return Set of OWL classes form the OWLDescriptions
	 */
	public static Set<OWLClass> getOWLClasses(Collection<OWLDescription> odesc){
		Set<OWLClass> result = new HashSet<OWLClass>();
		for(OWLDescription odes : odesc){
			result.addAll(getOWLClasses(odes));
		}
		return result;
	
	}
}

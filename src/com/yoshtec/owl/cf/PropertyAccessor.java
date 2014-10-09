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
package com.yoshtec.owl.cf;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoshtec.owl.Const;
import com.yoshtec.owl.PropertyAccessType;
import com.yoshtec.owl.XsdType;
import com.yoshtec.owl.XsdTypeMapper;
import com.yoshtec.owl.annotations.OwlClass;
import com.yoshtec.owl.annotations.OwlDataType;
import com.yoshtec.owl.annotations.OwlDataTypes;
import com.yoshtec.owl.annotations.dprop.OwlFunctionalDataProperty;
import com.yoshtec.owl.annotations.oprop.OwlFunctionalObjectProperty;
/**
 * This Class helps to access annotated Properties of 
 * Java Beans and link the concepts to the OWL world.
 * 
 * @author Jonas von Malottki
 *
 */
public interface PropertyAccessor{

	/**
	 * 
	 * @return {@code false} if this Property is an Array or a Collection   
	 */
	public boolean isSingleValue();

	/**
	 * Sets the value or in Case of Collections will add the Value to the
	 * Collection of the Object passed.
	 * 
	 * @param obj
	 * @param value
	 */
	public void setOrAddValue(Object obj, Object value);
	
	/**
	 * Commits the values from the cache to the Objects 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void commit() throws IllegalAccessException, InvocationTargetException; 

	/**
	 * Retrieves a Value from a Property. Primitives will be boxed.
	 * @param obj the Object the Value should be retrieved from
	 * @return the value of the Property from the Object
	 * @throws Exception
	 */
	public Object getValue(Object obj) throws IllegalAccessException, InvocationTargetException;
	
	public Collection<String> getLiterals(Object obj) throws Exception;
	
	/**
	 * @return the propUri
	 */
	public URI getPropUri();
	/**
	 * @param propUri the propUri to set
	 */
	public void setPropUri(URI propUri);
	
	/**
	 * @return the Set of Datatype URIs valid for this property
	 */
	public Set<URI> getDataTypeUris();

	/**
	 * @return the declaringClass
	 */
	public Class<?> getDeclaringClass();

	public boolean isAccessible();
	
	/**
     * @return the functional
     */
    public boolean isFunctional();

    /**
     * @param functional the functional to set
     */
    public void setFunctional(boolean functional);

    /**
     * 
     * @return the Type of Property Access e.g. field or Setter/getter-Pair
     */
    public PropertyAccessType getAccess(); 

    Class<?> getType();
    
//    /**
//     * @return the Field if this Property is accessed via fields otherwise {@code null}
//     */
//    public Field getField() {
//        return this.field;
//    }
//
//    /**
//     * @return the Getter if this Property is accessed via setter/getter 
//     * pairs {@code null} otherwise. 
//     */
//    public Method getGetter() {
//        return this.getter;
//    }
//
//    /**
//     * @return the setter Method if this Property is accessed via setter/getter 
//     * pairs {@code null} otherwise. 
//     */
//    public Method getSetter() {
//        return this.setter;
//    }
}


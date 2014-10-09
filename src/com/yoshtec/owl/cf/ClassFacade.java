package com.yoshtec.owl.cf;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import com.yoshtec.owl.XsdTypeMapper;

/**
 * Is a proxy for Accessing the Class and reading 
 * the Properties of the Objects.  
 * 
 * @author Jonas von Malottki
 *
 */
public interface ClassFacade {

    Class<?> getRepresentedClass();

    PropertyAccessor getProperty(URI uri);

    boolean hasProperty(URI uri);

    /**
     * @return the ontoBaseUri retrieved from the Package 
     */
    URI getOntoBaseUri();

    String getIdString(Object o);

    Set<URI> getImportedUris();


    /**
     * @return the Set of OwlClass URIs handled by 
     * this SimpleClassFacade
     */
    Set<URI> getClassUris();

    Collection<PropertyAccessor> getDataProperties();

    Collection<PropertyAccessor> getObjectProperties();

    boolean handlesClass(URI uri);

    Object getNewInstance() throws Exception;

    boolean hasSetableId();

    Object getNewInstance(String id) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException;

    void setId(Object obj, String id);

    /**
     * Commits the cached values to the Objects.
     * 
     * @throws InvocationTargetException if the setter/getter could not 
     * be invoked correctly.
     * @throws IllegalAccessException if the Access to the Properties has 
     * been denied or failed.
     */
    void commit() throws InvocationTargetException, IllegalAccessException;

    XsdTypeMapper getTypeMapper();

    void setTypeMapper(XsdTypeMapper typeMapper);

}
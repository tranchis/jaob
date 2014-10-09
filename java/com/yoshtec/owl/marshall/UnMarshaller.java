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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyURIMapper;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.yoshtec.owl.Const;
import com.yoshtec.owl.XsdType;
import com.yoshtec.owl.XsdTypeMapper;
import com.yoshtec.owl.annotations.OwlClass;
import com.yoshtec.owl.annotations.OwlClassImplementation;
import com.yoshtec.owl.annotations.OwlOntology;
import com.yoshtec.owl.annotations.OwlRegistry;
import com.yoshtec.owl.cf.ClassFacade;
import com.yoshtec.owl.cf.ClassFacadeFactory;
import com.yoshtec.owl.util.ClassUtil;
import com.yoshtec.owl.util.OntologyUtil;

/**
 * Unmarshals OWL Ontologies into Java Objects.
 * 
 * @author Jonas von Malottki
 *
 */
public final class UnMarshaller {

	static private final Logger log = LoggerFactory.getLogger(UnMarshaller.class); 

	/** The owl ontology Manager */
	protected final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	// only classes should be in there, no interfaces or other
	protected Map<URI,ClassFacade> registeredClasses = new HashMap<URI,ClassFacade>();

	/** List of already unmarshalled Objects */
	protected Map<URI,Object> unmarshalledObjects = null;

	/** List of the object properties that are not yet connected */
	protected List<ObjectPropHolder> unresolvedObjectProperties = null; 
	
	/** the Ontology */
	protected OWLOntology ontology = null;
	
	/** Default type mapper */
	protected XsdTypeMapper typeMapper;

	private ClassFacadeFactory cfFactory; 
	
	
	/**
	 * Holds the information necessary to 
	 * associate object properties and 
	 * their values with the unmarshalled objects 
	 *
	 */
	private static class ObjectPropHolder{
		public URI owlInduri = null;
		public ClassFacade cf = null;
		public URI propUri = null;
		public Object instance = null;
		
		public ObjectPropHolder(URI owlInduri, ClassFacade cf, URI propUri, Object instance) {
			super();
			this.owlInduri = owlInduri;
			this.cf = cf;
			this.propUri = propUri;
			this.instance = instance;
		}
	}
	
	public UnMarshaller(){
	   this(new XsdTypeMapper());
	}
	
	public UnMarshaller(XsdTypeMapper typeMapper){
	    
	    this.typeMapper = typeMapper == null ? new XsdTypeMapper() : typeMapper;
	    
	    cfFactory = new ClassFacadeFactory(this.typeMapper);
	}
	
	
	/**
	 * 
	 * @param ontologyURI
	 * @param physicalURI
	 */
	public void addURIMapping(URI ontologyURI, URI physicalURI){
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);
	}
	
	public void addURIMapper(OWLOntologyURIMapper mapper){
		manager.addURIMapper(mapper);
	}
	
	
	/**
	 * 
	 * @param <T>
	 * @param ontologyPhysicalUri
	 * @param individualUri 
	 * @return the individual specified by the individualUri if existent else null;
	 * @throws UnmarshalException if unmarshalling fails 
	 */
	public <T> T unmarshall(URI ontologyPhysicalUri, URI individualUri) throws UnmarshalException{
	    throw new NotImplementedException();
	    //return null;
	}
	
	/**
	 * Unmarshals the ontology represented from the physicalUri
	 * <br>
	 * Imports in the Ontology should be made Accessible via a Mapping 
	 * {@link #addURIMapping(URI, URI)} or {@link #addURIMapper(OWLOntologyURIMapper)}
	 * 
	 * @param physicalUri the physical URI of the ontology
	 * @return the collection of objects unmarshalled
	 * @throws UnmarshalException if Unmarshaling fails
	 */
	public Collection<Object> unmarshal(URI physicalUri) throws UnmarshalException {
		
		OWLOntology onto;
		try {
			onto = manager.loadOntologyFromPhysicalURI(physicalUri);
		} catch (OWLOntologyCreationException e) {
			throw new UnmarshalException("Error opening Ontology " + physicalUri, e);
		}
		return this.unmarshal(onto);
	}

	/**
	 * Unmarshals all objects from an ontology
	 * @param onto 
	 * @return the collection of objects unmarshalled
	 * @throws UnmarshalException if umarshalling failed
	 */
	public Collection<Object> unmarshal(OWLOntology onto) throws UnmarshalException {

		this.ontology = onto;

		// initialize some variables
		this.unmarshalledObjects = new HashMap<URI,Object>();
		this.unresolvedObjectProperties = new ArrayList<ObjectPropHolder>();
		
		// first unmarshall normal individuals 
		for(OWLIndividual oi : ontology.getReferencedIndividuals()){
			try {
				unmarshall(oi);
			} catch (IllegalArgumentException e) {
				throw new UnmarshalException("Error unmarshalling the individual " + oi, e);
			} catch (InstantiationException e) {
				throw new UnmarshalException("Error unmarshalling the individual " + oi, e);
			} catch (IllegalAccessException e) {
				throw new UnmarshalException("Error unmarshalling the individual " + oi, e);
			} catch (InvocationTargetException e) {
				throw new UnmarshalException("Error unmarshalling the individual " + oi, e);
			}
		}

		// then build the object graph
		for(ObjectPropHolder prop : unresolvedObjectProperties){
			prop.cf.getProperty(prop.propUri).setOrAddValue(prop.instance, unmarshalledObjects.get(prop.owlInduri));
		}
		
		for(ClassFacade cf : registeredClasses.values()){
			try {
				cf.commit();
			} catch (InvocationTargetException e) {
				throw new UnmarshalException("Error setting the value to object " + cf, e);
			} catch (IllegalAccessException e) {
				throw new UnmarshalException("Error setting the value to object " + cf, e);
			}
		}
		
		// copy values and sort out null objects
		Set<Object> result = new HashSet<Object>();
		for(Object obj : unmarshalledObjects.values()){
			if(obj != null){
				result.add(obj);
			}
		}
		
		// cleanup 
		this.unresolvedObjectProperties = null;
		this.unmarshalledObjects = null;
		
		return result;
	}

	protected void unmarshall(OWLIndividual oi) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

		if(!unmarshalledObjects.containsKey(oi.getURI())){
			// check which Object shall be instantiated
			Set<OWLClass> oclasses = OntologyUtil.getOWLClasses(oi.getTypes(ontology));
			log.debug("OWLClasses from Individual {}: {}", oi, oclasses);

			// check if more than one JavaClass is asserted to be 
			// the class of the individual
			if(oclasses.size() > 1){
				log.warn("More than one Class is asserted for ind: {} :{}", oi.toString(), oclasses);
			}

			// take the last one
			// TODO: could also be an instance of ??
			ClassFacade cf = null;
			for(OWLClass ocls : oclasses){
				log.debug(ocls.getURI().toString());
				if(registeredClasses.containsKey(ocls.getURI())){
					cf = registeredClasses.get(ocls.getURI());
				}
			}

			if(cf == null){
				log.warn("No SimpleClassFacade found for Individual {}", oi);
				// put the URI to the list, at least we tried for this individual
				unmarshalledObjects.put(oi.getURI(), null);
				// prematurely end
				return;
			}

			// Instantiation
			String id = oi.toString();
			Object myObj = cf.getNewInstance(id);
			unmarshalledObjects.put(oi.getURI(), myObj);
			if(cf.hasSetableId()){
				cf.setId(myObj,id);
			}

			// Data Properties!
			this.addDataProperties(oi, cf, myObj);

			// Object Properties
			this.addObjectProperties(oi, cf, myObj);
		}
	}
	


	private void addObjectProperties(OWLIndividual oi, ClassFacade cf, Object instance) {
		for (Entry<OWLObjectPropertyExpression, Set<OWLIndividual>> opentry : oi.getObjectPropertyValues(ontology).entrySet()){

			// retrieve current property URI
			URI propUri = opentry.getKey().asOWLObjectProperty().getURI();

			if (cf.hasProperty(propUri)) {

				// Walk through the values
				for (OWLIndividual ocd : opentry.getValue()) {
					// get the value an  Set the Property Value
					ObjectPropHolder ph = new ObjectPropHolder(ocd.getURI(),cf,propUri, instance);
					unresolvedObjectProperties.add(ph);
				}
			}
		}
	}


	private void addDataProperties(OWLIndividual oi, ClassFacade cf, Object instance) {
		for( Entry<OWLDataPropertyExpression, Set<OWLConstant>> dpEntry : oi.getDataPropertyValues(ontology).entrySet()) {

			// retrieve current property URI
			URI propUri = dpEntry.getKey().asOWLDataProperty().getURI();

			if(cf.hasProperty(propUri)){

				// Walk through the values
				for(OWLConstant ocd : dpEntry.getValue()) {
					//try to read the literal
					Object value = readValue(ocd.getLiteral(), ocd.asOWLTypedConstant().getDataType().getURI());

					// Set the Property Value
					cf.getProperty(propUri).setOrAddValue(instance, value);
				}
			}
		}
	}

	// TODO: Improve reading and printing values in a more JAXB manner
	private Object readValue(String literal, URI dt) {
		Class<?> type = typeMapper.getType(XsdType.fromUri(dt));

		if(type.isAssignableFrom(Calendar.class))
			return DatatypeConverter.parseDateTime(literal);

		if(type.isAssignableFrom(String.class))
			return literal;

		if(type.isAssignableFrom(Integer.class))
			return Integer.parseInt(literal);

		return null;
	}


	/**
	 * Convenience method for {@link #registerClass(Class)}
	 */
	public int registerClass(String className) throws ClassNotFoundException {
	    Class<?> clazz = Class.forName(className);
	    return registerClass(clazz);
	}


	/**
	 * Registers a Class with the Unmarshaller. Only objects from registered 
	 * classes in will be unmarshalled.
	 * </br>
	 * This behavior is intentional, since through imports in an ontology it
	 * is very likely to have Objects that are out of the scope and should 
	 * not be unmarshalled.  
	 * </br>
	 * Only classes that are annotated with {@code @OwlClass} or implementing
	 * a Interface that has be annotated with {@code @OwlClass} will be registered. 
	 * 
	 * TODO: handle Enums
	 * 
	 * @param clazz Class to be registered
	 * @return if the Class successfully registered
	 * @see OwlClass
	 */
	public int registerClass(Class<?> clazz){

		// do not add Interfaces, annotations for now
		if(clazz.isInterface() || clazz.isAnnotation() ){
			return 0;			
		}

		int result = 0;

		if(clazz.isAnnotationPresent(OwlRegistry.class)){
			// this class is a owl Registry
			for(Method method : clazz.getMethods()){
				if(method.getName().startsWith(Const.CREATE_PREFIX)){
					try{
						Object cn = method.invoke(clazz.newInstance());
						result += this.registerClass(cn.getClass());
					} catch (Exception e) {
						log.warn("could not create instance or invoke create method " + method.toGenericString(),e);
					}
				}
			}
			return result;
		}



		ClassFacade cf = cfFactory.createClassFacade(clazz);
		
		// First option:
		// true generic owlclass 
		{
			OwlClass oc = clazz.getAnnotation(OwlClass.class);
			if(oc != null){
				//this class is annotated
			    log.debug("registered OwlClass uri='{}' with Class {}", oc.uri(), clazz);
	
			    for( URI curi : cf.getClassUris() ){
			        log.debug("registered OwlClass uri='{}' with Class {}", curi, clazz);
			        registeredClasses.put(curi, cf);
			    }
			    
				return 1;
			}
		}

		// Second Option: specifically annotated to be an implementation
		// False owl Class, just implementing interfaces that represent the owlclass
		// TODO: this should be done in order to use Interfaces
		{
			OwlClassImplementation oci = clazz.getAnnotation(OwlClassImplementation.class);
			if(oci != null){
				for(Class<?> iface : oci.value()){
					if(iface.isInterface()){
						OwlClass loc = iface.getAnnotation(OwlClass.class);
						if(loc != null){
							registeredClasses.put(URI.create(loc.uri()), cf);
							log.debug("registered OwlClass uri='{}' with Class {}", loc.uri(), clazz);
							result++;
						} else {
							log.warn("{} associated Interface ({}) is not annotated with @OwlClass", clazz, iface);
						}
					} else {
						log.warn("Implementation of an OwlClass '{}' not associated with an Interface '{}' ", clazz, iface);
					}
				}
			}
		}

		// Third option: Looking for implemented interfaces and their annotations.
		for(Class<?> iface : clazz.getInterfaces()){
			// doing this only for the first level, eg. direct implemented interfaces
			OwlClass oa = iface.getAnnotation(OwlClass.class);
			if(oa != null){
				//this class is annotated
				registeredClasses.put(URI.create(oa.uri()), cf);
				log.debug("registered OwlClass uri='{}' with Class {}", oa.uri(), clazz);
				result++;
			}
		}
		return result;
	}

	/**
	 * Only Packages with the {@code @OwlOntology} Annotation will be registered.
	 * Shortcut for registering the Java classes of an ontology manually.
	 * 
	 * @param packagename Java name of the package to be registered
	 * @return if the package successfully registered
	 * @see OwlOntology
	 */
	public int registerPackage(String packagename){
		int result = 0;
		try{
			Package pack = Package.getPackage(packagename);

			OwlOntology oannot = pack.getAnnotation(OwlOntology.class);
			if(oannot != null){
				for( Class<?> clazz : ClassUtil.getClassesForPackage(packagename)){
					result += registerClass(clazz);
				}

			}
			return result;
		} catch (Exception e){
			log.warn("failed to register Package " + packagename, e);
			// TODO better error handling here
		}
		return result;
	}

	/**
	 * @return the current type mapping 
	 */
    public XsdTypeMapper getTypeMapper() {
        return this.typeMapper;
    }

    /**
     * Sets the type mapper. This is only 
     * neccessary if you want to override the default
     * type mapping between Java types and the owl/xsd types  
     * @param typeMapper new type mapping
     * @see XsdTypeMapper
     */
    public void setTypeMapper(XsdTypeMapper typeMapper) {
        if( this.typeMapper == typeMapper ){
            return;
        }
        this.typeMapper = typeMapper;
        cfFactory = new ClassFacadeFactory(typeMapper);
    }

}

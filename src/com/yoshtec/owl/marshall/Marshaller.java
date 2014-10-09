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

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.bind.DatatypeConverter;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyOutputTarget;
import org.semanticweb.owl.io.WriterOutputTarget;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoshtec.owl.XsdType;
import com.yoshtec.owl.XsdTypeMapper;
import com.yoshtec.owl.cf.ClassFacade;
import com.yoshtec.owl.cf.ClassFacadeFactory;
import com.yoshtec.owl.cf.PropertyAccessor;

/**
 * Marshalls annotated Java beans to Ontologies
 * 
 * 
 * TODO: Maybe could also be used for a JPA like join/merge thing?
 *  but classes are then required to have a OwlId annotation
 * 
 * @author Jonas von Malottki
 *
 */

public class Marshaller {
    
	static private final Logger log = LoggerFactory.getLogger(Marshaller.class);

	/** whether the annotated axioms should be marshaled or not */
	//private boolean createComplexAxioms = false;
	
	/** local ontology to marshal to */
	private OWLOntology ontology = null;
	
	/** Ontology Manager */
	private OWLOntologyManager manager = null;

	/** Ontology Data factory */
	private OWLDataFactory factory = null;
		
	/** Often used xsd:string */
	private OWLDataType owlString = null; 
	
	/** Maps Java classes to xsd types and vice versa */
	private XsdTypeMapper typeMapper = new XsdTypeMapper();
	
	/** List of already marshaled Objects */
	private Map<Object,OWLIndividual> visitedObjects = null;

	/** Object where the OWL object properties are not yet marshaled */
	private Stack<Object> missingObjectProps = null;
	
	/** already used and prepared classes */
	private Map<Class<?>,ClassFacade> classes = new HashMap<Class<?>,ClassFacade>();
	
	private ClassFacadeFactory cfFactory;
	
	/**
	 * Creates a new Marshaller to serialize annotated Java objects to 
	 * an ontology.
	 */
	public Marshaller() {
	    
		// create a manger for Ontologies
		manager = OWLManager.createOWLOntologyManager();	

		// We need a data factory to create various object from.  Each ontology has a reference
		// to a data factory that we can use.
		factory = manager.getOWLDataFactory();
		
		// setting default string data type 
		owlString = factory.getOWLDataType(XsdType.STRING.getUri());
		
		resetObjects();
	}
	
	/**
	 * Creates a new Ontology and the mapping for the URIs
	 */
	private OWLOntology createOntology(URI ontologyUri, URI physicalUri) throws OWLOntologyCreationException {
		
		// Set up a mapping, which maps the ontology URI to the physical URI
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyUri, physicalUri);
		manager.addURIMapper(mapper);

		// Now create the ontology - we use the ontology URI (not the physical URI)
		return manager.createOntology(ontologyUri);
	}
	
	/**
	 * Rests local working Structures:
	 * meaning clears the visited Objects HashMap and the still 
	 * not marshaled object properties for the iterative marshaling.
	 * 
	 */
	private void resetObjects(){
		this.visitedObjects = new HashMap<Object, OWLIndividual>();
		this.missingObjectProps = new Stack<Object>();
	}
	
	public OWLOntology marshal(Collection<?> objects, URI ontologyUri) throws MarshalException {
		return this.marshal(objects, ontologyUri, true);
	}
	
	public OWLOntology marshal(Collection<?> objects, URI ontologyUri, boolean deep) throws MarshalException {
		if(ontologyUri == null)
			throw new IllegalArgumentException("no ontology Uri set");
		
		try {
			return this.marshal(objects, manager.createOntology(ontologyUri), deep);
		} catch (OWLOntologyCreationException e) {
			throw new MarshalException("Could not create the Ontology " + ontologyUri, e);
		}
	}
	
	/** Creates an ontology,
     * Marshals the Objects passed
     * And saves the Ontology to the physical URI passed
     * 
     * @param objects Objects to be saved to the ontology
     * @param ontologyUri The URI of the Ontology
     * @param ontologyPhysicalUri the physical URI to save the Ontology to, e.g. filename
     * @return the newly created Ontology 
     * @throws MarshalException if marshalling had a Error
     */
	public OWLOntology marshal(Collection<?> objects, URI ontologyUri, URI ontologyPhysicalUri ) throws MarshalException {
	    return marshal(objects, ontologyUri, ontologyPhysicalUri, true);
	}
	
	/**
	 * Creates an ontology,
	 * Marshalls the Objects passed
	 * And saves the Ontology to the physical URI passed
	 * 
	 * @param objects Objects to be saved to the ontology
	 * @param ontologyUri The URI of the Ontology
	 * @param ontologyPhysicalUri the physical URI to save the Ontology to, e.g. filename
	 * @param deep if the object graph should be traversed or not, if it is not traversed only the uris of the
     * object Properties will be filled in.
	 * @return the newly created Ontology 
	 * @throws MarshalException if the marshalling failed
	 */
	public OWLOntology marshal(Collection<?> objects, URI ontologyUri, URI ontologyPhysicalUri, boolean deep) throws MarshalException {
		if(objects == null)
			throw new IllegalArgumentException("No Objects to be marshalled");
		
		// create the ontology
		OWLOntology ont;
		try {
			ont = createOntology(ontologyUri, ontologyPhysicalUri);
		} catch (OWLOntologyCreationException e) {
			throw new MarshalException("Error creating the ontology " + ontologyUri, e);
		}
	
		this.marshal(objects, ont, deep);
		
		try {
			manager.saveOntology(ontology, ontologyPhysicalUri);
		} catch (UnknownOWLOntologyException e) {
			throw new MarshalException("Error saving the ontology to " + ontologyPhysicalUri, e);
		} catch (OWLOntologyStorageException e) {
			throw new MarshalException("Error saving the ontology to " + ontologyPhysicalUri, e);
		}
		
		return ontology;
		
	}
	

	public OWLOntology marshal(Collection<?> objects, URI ontologyUri, Writer output) throws MarshalException {
		return this.marshal(objects, ontologyUri, output, true);
	}
	
	public OWLOntology marshal(Collection<?> objects, URI ontologyURI, Writer output, boolean deep) throws MarshalException {
		if(ontologyURI == null)
			throw new IllegalArgumentException("No ontologyURI specified");
		if(output == null)
			throw new IllegalArgumentException("Writer cannot be null");
		
		try {
			this.marshal(objects, manager.createOntology(ontologyURI), deep);
		} catch (OWLOntologyCreationException e) {
			throw new MarshalException("Unable to create Ontology " +  ontologyURI, e);
		}
		
		OWLOntologyOutputTarget target = new WriterOutputTarget(output);
		
		try {
			manager.saveOntology(ontology, target);
		} catch (UnknownOWLOntologyException e) {
			throw new MarshalException("Unable to write ontology to output", e);
		} catch (OWLOntologyStorageException e) {
			throw new MarshalException("Unable to write ontology to output", e);
		}
		
		return this.ontology;
	}
	
	/**
	 * Convenience Method for {@code marshal(objects, onto, true)}
     * @param objects the objects to be marshaled
     * @param onto the ontology to be marshaled to
     * @return the ontology passed
     * @throws MarshalException if the marshalling failed
	 */
	public OWLOntology marshal(Collection<?> objects, OWLOntology onto) throws MarshalException {
	    return marshal(objects, onto, true);
	}
	
	/**
	 * 
	 * @param objects the objects to be marshaled
	 * @param onto the ontology to be marshaled to
	 * @param deep if the object graph should be traversed or not, if it is not traversed only the uris of the
	 * object Properties will be filled in.
	 * @return the ontology passed
	 * @throws MarshalException if the marshalling failed
	 */
	public OWLOntology marshal(Collection<?> objects, OWLOntology onto, boolean deep) throws MarshalException {
		if(objects == null)
			throw new IllegalArgumentException("No Objects to be marshaled");
		if(onto == null)
			throw new IllegalArgumentException("Ontology shall not be null");
		
		this.ontology = onto;
		
		resetObjects();
		
		lmarshal(objects, deep);
		
		return ontology;
	}
	
	/**
	 * @param o
	 * @return A class facade able to handle the Object {@code o}
	 * @throws Exception
	 */
	private ClassFacade getClassFacade(Object o) throws OWLOntologyChangeException {
		ClassFacade cf = classes.get(o.getClass()); 
		if( cf == null ){
			cf = getCfFactory().createClassFacade(o.getClass());
			classes.put(o.getClass(), cf);
			
			// Base Uri of the Package
			manager.addAxiom(ontology, factory.getOWLImportsDeclarationAxiom(ontology, cf.getOntoBaseUri()));
		}
		return cf;
	}
	
	/**
	 * @return the {@link OWLIndividual} for the parameter {@code o}
	 */
	private OWLIndividual getOWLIndividual(Object o, boolean deep) throws OWLOntologyChangeException, MarshalException {
	
		if(!visitedObjects.containsKey(o)){
			
			ClassFacade cf = getClassFacade(o);

			// Individual creation
			URI induri = URI.create("#" + cf.getIdString(o));
			OWLIndividual ind = factory.getOWLIndividual(induri);
	
			// add the visited Object
			visitedObjects.put(o, ind);
			
			// add the Class URIs 
			for(URI uri : cf.getClassUris()){
				if( uri != null){
				    OWLClass ocls = factory.getOWLClass(uri);
				    manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(ind, ocls));
				}
			}

            // Data Properties
            for(PropertyAccessor prop : cf.getDataProperties()){
                try {
                    addDataProperty(prop.getValue(o), ind, prop);
                } catch (IllegalAccessException e) {
                    throw new MarshalException("Error in accessing object values from object " + o + " data property: " + prop.getPropUri(), e);
                } catch (InvocationTargetException e) {
                    throw new MarshalException("Error in accessing object values from object " + o + " data property: " + prop.getPropUri(), e);
                }
            }
			
			// only if deep is set we will descend further in the object graph
			if(deep){
    			// Object Property still missing
    			this.missingObjectProps.push(o);
			}
		}
		return visitedObjects.get(o);
	}
	
	/**
	 * Marshals all Objects from the missingObjectProps
	 * @throws MarshalException if something goes wrong
	 */
	private void lmarshal(Collection<?> objects, boolean deep) throws MarshalException {
		
		if( objects == null )
			return; //nothing to to!

		try {
			// two Phase system

		    // first marshal the plain currently known objects
		    for(Object obj : objects){
		        if( obj != null ){ // sort nasty nulls out
		            // this will add for every unknown object a missing object prop
		            getOWLIndividual(obj, true);
		        }
		    }

			// then fill in the missing object properties
			// while also following the object graph and 
			// discover new missing Individuals with its Object props 
			while(!missingObjectProps.isEmpty()){

				Object obj = missingObjectProps.pop();

				ClassFacade cf = getClassFacade(obj);

				OWLIndividual ind = getOWLIndividual(obj, deep);

				// Object Properties 
				for(PropertyAccessor prop : cf.getObjectProperties()){
					try{
						addObjectProperty(prop.getValue(obj), ind, prop, deep);
					} catch (InvocationTargetException e) {
						throw new MarshalException("Error in accessing object values from object " + obj + " property: " + prop.getPropUri(), e);
					} catch (IllegalAccessException e) {
						throw new MarshalException("Error in accessing object values from object " + obj + " property: " + prop.getPropUri(), e);
					}
				}
			}
		} catch (OWLOntologyChangeException e) {
			throw new MarshalException(e); //TODO: message
		} catch (IllegalStateException e) {
			throw new MarshalException(e);
		} catch (IllegalArgumentException e){
		    throw new MarshalException(e);
		}
	}
	
	private void addDataProperty(Object value, OWLIndividual ind, PropertyAccessor prop) throws OWLOntologyChangeException {
		if(value != null){

			//Datatype of the Property: defaulting to String
			OWLDataType dt = owlString;

			// if it is mapped to a data Property
			Set<URI> dturis = prop.getDataTypeUris();
			if(dturis != null && !dturis.isEmpty()){
				if(dturis.size() == 1){
					// get the first uri
					dt = factory.getOWLDataType(dturis.iterator().next());
				} else {
					log.warn("Cannot handle heterogenous DataProperties: {}", dturis);
				}
			} else {
				log.warn("No DataType set, defaulting to xsd:string: {}, {}", prop, ind);
			}
			
			// Process the values:
			URI propuri = prop.getPropUri();
			// unpack Lists 
			if( value instanceof Collection<?> ){
				for( Object lv : ((Collection<?>)value) ){
					addDataPropertyValue(ind, propuri, dt, printValue(lv));
				}
			} else if( value instanceof Object[]){ // or Object Arrays
				for( Object lv : ((Object[])value) ){
					addDataPropertyValue(ind, propuri, dt, printValue(lv));
				} 
			} else { //seems to be a single Value
				addDataPropertyValue(ind, propuri, dt, printValue(value));
			}
		}
	}
	
	
	private void addDataPropertyValue(OWLIndividual ind, URI property, OWLDataType dt, String literal) throws OWLOntologyChangeException{
		OWLDataProperty odp = factory.getOWLDataProperty(property);
		OWLConstant odc = factory.getOWLTypedConstant(literal,dt);
		manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(ind, odp, odc));
	}
	
	/**
	 * Processes Object Properties
	 * @param value the Value
	 * @param ind the OWLIndividual to which the Property shall be added
	 * @param prop the property to set
	 * @throws Exception
	 */
	private void addObjectProperty(Object value, OWLIndividual ind, PropertyAccessor prop, boolean deep) throws OWLOntologyChangeException, MarshalException {
		
		if(value != null){
			// Process the values:
			OWLObjectProperty oprop = factory.getOWLObjectProperty(prop.getPropUri());

			// unpack Lists 
			if( value instanceof Collection<?> ){
				for( Object lv : ((Collection<?>)value) ){
					OWLIndividual oobj = this.getOWLIndividual(lv, deep);
					addObjectPropertyValue(ind, oprop, oobj);
				}
			} else if( value instanceof Object[]){ // or Object Arrays
				for( Object lv : ((Object[])value) ){
					OWLIndividual oobj = this.getOWLIndividual(lv, deep);
					addObjectPropertyValue(ind, oprop, oobj);
				} 
			} else { //seems to be a single Value
				OWLIndividual oobj = this.getOWLIndividual(value, deep);
				addObjectPropertyValue(ind, oprop, oobj);
			}
		}
	}
	
	private void addObjectPropertyValue(OWLIndividual subj, OWLObjectProperty pred, OWLIndividual obj){
		OWLObjectPropertyAssertionAxiom addOVal = factory.getOWLObjectPropertyAssertionAxiom(subj, pred, obj);
		try {
			manager.addAxiom(ontology, addOVal);
		} catch (OWLOntologyChangeException e) {
			log.warn("Unable to create {} <{}> {}", new Object[]{subj,pred,obj});
		}
	}

	/** 
	 * Prints the value of an Object
	 * @param value
	 * @return
	 */
	protected String printValue(Object value){
		if(value instanceof Calendar){
			return DatatypeConverter.printDateTime((Calendar)value);
		}
		if( value instanceof java.util.Date ){
			Calendar cal = new GregorianCalendar();
			cal.setTime((java.util.Date)value);
			return DatatypeConverter.printDateTime(cal);
		}
		return value.toString();
	}

	/**
	 * URI Mappings can be added to this Marshaller via 
	 * the Ontology Manager
	 * @return the manager of this Marshaller. 
	 */
	public OWLOntologyManager getManager() {
		return manager;
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
    
    private ClassFacadeFactory getCfFactory(){
        if( cfFactory == null ){
            cfFactory = new ClassFacadeFactory(typeMapper);
        }
        return cfFactory;
    }
    
}

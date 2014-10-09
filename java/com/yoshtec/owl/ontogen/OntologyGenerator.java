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
package com.yoshtec.owl.ontogen;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoshtec.owl.XsdTypeMapper;
import com.yoshtec.owl.annotations.OwlClass;
import com.yoshtec.owl.annotations.OwlClassImplementation;
import com.yoshtec.owl.annotations.OwlOntology;
import com.yoshtec.owl.annotations.OwlRegistry;
import com.yoshtec.owl.cf.ClassFacade;
import com.yoshtec.owl.cf.ClassFacadeFactory;
import com.yoshtec.owl.cf.PropertyAccessor;

/**
 * Creates an Ontology where the Definition of the Classes and 
 * Properties are defined from an {@link OwlRegistry}.
 * <br>
 * Caution this works only if the Provided {@link OwlRegistry} contains 
 * Objects from a Package (or some upper Package) that defines the 
 * {@link OwlOntology} annotation. 
 * <br>
 * Some Important things to respect:
 * <ul>
 *  <li> Only Java Classes (and thus OwlClasses) will be considered that do explicitly 
 *  have an createXYZ Method in the Class of the OwlRegistry </li> 
 *  <li> If the return types are Interfaces and not classes, the corresponding Interface
 *  needs an OwlClassImplementation annotation TODO!</li>
 *  
 * 
 * TODO: Implement an offline version using the apt-Toolset
 *  
 * @author Jonas von Malottki
 *
 */
public class OntologyGenerator {

    static private final Logger log = LoggerFactory.getLogger(OntologyGenerator.class);
    
    /** Ontology Manager */
    private final OWLOntologyManager manager;

    /** Ontology Data factory */
    private final OWLDataFactory factory;
        
    /** the Owl Registry if it is used for the creation of the Ontology */
    private Class<?> owlRegistryClass;
    
    /** maps Java classes to xsd types and vice versa */
    private final XsdTypeMapper typeMapper;
    
    /** keeps the Mapping from Java Classes to the SimpleClassFacade */
    private Map<Class<?>, ClassFacade> classes = new HashMap<Class<?>, ClassFacade>();

    /** the ontology URI to be created, if this is not
     *  set the URI from the package will be taken */
    private URI ontologyUri = null;
    
    /** URIs of imported ontologies */
    private Set<URI> importUris = new HashSet<URI>();
    
    /** the ontology */
    private OWLOntology ontology;
    
    private final ClassFacadeFactory cfFactory;
    
    public OntologyGenerator(){
        this(new XsdTypeMapper());
    }
    
    public OntologyGenerator(XsdTypeMapper typeMapper){
        // create a manger for Ontologies
        manager = OWLManager.createOWLOntologyManager();    

        // We need a data factory to create various object from.  Each ontology has a reference
        // to a data factory that we can use.
        factory = manager.getOWLDataFactory();
        
        this.typeMapper = typeMapper == null ? new XsdTypeMapper() : typeMapper;
        
        cfFactory = new ClassFacadeFactory(this.typeMapper);
    } 
    
    
    /**
     * Generate the Owl Ontology
     * 
     * @return generated Ontology
     * @throws OWLOntologyCreationException
     * @throws Exception TODO: remove that!
     */
    public OWLOntology genOntology() throws OWLOntologyCreationException, Exception{
        PackageReflector pref = new PackageReflector();
        Package ontoPackage = pref.findOntologyPackage(owlRegistryClass);
        
        final URI localOntologyUri;
        if( ontologyUri == null ) {
            if( ontoPackage == null ) {
                throw new IllegalStateException("Unable to find matching OwlOntology Annotation on the Package of provided class." +
                		" Either set the Ontology URI through the setOntologyUri or " +
                		"create a package-info.java file and annotate the Package with @OwlOntology");
            }
            // use the reflected URI 
            localOntologyUri = pref.getPackageUri(ontoPackage);
        } else {
            localOntologyUri = ontologyUri;
        }

        // try to create the Ontology 
        log.info("Trying to create ontology with uri: '{}'" + localOntologyUri );
        ontology = manager.createOntology(localOntologyUri);
        
        // add the imports from the Package as imports to the Ontology
        importUris.addAll(pref.getPackageImports(ontoPackage));
        
        // add all imports to the ontology
        for( URI impuri : importUris ) {
            try {
                manager.addAxiom(ontology, factory.getOWLImportsDeclarationAxiom(ontology, impuri));
                log.debug("Added import URI '{}' to ontology", impuri);
            } catch (OWLOntologyChangeException e) {
                log.warn("Unable to add OwlImport '{}' to Ontology", impuri);
            }
        }
        
        /* Now walk through all the creation Methods of the OwlRegistry
         * and get the return types 
         */ // TODO more smarter way. look at all methods but only include OWlClassed stuff
        for( Method method : owlRegistryClass.getDeclaredMethods() ){
            // only public declared Methods are checked
            if( Modifier.isPublic(method.getModifiers()) ){
                addClass( method.getReturnType() );
            }
        }
        
        /* Walk through the collected classes and generate Owl Triples from it */ 
        final OWLClass owlThing = factory.getOWLThing();
        for(ClassFacade cf : classes.values()){
            
            for(URI curi : cf.getClassUris()){
                // first create class tuple 
                OWLClass oclass = factory.getOWLClass(curi);
                
                // add the class Declaration itself
                manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(oclass));
                
                // this is a subclass of owl:Thing!
                manager.addAxiom(ontology, factory.getOWLSubClassAxiom(oclass, owlThing) );
                
                
                /* Now take a look at the properties */
                addDataProperties(cf.getDataProperties(), oclass);
                
                addObjectProperties(cf.getObjectProperties(), oclass);

            }
        }
        
        return ontology;
    }
    
    private void addDataProperties(Collection<PropertyAccessor> dprops, OWLClass oclass) throws OWLOntologyChangeException {
        // then walk through the Data Properties
        for(PropertyAccessor pa : dprops){
            OWLDataProperty dprop = factory.getOWLDataProperty(pa.getPropUri());
            
            // Add the declaration of the Property 
            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(dprop));

            // add Datatypes to the Property
            for( URI dtUri : pa.getDataTypeUris() ){
                OWLDataType odt = factory.getOWLDataType(dtUri);

                // add the DataType to the Property
                manager.addAxiom(ontology, factory.getOWLDataPropertyRangeAxiom(dprop, odt));
            }
            
            // is the Property functional?
            if( pa.isFunctional() ){
                manager.addAxiom(ontology, factory.getOWLFunctionalDataPropertyAxiom(dprop));
            }
            
            // add the Property to the Class from before  
            manager.addAxiom(ontology, factory.getOWLDataPropertyDomainAxiom(dprop, oclass));
            
        }
    }
    
    private void addObjectProperties(Collection<PropertyAccessor> oprops, OWLClass oclass ) throws OWLOntologyChangeException{
        for( PropertyAccessor pa : oprops ){
            OWLObjectProperty oprop = factory.getOWLObjectProperty(pa.getPropUri());
            
            // Add the declaration of the Property 
            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(oprop));
            
            // is the Property functional?
            if( pa.isFunctional() ){
                manager.addAxiom(ontology, factory.getOWLFunctionalObjectPropertyAxiom(oprop));
            }
            
            // add the allowed Range to the Property
            for( URI dtUri : pa.getDataTypeUris() ){
                OWLClass range = factory.getOWLClass(dtUri);
                manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(oprop, range));
            }
            
            // add the Property to the Class from before  
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(oprop, oclass));
        }
    }
    
    public void saveOntology(File file) throws UnknownOWLOntologyException, OWLOntologyStorageException {
    	this.saveOntology(file.toURI());
    }
    
    public void saveOntology(URI uri) throws UnknownOWLOntologyException, OWLOntologyStorageException {
    	manager.saveOntology(ontology, uri);
    }
    
    public void setOwlRegistryClass(Class<?> registry){
        // check if this is an Registry
        if(!registry.isAnnotationPresent(OwlRegistry.class)){
            throw new IllegalStateException("the owlregistry Object is not a OwlRegistry");
        }
        owlRegistryClass = registry;
    }
    
    /**
     * Adds a class to be included in the Ontology generation. Primitive Java Types and 
     * classes not annotated with {@link OwlClass} or {@link OwlClassImplementation} will 
     * be ignored.
     * 
     * @param c class to add.
     */
    public void addClass(Class<?> c){
    	// check for primitive classes e.g. void, int, boolean, and the like
        if(!c.isPrimitive()){
    		// handle interface mode
    		// use the class
    		// but check if it is only an implementation for something
    		if(c.isAnnotationPresent(OwlClassImplementation.class)){
    			OwlClassImplementation oi = c.getAnnotation(OwlClassImplementation.class);
    			// look for the interfaces and add them to
    			for( Class<?> clazz : oi.value() ){
    				addClassFacadeMapping(clazz);
    			}
    		} 
    		addClassFacadeMapping(c);
    	}
    }
    
    private void addClassFacadeMapping(Class<?> c){
        if( c.isAnnotationPresent(OwlClass.class) ){
            if(!classes.containsKey(c)){
                ClassFacade cf = cfFactory.createClassFacade(c);
                classes.put(c, cf);
            }
        }
    }
    
    /**
     * @return the set of URIs that will be imported by the
     *  generated Ontology (modifiable)
     */
    public Set<URI> getImportUris(){
        return this.importUris;
    }
    
    /**
     * Adds an import URI to the list of imported URIs in the 
     * generated Ontology
     * @param importUri additional import
     */
    public void addImportURI(URI importUri){
        this.importUris.add(importUri);
    }
    
    
    /**
     * Sets the (logical) URI of the generated Ontology. This will override the
     * URI annotated in the Package if any. If set to {@code null} the Uri will be
     * reflected from the Package if possible.
     * @param ontologyUri the logical ontology URI
     */
    public void setOntologyUri(URI ontologyUri){
        this.ontologyUri = ontologyUri;
    }
    
    /**
     * 
     * @return the logical Ontology URI if {@code null} the uri will be reflected 
     * from the Package
     */
    public URI getOntologyUri(){
        return this.ontologyUri;
    }
}

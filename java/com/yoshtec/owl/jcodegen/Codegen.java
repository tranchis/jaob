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
package com.yoshtec.owl.jcodegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLCommentAnnotation;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyRange;
import org.semanticweb.owl.model.OWLRuntimeException;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.yoshtec.owl.PropertyType;
import com.yoshtec.owl.XsdType;
import com.yoshtec.owl.XsdTypeMapper;
import com.yoshtec.owl.annotations.OwlOntology;
import com.yoshtec.owl.annotations.OwlRegistry;
import com.yoshtec.owl.annotations.ontology.OwlImports;
import com.yoshtec.owl.util.LogUtil;
import com.yoshtec.owl.util.OntologyUtil;

/**
 * This Class generates iCode out of an ontology, where most of the declared classes
 * will result in a Java class and property declarations will be added to them 
 * with the appropriate setter and getter methods. 
 * </br>
 * <h2> Example: </h2>
 * <blockquote><pre>
   Codegen codegen = new Codegen();
   codegen.setJavaPackageName("mypackage");
   codegen.setOntologyUri("some.ontology.uri");
   codegen.setOntologyPhysicalUri( new File("filename.owl").toURI().toString());
   codegen.setJavaSourceFolder(new File("/My/Source/Folder"));
   codegen.genCode();
 * </pre></blockquote>
 * 
 * TODO: Map owl:deprecated to <code>@Deprecated</code> Annotation.
 * 
 *  
 * @author Jonas von Malottki
 *
 */

public class Codegen {

	/** Logger */
    static private final Logger log = LoggerFactory.getLogger(Codegen.class);

	// Settings
	
	/** logical URI of the Ontology */
	private String ontologyUri = null;
	
	/** physical URI of the Ontology */
	private String ontologyPhysicalUri = null;
	
	/** Java Package name to be created */
	private String javaPackageName = null;
	
	/** source folder to put generated classes in */
	private File javaSourceFolder = null;
	
	/** Suffix for an of an Interface */
	private String javaClassSuffix = "Impl";
	
	/** Class Name for the owl:thing Class */
	private String owlthingclassname = "Thing";
	
	/** whether a Object Factory Class should be generated */  
	private boolean createObjectFactory = true;
	
	/** name of the Object Factory Class */
	private String objectFactoryName = "ObjectFactory";
	
	/** if a OWL ID Property shall be generated */
	private boolean generateIdField = false;
	
	/** name of the OWL ID field / property */
	private String idFieldName = "name";
	
	/** if interfaces should be generated */
	private boolean generateInterfaces = true;
	
	// local work variables
	private OWLOntology ontology = null;
	private JCodeModel jmodel = null;
	private JPackage jpack = null;
	
	private final XsdTypeMapper typeMapper = new XsdTypeMapper();
	
	/** holds the interfaces and the Properties */
	private Map<String,JInterfaceProxy> interfaces = new HashMap<String, JInterfaceProxy>();
	
	/**
	 * Creates a new Code Generator.
	 */
	public Codegen() {
	}

	
	/**
	 * Loads an Ontology
	 * @return
	 * @throws OWLOntologyCreationException 
	 * @throws Exception
	 */
	private OWLOntology loadOntology() throws OWLOntologyCreationException{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology result = null;
		
		URI ontologyURI = URI.create(ontologyUri);
		
		if(ontologyPhysicalUri != null && ontologyUri != null){
			// Create a physical URI which can be resolved to point to where our ontology will be saved.
			URI physicalURI = URI.create(ontologyPhysicalUri);
			
			// Set up a mapping, which maps the ontology URI to the physical URI
			SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
			manager.addURIMapper(mapper);
		
			log.debug("Attempting to load Ontology (URI={}) from physical URI={}",ontologyURI, physicalURI);
			
			result = manager.loadOntologyFromPhysicalURI(physicalURI);
			
		} else {
			
			log.debug("Attempting to load Ontology from URI={}",ontologyURI);
			
			result = manager.loadOntology(ontologyURI);
		
		}
		
		log.info("Successfully loaded Ontology URI={}", ontologyURI);
				
		return result; 
		
	}
	
	/**
	 * Generates java code for a given Ontology.
	 * 
	 * The Java code makes heavily use of Annotations. And will be in respect to 
	 * handwritten annotated overly verbose.
	 * @throws CodegenException
	 */
	public void genCode() throws CodegenException {
		
		// check if everything is in a legal state for code generation
		checkstate();
		
		// Load an ontology
		try {
            ontology = loadOntology();
        } catch (OWLOntologyCreationException e) {
            throw new CodegenException("unable to open an ontology", e);
        }
		
		// build a JCodeModel to hold the Java representation
		jmodel = new JCodeModel();
		
		// add the User specified Package
		jpack = jmodel._package(javaPackageName);
	
		// add comments to the Package
		this.annotatePackage();
		
		// Interfaces and Classes
		this.addClasses();
	
		// Properties
		this.addProperties();		
		
		// finish the Interfaces and create an implementation
		for(JInterfaceProxy jiface : interfaces.values()){
			jiface.addImplementation(javaClassSuffix);
		}

		// create an object factory?
		if(createObjectFactory && objectFactoryName != null){
		    try {
		        JDefinedClass factory = jpack._class(JMod.PUBLIC, objectFactoryName);
		        factory.annotate(OwlRegistry.class);
		        factory.javadoc().add("Lets you create Classes from an OWL Ontology programmatically.");
		        // run through the interfaces with implementations
		        for(JInterfaceProxy jiface : interfaces.values()){
		            jiface.addtoObjectFactory(factory);
		        }
		    } catch (JClassAlreadyExistsException e) {
		        throw new CodegenException("Unable to create an ObjectFactory with the name " + objectFactoryName, e);
		    }
		}
		
		// Create an folder if not already existent
		try {
		    if(!javaSourceFolder.exists()){
		        javaSourceFolder.mkdir();
		    }
		} catch (SecurityException e) {
		    throw new CodegenException("Unable to access or create the Folder: " + javaSourceFolder, e);
		}
		
		
		// Write code
		try {
            jmodel.build(javaSourceFolder);
        } catch (IOException e) {
            throw new CodegenException("Unable to write Java files to Directory: " + javaSourceFolder, e);
        }
	}
	
	/**
	 * checks if all necessary fields are set.
	 */
	private void checkstate() throws IllegalStateException {
		
		if(javaPackageName == null)
			throw new IllegalStateException("Must set a Java package name!");
		
		if(javaSourceFolder == null)
			throw new IllegalStateException("Must set a Java source folder!");
		
		if (ontologyUri == null)
			throw new IllegalStateException("No ontology URL set!");
	}


	/** 
	 * Write Annotations to the Package
	 */
	private void annotatePackage(){
		// Base URI
		// Comments
		JDocComment jdoc =  jpack.javadoc();
		jdoc.append("Automatically generated Package from Ontology\n");
		jdoc.append("Ontology URI: <code>");
		jdoc.append(ontologyUri);
		jdoc.append("</code></br>\n");
		if(this.ontologyPhysicalUri != null)
			jdoc.append("Loaded from URI: <code>").append(ontologyPhysicalUri).append("</code>");
		
		//Annotation
		jpack.annotate(OwlOntology.class).param("uri", ontologyUri);
		
		// OWL Imports
		Set<OWLImportsDeclaration> imports = ontology.getImportsDeclarations();
		if(!imports.isEmpty()){
			JAnnotationArrayMember annot = jpack.annotate(OwlImports.class).paramArray("uris");
			jdoc.append("</br>\nImported Ontologies:\n<ul>");
			for(OWLImportsDeclaration imprt : imports ){
				jdoc.append("\t<li><code>").append(imprt.getImportedOntologyURI().toString()).append("</code></li>\n");
				annot.param(imprt.getImportedOntologyURI().toString());
			}
			jdoc.append("<ul>\n</br>\n</br>\n");
		}
		
		// owl annotations
		for(OWLOntologyAnnotationAxiom oaxiom : ontology.getAnnotations(ontology)){
			OWLAnnotation<?> oannot = oaxiom.getAnnotation();
			log.debug("Ontology annotation {}", oannot);
			if( oannot.isAnnotationByConstant() ){
				String value = oannot.getAnnotationValueAsConstant().getLiteral();
				jdoc.add("\nOWL Annotation ");
				//jdoc.append(oannot.toString());
				jdoc.append(": ");
				jdoc.append(value);
				jdoc.append("</br>");
			} else {
				
			}
		}
			
		
	}
	
	/**
	 * Reads the Classes from the Ontology and builds the corresponding 
	 * Interfaces.
	 */
	private void addClasses() throws CodegenException {
		/* run through the classes and generate Interface Stubs
		 * Random order so we have to first generate all classes and then 
		 * look for the Hierarchy 
		 */
		for(OWLClass ocls : ontology.getReferencedClasses()){
		
			// basically OWL classes map "best" to Java Interfaces
			log.debug("Using OWLClass: {}", ocls);
			
			// Initially build the proxies, which will later generate the Implementation
			
			// generate with normal name?
			String name = (ocls.isOWLThing() ? owlthingclassname : ocls.toString());
			JInterfaceProxy jinterface = new JInterfaceProxy(name, jpack, generateInterfaces, javaClassSuffix);

			interfaces.put(name, jinterface);

			// add some Annotations
			this.annotateClass(jinterface, ocls);
		}
		
		
		// another run for the correct Type hierarchy and Annotation
		for(OWLClass ocls : ontology.getReferencedClasses()){
			
			// get the corresponding Java interface 
			JInterfaceProxy jinterface = getInterface(ocls);
			
			// check for multiple inheritance 
			if( ocls.getSuperClasses(ontology).size() > 1 && !this.generateInterfaces ){
			    throw new CodegenException("Unable to create Classes with multiple inheritance. \n Use Interfaces via the generateInterfaces option."); 
			}
			
			// Type Hierarchy build Subclasses Connection
			for(OWLDescription odesc : ocls.getSubClasses(ontology)){
				
				JInterfaceProxy ljinterface = getInterface(odesc);
				
				log.debug("Java: Class {} extends {} ", ljinterface.fullName(), jinterface.fullName());
				
				// Subinterface them 
				ljinterface._implements(jinterface);
			}
		}
		
		// third Phase checking for equivalent classes
		for( final OWLClass ocls : ontology.getReferencedClasses() ){
		    final JInterfaceProxy jinterface = getInterface(ocls);
		    
		    for( final OWLDescription odesc : ocls.getEquivalentClasses(ontology) ){
		        OWLDescriptionVisitor vis = new OWLDescriptionVisitor(){

                    @Override
                    public void visit(OWLClass desc) {
                        log.warn("Not yet able to handle equivalent classes on {} eq {}", ocls, desc);
                        
                    }

                    @Override
                    public void visit(OWLObjectIntersectionOf desc) {
                        log.debug("Class {} is IntersectionOf: ", ocls, desc.getOperands());
                        for(OWLDescription idesc : desc.getOperands()){
                            JInterfaceProxy ljinterface = getInterface(idesc);
                            if( ljinterface != null ){
                                jinterface.addIntersection(ljinterface);
                            } else {
                                final String msg = "Nested class Operands: Class " + ocls + " is equivalent to "  + desc.toString() + " ";
                                log.debug(msg);
                                throw new IllegalStateException(msg);
                            }
                            
                        }
                    }

                    @Override
                    public void visit(OWLObjectUnionOf desc) {
                        log.debug("Class {} is UnionOf: {} ", ocls, desc.getOperands());
                        for(OWLDescription idesc : desc.getOperands()){
                            JInterfaceProxy ljinterface = getInterface(idesc);
                            jinterface.addUnion(ljinterface);
                        }
                    }

                    @Override
                    public void visit(OWLObjectComplementOf desc) {
                        log.warn("Not yet able to handle equivalent classes on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectSomeRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectAllRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectValueRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectMinCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectExactCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectMaxCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectSelfRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLObjectOneOf desc) {
                        log.debug("Individuals: {}" , desc.getIndividuals());
                    }

                    @Override
                    public void visit(OWLDataSomeRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLDataAllRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLDataValueRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLDataMinCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLDataExactCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }

                    @Override
                    public void visit(OWLDataMaxCardinalityRestriction desc) {
                        log.warn("Not yet able to handle restrictions on {} eq {}", ocls, desc);
                    }
                    
                };
                
                try {
                    odesc.accept(vis);
                } catch (IllegalStateException e){
                    throw new CodegenException(e);
                }
                
		    }
		}
	}
	
	
	private JInterfaceProxy getInterface(OWLDescription desc){
	    try {
	        // get the corresponding Java interface 
	        OWLClass ocls = desc.asOWLClass();

	        String name = (ocls.isOWLThing() ? owlthingclassname : ocls.toString());
	        return interfaces.get(name);
	    } catch (OWLRuntimeException e) {
	        log.debug("Found anonymous or strange class {}", desc);
	    }
	    return null;
        
	}
	
	private void addProperties(){
		
		// Add Id Property?
		if(generateIdField){
			Property prop = new Property();
			prop.setPtype(PropertyType.ID);
			prop.setBaseType(jmodel._ref(String.class));
			prop.setFunctional(true);
			prop.setName(this.idFieldName);
			
//			for(JInterfaceProxy jiface : interfaces.values()){
//				jiface.addProperty(prop);				
//			}
			
			this.interfaces.get(this.owlthingclassname).addProperty(prop);
		}
		
		// Data Properties
		for(OWLProperty<?,?> dprop : ontology.getReferencedDataProperties()){
			this.addProperties(dprop, PropertyType.DATA);
		}
		
		// Object Properties
		for(OWLProperty<?,?> oprop : ontology.getReferencedObjectProperties()){
			this.addProperties(oprop, PropertyType.OBJECT);
		}
			
	}
	
	private void addProperties(OWLProperty<?,?> prop, PropertyType type){
		
		log.debug("Property: {} \t {}", prop.getClass(), prop);
		
		Set<OWLDescription> domains = prop.getDomains(ontology);
		if(domains.isEmpty()){
			// if it is not associated with a special class
			// than it can be used at owl:Thing level
			log.debug("  Domain: \t\t owl:Thing");
			this.addProperty(prop, interfaces.get(owlthingclassname), type);
		} else {
			// for each included class Methods have to be generated
			for(OWLDescription odes : domains){
				
				log.debug("  Domain: \t\t {}",odes);
				
				// all associated classes will be added
				for(OWLClass ocls : OntologyUtil.getOWLClasses(odes)){
					// add a property
					this.addProperty(prop, interfaces.get(ocls.toString()), type);
				}
			}
		}
	}
	

	private void addProperty(OWLProperty<?,?> prop, JInterfaceProxy iface, PropertyType type){

		// addng a new Property
		Property jprop = new Property();
		
		// variable name in normal case and first letter uppercase
		StringBuffer pName = new StringBuffer(prop.toString()); // TODO: better to use some visitor thing here i guess
		pName.setCharAt(0,Character.toLowerCase(pName.charAt(0)));
		jprop.setName(pName.toString());
		
		// Property Type
		jprop.setPtype(type);
		
		// Functional?
		boolean functional = prop.isFunctional(ontology);
		jprop.setFunctional(functional);
		log.debug("  Adding Property {}{} to Class {}", new Object[] {prop, (functional ? "*" : ""), iface.name()});
		
		// Property URI
		jprop.setPropUri(prop.getURI());
		
		// has this Property a Range?
		Set<? extends OWLPropertyRange> ranges = prop.getRanges(ontology);
		
		// if there is no Range we can save some time
		if(!ranges.isEmpty()){
			
			// if it has more than one data Range than Make it a String
			// still then you are on your own in specifying the correct Syntax
			if(ranges.size() > 1 && prop instanceof OWLDataProperty){
				log.warn("  Range of Property '{}' is bigger than one: {}",prop ,ranges);
				// using string as fall back
				jprop.setBaseType(jmodel._ref(String.class));
				iface.addProperty(jprop);
			} else {
				//Set Data type to use for the Functions
				if( prop instanceof OWLDataProperty ){
					// should be now only one for OWLDataProperty
					jprop.setBaseType(this.switchType(ranges.iterator().next()));
				} else if( prop instanceof OWLObjectProperty ){
					
					if(ranges.size() > 1 || ranges.size() == 0){
						// more than one Class can be Included
						log.info("  Workaround using Object for Prop {} with Ranges: {}", prop, ranges);
						jprop.setBaseType(jmodel._ref(Object.class));
						// could also use thing here??
					} else {
						OWLPropertyRange pr = ranges.iterator().next();
						
						if(pr instanceof OWLClass){
							//
							jprop.setBaseType(interfaces.get(((OWLClass)pr).toString()));
							//jtype = jpack._getClass(((OWLClass)pr).toString());
						} else {
							jprop.setBaseType(jmodel._ref(Object.class));
							log.warn("Unable to handle this Construct: {}", pr);
							LogUtil.logObjectInfo(pr,log);
						}
					}
				} else {
					log.error(" Encoutered Unknown Property: {}",prop);
					throw new IllegalStateException(" Encoutered Unknown Property: " + prop);
				}
				iface.addProperty(jprop);
			}
			
			//  walk through the ranges an annotate
			for(OWLPropertyRange o : ranges){
				if( o instanceof OWLDataType){
					// DataProperty data type URI
					jprop.addDatatype(((OWLDataType)o).getURI());
				} else if( o instanceof OWLClass ){
					// ObjectProperty Class Data type URI
					jprop.addDatatype(((OWLClass)o).getURI());
				} else {
					log.warn("  Unable to annotate this construct: {} of Type {} ", o, o.getClass().getName());
					LogUtil.logObjectInfo(o, log);
				}
			}
		}
		
		for (OWLAnnotation<OWLObject> oa : prop.getAnnotations(ontology)) {
			if(oa.isAnnotationByConstant()) {
				jprop.addComment(oa.getAnnotationValueAsConstant().getLiteral());
			}
		}
	}

	
	/**
	 * 
	 * @param range
	 * @return
	 */
	private Class<?> switchDataType(OWLPropertyRange range){
		Class<?> result = null; 
		if(range instanceof OWLDataType){
			OWLDataType dt = (OWLDataType)range;
			result = typeMapper.getType(XsdType.fromUri(dt.getURI()));
			log.debug("DataType: {} OwlType: {}", result, dt);
		} else {
			log.warn("Unable to handle this Object {}",range.toString());
			LogUtil.logObjectInfo(range, log);
		}
		
		
		return result;
	}
	
	/**
	 * Convenience method returning a JType.
	 * @param range
	 * @return
	 */
	private JClass switchType(OWLPropertyRange range){
		Class<?> dt = switchDataType(range);
		
		if(dt == null){
			//TODO:
		}
		
		return jmodel.ref(dt);
	}
	

	/**
	 * Add some Annotations to the Interface 
	 * @param jinterface
	 * @param ocls
	 */
	private void annotateClass(JInterfaceProxy iface, OWLClass ocls){
			

		iface.setClassUri(ocls.getURI());
		
		
		// get further Annotation from the Ontology for the current class
		for( OWLAnnotation<?> oannot : ocls.getAnnotations(ontology)){
			log.debug("Annotation: {}", oannot.toString());
			
			if( oannot instanceof OWLConstantAnnotation ){
				
				OWLConstant val = oannot.getAnnotationValueAsConstant();

				
				// add annotation to the Interface
				// add the value pairs
				String content = val.getLiteral();
				URI uri = oannot.getAnnotationURI();
				String dturi = "";
				boolean comment = oannot instanceof OWLCommentAnnotation;
				
				if(val.isTyped()){
					dturi = val.asOWLTypedConstant().getDataType().getURI().toString();
				}
				
				// add the annotation
				iface.addAnnotation(uri, content, dturi, comment);

			} 
			
//			// Object annotations 
//			if( oannot instanceof OWLObjectAnnotation ){
//				JAnnotationUse jannot = jinterface.annotate(OwlAnnotation.class);
//				//TODO: jannot.param("dataTypeURI", "");
//				jannot.param("uri", oannot.getAnnotationURI().toString());
//			}
		}
	}
	
	/**
	 * Sets the name of the Java Package to be created 
	 * for the ontology.
	 * @param packageName
	 */
	public void setJavaPackageName(String packageName){
		if(packageName != null && (packageName.length() >= 0) && JJavaName.isJavaPackageName(packageName)) {
			this.javaPackageName = packageName;
		} else {
			throw new IllegalArgumentException("Invalid Java Package Name");
		}
	}
	
	public String getJavaPackageName(){
		return this.javaPackageName;
	}


	/**
	 * @return the ontologyUri
	 */
	public String getOntologyUri() {
		return ontologyUri;
	}

	/**
	 * @param ontologyUri the ontologyUri to set
	 */
	public void setOntologyUri(String ontologyUri) {
		// if this uri is not correct throw an exception
		URI.create(ontologyUri);
		this.ontologyUri = ontologyUri;
	}



	/**
	 * @return the javaSourceFolder
	 */
	public File getJavaSourceFolder() {
		return javaSourceFolder;
	}



	/**
	 * @param javaSourceFolder the javaSourceFolder to set
	 */
	public void setJavaSourceFolder(File javaSourceFolder) {
		this.javaSourceFolder = javaSourceFolder;
	}



	/**
	 * @return the javaClassSuffix
	 */
	public String getJavaClassSuffix() {
		return javaClassSuffix;
	}


	/**
	 * Set the Suffix for the Name of the generated Classes. Default is "Impl".
	 * E.g. having an Owl Class named Pizza would result in an {@code interface Pizza}
	 * and an class {@code class PizzaImpl implements Pizza}.
	 * 
	 * @param javaClassSuffix the javaClassSuffix to set
	 */
	public void setJavaClassSuffix(String javaClassSuffix) {
		if( javaClassSuffix == null ){
		    this.javaClassSuffix = "";
		} else {
		    this.javaClassSuffix = javaClassSuffix;
		}
	}



	/**
	 * @return the ontologyPhysicalUri
	 */
	public String getOntologyPhysicalUri() {
		return ontologyPhysicalUri;
	}



	/**
	 * @param ontologyPhysicalUri the ontologyPhysicalUri to set
	 */
	public void setOntologyPhysicalUri(String ontologyPhysicalUri) {
		// if this uri is not correct throw an exception
		URI.create(ontologyPhysicalUri);
		this.ontologyPhysicalUri = ontologyPhysicalUri;
	}


	/** 
	 * @return {@code true} if Interfaces will be generated
	 */
	public boolean isGenerateInterfaces() {
		return generateInterfaces;
	}

	/**
	 * Tells the codegen to generate Interfaces and Implementations thereof.
	 * If {@code false} then only implementations (=Java Classes) will be 
	 * generated, if this is impossible, because of multiple class inheritance in 
	 * the ontology a runtime error will be thrown during execution. <br/>
	 * The default value is {@code true}. 
	 * 
	 * @param generateInterfaces if interfaces should be generated 
	 */
	public void setGenerateInterfaces(boolean generateInterfaces) {
	    this.generateInterfaces = generateInterfaces;
	}


	/**
	 * @return {@code true} if Id Fields will be generated
	 */
	public boolean isGenerateIdField() {
		return generateIdField;
	}

	/**
	 * Tells the codegen to generate ID Fields for the Individual
	 * name (rdf:about, rdf:id or the like) for all generated classes
	 * <br />
	 * Default value is <code>false</code>.
	 * 
	 * @param generateIdField if ID fields should be generated
	 * @see #setIdFieldName(String)
	 */
	public void setGenerateIdField(boolean generateIdField) {
		this.generateIdField = generateIdField;
	}


	/**
	 * @return the name of the generated ID field
	 */
	public String getIdFieldName() {
		return idFieldName;
	}

	/**
	 * Sets the name of the owl Individual id field in the 
	 * generated Java classes.<br/>
	 * Default value is <code>"name"</code>.
	 *  
	 * @param idFieldName
	 * @see #setGenerateIdField(boolean)
	 */
	public void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}


	/**
	 * @return if a ObjectFactory will be generated
	 */
	public boolean isCreateObjectFactory() {
		return createObjectFactory;
	}


	/**
	 * Tells the codegen whether an ObjectFactory shall be generated 
	 * or not. The ObjectFactory will hold all the necessary 
	 * methods to generate new Instances of the Class defined
	 * in the Ontology programmatically.<br/>
	 * Default value is <code>true</code>.
	 * 
	 * @param createObjectFactory the createObjectFactory to set
	 * @see #setObjectFactoryName(String)
	 */
	public void setCreateObjectFactory(boolean createObjectFactory) {
		this.createObjectFactory = createObjectFactory;
	}


	/**
	 * @return the objectFactoryName
	 */
	public String getObjectFactoryName() {
		return objectFactoryName;
	}


	/**
	 * Sets the name of the ObjectFactory Java class.<br>
	 * Default value is <code>"ObjectFactory"</code>.
	 * 
	 * @param objectFactoryName the objectFactoryName to set
	 * @see #setCreateObjectFactory(boolean)
	 */
	public void setObjectFactoryName(String objectFactoryName) {
		this.objectFactoryName = objectFactoryName;
	}
}

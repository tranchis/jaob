package com.yoshtec.owl.testclasses.enumt;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GlassParse {

    
    final static private Logger log = LoggerFactory.getLogger(GlassParse.class);
    
    @Test
    public void testGlassParsing() throws Exception {
        
        // create a manger for Ontologies
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();    

        // We need a data factory to create various object from.  Each ontology has a reference
        // to a data factory that we can use.
        OWLDataFactory factory = manager.getOWLDataFactory();
        
        
        OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File("test/Glass1.owl")).toURI()); 

        for(OWLClass cl : ontology.getReferencedClasses() ){
            log.debug("Class {}", cl);
            
            for(OWLDescription cl2 : cl.getEquivalentClasses(ontology)){
                log.debug("EClass :: {}", cl2);
                
                OWLDescriptionVisitor vis = new OWLDescriptionVisitor(){

                    @Override
                    public void visit(OWLClass desc) {
                    }

                    @Override
                    public void visit(OWLObjectIntersectionOf desc) {
                    }

                    @Override
                    public void visit(OWLObjectUnionOf desc) {
                    }

                    @Override
                    public void visit(OWLObjectComplementOf desc) {
                    }

                    @Override
                    public void visit(OWLObjectSomeRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectAllRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectValueRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectMinCardinalityRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectExactCardinalityRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectMaxCardinalityRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectSelfRestriction desc) {
                    }

                    @Override
                    public void visit(OWLObjectOneOf desc) {
                        log.debug("Individuals: {}" , desc.getIndividuals());
                    }

                    @Override
                    public void visit(OWLDataSomeRestriction desc) {
                    }

                    @Override
                    public void visit(OWLDataAllRestriction desc) {
                    }

                    @Override
                    public void visit(OWLDataValueRestriction desc) {
                    }

                    @Override
                    public void visit(OWLDataMinCardinalityRestriction desc) {
                    }

                    @Override
                    public void visit(OWLDataExactCardinalityRestriction desc) {
                    }

                    @Override
                    public void visit(OWLDataMaxCardinalityRestriction desc) {
                    }
                    
                };
                
                cl2.accept(vis);

                
            }
            
        }
        
    }
    
}

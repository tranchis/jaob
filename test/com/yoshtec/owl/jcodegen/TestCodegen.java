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

import org.junit.Ignore;
import org.junit.Test;

public class TestCodegen {
	
	@Test
	public void testCodegenMatryoshka() throws Exception{
		Codegen codegen = new Codegen();
		
		// the java package to create the classes in
		codegen.setJavaPackageName("matryoshkatest");
		
		// Ontology loading parameters
		codegen.setOntologyUri("http://www.yoshtec.com/ontology/test/matryoshka");
		codegen.setOntologyPhysicalUri( new File("test/matryoshka.owl").toURI().toString());
		
		// where to write the source to
		codegen.setJavaSourceFolder(new File("otest"));
		
		// will generate "indName" String fields with @OwlIndividualId annotation and implementations
		codegen.setGenerateIdField(true);
		codegen.setIdFieldName("indName");
		
		// generate code
		codegen.genCode();
	}
	
	@Test
	public void testCodegenBucket() throws Exception{
		Codegen codegen = new Codegen();
		codegen.setJavaPackageName("buckettest");
		codegen.setOntologyUri("http://www.yoshtec.com/ontology/test/Bucket");
		codegen.setOntologyPhysicalUri( new File("test/bucket.owl").toURI().toString());
		codegen.setJavaSourceFolder(new File("otest"));
		codegen.setGenerateIdField(false);
		codegen.genCode();
	}
	
	@Test
	public void testCodegenGlass() throws Exception{
	    Codegen codegen = new Codegen();
	    codegen.setJavaPackageName("glass");
	    codegen.setOntologyUri("http://www.yoshtec.com/ontology/test/Glass");
	    codegen.setOntologyPhysicalUri( new File("test/Glass1.owl").toURI().toString());
	    codegen.setJavaSourceFolder(new File("otest"));
	    codegen.setGenerateIdField(false);
	    codegen.setGenerateInterfaces(false);
	    codegen.setJavaClassSuffix("");
	    codegen.genCode();
	}
	
	@Ignore
	@Test
	public void testCodegenPizza1() throws Exception{
		Codegen codegen = new Codegen();
		codegen.setJavaPackageName("pizza1");
		codegen.setOntologyUri("http://www.co-ode.org/ontologies/pizza/pizza.owl");
		codegen.setOntologyPhysicalUri( new File("test/pizza.owl").toURI().toString());
		codegen.setJavaSourceFolder(new File("otest"));
		codegen.setGenerateIdField(true);
		codegen.genCode();
	}
	
	@Test
	public void testCodegenIntersection() throws Exception{
	    Codegen codegen = new Codegen();
	    codegen.setJavaPackageName("intersect");
	    codegen.setOntologyUri("http://www.yoshtec.com/test/intersect.owl");
	    codegen.setOntologyPhysicalUri( new File("test/intersect.owl").toURI().toString());
	    codegen.setJavaSourceFolder(new File("otest"));
	    codegen.setGenerateIdField(false);
	    codegen.genCode();
	}

	@Test
	public void testCodegenUnion() throws Exception{
	    Codegen codegen = new Codegen();
	    codegen.setJavaPackageName("unionof");
	    codegen.setOntologyUri("http://www.yoshtec.com/test/unionof.owl");
	    codegen.setOntologyPhysicalUri( new File("test/unionof.owl").toURI().toString());
	    codegen.setJavaSourceFolder(new File("otest"));
	    codegen.setGenerateIdField(false);
	    codegen.genCode();
	}
	
	@Test(expected=CodegenException.class)
	public void testCodegenUnionIntersect() throws Exception{
	    Codegen codegen = new Codegen();
	    codegen.setJavaPackageName("unionintersect");
	    codegen.setOntologyUri("http://www.yoshtec.com/test/unionintersect.owl");
	    codegen.setOntologyPhysicalUri( new File("test/unionintersect.owl").toURI().toString());
	    codegen.setJavaSourceFolder(new File("otest"));
	    codegen.setGenerateIdField(false);
	    codegen.genCode();
	}
	
	

	/** Tests multiple inheritance with classes 
     * schould generate an error
     */
	@Test(expected=CodegenException.class)
	public void testCodegenMultipleError() throws Exception{
	    Codegen codegen = new Codegen();
	    codegen.setJavaPackageName("multiple");
	    codegen.setOntologyUri("http://www.yoshtec.com/test/multiple.owl");
	    codegen.setOntologyPhysicalUri( new File("test/multiple.owl").toURI().toString());
	    codegen.setJavaSourceFolder(new File("otest"));
	    codegen.setGenerateInterfaces(false);
	    codegen.setGenerateIdField(false);
	    codegen.genCode();
	}
	
}

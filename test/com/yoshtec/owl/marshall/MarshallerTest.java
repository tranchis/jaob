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

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.semanticweb.owl.model.OWLOntology;

import com.yoshtec.owl.testclasses.brain.Brain;

public class MarshallerTest {
    		
	@Test
	public void testMarshallerBrain1() throws Exception{
	    Brain brain = new Brain();
	    brain.setWeight(34.5f);
	    ArrayList<Brain> obj = new ArrayList<Brain>();
	    obj.add(brain);
	    
	    Marshaller marshaller = new Marshaller();
	    
	    marshaller.marshal(obj , URI.create("BrainInds.owl"), (new File("otest/BrainInds1.owl")).toURI());
	}
	

	
	@Test(expected=MarshalException.class)
	public void testError1() throws Exception {
		Collection<Object> col = new ArrayList<Object>();
		
		// add a non marshallable Object
		col.add(new Object());
		
		Marshaller m = new Marshaller();
		m.marshal(col, URI.create("xxxxx"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testError2() throws Exception {
		Marshaller m = new Marshaller();
		m.marshal(null, (URI)null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testError3() throws Exception {
		Marshaller m = new Marshaller();
		m.marshal(null, (OWLOntology)null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testError4() throws Exception {
		Marshaller m = new Marshaller();
		m.marshal(null, URI.create("ahkaks"), (Writer)null);
	}
	
}

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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.yoshtec.owl.testclasses.bucket.Bucket;
import com.yoshtec.owl.testclasses.bucket.Stone;
import com.yoshtec.owl.testclasses.bucket.Stuff;
import com.yoshtec.owl.testclasses.enumt.Glass;
import com.yoshtec.owl.testclasses.enumt.GlassColor;

public class GlassTest {

    
    @Test
    public void testMarshallerGlass1() throws Exception {
        Glass glass = new Glass();
        glass.setName("Coke");
        glass.setCol(GlassColor.GREEN);
        
        List<Glass> l = new ArrayList<Glass>();
        l.add(glass);
        
        Marshaller marshaller = new Marshaller();
        
        marshaller.marshal(l, URI.create("Glass1.owl"), (new File("otest/Glass1.owl")).toURI() );
    }
    
    @Test
    public void testMarshallerGlass2() throws Exception {
        Glass glass = new Glass();
        glass.setName("Coke");
        glass.setCol(GlassColor.BROWN);
        
        List<Glass> l = new ArrayList<Glass>();
        l.add(glass);
        
        Marshaller marshaller = new Marshaller();
        
        marshaller.marshal(l, URI.create("Glass2.owl"), (new File("otest/Glass2.owl")).toURI() );
    }
    
    
    @Test
    public void testUnMarshallerBucket1() throws Exception {
        UnMarshaller un = new UnMarshaller();

        un.registerClass(Glass.class);
        un.registerClass(GlassColor.class);

        Collection<Object> objects = un.unmarshal((new File("test/Glass1.owl")).toURI() );

        for(Object obj: objects){
            System.out.println(obj.toString());
        }

    }
    
}

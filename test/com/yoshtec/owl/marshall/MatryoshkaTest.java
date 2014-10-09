package com.yoshtec.owl.marshall;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.yoshtec.owl.testclasses.matryoshka.Matryoshka;
import com.yoshtec.owl.testclasses.matryoshka.MatryoshkaImpl;

public class MatryoshkaTest {

    /** number of objects to create */
    private final static int MAX_OBJ = 20000;
    
    @Test
    public void testMarshallerMatryoshka1() throws Exception {
        Matryoshka darth = new MatryoshkaImpl("DarthVader");
        darth.setColor("Black");
        darth.setSize(10);
        
        Matryoshka anakin = new MatryoshkaImpl("AnakinSkywalker");
        anakin.setColor("Brown");
        anakin.setSize(9);
        
        darth.setContains(anakin);
        anakin.setContained_in(darth);
        
        ArrayList<Matryoshka> a = new ArrayList<Matryoshka>();
        a.add(darth);
        
        Marshaller marshaller = new Marshaller();
        marshaller.marshal( a , URI.create("MatryoshkaInds.owl"), (new File("otest/MatryoshkaInds1.owl")).toURI());
        
    }
    
    @Test
    public void testMarshallerMatryoshka2() throws Exception {
        Matryoshka m1 = new MatryoshkaImpl("TheOne");
        m1.setColor("SoylentGreen");
        m1.setSize(MAX_OBJ+1);
 
        Matryoshka ml = m1;
        for (int i = MAX_OBJ; i > 0; i--) {
            // creating new Matryoshka
            Matryoshka tmp = new MatryoshkaImpl("No"+i);
            tmp.setSize(i);
            ml.setContains(tmp);
            ml = tmp;
        }
        
        ArrayList<Matryoshka> a = new ArrayList<Matryoshka>();
        a.add(m1);
        
        Marshaller marshaller = new Marshaller();
        marshaller.marshal( a , URI.create("MatryoshkaInds.owl"), (new File("otest/MatryoshkaInds2.owl")).toURI());
        
    }
    
    
    @Test
    public void testUnMarshallerMatryoshka1() throws Exception{
        UnMarshaller un = new UnMarshaller();
        
        //un.registerPackage("com.yoshtec.owl.testclasses.bucket");
        un.registerClass(MatryoshkaImpl.class);
        
        Collection<Object> objects = un.unmarshal((new File("test/matryoshka.owl")).toURI() );
        
        for(Object obj: objects){
            System.out.println(obj.toString());
        }
        
    }
    
}

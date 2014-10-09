package com.yoshtec.owl.marshall;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.yoshtec.owl.testclasses.matryoshka.Matryoshka;
import com.yoshtec.owl.testclasses.matryoshka.MatryoshkaImpl;

public class MarshalUnmarshalTest {

	static private final int MAX_OBJ = 500;
	
	final static URI tmpuri = (new File("otest/Mtemp.owl")).toURI();
	
	@Test
	public void testRoundTrip() throws Exception{
		
		{// create 
			Matryoshka m1 = new MatryoshkaImpl("TheOne");
			m1.setColor("SoylentGreen");
			m1.setSize(MAX_OBJ+1);

			Matryoshka ml = m1;
			for (int i = MAX_OBJ; i > 0; i--) {
				// creating new Matryoshka
				Matryoshka tmp = new MatryoshkaImpl("No"+String.format("%05d", i));
				tmp.setSize(i);
				tmp.setColor("pink"+i);
				ml.setContains(tmp);
				tmp.setContained_in(ml);
				ml = tmp;
			}

			ArrayList<Object> a = new ArrayList<Object>();
			a.add(m1);

			Marshaller marshaller = new Marshaller();
			try{
				marshaller.marshal( a , URI.create("MatryoshkaInds.owl"), tmpuri);
			} catch (Exception e){
				e.printStackTrace();
			}
			marshaller = null;
		}	

		{ //read

			UnMarshaller un = new UnMarshaller();
			un.addURIMapping(URI.create("http://www.yoshtec.com/ontology/test/matryoshka"), (new File("test/matryoshka.owl")).toURI());
			un.registerClass(MatryoshkaImpl.class);
			Collection<Object> objects = un.unmarshal(tmpuri);

			Assert.assertEquals(MAX_OBJ+1, objects.size());

			for(Object object : objects){
				System.out.println(object.toString());
			}
			
			System.out.println("Unmarshalled ");
		}
		
	}
		
}

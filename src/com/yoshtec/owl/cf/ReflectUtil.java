package com.yoshtec.owl.cf;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoshtec.owl.Const;
import com.yoshtec.owl.annotations.OwlClass;
import com.yoshtec.owl.annotations.OwlOntology;

final class ReflectUtil {

    private static final Logger log = LoggerFactory.getLogger(ReflectUtil.class);
    
    static URI getClassUri(Class<?> clazz){
        if( clazz.isAnnotationPresent(OwlClass.class) ){
            OwlClass oc = clazz.getAnnotation(OwlClass.class);
            if( oc.uri().equals(Const.DEFAULT_ANNOTATION_STRING) ){
                if( clazz.getPackage().isAnnotationPresent(OwlOntology.class) ){
                    return URI.create(
                            clazz.getPackage().getAnnotation(OwlOntology.class).uri()
                            + "#" +
                            clazz.getSimpleName());
                }  
                log.error("No Ontology Uri on Package {}", clazz.getPackage() );
            } else {
                return URI.create(oc.uri());
            }
        }
        
        return null;
    }
    
    static List<Package> getSubPackages(Package p){
        List<Package> res = new ArrayList<Package>();
        String packname = p.getName();
        int last = packname.lastIndexOf('.');
        while( packname.length() > 0 && last > 0 ){
            packname = packname.substring(0, last - 1);
            Package pl = Package.getPackage(packname);
            if( pl != null ){
                res.add(p);
            }
        }
        
        return res;
    }
    
    static String getBaseUri(Package p){
        if( p.isAnnotationPresent(OwlOntology.class) ){
            return p.getAnnotation(OwlOntology.class).uri();
        }
        return null;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

/**
 *
 * @author fnoorala
 */
public class RELATION extends Object {

    // URI for vocabulary elements
    protected static final String uri = "http://ns.inria.fr/smilk/elements/1.0/";

    // Return URI for vocabulary elements
    public static String getURI() {
        return uri;
    }

   
  
  
    
    // Define the property labels and objects
    
     static final String nhasSentences = "hasSentences";
        public static Property hasSentences = null;
        
     static final String nhasTriples = "hasTriple";
        public static Property hasTriples = null;
        
        // properties between triple and its elements
        static final String nhasSubject = "hasSubject";
        public static Property hasSubject = null;
        static final String nhasRelation = "hasRelation";
        public static Property hasRelation = null;
        static final String nhasObject = "hasObject";
        public static Property hasObject = null;

        // properties between relation as blank node and its tokens
        static final String nhasTokenSeq = "hasTokenSeq";
        public static Property hasTokenSeq = null;

        // properties of triple elements
        static final String nform = "form";
        public static Property form = null;
        static final String nlemma = "lemma";
        public static Property lemma = null;
        static final String ntype = "type";
        public static Property type = null;
        static final String npos = "pos";
        public static Property pos = null;
        static final String nid = "id";
        public static Property id = null;
        static final String ndepRel = "depRel";
        public static Property depRel = null;
        static final String nstart = "start";
        public static Property start = null;
        static final String nend = "end";
        public static Property end = null;
        static final String nhead= "head";
        public static Property head = null;

    // Instantiate the properties and the resource
    static {
        try {

            // Instantiate the properties
            hasSentences = new PropertyImpl(uri, nhasSentences);
            hasTriples = new PropertyImpl(uri, nhasTriples);
            
            hasSubject = new PropertyImpl(uri, nhasSubject);
            hasRelation = new PropertyImpl(uri, nhasRelation);
            hasObject = new PropertyImpl(uri, nhasObject);

            hasTokenSeq = new PropertyImpl(uri, nhasTokenSeq);

            form = new PropertyImpl(uri, nform);
            lemma = new PropertyImpl(uri, nlemma);

            type = new PropertyImpl(uri, ntype);
            pos = new PropertyImpl(uri, npos);
            id = new PropertyImpl(uri, nid);
            depRel = new PropertyImpl(uri, ndepRel);
            start = new PropertyImpl(uri, nstart);
            end = new PropertyImpl(uri, nend);
            head = new PropertyImpl(uri, nhead);

        } catch (Exception e) {

        }
    }

}

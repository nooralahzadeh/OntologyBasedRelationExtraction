/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction;

import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;

import org.xml.sax.SAXException;

import org.apache.jena.rdf.model.*;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.XSD;

/**
 *
 * @author fnoorala
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String outputFile = "/user/fnoorala/home/Desktop/SMILK/InformationExtraction/data/ExtractedRelations_v02.rdf";
        ///user/fnoorala/home/Desktop/SMILK/InformationExtraction/data/non_annotated_cosmetic_corpus.xml
        // read all the data from the folder and bulid the model
        OntModel model = constructOwlModelFromFile("/user/fnoorala/home/Desktop/SMILK/InformationExtraction/data/non_annotated_cosmetic_corpus");
        writeOntModelToFile(model, outputFile);
        analyseTriples(outputFile);
        // Renco renco = new Renco();
        // List<Triple> triples = extractDOMparser(renco.rencoByWebService(" Pour son cinquième anniversaire , La Vie en Rose , édition estivale du parfum de Viktor & Rolf ( L' Oréal Luxe ) créée par Olivier Polge ( IFF ) , s' offre une nouvelle composition autour de trois fleurs : la rose , le jasmin sambac et l' orchidée Heights "));
        // constructModel(triples);

    }

    public static List<Triple> patternSearch(String str) {

        // (?<tag>X) to define a named group name"
        // \k<tag> to backreference a named group "name"
        // ${tag} to reference to captured group in Matcher's replacement string
        // (?= : start of positive lookahead 
        // ) end of lookahead
        // . : match anything, this is to "move forward".
        String pattern = "(?= <(?<tag1>\\w+)>(.*?)</(\\k<tag1>)>(.*?)<(?<tag2>\\w+)>(.*?)</(\\k<tag2>)>).";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(str);

        List<Triple> triples = new ArrayList<Triple>();

        while (m.find()) {

            Triple triple = new Triple();
            triple.setSubjectType(m.group(1));
            triple.setSubject(m.group(2));

            triple.setRelation(m.group(4));

            triple.setObjectType(m.group(5));
            triple.setObject(m.group(6));
            triples.add(triple);
        }

        return triples;

    }

    public static void extract(String input) {

        try {

            BufferedReader fileReader = null;

            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(input));
            while ((line = fileReader.readLine()) != null) {

                List<Triple> triples = patternSearch(line);

                for (Triple t : triples) {
                    System.out.println(t.toString());
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static List<Sentence> extractDOMparser(String input) {

        List<Sentence> sentences = new ArrayList<>();

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(input);
            ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            Document doc = builder.parse(in);
            doc.getDocumentElement().normalize();

            NodeList nSentenceList = doc.getElementsByTagName("sentence");

            //walk on the sentence node
            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {

                Sentence sentence = new Sentence();
                sentence.setId(sent_temp);
                //sentence.setText(input);
                Node nSentNode = nSentenceList.item(sent_temp);
                NodeList nTokensList = nSentNode.getChildNodes();

                List<Triple> triples = new ArrayList<>();

                //walk on the tokens node
                for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {

                    Node nTokenNode = nTokensList.item(token_temp);

                    NodeList nList = nTokenNode.getChildNodes();

                    int x = 0, y = 0;
                    while (x < nList.getLength()) {

                        Node xNode = nList.item(x);

                        if (xNode instanceof Element) {

                            Element xElement = (Element) xNode;

                            // if (xElement.hasAttribute("type") || xElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                            if (xElement.hasAttribute("type")) {

                                Triple triple = new Triple();
                                Token subjectToken = new Token();

                                subjectToken.setId(Integer.parseInt(xElement.getAttribute("id")));
                                subjectToken.setForm(xElement.getAttribute("form"));
                                subjectToken.setStart(Integer.parseInt(xElement.getAttribute("start")));
                                subjectToken.setEnd(Integer.parseInt(xElement.getAttribute("end")));
                                subjectToken.setLema(xElement.getAttribute("lemma"));
                                subjectToken.setPos(xElement.getAttribute("pos"));
                                subjectToken.setDepRel(xElement.getAttribute("depRel"));
                                subjectToken.setHead(Integer.parseInt(xElement.getAttribute("head")));
                                subjectToken.setType(xElement.getAttribute("type"));
                                triple.setSubjectToken(subjectToken);

                                String subtype = (xElement.getAttribute("type") != null && !xElement.getAttribute("type").equalsIgnoreCase("")) ? xElement.getAttribute("type") : xElement.getAttribute("pos");

                                triple.setSubjectType(subtype);
                                triple.setSubject(xElement.getAttribute("form"));

                                y = x + 1;
                                LinkedList<Token> relationTokens = new LinkedList<>();
                                for (int j = y; j < nList.getLength(); j++) {

                                    Node yNode = nList.item(j);
                                    if (yNode instanceof Element) {
                                        Element yElement = (Element) yNode;
                                        // if (!yElement.hasAttribute("type") && !yElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                                        if (!yElement.hasAttribute("type")) {
                                            Token relationToken = new Token();
                                            relationToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                            relationToken.setForm(yElement.getAttribute("form"));
                                            relationToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                            relationToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                            relationToken.setLema(yElement.getAttribute("lemma"));
                                            relationToken.setPos(yElement.getAttribute("pos"));
                                            relationToken.setDepRel(yElement.getAttribute("depRel"));
                                            relationToken.setHead(Integer.parseInt(yElement.getAttribute("head")));

                                            relationTokens.add(relationToken);

                                        } else {

                                            Token objectyToken = new Token();

                                            objectyToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                            objectyToken.setForm(yElement.getAttribute("form"));
                                            objectyToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                            objectyToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                            objectyToken.setLema(yElement.getAttribute("lemma"));
                                            objectyToken.setPos(yElement.getAttribute("pos"));
                                            objectyToken.setDepRel(yElement.getAttribute("depRel"));
                                            objectyToken.setHead(Integer.parseInt(yElement.getAttribute("head")));
                                            objectyToken.setType(yElement.getAttribute("type"));
                                            triple.setObjectToken(objectyToken);

                                            String objtype = (yElement.getAttribute("type") != null && !yElement.getAttribute("type").equalsIgnoreCase("")) ? yElement.getAttribute("type") : yElement.getAttribute("pos");

                                            triple.setObjectType(objtype);
                                            triple.setObject(yElement.getAttribute("form"));

                                            triple.setRelationTokens(relationTokens);

                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }

                                            triple.setRelation(relation.toString().trim());
                                            relationTokens = new LinkedList<>();

                                            y = j;
                                            break;
                                        }
                                    }

                                }

                                if (triple.getObjectToken() != null && triple.getRelationTokens() != null) {

                                    //filter out the triple which has relation size more that 6 
                                    if (triple.getRelationTokens().size() < 6) {
                                        //filter out the triple which has length of relation surface form  is less than 2 
                                        if (triple.getRelationTokens().size() > 2) {
                                            triples.add(triple);
                                        } else if (triple.getRelationTokens().size() > 0 && triple.getRelationTokens().get(0).getForm().length() > 1) {
                                            triples.add(triple);
                                        }

                                    }

                                }

                                x = y;

                            } else {

                                x += 1;
                            }

                        } else {

                            x += 1;

                        }

                    }

                }

                sentence.setTriples(triples);
                sentences.add(sentence);

            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sentences;
    }

    // has a problem however we dont use it
    public static Model constructModel(AnnotatedDocument annoatedDocument) {
        // create an empty model
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("smlk", RELATION.getURI());
        m.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");

        try {

            String text = annoatedDocument.getText();

            String docId = String.format("%05d", (int) annoatedDocument.getId());
            String document = RELATION.getURI() + "doc_" + docId;
            Resource docResource = m.createResource(document);

            m.addLiteral(docResource, RELATION.text, ResourceFactory.createTypedLiteral(text));

            List<Sentence> sentences = annoatedDocument.getSentences();

            Seq sentenceList = m.createSeq();

            //list of sentence
            for (int i = 0; i < sentences.size(); i++) {

                String sentenceId = String.format("%04d", (int) i);

                String sentence = RELATION.getURI() + "sent_" + docId + "-" + sentenceId;
                Resource sentResource = m.createResource(sentence);

                sentenceList.add(i + 1, sentResource);

                Seq tripleList = m.createSeq();
                List<Triple> triples = sentences.get(i).getTriples();

                //list of triples
                for (int j = 0; j < triples.size(); j++) {
                    String tripleId = String.format("%04d", (int) i);
                    Triple t = triples.get(j);

                    String triple = RELATION.getURI() + "t" + docId + "-" + sentenceId + "-" + tripleId;
                    Resource tripleResource = m.createResource(triple);

                    String subject = RELATION.getURI() + "S_" + docId + "-" + sentenceId + "-" + tripleId;
                    Resource subjectResource = m.createResource(subject);

                    String object = RELATION.getURI() + "O_" + docId + "-" + sentenceId + "-" + tripleId;
                    Resource objectResource = m.createResource(object);

                    //make first statement
                    m.add(tripleResource, RELATION.hasSubject, subjectResource).add(tripleResource, RELATION.hasObject, objectResource);

                    //add properties to the subject resource
                    m.addLiteral(subjectResource, RELATION.form, ResourceFactory.createTypedLiteral(t.getSubjectToken().getForm()));
                    m.addLiteral(subjectResource, RELATION.lemma, ResourceFactory.createTypedLiteral(t.getSubjectToken().getLema()));

                    String rencoSubjectType = (t.getSubjectToken().getType() == null || t.getSubjectToken().getType().equalsIgnoreCase("")) ? "<NaN>" : t.getSubjectToken().getType();
                    m.addLiteral(subjectResource, RELATION.type, ResourceFactory.createTypedLiteral(rencoSubjectType));

                    m.addLiteral(subjectResource, RELATION.depRel, ResourceFactory.createTypedLiteral(t.getSubjectToken().getDepRel()));
                    m.addLiteral(subjectResource, RELATION.pos, ResourceFactory.createTypedLiteral(t.getSubjectToken().getPos()));
                    m.addLiteral(subjectResource, RELATION.id, ResourceFactory.createTypedLiteral(t.getSubjectToken().getId()));
                    m.addLiteral(subjectResource, RELATION.head, ResourceFactory.createTypedLiteral(t.getSubjectToken().getHead()));
                    m.addLiteral(subjectResource, RELATION.start, ResourceFactory.createTypedLiteral(t.getSubjectToken().getStart()));
                    m.addLiteral(subjectResource, RELATION.end, ResourceFactory.createTypedLiteral(t.getSubjectToken().getEnd()));

                    //add properties to the object resource
                    m.addLiteral(objectResource, RELATION.form, ResourceFactory.createTypedLiteral(t.getObjectToken().getForm()));
                    m.addLiteral(objectResource, RELATION.lemma, ResourceFactory.createTypedLiteral(t.getObjectToken().getLema()));

                    String rencoObjType = (t.getObjectToken().getType() == null || t.getObjectToken().getType().equalsIgnoreCase("")) ? "<NaN>" : t.getObjectToken().getType();
                    m.addLiteral(objectResource, RELATION.type, ResourceFactory.createTypedLiteral(rencoObjType));

                    m.addLiteral(objectResource, RELATION.depRel, ResourceFactory.createTypedLiteral(t.getObjectToken().getDepRel()));
                    m.addLiteral(objectResource, RELATION.pos, ResourceFactory.createTypedLiteral(t.getObjectToken().getPos()));
                    m.addLiteral(objectResource, RELATION.id, ResourceFactory.createTypedLiteral(t.getObjectToken().getId()));
                    m.addLiteral(objectResource, RELATION.head, ResourceFactory.createTypedLiteral(t.getObjectToken().getHead()));
                    m.addLiteral(objectResource, RELATION.start, ResourceFactory.createTypedLiteral(t.getObjectToken().getStart()));
                    m.addLiteral(objectResource, RELATION.end, ResourceFactory.createTypedLiteral(t.getObjectToken().getEnd()));

                    Seq tokenList = m.createSeq();

                    for (int ii = 0; ii < t.getRelationTokens().size(); ii++) {

                        String reltoken = RELATION.getURI() + "token_" + "_" + docId + "-" + sentenceId + "-" + tripleId + "-" + ii;
                        Resource reltokenResource = m.createResource(reltoken);
                        Token token = t.getRelationTokens().get(i);

                        m.addLiteral(reltokenResource, RELATION.form, ResourceFactory.createTypedLiteral(token.getForm()));
                        m.addLiteral(reltokenResource, RELATION.lemma, ResourceFactory.createTypedLiteral(token.getLema()));

                        String rencoTokenyTpe = (token.getType() == null || token.getType().equalsIgnoreCase("")) ? "<NaN>" : token.getType();
                        m.addLiteral(reltokenResource, RELATION.type, ResourceFactory.createTypedLiteral(rencoTokenyTpe));

                        m.addLiteral(reltokenResource, RELATION.depRel, ResourceFactory.createTypedLiteral(token.getDepRel()));
                        m.addLiteral(reltokenResource, RELATION.pos, ResourceFactory.createTypedLiteral(token.getPos()));
                        m.addLiteral(reltokenResource, RELATION.id, ResourceFactory.createTypedLiteral(token.getId()));
                        m.addLiteral(reltokenResource, RELATION.head, ResourceFactory.createTypedLiteral(token.getHead()));
                        m.addLiteral(reltokenResource, RELATION.start, ResourceFactory.createTypedLiteral(token.getStart()));
                        m.addLiteral(reltokenResource, RELATION.end, ResourceFactory.createTypedLiteral(token.getEnd()));
                        tokenList.add(ii + 1, reltokenResource);

                    }

                    m.add(tripleResource, RELATION.hasRelation, tokenList);
                    tripleList.add(j + 1, tripleResource);
                }

                m.add(sentResource, RELATION.hasTriples, tripleList);
            }
            m.add(docResource, RELATION.hasSentences, sentenceList);

        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
        return m;
    }

    public static Model constructRDFModelFromFile(String file) {
        Model mainModel = ModelFactory.createDefaultModel();

        Renco renco = new Renco();

        try {

            BufferedReader fileReader = null;

            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(file));
            AnnotatedDocument annotatedDocument = new AnnotatedDocument();
            int i = 0;
            while ((line = fileReader.readLine()) != null) {
                i++;

                List<Sentence> sentences = extractDOMparser(renco.rencoByWebService(line));
                annotatedDocument.setText(line);
                annotatedDocument.setId(i);
                annotatedDocument.setSentences(sentences);

//                for(Sentence s: annotatedDocument.getSentences()){
//                    System.err.println(s.getId()+": "+s.getText());
//                    for(Triple t : s.getTriples()){
//                        System.out.println("---> "+ t.toString());
//                    }
//                }
                Model subModel = constructModel(annotatedDocument);
                mainModel = mainModel.union(subModel);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mainModel;

    }

    public static OntModel constructOwlModelFromFile(String folder) {
        OntModel mainModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntModel ontModel = constructOntology();
        Renco renco = new Renco();
       
        try {
    //Create the file reader
           
            AnnotatedDocument annotatedDocument = new AnnotatedDocument();
            List<String> lines = readCorpus(folder);
            int i = 0;
            
            for (String line : lines) {
                i++;
                if (line.trim().length() > 1) {
                 
                    List<Sentence> sentences = extractDOMparser(renco.rencoByWebService(line));
                    
                    annotatedDocument.setText(line);
                    annotatedDocument.setId(i);
                    annotatedDocument.setSentences(sentences);
                    OntModel subModel = constructOntModel(ontModel, annotatedDocument);
                    
                    System.out.println("document "+ i +" of "+ lines.size() +" is added to ontology ");
                    mainModel.add(subModel);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mainModel;
    }

    public static void writeToFile(Model model, String fileName) {

        FileWriter out = null;
        try {

            out = new FileWriter(fileName);
            try {
                model.write(out, "RDF/XML");
            } finally {
                try {
                    out.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                // ignore
            }
        }
    }

    public static void writeOntModelToFile(OntModel model, String fileName) {

        FileWriter out = null;
        try {

            out = new FileWriter(fileName);
            try {
                model.write(out, "RDF/XML");
            } finally {
                try {
                    out.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                // ignore
            }
        }
    }

    public static void analyseTriples(String inputFileName) {
        try {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
            
            // use the FileManager to find the input file
            InputStream in = FileManager.get().open(inputFileName);
            if (in == null) {
                throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
            }
            
            // read the RDF/XML file
            model.read(in, "RDF/XML");
            
            String queryString = "";
            
            queryString = "" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "PREFIX smlk: <http://ns.inria.fr/smilk/elements/1.0/>\n"
                    + "SELECT    ?sentence ?triple ?subjectform ?subjectPos ?subject_Type  ?objectForm ?objectPos ?object_Type (group_concat(distinct ?tokenform ; separator = \" \") AS ?relation)   (group_concat(distinct ?tokenPosTag ; separator = \" \") AS ?relationposTags)  \n"
                    + "WHERE "
                    + " {\n"
                    + "SELECT   ?sentence ?triple ?subjectform ?subjectPos ?subject_Type  ?objectForm ?objectPos ?object_Type   ?tokenOffset ?tokenform  ?tokenPosTag \n"
                    + "WHERE"
                    + " {\n"
                    + " ?document smlk:hasSentence ?sentence. \n"
                    + " ?sentence smlk:hasTriple ?triple. \n"
                    + " ?triple smlk:hasRelation ?token.\n"
                    + " ?token smlk:form ?tokenform. \n"
                    + " ?token smlk:pos ?tokenPosTag. \n"
                    + " ?token smlk:id ?tokenOffset. \n"
                    //                + " OPTIONAL{?token smlk:hasNext ?nextToken."
                    //                + " ?nextToken smlk:form ?nexttokenform."
                    //                + " ?nextToken smlk:pos ?nexttokePosTag } \n"
                    + " ?triple smlk:hasSubject ?subject. \n"
                    + " ?subject smlk:type ?subjectType.\n"
                    + " ?subject smlk:pos ?subjectPos.\n"
                    + " ?subject smlk:form ?subjectform.\n"
                    + " BIND(IF ((?subjectType=\"<NaN>\"), \"-\", ?subjectType ) as ?subject_Type)"
                    + " ?triple smlk:hasObject ?object.\n"
                    + " ?object smlk:type ?objectType.\n"
                    + " ?object smlk:pos ?objectPos.\n"
                    + " ?object smlk:form ?objectForm.\n"
                    + " BIND(IF ((?objectType=\"<NaN>\" ) , \"-\", ?objectType ) as ?object_Type)"
                    + "}\n"
                    // + "GROUP BY ?subjectform ?subject ?subject_Type   ?objectForm ?object_Type  ?subjectPos  ?objectPos  "
                    + "ORDER BY DESC(?triple) ASC(?tokenOffset)"
                    + ""
                    + "}\n"
                    + "GROUP BY  ?sentence ?triple  ?subjectform ?subject_Type   ?objectForm ?object_Type  ?subjectPos  ?objectPos  "
                    + "";
            
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
           // ResultSetFormatter.out(System.out, results, query);
            
            
            //convert sparql query results into string array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ResultSetFormatter.out(ps, results, query);
            String queryOutput = new String(baos.toByteArray(), "UTF-8");
            
            System.out.println(queryOutput);
//        try {
//
//
//                    
//                    for (; results.hasNext();) {
//                        QuerySolution soln = results.nextSolution();
//                        
//                        RDFNode rel = soln.get("rel");
//                        Resource r = (Resource) rel;
// 
//                        RDFNode cnt = soln.get("cnt");
//                        Literal rcnt = (Literal) cnt;
//                 
//                        System.out.println(r.getLocalName() + " " + rcnt.getInt());
// 
//                    }
//            
//                } finally {
//                    qexec.close();
//                }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void query(Model model) {
        String queryString = "";
        queryString = "" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX smlk: <http://ns.inria.fr/smilk/elements/1.0/>\n"
                + "SELECT distinct  ?subjectPos ?subject_Type  ?objectPos ?object_Type (group_concat(distinct ?form ; separator = \" \") AS ?tokens)   (group_concat(distinct ?tokePosTags ; separator = \" \") AS ?posTags)  \n"
                + "WHERE"
                + " {\n"
                + " ?triple smlk:hasRelation ?relationTokens.\n"
                + " ?triple smlk:hasSubject ?subject. \n"
                + " ?relationTokens rdfs:member ?token. \n"
                + " ?token smlk:form ?form. \n"
                + " ?token smlk:pos ?tokePosTags. \n"
                + " ?triple smlk:hasObject ?object.\n"
                + " ?object smlk:type ?objectType.\n"
                + " ?subject smlk:type ?subjectType.\n"
                + " ?object smlk:pos ?objectPos.\n"
                + " ?subject smlk:pos ?subjectPos.\n"
                + " BIND(IF ((?subjectType=\"<NaN>\"|| ?subjectType=\"not_identified\"), \"-\", ?subjectType ) as ?subject_Type)"
                + " BIND(IF ((?objectType=\"<NaN>\" || ?objectType=\"not_identified\") , \"-\", ?objectType ) as ?object_Type)"
                + "}\n"
                + "GROUP BY ?subject ?subject_Type  ?object_Type  ?subjectPos  ?objectPos  "
                + "ORDER BY DESC(?subject_Type)  DESC(?object_Type) "
                + "";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();
        ResultSetFormatter.out(System.out, results, query);

    }

    public static OntModel constructOntology() {

        // create an empty model
        OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String NS = RELATION.getURI();

        ontoModel.setNsPrefix("smlk", RELATION.getURI());
        ontoModel.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");

        try {

            // Classes
            OntClass annotatedDocument = ontoModel.createClass(NS + "Document");
            OntClass annotatedSentence = ontoModel.createClass(NS + "Sentence");
            OntClass triple = ontoModel.createClass(NS + "Triple");

            OntClass subject = ontoModel.createClass(NS + "Subject");
            OntClass object = ontoModel.createClass(NS + "Object");

            OntClass token = ontoModel.createClass(NS + "Token");

            // Relations
            ObjectProperty hasSentence = ontoModel.createObjectProperty(NS + "hasSentence");
            hasSentence.addDomain(annotatedDocument);
            hasSentence.addRange(annotatedSentence);
            hasSentence.addLabel("has sentence", "en");

            ObjectProperty hasTriple = ontoModel.createObjectProperty(NS + "hasTriple");
            hasTriple.addDomain(annotatedSentence);
            hasTriple.addRange(triple);
            hasTriple.addLabel("has triple", "en");

            ObjectProperty hasSubject = ontoModel.createObjectProperty(NS + "hasSubject");
            hasSubject.addDomain(triple);
            hasSubject.addRange(subject);
            hasSubject.addLabel("has triple", "en");

            ObjectProperty hasObject = ontoModel.createObjectProperty(NS + "hasObject");
            hasObject.addDomain(triple);
            hasObject.addRange(object);
            hasObject.addLabel("has triple", "en");

            ObjectProperty hasRelation = ontoModel.createObjectProperty(NS + "hasRelation");
            hasRelation.addDomain(triple);
            hasRelation.addRange(token);
            hasRelation.addLabel("has relation", "en");

            ObjectProperty hasNext = ontoModel.createObjectProperty(NS + "hasNext");
            ObjectProperty hasPrevious = ontoModel.createObjectProperty(NS + "hasPrevious");
            hasNext.addDomain(token);
            hasNext.addRange(token);
            hasNext.addLabel("has next", "en");
            hasPrevious.addLabel("has previous", "en");
            hasNext.addInverseOf(hasPrevious);

            // Data properties
            DatatypeProperty text = ontoModel.createDatatypeProperty(NS + "text");
            text.addDomain(ontoModel.getOntClass(NS + "Document"));
            text.addRange(XSD.xstring);

            DatatypeProperty form = ontoModel.createDatatypeProperty(NS + "form");
            form.addDomain(ontoModel.getOntClass(NS + "Subject"));
            form.addDomain(ontoModel.getOntClass(NS + "Object"));
            form.addDomain(ontoModel.getOntClass(NS + "Token"));
            form.addRange(XSD.xstring);

            DatatypeProperty lemma = ontoModel.createDatatypeProperty(NS + "lemma");
            lemma.addDomain(ontoModel.getOntClass(NS + "Subject"));
            lemma.addDomain(ontoModel.getOntClass(NS + "Object"));

            lemma.addRange(XSD.xstring);

            DatatypeProperty type = ontoModel.createDatatypeProperty(NS + "type");
            type.addDomain(ontoModel.getOntClass(NS + "Subject"));
            type.addDomain(ontoModel.getOntClass(NS + "Object"));
            type.addDomain(ontoModel.getOntClass(NS + "Token"));
            type.addRange(XSD.xstring);

            DatatypeProperty depRel = ontoModel.createDatatypeProperty(NS + "depRel");
            depRel.addDomain(ontoModel.getOntClass(NS + "Subject"));
            depRel.addDomain(ontoModel.getOntClass(NS + "Object"));
            depRel.addDomain(ontoModel.getOntClass(NS + "Token"));
            depRel.addRange(XSD.xstring);

            DatatypeProperty pos = ontoModel.createDatatypeProperty(NS + "pos");
            pos.addDomain(ontoModel.getOntClass(NS + "Subject"));
            pos.addDomain(ontoModel.getOntClass(NS + "Object"));
            pos.addDomain(ontoModel.getOntClass(NS + "Token"));
            pos.addRange(XSD.xstring);

            DatatypeProperty id = ontoModel.createDatatypeProperty(NS + "id");
            id.addDomain(ontoModel.getOntClass(NS + "Document"));
            id.addDomain(ontoModel.getOntClass(NS + "Sentence"));
            id.addDomain(ontoModel.getOntClass(NS + "Subject"));
            id.addDomain(ontoModel.getOntClass(NS + "Object"));
            id.addDomain(ontoModel.getOntClass(NS + "Token"));
            id.addRange(XSD.integer);

            DatatypeProperty start = ontoModel.createDatatypeProperty(NS + "start");
            start.addDomain(ontoModel.getOntClass(NS + "Subject"));
            start.addDomain(ontoModel.getOntClass(NS + "Object"));
            start.addDomain(ontoModel.getOntClass(NS + "Token"));
            start.addRange(XSD.integer);

            DatatypeProperty end = ontoModel.createDatatypeProperty(NS + "end");
            end.addDomain(ontoModel.getOntClass(NS + "Subject"));
            end.addDomain(ontoModel.getOntClass(NS + "Object"));
            end.addDomain(ontoModel.getOntClass(NS + "Token"));
            start.addRange(XSD.integer);

            DatatypeProperty head = ontoModel.createDatatypeProperty(NS + "head");
            head.addDomain(ontoModel.getOntClass(NS + "Subject"));
            head.addDomain(ontoModel.getOntClass(NS + "Object"));
            head.addDomain(ontoModel.getOntClass(NS + "Token"));
            head.addRange(XSD.integer);

        } catch (Exception e) {

            System.out.println("Failed: " + e);
        }

        return ontoModel;

    }

    public static OntModel constructOntModel(OntModel ontModel, AnnotatedDocument annoatedDocument) {

        String NS = RELATION.getURI();

        ObjectProperty hasSentence = ontModel.getObjectProperty(NS + "hasSentence");
        ObjectProperty hasTriple = ontModel.getObjectProperty(NS + "hasTriple");
        ObjectProperty hasSubject = ontModel.getObjectProperty(NS + "hasSubject");
        ObjectProperty hasObject = ontModel.getObjectProperty(NS + "hasObject");
        ObjectProperty hasRelation = ontModel.getObjectProperty(NS + "hasRelation");
        ObjectProperty hasNext = ontModel.getObjectProperty(NS + "hasNext");

        DatatypeProperty text = ontModel.getDatatypeProperty(NS + "text");
        DatatypeProperty head = ontModel.getDatatypeProperty(NS + "head");
        DatatypeProperty lemma = ontModel.getDatatypeProperty(NS + "lemma");
        DatatypeProperty depRel = ontModel.getDatatypeProperty(NS + "depRel");
        DatatypeProperty start = ontModel.getDatatypeProperty(NS + "start");
        DatatypeProperty end = ontModel.getDatatypeProperty(NS + "end");
        DatatypeProperty type = ontModel.getDatatypeProperty(NS + "type");
        DatatypeProperty pos = ontModel.getDatatypeProperty(NS + "pos");
        DatatypeProperty id = ontModel.getDatatypeProperty(NS + "id");
        DatatypeProperty form = ontModel.getDatatypeProperty(NS + "form");

        //add document individual to ontModel
        String docId = String.format("%05d", (int) annoatedDocument.getId());
        Individual document = ontModel.createIndividual(NS + "doc_" + docId, ontModel.getOntClass(NS + "Document"));

        document.addProperty(text, ontModel.createTypedLiteral(annoatedDocument.getText()))
                .addProperty(id, ontModel.createTypedLiteral(docId));

        //add sentence individual to ontmodel
        List<Sentence> sentences = annoatedDocument.getSentences();
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            String sentId = String.format("%05d", (int) sentence.getId());
            Individual sent = ontModel.createIndividual(NS + "doc_" + docId + "-" + sentId, ontModel.getOntClass(NS + "Sentence"));
            sent.addProperty(id, ontModel.createTypedLiteral(sentId));

            document.addProperty(hasSentence, sent);

            List<Triple> triples = sentence.getTriples();
            for (int j = 0; j < triples.size(); j++) {
                String tripleId = String.format("%04d", (int) j);
                Triple t = triples.get(j);

                Individual triple = ontModel.createIndividual(NS + "t_" + docId + "-" + sentId + "-" + tripleId, ontModel.getOntClass(NS + "Triple"));
                //assign triple to sentence
                sent.addProperty(hasTriple, triple);

                Individual subject = ontModel.createIndividual(NS + "s_" + docId + "-" + sentId + "-" + tripleId, ontModel.getOntClass(NS + "Subject"));

                //assign subject to triple
                triple.addProperty(hasSubject, subject);
                //define the data properties of subject
                subject.addProperty(lemma, ontModel.createTypedLiteral(t.getSubjectToken().getLema()));
                subject.addProperty(form, ontModel.createTypedLiteral(t.getSubjectToken().getForm()));
                subject.addProperty(type, ontModel.createTypedLiteral(t.getSubjectToken().getType()));
                subject.addProperty(pos, ontModel.createTypedLiteral(t.getSubjectToken().getPos()));
                subject.addProperty(head, ontModel.createTypedLiteral(t.getSubjectToken().getHead()));
                subject.addProperty(start, ontModel.createTypedLiteral(t.getSubjectToken().getStart()));
                subject.addProperty(end, ontModel.createTypedLiteral(t.getSubjectToken().getEnd()));
                subject.addProperty(depRel, ontModel.createTypedLiteral(t.getSubjectToken().getDepRel()));
                subject.addProperty(id, ontModel.createTypedLiteral(t.getSubjectToken().getId()));

                Individual object = ontModel.createIndividual(NS + "o_" + docId + "-" + sentId + "-" + tripleId, ontModel.getOntClass(NS + "Object"));
                //assign object to triple
                triple.addProperty(hasObject, object);
                //define the data properties of object
                object.addProperty(lemma, ontModel.createTypedLiteral(t.getObjectToken().getLema()));
                object.addProperty(form, ontModel.createTypedLiteral(t.getObjectToken().getForm()));
                object.addProperty(type, ontModel.createTypedLiteral(t.getObjectToken().getType()));
                object.addProperty(pos, ontModel.createTypedLiteral(t.getObjectToken().getPos()));
                object.addProperty(head, ontModel.createTypedLiteral(t.getObjectToken().getHead()));
                object.addProperty(start, ontModel.createTypedLiteral(t.getObjectToken().getStart()));
                object.addProperty(end, ontModel.createTypedLiteral(t.getObjectToken().getEnd()));
                object.addProperty(depRel, ontModel.createTypedLiteral(t.getObjectToken().getDepRel()));
                object.addProperty(id, ontModel.createTypedLiteral(t.getObjectToken().getId()));

                LinkedList<Token> tokens = t.getRelationTokens();

                for (int k = 0; k < tokens.size(); k++) {

                    String tokenId = String.format("%04d", (int) k);
                    Token token = tokens.get(k);
                    Individual tok = ontModel.createIndividual(NS + "tok_" + docId + "-" + sentId + "-" + tripleId + "-" + tokenId, ontModel.getOntClass(NS + "Token"));

                    //assign relation token to triple
                    triple.addProperty(hasRelation, tok);

                    //define the data properties of  relation token
                    tok.addProperty(lemma, ontModel.createTypedLiteral(token.getLema()));
                    tok.addProperty(form, ontModel.createTypedLiteral(token.getForm()));

                    tok.addProperty(pos, ontModel.createTypedLiteral(token.getPos()));
                    tok.addProperty(head, ontModel.createTypedLiteral(token.getHead()));
                    tok.addProperty(start, ontModel.createTypedLiteral(token.getStart()));
                    tok.addProperty(end, ontModel.createTypedLiteral(token.getEnd()));
                    tok.addProperty(depRel, ontModel.createTypedLiteral(token.getDepRel()));
                    tok.addProperty(id, ontModel.createTypedLiteral(token.getId()));

                }

                if (tokens.size() > 1) {

                    int x = 0;
                    int y = x + 1;
                    while (x < tokens.size() && y < tokens.size()) {
                        String tokenId = String.format("%04d", (int) x);
                        String nextTokenId = String.format("%04d", (int) y);
                        Individual tok = ontModel.getIndividual(NS + "tok_" + docId + "-" + sentId + "-" + tripleId + "-" + tokenId);
                        Individual nextTok = ontModel.getIndividual(NS + "tok_" + docId + "-" + sentId + "-" + tripleId + "-" + nextTokenId);
                        tok.addProperty(hasNext, nextTok);
                        x++;
                        y = x + 1;

                    }

                }

            }

        }

        return ontModel;

    }

    public static List<String> readCorpus(String folderName) {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folderName);
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
       
        int i=0;
        for (String file : files) {
            System.out.println("Processing file #: "+ i + " of:  "+ files.size());
            i++;
            try {
                BufferedReader fileReader = null;

                String line = "";
                //Create the file reader
                fileReader = new BufferedReader(new FileReader(folderName+"/"+file));
                while ((line = fileReader.readLine()) != null) {
                    if (line.trim().length() > 1) {
                       
                        lines.add(line);
                    }

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return lines;
    }

}

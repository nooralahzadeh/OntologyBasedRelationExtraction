/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction;

/**
 *
 * @author fnoorala
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.ws.BindingProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.net.URI;
import javax.xml.namespace.QName;

public class Renco {

    public String rencoByWebService(String in) throws Exception {
    String sortie="";
    try {

            Client client = Client.create();
            String url="https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/renco/getRenco";
            String tempURL="https://172.42.1.166/renco-rest/rest/renco/getRenco";
            WebResource webResource = client.resource(url);

            ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, in);

             sortie = response.getEntity(String.class);

        System.out.println(sortie);
            

        } catch (Exception e) {
            System.out.println("ERROR in the text: " + in);
            e.printStackTrace();

        }
    return sortie;
    }
    
    
   
}

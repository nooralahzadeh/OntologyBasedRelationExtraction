/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 *
 * @author farhadzn
 */
public class openNLP {
 SentenceDetector _sentenceDetector = null;
public openNLP() {
       
 
InputStream modelIn = null;
try {
   // Loading sentence detection model
   modelIn = getClass().getResourceAsStream("/fr-sent.bin");
   final SentenceModel sentenceModel = new SentenceModel(modelIn);
   modelIn.close();
 
   _sentenceDetector = new SentenceDetectorME(sentenceModel);
    
    } catch (final IOException ioe) {
   ioe.printStackTrace();
} finally {
   if (modelIn != null) {
      try {
         modelIn.close();
      } catch (final IOException e) {} // oh well!
   }
}
}
 
    
public String[] senenceSegmentation(String content){
    

   String[] Sentences=_sentenceDetector.sentDetect(content);
   return Sentences;

    }
}

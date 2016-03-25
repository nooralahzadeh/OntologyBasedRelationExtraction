/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.smilk.ws.relationextraction;

import java.util.List;

/**
 *
 * @author fnoorala
 */
public class AnnotatedDocument {

    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Sentence> getSentences() {
        return Sentences;
    }

    public void setSentences(List<Sentence> Sentences) {
        this.Sentences = Sentences;
    }
    
    
    private int id;
    private List<Sentence> Sentences;
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    
    
}

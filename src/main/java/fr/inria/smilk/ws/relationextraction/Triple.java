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
public class Triple {

    public Token getSubjectToken() {
        return subjectToken;
    }

    public void setSubjectToken(Token subjectToken) {
        this.subjectToken = subjectToken;
    }

    public Token getObjectToken() {
        return objectToken;
    }

    public void setObjectToken(Token objectToken) {
        this.objectToken = objectToken;
    }

    public List<Token> getRelationTokens() {
        return relationTokens;
    }

    public void setRelationTokens(List<Token> relationTokens) {
        this.relationTokens = relationTokens;
    }

    @Override
    public String toString() {
        return this.getSubjectType() + " "+ this.getRelation() +" "+ this.getObjectType() ;
                }

    public Triple() {
    }

    public Triple(String subject, String relation, String object, String subjectType, String objectType) {
        this.subject = subject;
        this.relation = relation;
        this.object = object;
        this.subjectType = subjectType;
        this.objectType = objectType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    
    private String subject;
    private String relation;
    private String object;
    private String subjectType;
    private String objectType;
    
    
   private Token subjectToken;
   private Token objectToken;
   private List<Token> relationTokens;
    
    
}

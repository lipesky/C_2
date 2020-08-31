package com.example.commons;
import java.io.Serializable;

public class Email implements Serializable {
    private String dest;
    private String from;
    private String subject;
    private String body;

    public Email(String dest, String from, String subject, String body) {
        this.dest = dest;
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    // Getters and Setters

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    // Custom equals to correctly compare two Email objects
    public boolean equals( Object obj) {
        if(obj instanceof Email){
            Email email= (Email) obj;
            return (this.subject.equals(email.subject) &&
                    this.body.equals(email.body) &&
                    this.dest.equals(email.dest) &&
                    this.from.equals(email.from));
        }else{
            return super.equals(obj);
        }

    }


}

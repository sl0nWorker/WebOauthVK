package com.slon.testWebOauthVK.authorization.entities;

import javax.persistence.*;

@Entity // This tells Hibernate to make a table out of this class
public class User {
    @Id
    @Column
    private Integer idtoken;
    @Column(name = "userip")
    private String userip;
    @Column(name = "token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserip() {
        return userip;
    }

    public void setUserip(String userip) {
        this.userip = userip;
    }

    public Integer getIdtoken() {
        return idtoken;
    }

    public void setIdtoken(Integer idtoken) {
        this.idtoken = idtoken;
    }
}

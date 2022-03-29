package com.example.geschenkapp;

import java.sql.ResultSet;

//Data Holder for saving personal user data after log in
@SuppressWarnings("unused")
public class LoginHolder {
    private ResultSet user;
    public ResultSet getUser() {return user;}
    public void setUser(ResultSet user) {this.user = user;}

    private static final LoginHolder holder = new LoginHolder();
    public static LoginHolder getInstance() {return holder;}
}

package com.example.geschenkapp;

import java.sql.ResultSet;

//Data Holder for saving personal user data after log in
public class DataHolder {
    private ResultSet user;
    public ResultSet getUser() {return user;}
    public void setUser(ResultSet user) {this.user = user;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}

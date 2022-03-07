package com.example.geschenkapp;


public class DbHolder {
    private DbConnector db;
    public DbConnector getDb() {return db;}
    public void setDb(DbConnector db) {this.db = db;}

    private static final DbHolder holder = new DbHolder();
    public static DbHolder getInstance() {return holder;}
}

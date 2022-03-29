package com.example.geschenkapp;

//Data holder for database connector in order to access database across activities
@SuppressWarnings("unused")
public class DbHolder {
    private DbConnector db;
    public DbConnector getDb() {return db;}
    public void setDb(DbConnector db) {this.db = db;}

    private static final DbHolder holder = new DbHolder();
    public static DbHolder getInstance() {return holder;}
}

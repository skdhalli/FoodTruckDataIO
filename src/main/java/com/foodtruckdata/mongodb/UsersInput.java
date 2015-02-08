/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foodtruckdata.mongodb;

import com.foodtruckdata.interfaces.IUsersInput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author sdhalli
 */
public class UsersInput implements IUsersInput {

    public UsersInput(String host, int port, String db, String userName, String passwd) throws UnknownHostException
    {
        MongoCredential credential = MongoCredential.createMongoCRCredential(userName, "admin", passwd.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        mongoDB = mongoClient.getDB(db);
    }
    
    protected void finalize()
    {
        mongoClient.close();
    }
    
    private MongoClient mongoClient;
    private DB mongoDB;
    
    @Override
    public String AddTruck(String title, String logo, String menu, String phone, String email) {
        BasicDBObject document = new BasicDBObject();
        document.put("title", title);
        document.put("phone", phone);
        document.put("email", email);
        DBCollection coll = mongoDB.getCollection("Trucks");
        String retval = coll.insert(document).toString();
        return retval;
    }

    @Override
    public void AddUser() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void AddSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void FollowTruck() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void RateTruck() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foodtruckdata.mongodb;

import com.foodtruckdata.interfaces.IUsersInput;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bson.types.ObjectId;

/**
 *
 * @author sdhalli
 */
public class UsersInput implements IUsersInput {

    public UsersInput(String host, int port, String db, String userName, String passwd) throws UnknownHostException
    {
        MongoCredential credential = MongoCredential.createMongoCRCredential(userName, db, passwd.toCharArray());
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
    public String AddTruck(String title, String logo_img, String menu_img, String phone, String email, String username, String password) {
        BasicDBObject document = new BasicDBObject();
        document.put("title", title);
        document.put("phone", phone);
        document.put("email", email);
        document.put("Schedules", (new Object[]{}));
        document.put("Followers", (new Object[]{}));
        document.put("Ratings", (new Object[]{}));
        document.put("username", username);
        document.put("password", password);
        DBCollection coll = mongoDB.getCollection("Trucks");
        coll.insert(document);
        ObjectId truck_id = (ObjectId)document.get( "_id" );
        if(logo_img != "")
        {
            this.storeFile(logo_img, title, truck_id);
        }
        if(menu_img != "")
        {
            this.storeFile(menu_img, title, truck_id);
        }
        return truck_id.toString();
    }

    @Override
    public boolean TruckExists(String title, String email)
    {
        BasicDBObject filter = new BasicDBObject();
        BasicDBObject clause1 = new BasicDBObject("title", title);
        BasicDBObject clause2 = new BasicDBObject("email", email);
        filter.put("$or", new BasicDBObject[]{clause1, clause2});
        DBCollection coll = mongoDB.getCollection("Trucks");
        return coll.count(filter) > 0;
    }
    
    @Override
    public String AddUser(String firstName, String lastName, String email,Double lat_h, Double lng_h, Double lat_w, Double lng_w)
    {
        BasicDBObject document = new BasicDBObject();
        document.put("FirstName", firstName);
        document.put("LastName", lastName);
        document.put("Email", email);
        document.put("Latitude_home", lat_h);
        document.put("Longitude_home", lng_h);
        document.put("Latitude_work", lat_w);
        document.put("Longitude_work", lng_w);
        
        DBCollection coll = mongoDB.getCollection("Users");
        coll.insert(document);
        ObjectId user_id = (ObjectId)document.get( "_id" );
        return user_id.toString();
    }

    @Override
    public void AddSchedule(Date dateTime, String address, String truck_id) {
        //remove all outdated schedules
        BasicDBObject filter = new BasicDBObject("_id", new ObjectId(truck_id));
        DBCollection coll = mongoDB.getCollection("Trucks");
        Cursor cursor = coll.find(filter);
        if(cursor.hasNext())
        {
            BasicDBObject truck = (BasicDBObject)cursor.next();
            BasicDBObject[] schedules = (BasicDBObject[])truck.get("Schedules");
            for(BasicDBObject schedule:schedules)
            {
                if(((Date)schedule.get("Time")).compareTo(new Date()) < 0)
                {
                    coll.update(filter, (new BasicDBObject("$pull",(new BasicDBObject("Schedules",schedule)))));
                }
            }
        }
        //insert the new schedule
        if(dateTime.compareTo((new Date())) > 0)
        {
            BasicDBObject schedule = new BasicDBObject();
            schedule.put("Time", dateTime);
            schedule.put("Address",address);
            //put lat long also
            coll.update(filter, (new BasicDBObject("$addToSet",(new BasicDBObject("Schedules",schedule)))));
        }
    }

    @Override
    public void FollowTruck(String truck_id, String user_id) {
        BasicDBObject filter = new BasicDBObject("_id", new ObjectId(truck_id));
        DBCollection coll = mongoDB.getCollection("Trucks");
        Cursor cursor = coll.find(filter, (new BasicDBObject("$elemMatch",new BasicDBObject("Followers",new ObjectId(user_id)))));
        if(!cursor.hasNext())
        {
            coll.update(filter, (new BasicDBObject("$addToSet",(new BasicDBObject("Trucks.Followers",new BasicDBObject("_id",new ObjectId(user_id)))))));
        }
    }
    
    @Override
    public void UnFollowTruck(String truck_id, String user_id) {
        BasicDBObject filter = new BasicDBObject("_id", new ObjectId(truck_id));
        DBCollection coll = mongoDB.getCollection("Trucks");
        coll.update(filter, (new BasicDBObject("$pull",(new BasicDBObject("Trucks.Followers",new BasicDBObject("_id",new ObjectId(user_id)))))));
    }

    @Override
    public void RateTruck(String truck_id, String user_id, int rating) {
        //remove original rating
        BasicDBObject filter = new BasicDBObject("_id", new ObjectId(truck_id));
        DBCollection coll = mongoDB.getCollection("Trucks");
        coll.update(filter, (new BasicDBObject("$pull",(new BasicDBObject("Trucks.Ratings.UserID",new BasicDBObject("_id",new ObjectId(user_id)))))));
        coll.update(filter, (new BasicDBObject("$addToSet",(new BasicDBObject("Trucks.Ratings.UserID",new BasicDBObject("_id",new ObjectId(user_id)))))));
    }
    
    private InputStream getFTPInputStream(String ftp_location)
    {
        InputStream retval = null;
            
        String server = "162.219.245.61";
        int port = 21;
        String user = "jelastic-ftp";
        String pass = "HeZCHxeefB";
        FTPClient ftpClient = new FTPClient();
        
        try 
        {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            retval = ftpClient.retrieveFileStream(ftp_location);
        } catch (IOException ex) {
            Logger.getLogger(UsersInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally 
        {
            try 
            {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(UsersInput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return retval;
    }
    
    private void storeFile(String filepath, String filetype, ObjectId truck_id)
    {
        try {
                GridFS gridFS = new GridFS(mongoDB);
                InputStream file_stream = getFTPInputStream(filepath);
                StringWriter writer = new StringWriter();
                Charset par = null;
                IOUtils.copy(file_stream, writer, par);
                GridFSInputFile in = gridFS.createFile(file_stream);
                in.setFilename(filepath);
                in.put("TruckID", truck_id);
                in.put("FileType", filetype);
                in.save();
            } catch (IOException ex) {
                Logger.getLogger(UsersInput.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}

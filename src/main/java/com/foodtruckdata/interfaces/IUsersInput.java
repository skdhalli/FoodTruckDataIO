/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foodtruckdata.interfaces;

import java.util.Date;

/**
 *
 * @author sdhalli
 */
public interface IUsersInput {
    String AddTruck(String title, String logo, String menu, String phone_num, String email_address, String username, String password);
    boolean TruckExists(String title, String email_address);
    String AddUser(String firstName, String lastName, String email,Double lat_h, Double lng_h, Double lat_w, Double lng_w);
    void AddSchedule(Date dateTime, String address, String truck_id);
    void FollowTruck(String truck_id, String user_id);
    void UnFollowTruck(String truck_id, String user_id);
    void RateTruck(String truck_id, String user_id, int rating);
}

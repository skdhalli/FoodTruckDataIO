/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foodtruckdata.interfaces;

/**
 *
 * @author sdhalli
 */
public interface IUsersInput {
    String AddTruck(String title, String logo, String menu, String phone_num, String email_address);
    void AddUser();
    void AddSchedule();
    void FollowTruck();
    void RateTruck();
}

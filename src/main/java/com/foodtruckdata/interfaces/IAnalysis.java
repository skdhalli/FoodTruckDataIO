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
public interface IAnalysis {
    String[] GetNearByTrucks();
    String GetTruckSchedule();
    String GetTruckMenu();
    String GetTruckInfo();
    
    String GetFollowers();
    String GetHotSpots();        
}

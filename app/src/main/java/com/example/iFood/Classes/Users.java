package com.example.iFood.Classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**

 * Class holds information regarding the User information
 */
public class Users {

    private String Username;
    public String pic_url;
    public String Email;
    public String Phone;
    public String Fname;
    public String Lname;
    public String userRole;
    public String uid;
    public Object timestamp;


    public Users(){}
    public Users(String username, String email, String phone, String fname, String lname,String Pic,String roleUser,String uID) {
        Username = username;
        Email = email;
        Phone = phone;
        Fname = fname;
        Lname = lname;
        pic_url = Pic;
        userRole=roleUser;
        timestamp = ServerValue.TIMESTAMP;
        uid = uID;

    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return Username;
    }

    public String getPic_url(){
        return pic_url;
    }

    public Object getTimestamp() {
        return timestamp;
    }
    @Exclude
    public long timestamp() {
        return (long) timestamp;
    }

    @Override
    public String toString() {
        return "Users{" +
                "Username='" + Username + '\'' +
                ", pic_url='" + pic_url + '\'' +
                ", Email='" + Email + '\'' +
                ", Phone=" + Phone +
                ", Fname='" + Fname + '\'' +
                ", Lname='" + Lname + '\'' +
                ", userRole='" + userRole + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
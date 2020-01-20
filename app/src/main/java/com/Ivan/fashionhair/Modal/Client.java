package com.Ivan.fashionhair.Modal;

import java.util.Comparator;

public class Client implements Comparable<Client> {
    public String phoneNumber;
    public String userName;
    public String requestTime;
    public String state;
    public long orderNo;
    public String uid;
    public boolean ready;

    //required default constructor
    public Client() {
    }

    public Client(String uid, String phoneNumber, String userName, String time, String state, long orderNo, boolean ready) {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.requestTime = time;
        this.state = state;
        this.orderNo = orderNo;
        this.uid = uid;
        this.ready = ready;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public long getOrderNo() {
        return orderNo;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRequestTime(String requestTime)
    {
        this.requestTime = requestTime;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public int compareTo(Client compareClient) {

        long compareDate = ((Client) compareClient).getOrderNo();

        //ascending order
        int result = (int) (this.orderNo - compareDate);
        return result;

        //descending order
        //return compareQuantity - this.quantity;

    }
}
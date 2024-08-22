/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nicol
 */
public class TramasRemoto_SEL735 {
    private byte[] firmware = {(byte) 0x49, (byte) 0x44, (byte) 0x0D, (byte) 0x0A };
    private byte[] id = {(byte) 0x51, (byte) 0x55, (byte) 0x49, (byte) 0x0D}; 
    private List<byte[]> users = new ArrayList<>();
    private List<byte[]> passwords = new ArrayList<>();
    private byte[] displaySetting = { (byte) 0x53, (byte) 0x48, (byte) 0x4F, (byte) 0x0D };
    private byte[] date = { (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x0D, (byte) 0x0A };
    private byte[] setDate = { (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x20, (byte) 0x30, (byte) 0x34, (byte) 0x2F, (byte) 0x30, (byte) 0x34, (byte) 0x2F, (byte) 0x32, (byte) 0x34, (byte) 0x0D };
    private byte[] time = { (byte) 0x54, (byte) 0x49, (byte) 0x4D, (byte) 0x0D, (byte) 0x0A };
    private byte[] setTime = { (byte) 0x54, (byte) 0x49, (byte) 0x4D, (byte) 0x20, (byte) 0x31, (byte) 0x30, (byte) 0x3A, (byte) 0x35, (byte) 0x35, (byte) 0x3A, (byte) 0x31, (byte) 0x35, (byte) 0x0D };
    private byte[] loadProfileSettings = {(byte) 0x46, (byte) 0x49, (byte) 0x4C, (byte) 0x45, (byte) 0x20, (byte) 0x52, (byte) 0x45, (byte) 0x41, (byte) 0x44, (byte) 0x20, (byte) 0x53, (byte) 0x45, (byte) 0x54, (byte) 0x5F, (byte) 0x31, (byte) 0x2E, (byte) 0x54, (byte) 0x58, (byte) 0x54, (byte) 0x0D};
    private byte[] loadProfileSettingsR = {(byte) 0x46, (byte) 0x49, (byte) 0x4C, (byte) 0x45, (byte) 0x20, (byte) 0x52, (byte) 0x45, (byte) 0x41, (byte) 0x44, (byte) 0x20, (byte) 0x53, (byte) 0x45, (byte) 0x54, (byte) 0x5F, (byte) 0x52, (byte) 0x2E, (byte) 0x54, (byte) 0x58, (byte) 0x54, (byte) 0x0D};   
    private byte[] loadProfile = {(byte) 0x46, (byte) 0x49, (byte) 0x4C, (byte) 0x45, (byte) 0x20, (byte) 0x52, (byte) 0x45, (byte) 0x41, (byte) 0x44, (byte) 0x20, (byte) 0x4C, (byte) 0x44, (byte) 0x50, (byte) 0x5F, (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x41, (byte) 0x2E, (byte) 0x42, (byte) 0x49, (byte) 0x4E, (byte) 0x20, (byte) 0x31, (byte) 0x32, (byte) 0x2F, (byte) 0x31, (byte) 0x39, (byte) 0x2F, (byte) 0x32, (byte) 0x30, (byte) 0x32, (byte) 0x33, (byte) 0x20, (byte) 0x31, (byte) 0x31, (byte) 0x3A, (byte) 0x31, (byte) 0x35, (byte) 0x3A, (byte) 0x30, (byte) 0x30, (byte) 0x20, (byte) 0x31, (byte) 0x32, (byte) 0x2F, (byte) 0x31, (byte) 0x39, (byte) 0x2F, (byte) 0x32, (byte) 0x30, (byte) 0x32, (byte) 0x33, (byte) 0x20, (byte) 0x31, (byte) 0x35, (byte) 0x3A, (byte) 0x35, (byte) 0x38, (byte) 0x3A, (byte) 0x30, (byte) 0x30, (byte) 0x0D};
    private byte[] events = {(byte) 0x53, (byte) 0x45, (byte) 0x52, (byte) 0x0D};
    
    protected static byte[] Hexhars = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'};

    public TramasRemoto_SEL735() {
        inicializarUsersAndPwds();
    }    
    
    
    public final void inicializarUsersAndPwds() {
        //Users
        users.add( new byte[]{ (byte) 0x41, (byte) 0x43, (byte) 0x43, (byte) 0x0D }); //ACC
        users.add( new byte[]{ (byte) 0x45, (byte) 0x41, (byte) 0x43, (byte) 0x0D }); //EAC
        users.add( new byte[]{ (byte) 0x32, (byte) 0x41, (byte) 0x43, (byte) 0x0D }); //2AC
        users.add( new byte[]{ (byte) 0x43, (byte) 0x41, (byte) 0x4C, (byte) 0x0D }); //CAL
        //Default Passwords 
        passwords.add( new byte[]{ (byte) 0x4F, (byte) 0x54, (byte) 0x54, (byte) 0x45, (byte) 0x52 } ); // OTTER
        passwords.add( new byte[]{ (byte) 0x42, (byte) 0x4C, (byte) 0x4F, (byte) 0x4E, (byte) 0x44, (byte) 0x45, (byte) 0x4C } ); // BLONDEL
        passwords.add( new byte[]{ (byte) 0x54, (byte) 0x41, (byte) 0x49, (byte) 0x4C } ); // TAIL
        passwords.add( new byte[]{ (byte) 0x50, (byte) 0x41, (byte) 0x50, (byte) 0x4F, (byte) 0x55, (byte) 0x4C, (byte) 0x49, (byte) 0x53 } ); // PAPOULIS
    }

    public byte[] getFirmware() {
        return firmware;
    }

    public void setFirmware(byte[] firmware) {
        this.firmware = firmware;
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public List<byte[]> getUsers() {
        return users;
    }

    public void setUsers(List<byte[]> users) {
        this.users = users;
    }

    public List<byte[]> getPasswords() {
        return passwords;
    }

    public void setPasswords(List<byte[]> passwords) {
        this.passwords = passwords;
    }

    public byte[] getDisplaySetting() {
        return displaySetting;
    }

    public void setDisplaySetting(byte[] displaySetting) {
        this.displaySetting = displaySetting;
    }

    public byte[] getDate() {
        return date;
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

    public byte[] getTime() {
        return time;
    }

    public void setTime(byte[] time) {
        this.time = time;
    }

    public byte[] getSetDate() {
        return setDate;
    }

    public void setSetDate(byte[] setDate) {
        this.setDate = setDate;
    }

    public byte[] getSetTime() {
        return setTime;
    }

    public void setSetTime(byte[] setTime) {
        this.setTime = setTime;
    }        

    public byte[] getLoadProfileSettings() {
        return loadProfileSettings;
    }

    public void setLoadProfileSettings(byte[] loadProfileSettings) {
        this.loadProfileSettings = loadProfileSettings;
    }

    public byte[] getLoadProfileSettingsR() {
        return loadProfileSettingsR;
    }

    public void setLoadProfileSettingsR(byte[] loadProfileSettingsR) {
        this.loadProfileSettingsR = loadProfileSettingsR;
    }

    public byte[] getLoadProfile() {
        return loadProfile;
    }

    public void setLoadProfile(byte[] loadProfile) {
        this.loadProfile = loadProfile;
    }       
    
    public byte[] getEvents() {
        return events;
    }

    public void setEvents(byte[] events) {
        this.events = events;
    }
    
    public String encode(byte[] b, int ancho) {

        StringBuilder s = new StringBuilder(2 * b.length);

        for (int i = 0; i < ancho; i++) {

            int v = b[i] & 0xff;
            if (i != 0) {
                s.append(" ");
            }
            s.append((char) Hexhars[v >> 4]);
            s.append((char) Hexhars[v & 0xf]);
        }

        return s.toString().toUpperCase();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servtelesimex;

import Control.ControlProcesos;
import Entidades.Abortar;

import Entidades.Ecaller;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author dperez
 */
public class LeeListaMedidoresProg extends Thread {

    ProgramacionMedidores lcm;
    //ProtocoloApplet pf;
    List<Ecaller> medidores;
    LeeMedidor lm = null;
    Thread tr;
    ControlProcesos cp;
    File filep;
    Date horafin;
    private static final HashMap<String, AtomicInteger> connectionsMap = new HashMap<>();
    //static private AtomicInteger conexSimultaneas = new AtomicInteger(0);
            
    public LeeListaMedidoresProg(List<Ecaller> medidores, ProgramacionMedidores lcm, ControlProcesos cp, File filep, Date horafin) {        
        this.lcm = lcm;
        this.medidores = medidores;
        this.cp = cp;
        this.filep = filep;
        this.horafin = horafin;
    }
    
    public static AtomicInteger getConnCounter(String idProg) {
        return connectionsMap.get(idProg);
    }
    
    public static void assignConnCounter2Prog( String idProg ) {
        connectionsMap.put( idProg, new AtomicInteger(0) );
    }
    
    public static void deleteConnCounter2Prog( String idProg ) {
        connectionsMap.remove( idProg );
    }

    public static int getConexSimultaneas( String idProg ) {
        AtomicInteger tempConnCounter = getConnCounter(idProg);
        if (tempConnCounter != null) {
            return tempConnCounter.get();
        } else {
            return 0;
        }               
    }

    public static void setConexSimultaneas( int conexSimultaneas, String idProg ) {
        AtomicInteger tempConnCounter = getConnCounter(idProg);
        if (tempConnCounter != null) {
            tempConnCounter.set(conexSimultaneas);
        }             
    }

    public static void incrementaConexSimultaneas( String idProg ) {
        AtomicInteger tempConnCounter = getConnCounter(idProg);
        while(true && tempConnCounter != null){
            int currentValue = getConexSimultaneas( idProg );
            int newValue = currentValue + 1;
            if (tempConnCounter.compareAndSet(currentValue,newValue)){
                return;
            }
        }        
    }

    public static void decrementaConexSimultaneas( String idProg ) {
        AtomicInteger tempConnCounter = getConnCounter(idProg);
        while(true && tempConnCounter != null){
            int currentValue = getConexSimultaneas( idProg );
            int newValue = currentValue - 1;
            if (tempConnCounter.compareAndSet(currentValue,newValue)){
                return;
            }
        }  
    }

    @Override
    public void run() {
        if (medidores.size() == 1) { //No es cola si no conexiones TCP independientes    
            Ecaller tempCaller = medidores.get(0);
//            System.out.println("Lee medidor: " + tempCaller.getMedidor().getnSerie()+(tempCaller.isDisconnect()?" - Desconexión de Relé": ""));
            lm = new LeeMedidor(tempCaller.getMedidor(), tempCaller.getConfmodem(), tempCaller.isPerfil(), false , tempCaller.isEventos(), tempCaller.isRegistros(), tempCaller.isAcumulados(), tempCaller.isConfhora(), tempCaller.isDisconnect(), tempCaller.isReconnect(), cp, lcm.objabortar, ProgramacionMedidores.getZid(), ProgramacionMedidores.getNDesfase(), true);            
            try {
                lm.medir(false, horafin);
            } catch (Exception e) {
                    System.err.println(getErrorString(e.getStackTrace(), 3));
            }
            cp.escribir(filep, "El medidor marca:"+tempCaller.getMedidor().getMarcaMedidor().getNombre()+" y serie: "+tempCaller.getMedidor().getnSerie()+" ha finalizado.");
        } else if (medidores.size() > 1){ // Es una cola
            int lastIndex = medidores.get(medidores.size() - 1).getIndex();
            boolean isTheLast = false;
            for (Ecaller med: medidores) {
                int index = med.getIndex();
                isTheLast = index == lastIndex;
                lm = new LeeMedidor(med.getMedidor(), med.getConfmodem(), med.isPerfil(), false , med.isEventos(), med.isRegistros(), med.isAcumulados(), med.isConfhora(), med.isDisconnect(), med.isReconnect(), cp, lcm.objabortar, ProgramacionMedidores.getZid(), ProgramacionMedidores.getNDesfase(), isTheLast);
                try {   
                    lm.medir(false, horafin);
                } catch (Exception e) {
                    System.err.println(getErrorString(e.getStackTrace(), 3));
                }
                cp.escribir(filep, "El medidor marca:"+med.getMedidor().getMarcaMedidor().getNombre()+" y serie: "+med.getMedidor().getnSerie()+" ha finalizado.");
            }
            System.out.println("continua");
        }
    }
    private String getErrorString(StackTraceElement ste[], int depthOfError) {
        String error = "";
        int len = ste.length >= depthOfError ? depthOfError : ste.length;
        for (int i = 0; i < len; i++) {
            error += ste[i];
        }
        return error;
    }
}
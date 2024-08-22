/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

/**
 *
 * @author dperez
 */
public class EConfModem {
    public String peticion0;
    public String peticion1;
    public String peticion2;
    public String peticion3;
    public String peticion4;
    public String peticion5;
    public String peticion6;
    public String peticion7;
    public String peticion8;
    public String peticion9;
    

    public EConfModem() {
    }

    public String getpeticion(int n){
        switch (n){
            case 0:{
                return getPeticion0();
            }
            case 1:{
                return getPeticion1();
            }
            case 2:{
                return getPeticion2();
            }
            case 3:{
                return getPeticion3();
            }
            case 4:{
                return getPeticion4();
            }
            case 5:{
                return getPeticion5();
            }
            case 6:{
                return getPeticion6();
            }
            case 7:{
                return getPeticion7();
            }
            case 8:{
                return getPeticion8();
            }
            case 9:{
                return getPeticion9();
            }

        }
        return "";
    }

    public String getPeticion0() {
        return peticion0;
    }

    public void setPeticion0(String peticion0) {
        this.peticion0 = peticion0;
    }

    
    
    public String getPeticion1() {
        return peticion1;
    }

    public void setPeticion1(String peticion1) {
        this.peticion1 = peticion1;
    }
      

    public String getPeticion2() {
        return peticion2;
    }

    public void setPeticion2(String peticion2) {
        this.peticion2 = peticion2;
    }

    public String getPeticion3() {
        return peticion3;
    }

    public void setPeticion3(String peticion3) {
        this.peticion3 = peticion3;
    }

    public String getPeticion4() {
        return peticion4;
    }

    public void setPeticion4(String peticion4) {
        this.peticion4 = peticion4;
    }

    public String getPeticion5() {
        return peticion5;
    }

    public void setPeticion5(String peticion5) {
        this.peticion5 = peticion5;
    }

    public String getPeticion6() {
        return peticion6;
    }

    public void setPeticion6(String peticion6) {
        this.peticion6 = peticion6;
    }

    public String getPeticion7() {
        return peticion7;
    }

    public void setPeticion7(String peticion7) {
        this.peticion7 = peticion7;
    }

    public String getPeticion8() {
        return peticion8;
    }

    public void setPeticion8(String peticion8) {
        this.peticion8 = peticion8;
    }

    public String getPeticion9() {
        return peticion9;
    }

    public void setPeticion9(String peticion9) {
        this.peticion9 = peticion9;
    }

   
    

}

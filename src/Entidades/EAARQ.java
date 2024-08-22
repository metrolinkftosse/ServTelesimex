/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author dperez
 */
public class EAARQ {

    String PROTOCOL_VERSION;
    String CALLED_AP_TITLE;
    String CALLED_AE_QUANTIFIER;
    String CALLED_AP_INVOCATION_ID;
    String CALLED_AE_INVOCATION_ID;
    String CALLING_AP_TITLE;
    String CALLING_AE_QUANTIFIER;
    String CALLING_AP_INVOCATION_ID;
    String CALLING_AE_INVOCATION_ID;
    String SENDER_ACSE_REQUIREMENTS;
    String MECHANISM_NAME;
    String CALLING_AUTHENTICATION_VALUE;
    String IMPLEMENTATION_INFORMATION;
    String USER_INFORMATION;
    String VCMARCA;

    public EAARQ() {
    }

    public String getCALLED_AE_INVOCATION_ID() {
        return CALLED_AE_INVOCATION_ID;
    }

    public void setCALLED_AE_INVOCATION_ID(String CALLED_AE_INVOCATION_ID) {
        this.CALLED_AE_INVOCATION_ID = CALLED_AE_INVOCATION_ID;
    }

    public String getCALLED_AE_QUANTIFIER() {
        return CALLED_AE_QUANTIFIER;
    }

    public void setCALLED_AE_QUANTIFIER(String CALLED_AE_QUANTIFIER) {
        this.CALLED_AE_QUANTIFIER = CALLED_AE_QUANTIFIER;
    }

    public String getCALLED_AP_INVOCATION_ID() {
        return CALLED_AP_INVOCATION_ID;
    }

    public void setCALLED_AP_INVOCATION_ID(String CALLED_AP_INVOCATION_ID) {
        this.CALLED_AP_INVOCATION_ID = CALLED_AP_INVOCATION_ID;
    }

    public String getCALLED_AP_TITLE() {
        return CALLED_AP_TITLE;
    }

    public void setCALLED_AP_TITLE(String CALLED_AP_TITLE) {
        this.CALLED_AP_TITLE = CALLED_AP_TITLE;
    }

    public String getCALLING_AE_INVOCATION_ID() {
        return CALLING_AE_INVOCATION_ID;
    }

    public void setCALLING_AE_INVOCATION_ID(String CALLING_AE_INVOCATION_ID) {
        this.CALLING_AE_INVOCATION_ID = CALLING_AE_INVOCATION_ID;
    }

    public String getCALLING_AE_QUANTIFIER() {
        return CALLING_AE_QUANTIFIER;
    }

    public void setCALLING_AE_QUANTIFIER(String CALLING_AE_QUANTIFIER) {
        this.CALLING_AE_QUANTIFIER = CALLING_AE_QUANTIFIER;
    }

    public String getCALLING_AP_INVOCATION_ID() {
        return CALLING_AP_INVOCATION_ID;
    }

    public void setCALLING_AP_INVOCATION_ID(String CALLING_AP_INVOCATION_ID) {
        this.CALLING_AP_INVOCATION_ID = CALLING_AP_INVOCATION_ID;
    }

    public String getCALLING_AP_TITLE() {
        return CALLING_AP_TITLE;
    }

    public void setCALLING_AP_TITLE(String CALLING_AP_TITLE) {
        this.CALLING_AP_TITLE = CALLING_AP_TITLE;
    }

    public String getCALLING_AUTHENTICATION_VALUE() {
        return CALLING_AUTHENTICATION_VALUE;
    }

    public void setCALLING_AUTHENTICATION_VALUE(String CALLING_AUTHENTICATION_VALUE) {
        this.CALLING_AUTHENTICATION_VALUE = CALLING_AUTHENTICATION_VALUE;
    }

    public String getIMPLEMENTATION_INFORMATION() {
        return IMPLEMENTATION_INFORMATION;
    }

    public void setIMPLEMENTATION_INFORMATION(String IMPLEMENTATION_INFORMATION) {
        this.IMPLEMENTATION_INFORMATION = IMPLEMENTATION_INFORMATION;
    }

    public String getMECHANISM_NAME() {
        return MECHANISM_NAME;
    }

    public void setMECHANISM_NAME(String MECHANISM_NAME) {
        this.MECHANISM_NAME = MECHANISM_NAME;
    }

    public String getPROTOCOL_VERSION() {
        return PROTOCOL_VERSION;
    }

    public void setPROTOCOL_VERSION(String PROTOCOL_VERSION) {
        this.PROTOCOL_VERSION = PROTOCOL_VERSION;
    }

    public String getSENDER_ACSE_REQUIREMENTS() {
        return SENDER_ACSE_REQUIREMENTS;
    }

    public void setSENDER_ACSE_REQUIREMENTS(String SENDER_ACSE_REQUIREMENTS) {
        this.SENDER_ACSE_REQUIREMENTS = SENDER_ACSE_REQUIREMENTS;
    }

    public String getUSER_INFORMATION() {
        return USER_INFORMATION;
    }

    public void setUSER_INFORMATION(String USER_INFORMATION) {
        this.USER_INFORMATION = USER_INFORMATION;
    }

    public String getVCMARCA() {
        return VCMARCA;
    }

    public void setVCMARCA(String VCMARCA) {
        this.VCMARCA = VCMARCA;
    }
    
}

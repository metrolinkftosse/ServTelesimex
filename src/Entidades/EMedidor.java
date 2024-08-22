/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Entidades;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

/**
 *
 * @author dperez
 */
public class EMedidor {
    
    public String nSerie;
    public ECliente cliente;
    public String sic;    
    public String idmedidor;
    public String idProg;
    public String municipio;
    public String operador;
    public EMarcaMedidor marcaMedidor;   
    public int tipoconexion;
    public String direccionip;
    public String puertoip;
    public String numtelefonico;
    public String puertocomm;
    public EVelocidad velocidadpuerto;
    public EGrupo grupo;
    public int reintentos;
    public long timeout;
    public int ndias;
    public boolean lconfigurado;    
    public EMarcaModem marcaModem;
    public Timestamp fecha;
    public int ncanales;
    public String password;
    public String password2;
    public int ndiasreg;
    public int nmesreg;
    public String observacion;
    public String direccionFisica;
    public String direccionLogica;
    public String direccionCliente;
    public String bytesdireccion;
    private boolean seguridad;
    public LinkedHashMap<Integer, String> codigosDLMS = new LinkedHashMap<>();
    public LinkedHashMap<Integer, String> parametrosAvanzadosPorMedidor = new LinkedHashMap<>();
    public boolean MedLeido=false;
    public int ndiaseventos;
    public boolean lfaltantes=false;
    public boolean ldesfasados=false;

    public EMedidor() {
    }

    public EMedidor(String nSerie, EMarcaMedidor marcaMedidor) {
        this.nSerie = nSerie;
        this.marcaMedidor = marcaMedidor;
    }

    public EGrupo getGrupo() {
        return grupo;
    }

    public void setGrupo(EGrupo grupo) {
        this.grupo = grupo;
    }
  

    public String getDireccionip() {
        return direccionip;
    }

    public void setDireccionip(String direccionip) {
        this.direccionip = direccionip;
    }


    public EMarcaMedidor getMarcaMedidor() {
        return marcaMedidor;
    }

    public void setMarcaMedidor(EMarcaMedidor marcaMedidor) {
        this.marcaMedidor = marcaMedidor;
    }

    public EMarcaModem getMarcaModem() {
        return marcaModem;
    }

    public void setMarcaModem(EMarcaModem marcaModem) {
        this.marcaModem = marcaModem;
    }

    
    public String getnSerie() {
        return nSerie;
    }

    public void setnSerie(String nSerie) {
        this.nSerie = nSerie;
    }

    public String getSic() {
        return sic;
    }

    public void setSic(String sic) {
        this.sic = sic;
    }
    
    public String getIdmedidor() {
        return idmedidor;
    }

    public void setIdmedidor(String idmedidor) {
        this.idmedidor = idmedidor;
    }

    public String getIdProg() {
        return idmedidor;
    }

    public void setIdProg(String idmedidor) {
        this.idmedidor = idmedidor;
    }

    public int getNdias() {
        return ndias;
    }

    public void setNdias(int ndias) {
        this.ndias = ndias;
    }

    public String getNumtelefonico() {
        return numtelefonico;
    }

    public void setNumtelefonico(String numtelefonico) {
        this.numtelefonico = numtelefonico;
    }

    public String getPuertocomm() {
        return puertocomm;
    }

    public void setPuertocomm(String puertocomm) {
        this.puertocomm = puertocomm;
    }

    public String getPuertoip() {
        return puertoip;
    }

    public void setPuertoip(String puertoip) {
        this.puertoip = puertoip;
    }

    public int getReintentos() {
        return reintentos;
    }

    public void setReintentos(int reintentos) {
        this.reintentos = reintentos;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getTipoconexion() {
        return tipoconexion;
    }

    public void setTipoconexion(int tipoconexion) {
        this.tipoconexion = tipoconexion;
    }

    public EVelocidad getVelocidadpuerto() {
        return velocidadpuerto;
    }

    public void setVelocidadpuerto(EVelocidad velocidadpuerto) {
        this.velocidadpuerto = velocidadpuerto;
    }

    public boolean isLconfigurado() {
        return lconfigurado;
    }

    public void setLconfigurado(boolean lconfigurado) {
        this.lconfigurado = lconfigurado;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public ECliente getCliente() {
        return cliente;
    }

    public void setCliente(ECliente cliente) {
        this.cliente = cliente;
    }

    public int getNcanales() {
        return ncanales;
    }

    public void setNcanales(int ncanales) {
        this.ncanales = ncanales;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNdiasreg() {
        return ndiasreg;
    }

    public void setNdiasreg(int ndiasreg) {
        this.ndiasreg = ndiasreg;
    }

    public int getNmesreg() {
        return nmesreg;
    }

    public void setNmesreg(int nmesreg) {
        this.nmesreg = nmesreg;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getDireccionFisica() {
        return direccionFisica;
    }

    public void setDireccionFisica(String direccionFisica) {
        this.direccionFisica = direccionFisica;
    }

    public String getDireccionLogica() {
        return direccionLogica;
    }

    public void setDireccionLogica(String direccionLogica) {
        this.direccionLogica = direccionLogica;
    }

    public String getDireccionCliente() {
        return direccionCliente;
    }

    public void setDireccionCliente(String direccionCliente) {
        this.direccionCliente = direccionCliente;
    }

    public String getBytesdireccion() {
        return bytesdireccion;
    }

    public void setBytesdireccion(String bytesdireccion) {
        this.bytesdireccion = bytesdireccion;
    }
    
    public boolean getSeguridad() {
        return seguridad;
    }
    
    public void setSeguridad(boolean seguridad) {
        this.seguridad = seguridad;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }
    
     public LinkedHashMap<Integer,String> getCodigosDLMS() {
        return this.codigosDLMS;
    }

    public void setCodigosDLMS(LinkedHashMap<Integer,String> codigosDLMS) {
        this.codigosDLMS = codigosDLMS;
    }
    
    public void addCodigoDLMS(Integer key, String value){
        System.out.println("Còdigo agregado: "+key+ ", valor: "+value);
        this.codigosDLMS.put(key, value);
    }
    

    public boolean isMedLeido() {
        return MedLeido;
    }

    public void setMedLeido(boolean MedLeido) {
        this.MedLeido = MedLeido;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public int getNdiaseventos() {
        return ndiaseventos;
    }

    public void setNdiaseventos(int ndiaseventos) {
        this.ndiaseventos = ndiaseventos;
    }

    public boolean isLfaltantes() {
        return lfaltantes;
    }

    public void setLfaltantes(boolean lfaltantes) {
        this.lfaltantes = lfaltantes;
    }

    public boolean isLdesfasados() {
        return ldesfasados;
    }

    public void setLdesfasados(boolean ldesfasados) {
        this.ldesfasados = ldesfasados;
    }
    
    public LinkedHashMap<Integer, String> getParametrosAvanzadosPorMedidor() {
        return parametrosAvanzadosPorMedidor;
    }

    public void setParametrosAvanzadosPorMedidor(LinkedHashMap<Integer, String> parametrosAvanzadosPorMedidor) {
        this.parametrosAvanzadosPorMedidor = parametrosAvanzadosPorMedidor;
    }
    
    public void addParametrosAvanzadosPorMedidor(Integer key, String value){
        System.out.println("Parámetro Avanzado agregado: "+key+ ", valor: "+value);
        this.parametrosAvanzadosPorMedidor.put(key, value);
    }
            
}

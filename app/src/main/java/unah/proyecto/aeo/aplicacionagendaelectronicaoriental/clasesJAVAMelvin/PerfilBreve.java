package unah.proyecto.aeo.aplicacionagendaelectronicaoriental.clasesJAVAMelvin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.Serializable;

/**
 * Created by melvinrivera on 21/2/18.
 */

public class PerfilBreve implements Serializable{
    private String nombre;
    private Bitmap imagen;
    private String numeroTelefono;
    private String direccion;
    private int id;
    int estado;
    String dato;

    public PerfilBreve(String nombre, Bitmap imagen, String numeroTelefono, String direccion,int id, int estado, String dato) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.numeroTelefono = numeroTelefono;
        this.direccion = direccion;
        this.id = id;
        this.estado = estado;
        this.dato=dato;
    }

    public PerfilBreve(){

    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
        try {
            byte[] byteCode = Base64.decode(dato, Base64.DEFAULT);
            this.imagen = BitmapFactory.decodeByteArray(byteCode, 0, byteCode.length);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

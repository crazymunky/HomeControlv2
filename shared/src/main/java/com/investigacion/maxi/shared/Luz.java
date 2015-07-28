package com.investigacion.maxi.shared;

/**
 * Created by maxit on 7/27/2015.
 */
public class Luz {
    private String nombre;
    private boolean prendida;

    public Luz(String nombre, boolean prendida) {
        this.nombre = nombre;
        this.prendida = prendida;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isPrendida() {
        return prendida;
    }

    public void setPrendida(boolean prendida) {
        this.prendida = prendida;
    }
}

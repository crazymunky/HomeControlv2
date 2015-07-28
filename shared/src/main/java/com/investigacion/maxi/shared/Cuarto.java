package com.investigacion.maxi.shared;

import java.util.List;

/**
 * Created by maxit on 7/27/2015.
 */
public class Cuarto {
    private String nombre;
    private List<Luz> luces;

    public Cuarto(String nombre, List<Luz> luces) {
        this.nombre = nombre;
        this.luces = luces;
    }



    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Luz> getLuces() {
        return luces;
    }

    public void setLuces(List<Luz> luces) {
        this.luces = luces;
    }
}

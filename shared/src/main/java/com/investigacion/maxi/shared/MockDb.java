package com.investigacion.maxi.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxit on 7/27/2015.
 */
public class MockDb {
    private List<Cuarto> cuartos;

    public MockDb() {
        this.cuartos = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            List<Luz> listLuces = new ArrayList();
            for (int j = 1; j < 6; j++) {
                Luz newLuz = new Luz("Luz " + j, false);
                listLuces.add(newLuz);
            }
            Cuarto newCuarto = new Cuarto("Cuarto " + i, listLuces);
            this.cuartos.add(newCuarto);
        }
    }

    public List<Cuarto> getCuartos(){
        return cuartos;
    }

    public void setCuartos(List<Cuarto> cuartos) {
        this.cuartos = cuartos;
    }
}

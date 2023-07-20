package modelo;

import vista.*;
import controlador.*;

public class Main {

    public static void main(String[] args) {
        VMoore vista = new VMoore();
        VFichero lectura = new VFichero();
        Controlador_Moore control = new Controlador_Moore(vista, lectura);
        control.iniciarVentanaPrincipal();
    }
}

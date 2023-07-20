package controlador;

import static javax.swing.WindowConstants.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import modelo.*;
import vista.*;

public final class Controlador_Moore implements ActionListener {

    private final VMoore vista;
    private final VFichero lectura;
    private MMoore tupla;
    private String cadena_entrada;
    private boolean post_precargado = false;
    private int cont = 0;

    public Controlador_Moore(VMoore vista, VFichero lectura) {
        this.vista = vista;
        this.lectura = lectura;
        registrarOyentes();
    }

    private void registrarOyentes() {
        this.vista.jbCargar.addActionListener(this);
        this.vista.jbComputar.addActionListener(this);
        this.lectura.jbBuscar.addActionListener(this);
        this.lectura.jbContinuar.addActionListener(this);
        this.lectura.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                post_precargado = true;
                cont = 1;
            }
        });
    }

    public void iniciarVentanaPrincipal() {
        vista.setDefaultCloseOperation(EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.txTupla.setEditable(false);
        vista.txComputos.setEditable(false);
        vista.txCadenaSalida.setEditable(false);
        vista.setResizable(false);
        vista.setVisible(true);
    }

    private void iniciarVentanaLecturaArchivo() {
        lectura.setLocationRelativeTo(null);
        lectura.txTupla.setEditable(false);
        lectura.txDirArchivo.setEditable(false);
        lectura.setResizable(false);
        lectura.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (post_precargado == false) {
            lectura.txTupla.setText("({0,1,2,3,4,5}, {0}, {0,1}, {A,B,C}, {C,C,C,A,C,B})\n"
                    + "delta:\n"
                    + "0,0: 0;\n"
                    + "0,1: 1;\n"
                    + "1,2: 0;\n"
                    + "1,4: 1;\n"
                    + "2,0: 0;\n"
                    + "2,3: 1;\n"
                    + "3,2: 0;\n"
                    + "3,4: 1;\n"
                    + "4,4: 1;\n"
                    + "4,5: 0;\n"
                    + "5,0: 0;\n"
                    + "5,3: 1;");
            lectura.jbBuscar.setEnabled(false);
            lectura.jTitulo.setText("Máquina de Moore precargada");
            lectura.jbContinuar.setEnabled(true);
            post_precargado = true;
        } else {
            lectura.txTupla.setText("");
            lectura.jbContinuar.setEnabled(false);
            lectura.jbBuscar.setEnabled(true);
            lectura.jTitulo.setText("Creación de la Máquina de Moore");
        }
        lectura.setVisible(true);
    }

    private void generarTupla() {
        String contenido = lectura.txTupla.getText();
        if (cont == 0) {
            try {
                lectura.seleccionar.setFileFilter(lectura.filtro);
                if (lectura.seleccionar.showDialog(null, "Guardar") == JFileChooser.APPROVE_OPTION) {
                    vista.txTupla.setText(contenido);
                    File precargado = lectura.seleccionar.getSelectedFile();
                    if (!precargado.exists()) {
                        precargado.createNewFile();
                    } else {
                        String nr = precargado.getAbsolutePath();
                        for (int i = 1; i < Long.MAX_VALUE; i++) {
                            precargado = new File(nr + " (" + i + ")");
                            if (!precargado.exists()) {
                                precargado.createNewFile();
                                break;
                            }
                        }
                    }
                    FileWriter fw = new FileWriter(precargado);
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write(contenido);
                    }
                    tupla = new MMoore(precargado);
                    String cadenaPrueba = "01010";
                    vista.txCadenaEntrada.setText(cadenaPrueba);
                    //Validacion con if-else del formato del .txt
                    cadena_entrada = vista.txCadenaEntrada.getText();
                    lectura.dispose();
                    generarComputos();
                    cont = 1;
                } else {
                    for (Window window : Window.getWindows()) {
                        if (vista != window) {
                            window.toFront();
                            return;
                        }
                    }
                }
            } catch (HeadlessException | IOException e) {
                e.printStackTrace();
            }
        } else {
            vista.txTupla.setText(contenido);
            vista.txComputos.setText("");
            tupla = new MMoore(lectura.archivo);
            lectura.dispose();
        }
        vista.setDefaultCloseOperation(EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.txTupla.setEditable(false);
        vista.txComputos.setEditable(false);
        vista.setResizable(false);
        vista.setVisible(true);
    }

    private void generarComputos() {
        String info = vista.txTupla.getText();
        if (!info.equals("")) {
            cadena_entrada = vista.txCadenaEntrada.getText();
            if (tupla.generarComputos(cadena_entrada) == false) {
                JOptionPane.showMessageDialog(vista, "¡Cadena rechazada!");
            } else {
                JOptionPane.showMessageDialog(vista, "¡Cadena aceptada!");
            }
            vista.txCadenaSalida.setText(tupla.getCadenaSalida());
            vista.txComputos.setText(tupla.getImpresor());
        } else {
            JOptionPane.showMessageDialog(vista, "¡No hay una Máquina de Moore para ejecutar computaciones! Por favor cárguela.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(vista.jbCargar)) {
            iniciarVentanaLecturaArchivo();
        }
        if (e.getSource().equals(vista.jbComputar)) {
            generarComputos();
        }
        if (e.getSource().equals(lectura.jbContinuar)) {
            generarTupla();
        }
    }

}

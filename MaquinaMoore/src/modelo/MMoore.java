package modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public final class MMoore {

    private List<String> ConjQ = new ArrayList<>();
    private List<String> ConjSigma = new ArrayList<>();
    private List<String> ConjSalidas = new ArrayList<>();
    private List<String> delta_lectura = new ArrayList<>();
    private List<String> delta_salida = new ArrayList<>();
    private String estado_inicial;
    private String textofichero;
    private String[][] automata = null;
    private File ruta;
    private String estados;
    private String cadena_salida;
    private String impresor;

    public MMoore(File r) {
        try {
            leerTupla(r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getImpresor() {
        return this.impresor;
    }

    public String getCadenaSalida() {
        return this.cadena_salida;
    }

    private void leerTupla(File r) throws FileNotFoundException, IOException {
        ruta = new File(r.getAbsolutePath());
        FileReader leerFichero = new FileReader(ruta);
        try (BufferedReader bufferLectura = new BufferedReader(leerFichero)) {
            String cadenaElement;
            int linea = 0;
            while ((textofichero = bufferLectura.readLine()) != null) {
                if (textofichero.matches("#.*")) {
                } else if (textofichero.matches("\b*")) {
                } else {
                    if (linea >= 2) { //
                        String[] separarEspaciosDelta = textofichero.split(" ");
                        for (String espacio : separarEspaciosDelta) {
                            delta_lectura.add(espacio);
                        }
                    } else {
                        String[] separarEspacios = textofichero.split(" ");
                        for (String espacio : separarEspacios) {
                            if (espacio.matches("#.*")) {
                                break;
                            }
                        }
                        if (linea == 0) {
                            String[] elementos;
                            for (int i = 0; i < separarEspacios.length; i++) {
                                cadenaElement = separarEspacios[i];
                                cadenaElement = ajustarCaracteres(cadenaElement, i);
                                elementos = identificarCaracteresSubCadena(cadenaElement);
                                switch (i) {
                                    case 0:
                                        for (String elemento : elementos) {
                                            ConjQ.add(elemento);
                                        }
                                        automata = new String[ConjQ.size()][ConjQ.size()];
                                        estados = ConjQ.toString();
                                        break;
                                    case 1:
                                        estado_inicial = cadenaElement.substring(0, 1);
                                        break;
                                    case 2:
                                        for (String elemento : elementos) {
                                            ConjSigma.add(elemento);
                                            System.out.println(ConjSigma.get(ConjSigma.size() - 1) + "PARAME BOLAS");
                                        }
                                        break;
                                    case 3:
                                        for (String elemento : elementos) {
                                            ConjSalidas.add(elemento);
                                        }
                                        break;
                                    case 4:
                                        for (String elemento : elementos) {
                                            delta_salida.add(elemento);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    linea++;
                }
            }
        }
        asignarTransiciones();
    }

    private String ajustarCaracteres(String cadena, int indice) {
        cadena = cadena.replace("{", "");
        cadena = cadena.replace("}", "");
        switch (indice) {
            case 0:
                cadena = cadena.replace("(", "");
                break;
            case 4:
                cadena = cadena.replace(")", "");
                break;
        }
        return cadena;
    }

    private String[] identificarCaracteresSubCadena(String cadena) {
        String[] elementos = cadena.split(",");
        return elementos;
    }

    private boolean validarNumero(String cadena) {
        boolean resultado;
        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException e) {
            resultado = false;
        }
        return resultado;
    }

    private void asignarTransiciones() {
        int fila = 0, columna = 0, posDivisor;
        String f, c, transiciones_concatenadas = null;
        for (int i = 0; i < delta_lectura.size(); i++) {
            String cadena = delta_lectura.get(i);
            if (cadena.endsWith(":")) {
                transiciones_concatenadas = "";
                for (int j = 0; j < cadena.length() - 1; j++) {
                    if (cadena.substring(j, j + 1).equals(",")) {
                        /*posDivisor = cadena.lastIndexOf(",");
                        System.out.println(posDivisor + " <--- Mi variable teorica ," + " La version de antaño (j) ---> " + j);*/
                        f = cadena.substring(0, j);
                        c = cadena.substring(j + 1, cadena.length() - 1);
                        if ((validarNumero(f)) && (validarNumero(c))) {
                            fila = Integer.parseInt(f);
                            columna = Integer.parseInt(c);
                            transiciones_concatenadas += cadena + " ";
                            break;
                        }
                    }
                }
            } else if (cadena.endsWith(";")) {
                transiciones_concatenadas += cadena + " ";
                automata[fila][columna] = transiciones_concatenadas;
            }
        }
    }

    private String identificarSubCadenaSiguiente(String subcadena_restante, List<String> conjunto) {
        int n = conjunto.size(), distancia;
        String cadenaContinuar = null, cadenaPrueba, subcadenaLectura = subcadena_restante;
        for (int j = 0; j < n; j++) {
            distancia = conjunto.get(j).length();
            try {
                cadenaPrueba = subcadenaLectura.substring(0, distancia);
            } catch (StringIndexOutOfBoundsException e) {
                break;
            }
            if (cadenaPrueba.equals(conjunto.get(j))) {
                cadenaContinuar = cadenaPrueba;
                return cadenaContinuar;
            }

        }
        return cadenaContinuar;
    }

    private void generarImpresion(int iteracion, String subcadena_lectura, int estadoActual, String subcadena_salida, String[] transiciones, int transicion) {
        impresor += "  |            " + iteracion + "            |                       " + subcadena_lectura + "                    |           " + estadoActual + "           |                    " + subcadena_salida + "                     |                  " + transiciones[transicion] + "                 |\n";
    }

    public boolean generarComputos(String cadena_entrada) {
        impresor = "";
        cadena_salida = "";
        int estadoActual = Integer.parseInt(estado_inicial), estadoDirigido = 0, nTrans, k, iteracion = 0;
        String transicionesNoEjecutadas, subcadena_salida = delta_salida.get(estadoActual);
        boolean warning = false, exit;
        String[] ini = {"--"};
        impresor += "  |       Iteración     |        Subcadena entrada       |      Estado      |       Subcadena salida        |        Transiciones         |\n";
        cadena_salida = cadena_salida.concat(subcadena_salida);
        generarImpresion(++iteracion, ini[0], estadoActual, subcadena_salida, ini, 0);
        for (k = 0; k < cadena_entrada.length(); k++) {
            if (!warning) {
                String subcadena_lectura = cadena_entrada.substring(k, cadena_entrada.length());
                subcadena_lectura = identificarSubCadenaSiguiente(subcadena_lectura, ConjSigma);
                if (subcadena_lectura != null) {
                    for (int i = estadoActual; i < automata.length; i++) {
                        exit = false;
                        transicionesNoEjecutadas = null;
                        nTrans = 0;
                        for (int j = estadoDirigido; j < automata.length; j++) {
                            if (automata[i][j] != null) {
                                String[] transiciones = automata[i][j].split(" ");
                                if (i == estadoActual) {
                                    nTrans += transiciones.length - 1;
                                    int transicion = 1;
                                    while ((!exit) && (transicion < transiciones.length)) {
                                        String funcion = transiciones[transicion];
                                        funcion = funcion.replace(";", "");
                                        if (subcadena_lectura.equals(funcion)) {
                                            i = j;
                                            j = 0;
                                            estadoActual = i;
                                            estadoDirigido = j;
                                            exit = true;
                                            subcadena_salida = delta_salida.get(estadoActual);
                                            cadena_salida = cadena_salida.concat(subcadena_salida);
                                            generarImpresion(++iteracion, subcadena_lectura, estadoActual, subcadena_salida, transiciones, transicion);
                                            k += subcadena_lectura.length() - 1;
                                        } else {
                                            transicionesNoEjecutadas += transiciones[transicion] + " ";
                                        }
                                        transicion++;
                                    }
                                }
                            }
                            if (exit) {
                                break;
                            }
                        }
                        if (transicionesNoEjecutadas != null) {
                            String[] fallidas = transicionesNoEjecutadas.split(" ");
                            if (fallidas.length == nTrans) {
                                exit = true;
                                warning = true;
                            }
                        }
                        if (exit) {
                            break;
                        }
                    }
                } else {
                    warning = true;
                }
            }
            if (k == cadena_entrada.length() - 1) {
                return !warning;
            }
        }
        return false;
    }
}

// Autor: Xavier Tacuri
// Clase DAO (Data Access Object) encargada de gestionar
// la lectura y escritura de contactos en un archivo de texto

package modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {

    // Archivo donde se almacenan los contactos
    private File archivo;

    // Objeto persona que se va a guardar
    private persona persona;

    // Constructor: recibe una persona y prepara el archivo
    public personaDAO(persona persona) {
        this.persona = persona;

        // Se define el archivo en la raíz del proyecto
        archivo = new File("contactos.txt");

        // Se asegura que el archivo exista
        prepararArchivo();
    }

    // Método que crea el archivo si no existe
    private void prepararArchivo() {
        if (!archivo.exists()) {
            try {
                // Crea el archivo
                archivo.createNewFile();

                // Escribe el encabezado (columnas)
                FileWriter writer = new FileWriter(archivo, true);
                writer.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
                writer.close();

            } catch (IOException e) {
                // Muestra error en consola si falla
                e.printStackTrace();
            }
        }
    }

    // Método para guardar un contacto en el archivo
    public boolean escribirArchivo() {
        try {
            // Se abre el archivo en modo "append" (agregar)
            FileWriter writer = new FileWriter(archivo, true);

            // Se escribe la información del contacto
            writer.write(persona.datosContacto() + "\n");

            writer.close();
            return true;

        } catch (IOException e) {
            // En caso de error se imprime en consola
            e.printStackTrace();
            return false;
        }
    }

    // Método para leer todos los contactos del archivo
    public List<persona> leerArchivo() throws IOException {

        // Lista donde se almacenarán los contactos leídos
        List<persona> personas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String linea;

        // Variable para ignorar la primera línea (encabezado)
        boolean primeraLinea = true;

        while ((linea = br.readLine()) != null) {

            // Se omite la primera línea
            if (primeraLinea) {
                primeraLinea = false;
                continue;
            }

            // Elimina espacios innecesarios
            linea = linea.trim();

            // Si la línea está vacía se ignora
            if (linea.isEmpty()) {
                continue;
            }

            // Se separan los datos usando ";"
            String[] partes = linea.split(";");

            // Si no tiene los datos completos se ignora
            if (partes.length < 5) {
                continue;
            }

            // Se crea un objeto persona con los datos leídos
            persona p = new persona();
            p.setNombre(partes[0]);
            p.setTelefono(partes[1]);
            p.setEmail(partes[2]);
            p.setCategoria(partes[3]);
            p.setFavorito(Boolean.parseBoolean(partes[4]));

            // Se agrega a la lista
            personas.add(p);
        }

        br.close();

        return personas;
    }

    // Método para actualizar todos los contactos (modificar o eliminar)
    public void actualizarContactos(List<persona> personas) throws IOException {

        // Se abre el archivo en modo sobrescritura
        FileWriter writer = new FileWriter(archivo, false);

        // Se vuelve a escribir el encabezado
        writer.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");

        // Se recorren todos los contactos y se reescriben
        for (persona p : personas) {
            writer.write(p.datosContacto() + "\n");
        }

        writer.close();
    }
}
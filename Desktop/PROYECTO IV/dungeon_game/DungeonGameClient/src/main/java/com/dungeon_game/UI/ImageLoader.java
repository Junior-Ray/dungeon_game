/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dungeon_game.UI;



import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 *
 * @author GABRIEL SALGADO
 */
public class ImageLoader {
    
    private ImageLoader() {
        // Constructor privado: esta clase solo debe tener métodos estáticos (utilidad).
    }
    public static Image load(String path) {
        try {
            // El ClassLoader busca el archivo dentro de src/main/resources en el JAR.
            // La ruta NO debe empezar con "/"
            URL url = ImageLoader.class.getClassLoader().getResource(path);
            
            if (url == null) {
                 System.err.println("ERROR: Recurso no encontrado: " + path);
                 return null;
            }
            
            // Usamos ImageIO para leer la URL del recurso
            return ImageIO.read(url); 
            
        } catch (IOException e) {
            System.err.println("ERROR: Error al leer la imagen: " + path);
            e.printStackTrace();
            return null;
        }
    }
}

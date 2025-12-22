/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // CAMBIO AQUÍ: de 'chat' a 'dungeon_game'
    private static final String URL  = "jdbc:mysql://localhost:3306/dungeon_game?useSSL=false&serverTimezone=UTC";
    
    // REVISA TUS CREDENCIALES:
    private static final String USER = "root";  // Usuario por defecto en XAMPP/MySQL local
    private static final String PASS = "1234";  // <-- ¿Seguro que es 1234? Si usas XAMPP suele ser "" (vacío)

    public static Connection getConnection() throws SQLException {
        try {
            // Carga explícita del driver (ayuda a evitar errores de "No suitable driver")
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontró el Driver de MySQL (mysql-connector-j).");
            e.printStackTrace();
        }
        
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
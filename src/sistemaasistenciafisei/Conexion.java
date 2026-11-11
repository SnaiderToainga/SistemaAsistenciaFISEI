/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaasistenciafisei;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;
/**
 *
 * @author User
 */



public class Conexion {
      public Connection conectar(){
        Connection con=null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Tener en cuenta su puerto y lo cambian segun el suyo para pruebas
            con=DriverManager.getConnection("jdbc:mysql://localhost:3306/fisei_asistencia","root","");
            // JOptionPane.showMessageDialog(null, "conectado");
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Error conexion: " + ex);
        }
         return con;   
    }   
}

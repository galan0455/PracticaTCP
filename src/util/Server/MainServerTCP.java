package util.Server;

import datos.Mensaje;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainServerTCP {
    private static final List<ObjectOutputStream> listaUsuariosConectados = new ArrayList<>();
    private static final List<String> listaNombresConectados = new ArrayList<>();
    private static JTextArea textArea;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Servidor Chat");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setVisible(true);

        int puerto = 6001;
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            textArea.append("Servidor escuchando en el puerto " + puerto + "\n");

            while (true) {
                Socket cliente = servidor.accept();
                textArea.append("Nuevo cliente conectado\n");

                ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(cliente.getOutputStream());

                new Thread(() -> manejarCliente(cliente, ois, oos)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarCliente(Socket cliente, ObjectInputStream ois, ObjectOutputStream oos) {
        String nombreUsuario;
        try {
            while (true) {
                Mensaje mensajeRecibido = (Mensaje) ois.readObject();
                nombreUsuario = mensajeRecibido.getNombre();
                if (listaNombresConectados.contains(nombreUsuario)) {
                    oos.writeObject("El nombre ya está en uso, elige otro");
                } else {
                    listaNombresConectados.add(nombreUsuario);
                    listaUsuariosConectados.add(oos);
                    oos.writeObject("Bienvenido, " + nombreUsuario + "!");
                    textArea.append(nombreUsuario + " se ha conectado.\n");
                    break;
                }
            }

            while (true) {
                Mensaje mensaje = (Mensaje) ois.readObject();
                textArea.append(mensaje.getNombre() + ": " + mensaje.getMensaje() + "\n");
                for (ObjectOutputStream clienteOut : listaUsuariosConectados) {
                    clienteOut.writeObject(mensaje);
                    clienteOut.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            textArea.append("Cliente desconectado\n");
        } finally {
            listaUsuariosConectados.remove(oos);
            try {
                cliente.close();
                ois.close();
                oos.close();
            } catch (IOException ignored) {}
        }
    }
}

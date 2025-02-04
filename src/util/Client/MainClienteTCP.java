package util.Client;

import datos.Mensaje;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainClienteTCP {
    private static JTextArea textArea;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static String nombre;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cliente Chat");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JTextField textField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 6001);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                nombre = JOptionPane.showInputDialog("Introduce tu nombre:");
                if (nombre == null || nombre.trim().isEmpty()) {
                    continue;
                }
                oos.writeObject(new Mensaje(nombre, ""));
                String respuesta = (String) ois.readObject();
                if (respuesta.startsWith("Bienvenido")) {
                    textArea.append(respuesta + "\n");
                    break;
                } else {
                    JOptionPane.showMessageDialog(frame, respuesta);
                }
            }

            new Thread(() -> {
                try {
                    while (true) {
                        Mensaje mensaje = (Mensaje) ois.readObject();
                        textArea.append(mensaje.getNombre() + ": " + mensaje.getMensaje() + "\n");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    textArea.append("Conexión cerrada.\n");
                }
            }).start();

            sendButton.addActionListener(e -> enviarMensaje(textField));
            textField.addActionListener(e -> enviarMensaje(textField));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensaje(JTextField textField) {
        String texto = textField.getText().trim();
        if (!texto.isEmpty()) {
            try {
                oos.writeObject(new Mensaje(nombre, texto));
                textField.setText("");
            } catch (IOException e) {
                textArea.append("Error al enviar mensaje.\n");
            }
        }
    }
}

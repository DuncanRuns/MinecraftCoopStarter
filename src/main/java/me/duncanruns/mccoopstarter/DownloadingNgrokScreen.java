package me.duncanruns.mccoopstarter;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class DownloadingNgrokScreen extends JFrame {
    private final JProgressBar bar;

    public DownloadingNgrokScreen() {
        this.setLayout(new GridBagLayout());
        JLabel text = new JLabel("Downloading ngrok...");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(text, gbc);
        bar = new JProgressBar(0, 100);
        this.add(bar, gbc);

        this.setSize(300, 100);
        this.setTitle("Coop Starter: Downloading Ngrok");
        this.setVisible(true);
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        new DownloadingNgrokScreen();
    }

    public void download() throws IOException {
        URL url = new URL("https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip");
        URLConnection connection = url.openConnection();
        connection.connect();
        int fileSize = connection.getContentLength();
        bar.setMaximum(fileSize);
        bar.setValue(0);
        int i = 0;
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream("ngrok.zip")) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                i += bytesRead;
                if (i >= 102400) {
                    bar.setValue(bar.getValue() + i);
                    i = 0;
                }
            }
        } catch (IOException e) {
            // handle exception
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream("ngrok.exe");
             ZipFile zf = new ZipFile(new File("ngrok.zip"));
             InputStream in = zf.getInputStream(zf.getEntry("ngrok.exe"))) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.delete(Paths.get("ngrok.zip"));
        } catch (Exception ignored) {
        }
    }
}

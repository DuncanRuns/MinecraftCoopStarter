package me.duncanruns.mccoopstarter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NgrokRunningScreen extends JFrame {
    private boolean closed;

    public NgrokRunningScreen() {
        this.setLayout(new GridBagLayout());
        JLabel text = new JLabel("Launching...");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(text, gbc);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closed = true;
            }
        });
        setSize(300, 50);
        setTitle("Coop Starter");
        setVisible(true);
    }

    public boolean isClosed() {
        return closed;
    }

    public void notifyIp(String ip) {
        this.getContentPane().removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(new JLabel("Your friends can connect with the IP: " + ip), gbc);
        JButton button = new JButton("Copy IP");
        button.addActionListener(e -> Clipboard.copy(ip));
        add(button, gbc);
        revalidate();
        setSize(400, 150);
    }
}

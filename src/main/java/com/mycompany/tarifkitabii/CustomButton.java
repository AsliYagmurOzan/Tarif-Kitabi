package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton extends JButton {

    private Color normalColor; 
    private Color hoverColor;  

    public CustomButton(String text, Color normalColor, Color hoverColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;

        setContentAreaFilled(false); 
        setFocusPainted(false); 
        setBorderPainted(false); 
        setForeground(Color.WHITE); 

        setBackground(normalColor); 
        
        addMouseListener(new MouseAdapter() {
            @Override
            //mouse gelince renk değiştirme
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor); 
                repaint(); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor); 
                repaint(); 
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5); 


        super.paintComponent(g); 
    }
}


package com.mycompany.tarifkitabii;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MalzemelerEkrani extends JFrame {

    private AnaEkran previousFrame;
    private List<JLabel> malzemeLabels = new ArrayList<>();
    private final JPanel malzemelerPanel = new JPanel();
    private List<String> orijinalMalzemeler = new ArrayList<>();
    private List<String> mevcutMalzemeler = new ArrayList<>();
    private List<String> malzemeBirimleri = new ArrayList<>();
    private List<String> malzemeMiktarlari = new ArrayList<>();
    private List<Double> malzemeFiyatlari = new ArrayList<>();
    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";

    public MalzemelerEkrani(AnaEkran previousFrame) {
        this.previousFrame = previousFrame;

        setTitle("Malzemeler");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Arka plan rengini ayarla
        malzemelerPanel.setBackground(Color.decode("#FFE6F0"));
        malzemelerPanel.setLayout(new GridLayout(0, 1, 10, 10));


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.decode("#FFE6F0")); 
        JTextField searchField = new JTextField(20);
        topPanel.add(new JLabel("Malzeme Ara: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#FFE6F0")); 

        malzemelerPanel.setLayout(new GridLayout(0, 1, 10, 10)); 
        malzemelerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                String sql = "SELECT MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat FROM Malzemeler";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String malzemeAdi = rs.getString("MalzemeAdi");
                    orijinalMalzemeler.add(malzemeAdi);
                    mevcutMalzemeler.add(malzemeAdi.toLowerCase());
                    malzemeMiktarlari.add(rs.getString("ToplamMiktar"));
                    malzemeBirimleri.add(rs.getString("MalzemeBirim"));
                    malzemeFiyatlari.add(rs.getDouble("BirimFiyat"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Veritabanına erişirken bir hata oluştu: " + e.getMessage());
        }

        for (int i = 0; i < orijinalMalzemeler.size(); i++) {
            String malzemeAdi = orijinalMalzemeler.get(i);
            String miktar = malzemeMiktarlari.get(i);
            String birim = malzemeBirimleri.get(i);
            Double fiyat = malzemeFiyatlari.get(i);

            JLabel malzemeLabel = new JLabel(malzemeAdi + " - " + miktar + " " + birim + " - Fiyat: " + fiyat + " TL");
            malzemeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            malzemeLabel.setHorizontalAlignment(JLabel.CENTER);
            malzemeLabel.setPreferredSize(new Dimension(560, 30)); 
            malzemelerPanel.add(malzemeLabel);
            malzemeLabels.add(malzemeLabel);

            malzemeLabel.addMouseListener(createMouseListener(malzemeLabel, malzemeAdi, miktar, fiyat, birim));
        }
        //arama butonu 
        searchField.setBorder(BorderFactory.createCompoundBorder(
                searchField.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))); 
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchMalzemeler();
            }

            public void removeUpdate(DocumentEvent e) {
                searchMalzemeler();
            }

            public void changedUpdate(DocumentEvent e) {
                searchMalzemeler();
            }

            private void searchMalzemeler() {
                String searchText = searchField.getText().toLowerCase();
                List<JLabel> matchedLabels = new ArrayList<>();
                List<JLabel> unmatchedLabels = new ArrayList<>();
                for (JLabel label : malzemeLabels) {
                    String text = label.getText().toLowerCase();
                    if (text.contains(searchText)) {
                        matchedLabels.add(label);
                        label.setVisible(true);
                    } else {
                        unmatchedLabels.add(label);
                        label.setVisible(false);
                    }
                }
                malzemelerPanel.removeAll();
                matchedLabels.forEach(malzemelerPanel::add);
                unmatchedLabels.forEach(malzemelerPanel::add);
                malzemelerPanel.revalidate();
                malzemelerPanel.repaint();
            }
        });

        // yeni malzeme ekleme butonu
        CustomButton yeniMalzemeButton = new CustomButton("Yeni Malzeme Ekle",
                new Color(219, 120, 158), 
                new Color(255, 138, 185));
        yeniMalzemeButton.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
            JTextField malzemeAdiField = new JTextField();
            JComboBox<String> malzemeBirimComboBox = new JComboBox<>(new String[]{"kilogram", "litre", "adet", "gram", "mililtre"});
            JTextField malzemeMiktarField = new JTextField();
            JTextField birimFiyatField = new JTextField();

            panel.add(new JLabel("Malzeme Adı:"));
            panel.add(malzemeAdiField);
            panel.add(new JLabel("Malzeme Birimi:"));
            panel.add(malzemeBirimComboBox);
            panel.add(new JLabel("Malzeme Miktarı:"));
            panel.add(malzemeMiktarField);
            panel.add(new JLabel("Birim Fiyat:"));
            panel.add(birimFiyatField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Yeni Malzeme Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String yeniMalzemeAdi = malzemeAdiField.getText();
                String yeniMalzemeBirim = (String) malzemeBirimComboBox.getSelectedItem();
                String yeniMalzemeMiktar = malzemeMiktarField.getText();
                String birimFiyat = birimFiyatField.getText();

                if (yeniMalzemeAdi != null && !yeniMalzemeAdi.trim().isEmpty() && yeniMalzemeBirim != null && yeniMalzemeMiktar != null && !yeniMalzemeMiktar.trim().isEmpty() && birimFiyat != null && !birimFiyat.trim().isEmpty()) {
                    if (mevcutMalzemeler.contains(yeniMalzemeAdi.toLowerCase())) {
                        JOptionPane.showMessageDialog(null, "Bu malzeme zaten mevcut: " + yeniMalzemeAdi);
                    } else {
                        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
                            String sql = "INSERT OR IGNORE INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES ('" + yeniMalzemeAdi + "', '" + yeniMalzemeMiktar + "', '" + yeniMalzemeBirim + "', '" + birimFiyat + "')";
                            stmt.execute(sql);
                            orijinalMalzemeler.add(yeniMalzemeAdi);
                            mevcutMalzemeler.add(yeniMalzemeAdi.toLowerCase());
                            JLabel malzemeLabel = new JLabel(yeniMalzemeAdi + " - " + yeniMalzemeMiktar + " " + yeniMalzemeBirim + " - Fiyat: " + birimFiyat + " TL");
                            malzemeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                            malzemeLabel.setHorizontalAlignment(JLabel.CENTER);
                            malzemelerPanel.add(malzemeLabel);
                            malzemelerPanel.revalidate();
                            malzemelerPanel.repaint();
                            if (previousFrame != null) {
                                previousFrame.refreshData();
                            }

                            malzemeLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    JPanel panel = new JPanel(new GridLayout(2, 2));
                                    panel.add(new JLabel("Yeni Miktar:"));
                                    JTextField yeniMiktarField = new JTextField(yeniMalzemeMiktar);
                                    panel.add(yeniMiktarField);

                                    panel.add(new JLabel("Yeni Birim Fiyat:"));
                                    JTextField yeniFiyatField = new JTextField(birimFiyat);
                                    panel.add(yeniFiyatField);

                                    int option = JOptionPane.showOptionDialog(null, panel, "Miktar ve Fiyat Güncelle/Sil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Güncelle", "Sil", "İptal"}, "Güncelle");

                                    if (option == JOptionPane.YES_OPTION) {
                                        String yeniMiktar = yeniMiktarField.getText();
                                        String yeniFiyat = yeniFiyatField.getText();

                                        if (yeniMiktar != null && !yeniMiktar.trim().isEmpty() && yeniFiyat != null && !yeniFiyat.trim().isEmpty()) {
                                            try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
                                                String sql = "UPDATE Malzemeler SET ToplamMiktar = '" + yeniMiktar + "', BirimFiyat = '" + yeniFiyat + "' WHERE MalzemeAdi = '" + yeniMalzemeAdi + "'";
                                                stmt.execute(sql);
                                                malzemeLabel.setText(yeniMalzemeAdi + " - " + yeniMiktar + " " + yeniMalzemeBirim + " - Fiyat: " + yeniFiyat + " TL");
                                                JOptionPane.showMessageDialog(null, "Miktar ve fiyat güncellendi: " + yeniMalzemeAdi);
                                                if (previousFrame != null) {
                                                    previousFrame.refreshData();
                                                }

                                            } catch (SQLException ex) {
                                                System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                                            }
                                        }
                                    } else if (option == JOptionPane.NO_OPTION) {
                                        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
                                            String sql = "DELETE FROM Malzemeler WHERE MalzemeAdi = '" + yeniMalzemeAdi + "'";
                                            stmt.execute(sql);
                                            malzemelerPanel.remove(malzemeLabel);
                                            malzemelerPanel.revalidate();
                                            malzemelerPanel.repaint();
                                            JOptionPane.showMessageDialog(null, "Malzeme silindi: " + yeniMalzemeAdi);
                                            if (previousFrame != null) {
                                                previousFrame.refreshData();
                                            }

                                        } catch (SQLException ex) {
                                            System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                                        }
                                    }
                                }
                            });

                            JOptionPane.showMessageDialog(null, "Yeni malzeme eklendi: " + yeniMalzemeAdi);
                        } catch (SQLException ex) {
                            System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        // Panel düzeni
        topPanel.add(new JLabel("Malzeme Ara: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(malzemelerPanel), BorderLayout.CENTER);

        buttonPanel.add(yeniMalzemeButton);

        CustomButton geriDonButton = new CustomButton("Geri",
                new Color(219, 120, 158),
                new Color(255, 138, 185));
        geriDonButton.addActionListener(e -> {
            new AnaEkran();
            dispose();
        });
        buttonPanel.add(geriDonButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private MouseListener createMouseListener(JLabel malzemeLabel, String malzemeAdi, String miktar, Double fiyat, String birim) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try (Connection conn = DriverManager.getConnection(url); PreparedStatement stmt = conn.prepareStatement("SELECT MalzemeAdi, ToplamMiktar, BirimFiyat FROM Malzemeler WHERE MalzemeAdi = ?")) {

                    stmt.setString(1, malzemeAdi);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String guncelAd = rs.getString("MalzemeAdi");
                        String guncelMiktar = rs.getString("ToplamMiktar");
                        String guncelFiyat = rs.getString("BirimFiyat");

                        JPanel panel = new JPanel(new GridLayout(3, 2));
                        panel.add(new JLabel("Yeni İsim:"));
                        JTextField yeniAdField = new JTextField(guncelAd);
                        panel.add(yeniAdField);

                        panel.add(new JLabel("Yeni Miktar:"));
                        JTextField yeniMiktarField = new JTextField(guncelMiktar);
                        panel.add(yeniMiktarField);

                        panel.add(new JLabel("Yeni Birim Fiyat:"));
                        JTextField yeniFiyatField = new JTextField(guncelFiyat);
                        panel.add(yeniFiyatField);

                        int option = JOptionPane.showOptionDialog(null, panel, "Malzeme Güncelle/Sil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Güncelle", "Sil", "İptal"}, "Güncelle");

                        if (option == JOptionPane.YES_OPTION) {
                            String yeniAd = yeniAdField.getText();
                            String yeniMiktar = yeniMiktarField.getText();
                            String yeniFiyat = yeniFiyatField.getText();

                            if (yeniAd != null && !yeniAd.trim().isEmpty() && yeniMiktar != null && !yeniMiktar.trim().isEmpty() && yeniFiyat != null && !yeniFiyat.trim().isEmpty()) {
                                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE Malzemeler SET MalzemeAdi = ?, ToplamMiktar = ?, BirimFiyat = ? WHERE MalzemeAdi = ?")) {
                                    updateStmt.setString(1, yeniAd);
                                    updateStmt.setString(2, yeniMiktar);
                                    updateStmt.setString(3, yeniFiyat);
                                    updateStmt.setString(4, malzemeAdi);
                                    updateStmt.executeUpdate();

                                    malzemeLabel.setText(yeniAd + " - " + yeniMiktar + " " + birim + " - Fiyat: " + yeniFiyat + " TL");

                                    for (MouseListener listener : malzemeLabel.getMouseListeners()) {
                                        malzemeLabel.removeMouseListener(listener);
                                    }
                                    malzemeLabel.addMouseListener(createMouseListener(malzemeLabel, yeniAd, yeniMiktar, Double.parseDouble(yeniFiyat), birim));

                                    int index = orijinalMalzemeler.indexOf(malzemeAdi);
                                    if (index >= 0) {
                                        orijinalMalzemeler.set(index, yeniAd);
                                    }

                                    int lowerIndex = mevcutMalzemeler.indexOf(malzemeAdi.toLowerCase());
                                    if (lowerIndex >= 0) {
                                        mevcutMalzemeler.set(lowerIndex, yeniAd.toLowerCase());
                                    }

                                    JOptionPane.showMessageDialog(null, "Malzeme güncellendi: " + yeniAd);
                                    if (previousFrame != null) {
                                        previousFrame.refreshData();
                                    }

                                } catch (SQLException ex) {
                                    System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                                }
                            }
                        } else if (option == JOptionPane.NO_OPTION) {
                            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Malzemeler WHERE MalzemeAdi = ?")) {
                                deleteStmt.setString(1, malzemeAdi);
                                deleteStmt.executeUpdate();

                                malzemelerPanel.remove(malzemeLabel);
                                malzemelerPanel.revalidate();
                                malzemelerPanel.repaint();

                                orijinalMalzemeler.remove(malzemeAdi);
                                mevcutMalzemeler.remove(malzemeAdi.toLowerCase());

                                JOptionPane.showMessageDialog(null, "Malzeme silindi: " + malzemeAdi);
                                if (previousFrame != null) {
                                    previousFrame.refreshData();
                                }

                            } catch (SQLException ex) {
                                System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.out.println("Veritabanına erişirken bir hata oluştu: " + ex.getMessage());
                }
            }
        };
    }
}

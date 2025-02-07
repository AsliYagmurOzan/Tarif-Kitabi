package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TarifEklemeEkrani extends JFrame {

    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
    private List<String> mevcutMalzemeler = new ArrayList<>();
    private JPanel malzemelerPanel;
    private JTextArea talimatArea;
    private JTextField tarifAdiField;
    private JComboBox<String> kategoriComboBox;
    private JTextField hazirlamaSuresiField;
    private JTextField resimYoluField;
    private JFrame parent;
    private JTextField kaloriField;
    private JTextField kisiSayisiField;

    public TarifEklemeEkrani(JFrame parent) {
        this.parent = parent;

        setTitle("Yeni Tarif Ekle");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new BackgroundPanel("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\arkaplan.png"); 
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        tarifAdiField = new JTextField(30);
        mainPanel.add(createInputField("Tarif Adı:", tarifAdiField), gbc);

        gbc.gridy++;
        kategoriComboBox = new JComboBox<>(new String[]{"Ana Yemek", "Tatlı", "Çorba", "Salata/Meze", "Kahvaltılık"});
        mainPanel.add(createComboBoxField("Kategori:", kategoriComboBox), gbc);

        gbc.gridy++;
        hazirlamaSuresiField = new JTextField(10);
        mainPanel.add(createInputField("Hazırlama Süresi (dakika):", hazirlamaSuresiField), gbc);

        gbc.gridy++;
        kaloriField = new JIntegerField(10);
        mainPanel.add(createInputField("Kalori:", kaloriField), gbc);

        gbc.gridy++;
        kisiSayisiField = new JTextField(10);
        mainPanel.add(createInputField("Kişi Sayısı:", kisiSayisiField), gbc);

        gbc.gridy++;
        mainPanel.add(createTextAreaField("Tarif Talimatları:"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Gerekli Malzemeler:"), gbc);
        gbc.gridy++;
        gbc.gridwidth = 3;
        malzemelerPanel = new JPanel();
        malzemelerPanel.setLayout(new BoxLayout(malzemelerPanel, BoxLayout.Y_AXIS));
        JScrollPane malzemelerScrollPane = new JScrollPane(malzemelerPanel);
        malzemelerScrollPane.setPreferredSize(new Dimension(500, 250));
        mainPanel.add(malzemelerScrollPane, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        CustomButton malzemeEkleButton = new CustomButton("Malzeme Ekle",
                new Color(219, 120, 158),
                new Color(255, 138, 185)); 
        malzemeEkleButton.addActionListener(e -> malzemeSecimiEkle());
        mainPanel.add(malzemeEkleButton, gbc);

        gbc.gridx++;
        CustomButton yeniMalzemeButton = new CustomButton("Yeni Malzeme Ekle",
                new Color(219, 120, 158),
                new Color(255, 138, 185));
        yeniMalzemeButton.addActionListener(e -> yeniMalzemeEkle());
        mainPanel.add(yeniMalzemeButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        resimYoluField = new JTextField(30);
        mainPanel.add(createInputField("Resim Yolu:", resimYoluField, true), gbc);

        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        CustomButton kaydetButton = new CustomButton("Tarifi Kaydet",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        kaydetButton.addActionListener(e -> {
            if (tarifAdiField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tarif adı boş olamaz.");
                return;
            }
            if (talimatArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tarif talimatı boş olamaz.");
                return;
            }
            if (malzemelerPanel.getComponentCount() == 0) {
                JOptionPane.showMessageDialog(this, "En az bir malzeme eklemelisiniz.");
                return;
            }

            try {
                int kalori = Integer.parseInt(kaloriField.getText());

                tarifiKaydet(
                        tarifAdiField.getText(),
                        (String) kategoriComboBox.getSelectedItem(),
                        hazirlamaSuresiField.getText(),
                        talimatArea.getText(),
                        resimYoluField.getText(),
                        kisiSayisiField.getText(),
                        kalori
                );

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Kalori bilgisi geçerli bir sayı olmalıdır.");
            }
        });
        buttonPanel.add(kaydetButton);

        CustomButton iptalButton = new CustomButton("İptal",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        iptalButton.addActionListener(e -> {
            if (parent instanceof AnaEkran) {
                AnaEkran anaEkran = (AnaEkran) parent;
                anaEkran.setVisible(true); 
            } else {
                new AnaEkran(); 
            }
            dispose(); 
        });
        buttonPanel.add(iptalButton);

        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private static class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            backgroundImage = new ImageIcon(imagePath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private JPanel createInputField(String labelText, JTextField textField) {
        return createInputField(labelText, textField, false);
    }

    private JPanel createInputField(String labelText, JTextField textField, boolean addButton) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setOpaque(false); 
        panel.setOpaque(false);
        panel.add(label);
        panel.add(textField);

        if (addButton) {
            CustomButton selectButton = new CustomButton("Seç",
                    new Color(219, 120, 158), 
                    new Color(255, 138, 185)); 
            selectButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });
            panel.add(selectButton);
        }
        return panel;
    }

    private JPanel createComboBoxField(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setOpaque(false); 
        comboBox.setOpaque(false);
        panel.setOpaque(false); 
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

    private JPanel createTextAreaField(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        label.setOpaque(false); 
        panel.setOpaque(false); 
        talimatArea = new JTextArea(5, 30);
        talimatArea.setLineWrap(true);
        talimatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(talimatArea);
        panel.add(label);
        panel.add(scrollPane);
        return panel;
    }

    private void malzemeSecimiEkle() {
        JPanel malzemePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> malzemeComboBox = new JComboBox<>(getMalzemeler().toArray(new String[0]));
        JTextField miktarField = new JTextField(5);
        JComboBox<String> birimComboBox = new JComboBox<>(new String[]{"kilogram", "gram", "litre", "mililitre", "adet"});

        CustomButton silButton = new CustomButton("Sil",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        silButton.addActionListener(e -> {
            malzemelerPanel.remove(malzemePanel);
            malzemelerPanel.revalidate();
            malzemelerPanel.repaint();
        });

        malzemePanel.add(new JLabel("Malzeme:"));
        malzemePanel.add(malzemeComboBox);
        malzemePanel.add(new JLabel("Miktar:"));
        malzemePanel.add(miktarField);
        malzemePanel.add(new JLabel("Birim:"));
        malzemePanel.add(birimComboBox);
        malzemePanel.add(silButton);

        malzemelerPanel.add(malzemePanel);
        malzemelerPanel.revalidate();
        malzemelerPanel.repaint();
    }

    private List<String> getMalzemeler() {
        List<String> malzemeler = new ArrayList<>();
        String query = "SELECT MalzemeAdi FROM Malzemeler";
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                malzemeler.add(rs.getString("MalzemeAdi"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Malzemeler alınırken hata oluştu: " + e.getMessage());
        }
        return malzemeler;
    }

    private void yeniMalzemeEkle() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField malzemeAdiField = new JTextField();
        JComboBox<String> malzemeBirimComboBox = new JComboBox<>(new String[]{"kilogram", "litre", "adet", "gram", "mililitre"});
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

            if (yeniMalzemeAdi != null && !yeniMalzemeAdi.trim().isEmpty()) {
                addMalzemeToDatabase(yeniMalzemeAdi, yeniMalzemeBirim, yeniMalzemeMiktar, birimFiyat);
            }
        }
    }

    private void addMalzemeToDatabase(String malzemeAdi, String birim, String miktar, String birimFiyat) {
        if (mevcutMalzemeler.contains(malzemeAdi)) {
            JOptionPane.showMessageDialog(this, "Bu malzeme zaten mevcut.");
        } else {
            try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
                String sql = "INSERT INTO Malzemeler (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES ('" + malzemeAdi + "', '" + miktar + "', '" + birim + "', '" + birimFiyat + "')";
                stmt.executeUpdate(sql);
                mevcutMalzemeler.add(malzemeAdi);
                JOptionPane.showMessageDialog(this, "Yeni malzeme başarıyla eklendi.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Malzeme eklenirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private void tarifiKaydet(String tarifAdi, String kategori, String hazirlamaSuresi, String talimatlar, String resimYolu, String kisiSayisi, Integer kalori) {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO Tarifler (TarifAdi, Kategori, HazirlamaSuresi, Talimatlar, ResimYolu, Kalori, KisiSayisi) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, tarifAdi);
                pstmt.setString(2, kategori);
                pstmt.setString(3, hazirlamaSuresi);
                pstmt.setString(4, talimatlar);
                pstmt.setString(5, resimYolu);
                pstmt.setInt(6, kalori);
                pstmt.setString(7, kisiSayisi);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int tarifId = 0;
                if (rs.next()) {
                    tarifId = rs.getInt(1);
                }

                for (Component component : malzemelerPanel.getComponents()) {
                    if (component instanceof JPanel) {
                        JPanel malzemePanel = (JPanel) component;
                        JComboBox<String> malzemeComboBox = (JComboBox<String>) malzemePanel.getComponent(1);
                        JTextField miktarField = (JTextField) malzemePanel.getComponent(3);
                        JComboBox<String> birimComboBox = (JComboBox<String>) malzemePanel.getComponent(5); // Birim ComboBox

                        String malzemeAdi = (String) malzemeComboBox.getSelectedItem();
                        String miktar = miktarField.getText();
                        String birim = (String) birimComboBox.getSelectedItem();

                        if (!miktar.isEmpty()) {
                            String malzemeSql = "INSERT INTO TarifMalzemeIliskisi (TarifID, MalzemeID, MalzemeMiktar, MalzemeBirim) VALUES (?, (SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?), ?, ?)";
                            try (PreparedStatement malzemePstmt = conn.prepareStatement(malzemeSql)) {
                                malzemePstmt.setInt(1, tarifId);
                                malzemePstmt.setString(2, malzemeAdi);
                                malzemePstmt.setString(3, miktar);
                                malzemePstmt.setString(4, birim);
                                malzemePstmt.executeUpdate();
                            }
                        }
                    }
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Tarif başarıyla kaydedildi.");

            if (parent instanceof AnaEkran) {
                AnaEkran anaEkran = (AnaEkran) parent;
                anaEkran.refreshData();
            } else {
                new AnaEkran(); 
            }

            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Tarif eklenirken hata oluştu: " + e.getMessage());
        }
    }

}

class JIntegerField extends JTextField {

    public JIntegerField(int columns) {
        super(columns);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); 
                }
            }
        });
    }

    public Integer getInteger() {
        String text = getText();
        return text.isEmpty() ? null : Integer.parseInt(text);
    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest(); 
    }
}

package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarifDuzeltmeEkrani extends JFrame {

    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
    private List<String> mevcutMalzemeler = new ArrayList<>();
    private JPanel malzemelerPanel;
    private Recipe recipe;
    private JFrame previousFrame;
    private JTextArea talimatArea;
    private JTextField nameTextField;
    private JComboBox<String> categoryComboBox;
    private JTextField prepTimeTextField;
    private JTextField imagePathTextField;
    private JTextField calorieTextField;
    private JTextField servingsTextField;

    public TarifDuzeltmeEkrani(Recipe recipe, JFrame previousFrame) {
        this.recipe = recipe;
        this.previousFrame = previousFrame;

        setTitle("Tarif Düzenle");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pencereyi tam ekran yap
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new BackgroundPanel("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\arkaplan.png"); // Arka plan resmi yolu
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        nameTextField = new JTextField(recipe.getName(), 30);
        mainPanel.add(createInputField("Tarif Adı:", nameTextField), gbc);

        gbc.gridy++;
        categoryComboBox = new JComboBox<>(new String[]{"Ana Yemek", "Tatlı", "Çorba", "Salata/Meze", "Kahvaltılık"});
        categoryComboBox.setSelectedItem(recipe.getCategory());
        mainPanel.add(createComboBoxField("Kategori:", categoryComboBox), gbc);

        gbc.gridy++;
        prepTimeTextField = new JTextField(String.valueOf(recipe.getPrepTime()), 10);
        mainPanel.add(createInputField("Hazırlama Süresi (dakika):", prepTimeTextField), gbc);

        gbc.gridy++;
        calorieTextField = new JTextField(String.valueOf(recipe.getKalori()), 10); // Kalori int olarak alınıyor
        mainPanel.add(createInputField("Kalori:", calorieTextField), gbc);

        gbc.gridy++;
        servingsTextField = new JTextField(recipe.getKisiSayisi(), 10); // Kişi sayısı varchar olarak alınıyor
        mainPanel.add(createInputField("Kişi Sayısı:", servingsTextField), gbc);

        gbc.gridy++;
        mainPanel.add(createTextAreaField("Tarif Talimatları:", recipe.getInstructions()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3; 
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Gerekli Malzemeler:"), gbc);
        gbc.gridy++;
        gbc.gridwidth = 3;
        malzemelerPanel = new JPanel();
        malzemelerPanel.setLayout(new BoxLayout(malzemelerPanel, BoxLayout.Y_AXIS));
        loadMalzemeler();

        JScrollPane malzemelerScrollPane = new JScrollPane(malzemelerPanel);
        malzemelerScrollPane.setPreferredSize(new Dimension(500, 250));
        mainPanel.add(malzemelerScrollPane, gbc);

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
        imagePathTextField = new JTextField(recipe.getImagePath(), 30);
        mainPanel.add(createInputField("Resim Yolu:", imagePathTextField, true), gbc);

        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        buttonPanel.setOpaque(false); 

        CustomButton kaydetButton = new CustomButton("Değişiklikleri Kaydet",
                new Color(219, 120, 158), 
                new Color(255, 138, 185));

        kaydetButton.addActionListener(e -> {
            tarifGuncelle();
            if (previousFrame instanceof AnaEkran) {
                ((AnaEkran) previousFrame).refreshData();
            } else if (previousFrame instanceof Tarif) {
                ((Tarif) previousFrame).updateRecipeDetails(recipe);
            }
            previousFrame.setVisible(true);
            dispose();
        });

        CustomButton iptalButton = new CustomButton("İptal",
                new Color(219, 120, 158),
                new Color(255, 138, 185)); 

        iptalButton.addActionListener(e -> {
            previousFrame.setVisible(true); 
            dispose();
        });

        buttonPanel.add(kaydetButton);
        buttonPanel.add(iptalButton);

        mainPanel.add(buttonPanel, gbc);
        add(mainPanel);

        setVisible(true);

    }

    private static class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            backgroundImage = new ImageIcon(imagePath).getImage();
            setOpaque(false); 
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
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setOpaque(false);
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
        panel.setOpaque(false); 
        JLabel label = new JLabel(labelText);
        label.setOpaque(false); 
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

    private JPanel createTextAreaField(String labelText, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false); 
        JLabel label = new JLabel(labelText);
        label.setOpaque(false); 
        talimatArea = new JTextArea(text, 5, 30);
        talimatArea.setLineWrap(true);
        talimatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(talimatArea);
        panel.add(label);
        panel.add(scrollPane);
        return panel;
    }

    private void loadMalzemeler() {
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            String query = "SELECT m.MalzemeAdi, t.MalzemeMiktar, t.MalzemeBirim FROM TarifMalzemeIliskisi t "
                    + "JOIN Malzemeler m ON t.MalzemeID = m.MalzemeID WHERE t.TarifID = " + recipe.getId();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String malzemeAdi = rs.getString("MalzemeAdi");
                String miktar = String.valueOf(rs.getDouble("MalzemeMiktar"));
                String birim = rs.getString("MalzemeBirim");
                JPanel malzemePanel = createMalzemePanel(malzemeAdi, miktar, birim);
                malzemelerPanel.add(malzemePanel);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Malzemeler yüklenirken hata oluştu: " + e.getMessage());
        }
        malzemelerPanel.revalidate();
        malzemelerPanel.repaint();
    }

    private JPanel createMalzemePanel(String malzemeAdi, String miktar, String birim) {
        JPanel malzemePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        malzemePanel.setOpaque(false);

        JComboBox<String> malzemeComboBox = new JComboBox<>(getMalzemeler().toArray(new String[0]));
        malzemeComboBox.setSelectedItem(malzemeAdi);
        JTextField miktarField = new JTextField(miktar, 5);

        JComboBox<String> birimComboBox = new JComboBox<>(new String[]{"kilogram", "litre", "adet", "gram", "mililitre"});
        birimComboBox.setSelectedItem(birim);

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

        return malzemePanel;
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

    private void malzemeSecimiEkle() {
        JPanel malzemePanel = createMalzemePanel("", "", "");
        malzemelerPanel.add(malzemePanel);
        malzemelerPanel.revalidate();
        malzemelerPanel.repaint();
    }

    private void tarifGuncelle() {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            String sql = "UPDATE Tarifler SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ?, ResimYolu = ?, Kalori = ?, KisiSayisi = ? WHERE TarifID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nameTextField.getText());
                pstmt.setString(2, (String) categoryComboBox.getSelectedItem());
                pstmt.setInt(3, Integer.parseInt(prepTimeTextField.getText()));
                pstmt.setString(4, talimatArea.getText());
                pstmt.setString(5, imagePathTextField.getText());
                pstmt.setInt(6, Integer.parseInt(calorieTextField.getText()));
                pstmt.setString(7, servingsTextField.getText());
                pstmt.setInt(8, recipe.getId());
                pstmt.executeUpdate();
            }

            String deleteSql = "DELETE FROM TarifMalzemeIliskisi WHERE TarifID = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, recipe.getId());
                deleteStmt.executeUpdate();
            }

            for (Component component : malzemelerPanel.getComponents()) {
                if (component instanceof JPanel) {
                    JPanel malzemePanel = (JPanel) component;
                    JComboBox<String> malzemeComboBox = (JComboBox<String>) malzemePanel.getComponent(1);
                    JTextField miktarField = (JTextField) malzemePanel.getComponent(3);
                    JComboBox<String> birimComboBox = (JComboBox<String>) malzemePanel.getComponent(5); 

                    String malzemeAdi = (String) malzemeComboBox.getSelectedItem();
                    String miktar = miktarField.getText();
                    String birim = (String) birimComboBox.getSelectedItem();

                    if (!miktar.isEmpty()) {
                        String malzemeSql = "INSERT INTO TarifMalzemeIliskisi (TarifID, MalzemeID, MalzemeMiktar, MalzemeBirim) VALUES (?, (SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?), ?, ?)";
                        try (PreparedStatement malzemePstmt = conn.prepareStatement(malzemeSql)) {
                            malzemePstmt.setInt(1, recipe.getId());
                            malzemePstmt.setString(2, malzemeAdi);
                            malzemePstmt.setString(3, miktar);
                            malzemePstmt.setString(4, birim); 
                            malzemePstmt.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            recipe.name = nameTextField.getText();
            recipe.category = (String) categoryComboBox.getSelectedItem();
            recipe.prepTime = Integer.parseInt(prepTimeTextField.getText());
            recipe.instructions = talimatArea.getText();
            recipe.imagePath = imagePathTextField.getText();
            recipe.kalori = Integer.parseInt(calorieTextField.getText());
            recipe.kisiSayisi = servingsTextField.getText();

            JOptionPane.showMessageDialog(this, "Tarif başarıyla güncellendi.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Tarif güncellenirken hata oluştu: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Hazırlama süresi geçerli bir sayı olmalıdır.");
        }
    }

}

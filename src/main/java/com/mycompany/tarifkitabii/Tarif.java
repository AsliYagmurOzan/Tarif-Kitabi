package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Tarif extends JFrame {

    private Recipe recipe;
    private JFrame previousFrame;
    private JLabel titleLabel, photoLabel;
    private JTextArea instructionsArea;
    private JPanel ingredientsPanel, detailsPanel;

    public Tarif(Recipe recipe, JFrame previousFrame) {
        this.recipe = recipe;
        this.previousFrame = previousFrame;

        setTitle(recipe.getName());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.decode("#FFE6F0")); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setOpaque(false); 

        titleLabel = new JLabel(recipe.getName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 0, 0, 120));
        titleLabel.setForeground(Color.WHITE);

        photoLabel = new JLabel();
        photoLabel.setLayout(new BorderLayout());
        if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            ImageIcon defaultIcon = new ImageIcon("path/to/default-image.jpg");
            Image scaledImage = defaultIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaledImage));
        }
        photoLabel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(photoLabel);

        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false); 
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        instructionsArea = new JTextArea(); 
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setLineWrap(true);
        instructionsArea.setEditable(false);
        instructionsArea.setFont(new Font("Serif", Font.PLAIN, 16));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leftPanel.add(detailsPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        ingredientsPanel = new JPanel();
        ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
        ingredientsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.decode("#A8316D")), "Gereken Malzemeler", 0, 0,
                new Font("Serif", Font.BOLD, 16), Color.decode("#A8316D")));
        ingredientsPanel.setOpaque(false); // Arka planı görünmez yapıldı

        JScrollPane ingredientsScrollPane = new JScrollPane(ingredientsPanel);
        ingredientsScrollPane.setPreferredSize(new Dimension(350, 250));
        ingredientsScrollPane.setBackground(Color.decode("#FFFFFF"));
        ingredientsScrollPane.getViewport().setOpaque(false);
        loadIngredients(ingredientsPanel);
        leftPanel.add(ingredientsScrollPane);

        instructionsArea = new JTextArea(recipe.getInstructions());
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setLineWrap(true);
        instructionsArea.setEditable(false);
        instructionsArea.setFont(new Font("Serif", Font.PLAIN, 18));
        instructionsArea.setBackground(Color.decode("#FFFFFF"));
        instructionsArea.setForeground(Color.BLACK);
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instructionsLabel = new JLabel("Talimatlar");
        instructionsLabel.setFont(new Font("Serif", Font.BOLD, 25));
        instructionsLabel.setForeground(Color.decode("#A8316D"));

        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setOpaque(false); 
        instructionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        instructionsPanel.add(instructionsLabel, BorderLayout.NORTH); 

        JScrollPane centeredScrollPane = new JScrollPane(instructionsArea);
        centeredScrollPane.setPreferredSize(new Dimension(500, 300));
        instructionsPanel.add(centeredScrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(instructionsPanel, BorderLayout.CENTER);

        CustomButton backButton = new CustomButton("Geri",
                new Color(219, 120, 158), 
                new Color(255, 138, 185));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            new AnaEkran();
            dispose();
        });

        CustomButton deleteButton = new CustomButton("Tarifi Sil",
                new Color(219, 120, 158),
                new Color(255, 138, 185));
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> {
            int confirmation = JOptionPane.showConfirmDialog(null, "Tarifi silmek istediğinize emin misiniz?", "Tarifi Sil", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                deleteRecipe();
            }
        });

        CustomButton editButton = new CustomButton("Tarifi Düzenle",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        editButton.setFont(new Font("Arial", Font.BOLD, 14));
        editButton.setForeground(Color.WHITE);
        editButton.addActionListener(e -> new TarifDuzeltmeEkrani(recipe, Tarif.this));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false); 
        buttonPanel.add(backButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);

        updateRecipeDetails(recipe);
    }

    private void loadIngredients(JPanel ingredientsPanel) {
        String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            String sql = "SELECT m.MalzemeAdi, t.MalzemeMiktar, t.MalzemeBirim, m.ToplamMiktar AS MevcutMiktar, m.MalzemeBirim AS MevcutBirim "
                    + "FROM TarifMalzemeIliskisi t "
                    + "JOIN Malzemeler m ON t.MalzemeID = m.MalzemeID "
                    + "WHERE t.TarifID = " + recipe.getId();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String malzemeAdi = rs.getString("MalzemeAdi");
                double gerekliMiktar = rs.getDouble("MalzemeMiktar");
                String gerekliBirim = rs.getString("MalzemeBirim");
                double mevcutMiktar = rs.getDouble("MevcutMiktar");
                String mevcutBirim = rs.getString("MevcutBirim");

                double mevcutMiktarConverted = convertToCommonUnit(mevcutMiktar, mevcutBirim, gerekliBirim);
                double eksikMiktar = gerekliMiktar - mevcutMiktarConverted;
                String labelText = malzemeAdi + ": " + gerekliMiktar + " " + gerekliBirim;

                if (eksikMiktar > 0) {
                    labelText += " (Eksik: " + eksikMiktar + " " + gerekliBirim + ")";
                }

                JLabel ingredientLabel = new JLabel(labelText);
                ingredientLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                ingredientsPanel.add(ingredientLabel);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Malzemeler yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    private double convertToCommonUnit(double miktar, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) {
            return miktar;
        }

        switch (fromUnit) {
            case "kilogram":
                if (toUnit.equals("gram")) {
                    return miktar * 1000;
                }
                break;
            case "gram":
                if (toUnit.equals("kilogram")) {
                    return miktar / 1000;
                }
                break;
            case "litre":
                if (toUnit.equals("mililitre")) {
                    return miktar * 1000;
                }
                break;
            case "mililitre":
                if (toUnit.equals("litre")) {
                    return miktar / 1000;
                }
                break;
        }

        return miktar;
    }

    private void deleteRecipe() {
        String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                String deleteRelationSql = "DELETE FROM TarifMalzemeIliskisi WHERE TarifID = " + recipe.getId();
                stmt.execute(deleteRelationSql);

                String deleteRecipeSql = "DELETE FROM Tarifler WHERE TarifID = " + recipe.getId();
                stmt.execute(deleteRecipeSql);

                conn.commit();
                JOptionPane.showMessageDialog(null, "Tarif başarıyla silindi.");

                AnaEkran anaEkran = new AnaEkran();
                anaEkran.refreshData();
                anaEkran.setVisible(true);

                dispose();

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Tarif silinirken hata oluştu: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Tarif silinirken hata oluştu: " + e.getMessage());
        }
    }

    public void updateRecipeDetails(Recipe updatedRecipe) {
        this.recipe = updatedRecipe;

        titleLabel.setText(recipe.getName());
        instructionsArea.setText(recipe.getInstructions());

        if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaledImage));
        }

        detailsPanel.removeAll();
        JLabel servingsLabel = new JLabel("Kişi Sayısı: " + recipe.getKisiSayisi());
        JLabel calorieLabel = new JLabel("Kalori: " + recipe.getKalori() + " kcal");
        JLabel prepTimeLabel = new JLabel("Hazırlama Süresi: " + recipe.getPrepTime() + " dk");

        Font infoFont = new Font("Serif", Font.PLAIN, 16);
        servingsLabel.setFont(infoFont);
        calorieLabel.setFont(infoFont);
        prepTimeLabel.setFont(infoFont);

        detailsPanel.add(servingsLabel);
        detailsPanel.add(calorieLabel);
        detailsPanel.add(prepTimeLabel);

        ingredientsPanel.removeAll();
        loadIngredients(ingredientsPanel);

        revalidate();
        repaint();
    }
}

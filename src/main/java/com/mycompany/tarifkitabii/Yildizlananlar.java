package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Yildizlananlar extends JFrame {

    private List<Recipe> favoriYemekler = new ArrayList<>();
    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";

    public Yildizlananlar(List<Integer> favoriYemekIdleri, AnaEkran previousFrame) {
        setTitle("Yıldızlanan Tarifler");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BackgroundPanel mainPanel = new BackgroundPanel("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\sakura.png");
        mainPanel.setLayout(new BorderLayout());

        loadFavoriteRecipes(favoriYemekIdleri);

        CustomButton geriButton = new CustomButton("Geri",
                new Color(219, 120, 158),
                new Color(255, 138, 185)); 
        geriButton.addActionListener(e -> {
            new AnaEkran();
            dispose(); 
        });

        JPanel geriPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        geriPanel.setOpaque(false);
        geriPanel.add(geriButton);
        mainPanel.add(geriPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0, 3, 20, 20));
        contentPanel.setOpaque(false); 
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        for (Recipe recipe : favoriYemekler) {
            JPanel recipePanel = new JPanel();
            recipePanel.setLayout(new BorderLayout());
            recipePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
            recipePanel.setPreferredSize(new Dimension(200, 250));

            String originalTitle = recipe.getName();
            JLabel titleLabel = new JLabel(originalTitle);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            recipePanel.add(titleLabel, BorderLayout.NORTH);

            double cost = recipe.getCost();
            JLabel costLabel = new JLabel("Maliyet: " + String.format("%.2f", cost) + " TL");
            costLabel.setHorizontalAlignment(JLabel.CENTER);
            costLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            recipePanel.add(costLabel, BorderLayout.SOUTH);

            if (cost > 0.0) {
                Color redColor = Color.RED;
                titleLabel.setForeground(redColor);
                costLabel.setForeground(redColor);
                recipePanel.setBorder(BorderFactory.createLineBorder(redColor, 2));
                if (!titleLabel.getText().endsWith(" ❢")) {
                    titleLabel.setText(titleLabel.getText() + " ❢");
                }
            } else {
                Color greenColor = new Color(0, 128, 0);
                titleLabel.setForeground(greenColor);
                costLabel.setForeground(greenColor);
                recipePanel.setBorder(BorderFactory.createLineBorder(greenColor, 2));
                if (titleLabel.getText().endsWith(" ❢")) {
                    titleLabel.setText(originalTitle);
                }
            }

            JLabel imageLabel = new JLabel();
            if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
                ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
                Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                ImageIcon defaultIcon = new ImageIcon("path/to/default-image.jpg");
                Image scaledImage = defaultIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            }
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            recipePanel.add(imageLabel, BorderLayout.CENTER);

            recipePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    new Tarif(recipe, previousFrame);
                    setVisible(false); 
                }
            });

            contentPanel.add(recipePanel);
        }

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        setVisible(true);
    }

    class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            ImageIcon icon = new ImageIcon(imagePath);
            backgroundImage = icon.getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private double birimDonustur(double miktar, String mevcutBirim, String hedefBirim) {
        if (mevcutBirim == null || hedefBirim == null || mevcutBirim.equals(hedefBirim)) {
            return miktar; 
        }

        if (mevcutBirim.equals("adet") || hedefBirim.equals("adet")) {
            return miktar;
        }

        switch (mevcutBirim) {
            case "kilogram":
                if (hedefBirim.equals("gram")) {
                    return miktar * 1000;
                }
                break;
            case "gram":
                if (hedefBirim.equals("kilogram")) {
                    return miktar / 1000;
                }
                break;
            case "litre":
                if (hedefBirim.equals("mililitre")) {
                    return miktar * 1000;
                }
                break;
            case "mililitre":
                if (hedefBirim.equals("litre")) {
                    return miktar / 1000;
                }
                break;
            default:
                System.out.println("Bilinmeyen birim dönüşümü: " + mevcutBirim + " -> " + hedefBirim);
        }
        return miktar;
    }

    private double calculateRecipeCost(Connection conn, int recipeId) {
        double totalCost = 0.0;

        String costQuery = "SELECT tm.MalzemeMiktar, tm.MalzemeBirim AS TarifBirim, "
                + "m.BirimFiyat, m.ToplamMiktar, m.MalzemeBirim AS DepoBirim "
                + "FROM TarifMalzemeIliskisi tm "
                + "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID "
                + "WHERE tm.TarifID = ?";

        try (PreparedStatement costStmt = conn.prepareStatement(costQuery)) {
            costStmt.setInt(1, recipeId);
            ResultSet costRs = costStmt.executeQuery();

            while (costRs.next()) {
                double tarifMiktar = costRs.getDouble("MalzemeMiktar");
                String tarifBirim = costRs.getString("TarifBirim");
                double birimFiyat = costRs.getDouble("BirimFiyat");
                double depoMiktar = costRs.getDouble("ToplamMiktar");
                String depoBirim = costRs.getString("DepoBirim");

                double donusturulmusMiktar = birimDonustur(tarifMiktar, tarifBirim, depoBirim);
                double eksikMiktar = Math.max(0, donusturulmusMiktar - depoMiktar);
                double malzemeMaliyeti = eksikMiktar * birimFiyat;

                totalCost += malzemeMaliyeti;
            }
        } catch (SQLException e) {
            System.out.println("Maliyet hesaplama hatası: " + e.getMessage());
        }

        return totalCost;
    }

    private void loadFavoriteRecipes(List<Integer> favoriYemekIdleri) {
        try (Connection conn = DriverManager.getConnection(url)) {
            for (int id : favoriYemekIdleri) {
                String sql = "SELECT t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar, t.ResimYolu, t.Kalori, t.KisiSayisi "
                        + "FROM Tarifler t "
                        + "WHERE t.TarifID = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        String name = rs.getString("TarifAdi");
                        String category = rs.getString("Kategori");
                        int prepTime = rs.getInt("HazirlamaSuresi");
                        String instructions = rs.getString("Talimatlar");
                        String imagePath = rs.getString("ResimYolu");
                        int kalori = rs.getInt("Kalori");
                        String kisiSayisi = rs.getString("KisiSayisi");

                        double cost = calculateRecipeCost(conn, id);

                        Recipe recipe = new Recipe(id, name, category, prepTime, instructions, imagePath, cost, 0.0, kalori, kisiSayisi); // Eşleşme yüzdesini şimdilik 0 olarak ayarla

                        favoriYemekler.add(recipe);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Favori yemekler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }
}

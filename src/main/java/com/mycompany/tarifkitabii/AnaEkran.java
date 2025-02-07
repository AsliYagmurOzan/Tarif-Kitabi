package com.mycompany.tarifkitabii;

import org.apache.commons.text.similarity.LevenshteinDistance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class BackgroundPanel extends JPanel {

    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

}

public class AnaEkran extends JFrame {

    private List<Integer> yildizlananIdleri = new ArrayList<>();
    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
    private JPanel mainPanel;
    private List<Recipe> recipes;
    private List<Recipe> displayedRecipes;
    private List<JCheckBox> categoryCheckBoxes;
    private JTextField minCostField;
    private JTextField maxCostField;
    private JPanel filterPanel;
    private List<JCheckBox> ingredientCheckBoxes;
    private JLabel sortCriteriaLabel;
    private boolean highlightByCost = false; 

    public AnaEkran() {

        JPanel mainContainer = new BackgroundPanel("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\sakura.png");
        mainContainer.setLayout(new BorderLayout());

        setTitle("Tarif Rehberi");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadStarsRecipes();

        CustomButton logoButton = new CustomButton("Ana Sayfa",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        logoButton.setPreferredSize(new Dimension(120, 40));
        logoButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        logoButton.setForeground(Color.BLACK); 

        ImageIcon logoIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); 
        logoButton.setIcon(new ImageIcon(logoImage));

        logoButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        logoButton.setIconTextGap(7);

        logoButton.addActionListener(e -> {
            new SliderScreen(); 
            dispose(); 
        });

        CustomButton profilButton = new CustomButton("",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        profilButton.setPreferredSize(new Dimension(40, 40));
        profilButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        profilButton.setToolTipText("Profil");

        ImageIcon profileIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\profil.png"); // Profil ikonunuzun yolunu ayarlayın
        Image profileImage = profileIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        profilButton.setIcon(new ImageIcon(profileImage));

        profilButton.addActionListener(e -> {
            JPopupMenu profilMenu = new JPopupMenu();

            Font menuFont = new Font("Arial", Font.PLAIN, 14);

            JMenuItem malzemelerItem = new JMenuItem("Malzemelerim");
            malzemelerItem.setFont(menuFont); 

            JMenuItem yildizlananlarItem = new JMenuItem("Yıldızlananlar");
            yildizlananlarItem.setFont(menuFont); 

            malzemelerItem.addActionListener(event -> {
                new MalzemelerEkrani(this);
                setVisible(false);
            });

            yildizlananlarItem.addActionListener(event -> {
                new Yildizlananlar(yildizlananIdleri, this);
                setVisible(false);
            });

            profilMenu.add(malzemelerItem);
            profilMenu.addSeparator(); 
            profilMenu.add(yildizlananlarItem);

            profilMenu.show(profilButton, profilButton.getWidth() / 2, profilButton.getHeight() / 2);
        });

        // Arama çubuğu
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 40)); 
        searchField.setFont(new Font("Arial", Font.PLAIN, 17));

        CustomButton searchButton = new CustomButton("",
                new Color(219, 120, 158),
                new Color(255, 138, 185));
        searchButton.setPreferredSize(new Dimension(40, 40));
        searchButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        searchButton.setToolTipText("Ara");

        ImageIcon searchIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\buyutec.png"); 
        Image searchImage = searchIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        searchButton.setIcon(new ImageIcon(searchImage));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.decode("#FFE6F0"));

        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100)); 

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));

        topRow.add(logoButton);
        topRow.add(Box.createHorizontalStrut(10)); 
        topRow.add(searchField);
        topRow.add(Box.createHorizontalStrut(0)); 
        topRow.add(searchButton);
        topRow.add(Box.createHorizontalStrut(5));
        topRow.add(profilButton);

        JPanel bottomRow = new JPanel();
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
        bottomRow.setBackground(Color.decode("#FFE6F0"));

        CustomButton suggestedButton = new CustomButton("",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        suggestedButton.setPreferredSize(new Dimension(40, 40));
        suggestedButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        suggestedButton.setToolTipText("Sırala");

        ImageIcon sortIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\sirala.png"); 
        Image sortImage = sortIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        suggestedButton.setIcon(new ImageIcon(sortImage));

        CustomButton filterToggleButton = new CustomButton("",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        filterToggleButton.setPreferredSize(new Dimension(40, 40));
        filterToggleButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        filterToggleButton.setToolTipText("Filtrele");

        ImageIcon filterIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\filtrele.png"); 
        Image filterImage = filterIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        filterToggleButton.setIcon(new ImageIcon(filterImage));

        filterToggleButton.addActionListener(e -> {
            filterPanel.setVisible(!filterPanel.isVisible());
        });

        CustomButton highlightButton = new CustomButton("",
                new Color(219, 120, 158),
                new Color(255, 138, 185)); 
        highlightButton.setPreferredSize(new Dimension(40, 40));
        highlightButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        highlightButton.setToolTipText("Yapılabilen Yemekler");

        ImageIcon highlightIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\yapilabilenler.png"); 
        Image highlightImage = highlightIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        highlightButton.setIcon(new ImageIcon(highlightImage));

        highlightButton.addActionListener(e -> {
            highlightByCost = !highlightByCost;
            updateRecipeDisplay(displayedRecipes);
        });

        CustomButton addRecipeButton = new CustomButton("",
                new Color(219, 120, 158), 
                new Color(255, 138, 185));
        addRecipeButton.setPreferredSize(new Dimension(40, 40));
        addRecipeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        addRecipeButton.setToolTipText("Tarif Ekle");

        ImageIcon addIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\ekle.png"); 
        Image addImage = addIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        addRecipeButton.setIcon(new ImageIcon(addImage));

        addRecipeButton.addActionListener(e -> {
            new TarifEklemeEkrani(this);
        });

        bottomRow.add(suggestedButton);
        bottomRow.add(Box.createHorizontalStrut(10));
        bottomRow.add(filterToggleButton);
        bottomRow.add(Box.createHorizontalStrut(10));
        bottomRow.add(highlightButton);
        bottomRow.add(Box.createHorizontalStrut(10));
        bottomRow.add(addRecipeButton);

        topPanel.add(topRow);
        topPanel.add(Box.createVerticalStrut(10)); 
        topPanel.add(bottomRow);

        add(topPanel, BorderLayout.NORTH);

       
        sortCriteriaLabel = new JLabel(" "); // Başlangıçta boş
        sortCriteriaLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        sortCriteriaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainContainer.add(sortCriteriaLabel, BorderLayout.NORTH);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 4, 20, 20)); 
        mainPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(Box.createHorizontalStrut(100), BorderLayout.WEST); 
        centerPanel.add(mainPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createHorizontalStrut(100), BorderLayout.EAST); 
        centerPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtreler"));
        filterPanel.setPreferredSize(new Dimension(200, getHeight()));
        filterPanel.setVisible(false); 
        filterPanel.setOpaque(false);

        mainContainer.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(filterPanel, BorderLayout.EAST);
        mainContainer.setOpaque(false);

        add(mainContainer, BorderLayout.CENTER);

        JLabel costFilterLabel = new JLabel("Maliyet Aralığı");
        minCostField = new JTextField(5);
        maxCostField = new JTextField(5);

        JPanel costFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        costFilterPanel.add(new JLabel("Min:"));
        costFilterPanel.add(minCostField);
        costFilterPanel.add(new JLabel("Max:"));
        costFilterPanel.add(maxCostField);

        List<String> categories = getCategories();

        JLabel categoryFilterLabel = new JLabel("Tarif Türü");
        JPanel categoryFilterPanel = new JPanel();
        categoryFilterPanel.setLayout(new BoxLayout(categoryFilterPanel, BoxLayout.Y_AXIS));

        categoryCheckBoxes = new ArrayList<>();
        for (String category : categories) {
            JCheckBox checkBox = new JCheckBox(category);
            categoryCheckBoxes.add(checkBox);
            categoryFilterPanel.add(checkBox);
        }

        JScrollPane categoryScrollPane = new JScrollPane(categoryFilterPanel);
        categoryScrollPane.setPreferredSize(new Dimension(180, 150));

        List<String> ingredients = getIngredients();

        JLabel ingredientFilterLabel = new JLabel("Malzemeler");
        JPanel ingredientFilterPanel = new JPanel();
        ingredientFilterPanel.setLayout(new BoxLayout(ingredientFilterPanel, BoxLayout.Y_AXIS));

        ingredientCheckBoxes = new ArrayList<>();
        for (String ingredient : ingredients) {
            JCheckBox checkBox = new JCheckBox(ingredient);
            ingredientCheckBoxes.add(checkBox);
            ingredientFilterPanel.add(checkBox);
        }

        JScrollPane ingredientScrollPane = new JScrollPane(ingredientFilterPanel);
        ingredientScrollPane.setPreferredSize(new Dimension(180, 150));

        JButton applyFilterButton = new JButton("Filtrele");

        JLabel ingredientCountLabel = new JLabel("Malzeme Sayısı");
        JTextField minIngredientCountField = new JTextField(5);
        JTextField maxIngredientCountField = new JTextField(5);

        JPanel ingredientCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ingredientCountPanel.add(new JLabel("Min:"));
        ingredientCountPanel.add(minIngredientCountField);
        ingredientCountPanel.add(new JLabel("Max:"));
        ingredientCountPanel.add(maxIngredientCountField);

        filterPanel.add(ingredientCountLabel);
        filterPanel.add(ingredientCountPanel);
        filterPanel.add(Box.createVerticalStrut(10)); 

        filterPanel.add(costFilterLabel);
        filterPanel.add(costFilterPanel);
        filterPanel.add(Box.createVerticalStrut(10)); 

        filterPanel.add(categoryFilterLabel);
        filterPanel.add(categoryScrollPane);
        filterPanel.add(Box.createVerticalStrut(10)); 

        filterPanel.add(ingredientFilterLabel);
        filterPanel.add(ingredientScrollPane);
        filterPanel.add(Box.createVerticalStrut(10)); 

        filterPanel.add(applyFilterButton);

        Font menuFont = new Font("Arial", Font.PLAIN, 14);

        JMenuItem sortCostAscItem = new JMenuItem("Maliyete Göre Artan");
        sortCostAscItem.setFont(menuFont);

        JMenuItem sortCostDescItem = new JMenuItem("Maliyete Göre Azalan");
        sortCostDescItem.setFont(menuFont);

        JMenuItem sortPrepTimeAscItem = new JMenuItem("Hazırlama Süresine Göre Artan");
        sortPrepTimeAscItem.setFont(menuFont);

        JMenuItem sortPrepTimeDescItem = new JMenuItem("Hazırlama Süresine Göre Azalan");
        sortPrepTimeDescItem.setFont(menuFont);

        sortCostAscItem.addActionListener(e -> {
            List<Recipe> sortedRecipes = displayedRecipes.stream()
                    .sorted(Comparator.comparingDouble(Recipe::getCost))
                    .collect(Collectors.toList());
            updateRecipeDisplay(sortedRecipes);
            sortCriteriaLabel.setText("Sıralama: Maliyete Göre Artan");
        });

        sortCostDescItem.addActionListener(e -> {
            List<Recipe> sortedRecipes = displayedRecipes.stream()
                    .sorted(Comparator.comparingDouble(Recipe::getCost).reversed())
                    .collect(Collectors.toList());
            updateRecipeDisplay(sortedRecipes);
            sortCriteriaLabel.setText("Sıralama: Maliyete Göre Azalan");
        });

        sortPrepTimeAscItem.addActionListener(e -> {
            List<Recipe> sortedRecipes = displayedRecipes.stream()
                    .sorted(Comparator.comparingInt(Recipe::getPrepTime))
                    .collect(Collectors.toList());
            updateRecipeDisplay(sortedRecipes);
            sortCriteriaLabel.setText("Sıralama: Hazırlama Süresine Göre Artan");
        });

        sortPrepTimeDescItem.addActionListener(e -> {
            List<Recipe> sortedRecipes = displayedRecipes.stream()
                    .sorted(Comparator.comparingInt(Recipe::getPrepTime).reversed())
                    .collect(Collectors.toList());
            updateRecipeDisplay(sortedRecipes);
            sortCriteriaLabel.setText("Sıralama: Hazırlama Süresine Göre Azalan");
        });

        JMenuItem resetSortItem = new JMenuItem("Sıralamayı Sıfırla");
        resetSortItem.setFont(menuFont);

        resetSortItem.addActionListener(e -> {
            updateRecipeDisplay(recipes); 
            sortCriteriaLabel.setText(" ");
        });

        JPopupMenu sortMenu = new JPopupMenu();
        sortMenu.add(sortCostAscItem);
        sortMenu.add(sortCostDescItem);
        sortMenu.addSeparator();
        sortMenu.add(sortPrepTimeAscItem);
        sortMenu.add(sortPrepTimeDescItem);
        sortMenu.addSeparator();
        sortMenu.add(resetSortItem);
        suggestedButton.addActionListener(e -> {
            sortMenu.show(suggestedButton, suggestedButton.getWidth() / 2, suggestedButton.getHeight() / 2);
        });

        recipes = loadRecipes();

        updateRecipeDisplay(recipes);

        setVisible(true);

        ActionListener searchAction = (ActionEvent e) -> {
            String searchText = searchField.getText().trim();

            String minCostText = minCostField.getText().trim();
            String maxCostText = maxCostField.getText().trim();
            Double minCost = null;
            Double maxCost = null;

            try {
                if (!minCostText.isEmpty()) {
                    minCost = Double.parseDouble(minCostText);
                }
                if (!maxCostText.isEmpty()) {
                    maxCost = Double.parseDouble(maxCostText);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir maliyet değeri girin.");
                return;
            }

            String minIngredientCountText = minIngredientCountField.getText().trim();
            String maxIngredientCountText = maxIngredientCountField.getText().trim();
            Integer minIngredientCount = null;
            Integer maxIngredientCount = null;

            try {
                if (!minIngredientCountText.isEmpty()) {
                    minIngredientCount = Integer.parseInt(minIngredientCountText);
                }
                if (!maxIngredientCountText.isEmpty()) {
                    maxIngredientCount = Integer.parseInt(maxIngredientCountText);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir malzeme sayısı girin.");
                return;
            }

            // Seçili kategorileri alalım
            List<String> selectedCategories = new ArrayList<>();
            for (JCheckBox checkBox : categoryCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedCategories.add(checkBox.getText());
                }
            }

            // Seçili malzemeleri alalım
            List<String> selectedIngredients = new ArrayList<>();
            for (JCheckBox checkBox : ingredientCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedIngredients.add(checkBox.getText());
                }
            }

            List<Recipe> filteredRecipes;
            if (!searchText.isEmpty()) {
                filteredRecipes = fuzzySearchRecipes(searchText, recipes); // fuzzy search
            } else {
                filteredRecipes = searchRecipes(searchText, minCost, maxCost, selectedCategories, selectedIngredients, minIngredientCount, maxIngredientCount);
            }

            updateRecipeDisplay(filteredRecipes);
            sortCriteriaLabel.setText(" "); // Etiketi sıfırlıyoruz
        };

        searchButton.addActionListener(searchAction);
        applyFilterButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);
    }

    private List<String> getRecipeIngredients(int recipeId) {
        List<String> ingredients = new ArrayList<>();
        String query = "SELECT m.MalzemeAdi FROM TarifMalzemeIliskisi tm "
                + "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID "
                + "WHERE tm.TarifID = ?";

        try (Connection conn = DriverManager.getConnection(url); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ingredients.add(rs.getString("MalzemeAdi"));
            }

        } catch (SQLException e) {
            System.out.println("Malzemeler alınırken bir hata oluştu: " + e.getMessage());
        }
        return ingredients;
    }

    private List<Recipe> fuzzySearchRecipes(String searchText, List<Recipe> allRecipes) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int toleranceThreshold = 2; // Tarif adı için hata toleransı

        List<Recipe> matchedRecipes = new ArrayList<>();
        String[] searchWords = searchText.toLowerCase().split(" "); // Arama metnini kelimelere ayırma

        for (Recipe recipe : allRecipes) {
            String[] recipeNameWords = recipe.getName().toLowerCase().split(" "); // Tarif adını kelimelere ayırma
            List<String> ingredientWords = getRecipeIngredients(recipe.getId()); 
            boolean matchFound = false;

            // Tarif adına göre arama
            for (String searchWord : searchWords) {
                for (String recipeWord : recipeNameWords) {
                    int distance = levenshtein.apply(searchWord, recipeWord);

                    if (distance <= toleranceThreshold) {
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound) {
                    break;
                }
            }

            // Tarif malzemelerine göre arama 
            if (!matchFound && ingredientWords != null) {
                for (String searchWord : searchWords) {
                    for (String ingredient : ingredientWords) {
                        if (ingredient.toLowerCase().contains(searchWord)) {
                            matchFound = true;
                            break;
                        }
                    }
                    if (matchFound) {
                        break;
                    }
                }
            }

            if (matchFound) {
                matchedRecipes.add(recipe);
            }
        }
        return matchedRecipes;
    }

    private List<String> getIngredients() {
        List<String> ingredients = new ArrayList<>();
        String query = "SELECT DISTINCT MalzemeAdi FROM Malzemeler";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String ingredient = rs.getString("MalzemeAdi");
                if (ingredient != null && !ingredient.isEmpty()) {
                    ingredients.add(ingredient);
                }
            }

        } catch (SQLException e) {
            System.out.println("Malzemeler alınırken bir hata oluştu: " + e.getMessage());
        }

        return ingredients;
    }

    private List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT Kategori FROM Tarifler";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String category = rs.getString("Kategori");
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                }
            }

        } catch (SQLException e) {
            System.out.println("Kategoriler alınırken bir hata oluştu: " + e.getMessage());
        }

        return categories;
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

    private List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String query = "SELECT t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar, t.ResimYolu, t.Kalori, t.KisiSayisi, "
                + "tm.MalzemeMiktar, tm.MalzemeBirim AS TarifBirim, m.BirimFiyat, m.ToplamMiktar, m.MalzemeBirim AS DepoBirim "
                + "FROM Tarifler t "
                + "JOIN TarifMalzemeIliskisi tm ON t.TarifID = tm.TarifID "
                + "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            int currentRecipeId = -1;
            Recipe recipe = null;
            double totalCost = 0.0;

            while (rs.next()) {
                int id = rs.getInt("TarifID");

                if (id != currentRecipeId) {
                    if (recipe != null) {
                        recipe.setCost(totalCost);
                        recipes.add(recipe);
                    }
                    String name = rs.getString("TarifAdi");
                    String category = rs.getString("Kategori");
                    int prepTime = rs.getInt("HazirlamaSuresi");
                    String instructions = rs.getString("Talimatlar");
                    String imagePath = rs.getString("ResimYolu");
                    int kalori = rs.getInt("Kalori");
                    String kisiSayisi = rs.getString("KisiSayisi");

                    recipe = new Recipe(id, name, category, prepTime, instructions, imagePath, 0.0, 0.0, kalori, kisiSayisi);
                    currentRecipeId = id;
                    totalCost = 0.0; 
                }

                double tarifMiktar = rs.getDouble("MalzemeMiktar");
                String tarifBirim = rs.getString("TarifBirim");
                double birimFiyat = rs.getDouble("BirimFiyat");
                double depoMiktar = rs.getDouble("ToplamMiktar");
                String depoBirim = rs.getString("DepoBirim");

                double cost;
                if (tarifBirim.equals("adet")) {
                    double eksikMiktar = Math.max(0, tarifMiktar - depoMiktar);
                    cost = eksikMiktar * birimFiyat;
                } else {
                    double donusturulmusMiktar = birimDonustur(tarifMiktar, tarifBirim, depoBirim);
                    double eksikMiktar = Math.max(0, donusturulmusMiktar - depoMiktar);
                    cost = eksikMiktar * birimFiyat;
                }

                totalCost += cost;
            }

            if (recipe != null) {
                recipe.setCost(totalCost);
                recipes.add(recipe);
            }

            System.out.println("Veri seti veritabanından yüklendi.");

        } catch (SQLException e) {
            System.out.println("Veritabanına erişirken bir hata oluştu: " + e.getMessage());
        }

        return recipes;
    }

    private void loadStarsRecipes() {
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            String sql = "SELECT TarifID FROM Yildizlananlarim";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("TarifID");
                yildizlananIdleri.add(id);
            }
        } catch (SQLException e) {
            System.out.println("Yıldızlı yemekler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    private void addFavoriteToDatabase(Recipe recipe) {
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            String sql = "INSERT OR IGNORE INTO Yildizlananlarim (KullaniciID, TarifID) VALUES (1, " + recipe.getId() + ")";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Yıldızlı yemek eklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    private void removeFavoriteFromDatabase(Recipe recipe) {
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            String sql = "DELETE FROM Yildizlananlarim WHERE TarifID = " + recipe.getId();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Favori yemek silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    private List<Recipe> searchRecipes(String searchText, Double minCost, Double maxCost, List<String> categories, List<String> selectedIngredients, Integer minIngredientCount, Integer maxIngredientCount) {
        List<Recipe> filteredRecipes = new ArrayList<>();

        boolean isIngredientSearch = selectedIngredients != null && !selectedIngredients.isEmpty(); 
        boolean isOnlyIngredientCountSearch = selectedIngredients == null || selectedIngredients.isEmpty(); 

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar, t.ResimYolu, t.Kalori, t.KisiSayisi, ")
                .append("tm.MalzemeMiktar, tm.MalzemeBirim AS TarifBirim, m.BirimFiyat, m.ToplamMiktar, m.MalzemeBirim AS DepoBirim, ")
                .append("COUNT(DISTINCT tm.MalzemeID) AS TotalIngredients, ");

        if (isIngredientSearch) {
            queryBuilder.append("SUM(CASE WHEN m.MalzemeAdi IN (");
            for (int i = 0; i < selectedIngredients.size(); i++) {
                queryBuilder.append("?");
                if (i < selectedIngredients.size() - 1) {
                    queryBuilder.append(", ");
                }
            }
            queryBuilder.append(") THEN 1 ELSE 0 END) AS MatchingIngredients ");
        } else {
            queryBuilder.append("0 AS MatchingIngredients ");
        }

        queryBuilder.append("FROM Tarifler t ")
                .append("JOIN TarifMalzemeIliskisi tm ON t.TarifID = tm.TarifID ")
                .append("JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID ");

        boolean hasWhereClause = false;

        if (searchText != null && !searchText.isEmpty()) {
            queryBuilder.append("WHERE (t.TarifAdi LIKE ? COLLATE NOCASE OR m.MalzemeAdi LIKE ? COLLATE NOCASE) ");
            hasWhereClause = true;
        }

        if (categories != null && !categories.isEmpty()) {
            if (hasWhereClause) {
                queryBuilder.append("AND ");
            } else {
                queryBuilder.append("WHERE ");
                hasWhereClause = true;
            }
            queryBuilder.append("t.Kategori IN (");
            for (int i = 0; i < categories.size(); i++) {
                queryBuilder.append("?");
                if (i < categories.size() - 1) {
                    queryBuilder.append(", ");
                }
            }
            queryBuilder.append(") ");
        }

        queryBuilder.append("GROUP BY t.TarifID, t.TarifAdi, t.Kategori, t.HazirlamaSuresi, t.Talimatlar, t.ResimYolu, t.Kalori, t.KisiSayisi ");

        queryBuilder.append("HAVING 1 = 1 ");

        if (minIngredientCount != null) {
            queryBuilder.append("AND COUNT(DISTINCT tm.MalzemeID) >= ? ");
        }
        if (maxIngredientCount != null) {
            queryBuilder.append("AND COUNT(DISTINCT tm.MalzemeID) <= ? ");
        }

        if (isIngredientSearch && !isOnlyIngredientCountSearch) {
            queryBuilder.append("AND MatchingIngredients >= ? ");  // Seçilen malzemelerin sayısına göre filtre
        }

        queryBuilder.append(" ORDER BY MatchingIngredients DESC");

        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;

            // Arama metni
            if (searchText != null && !searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }

            // Kategori parametreleri
            if (categories != null && !categories.isEmpty()) {
                for (String category : categories) {
                    pstmt.setString(paramIndex++, category);
                }
            }

            // Seçilen malzemeler parametreleri
            if (isIngredientSearch) {
                for (String ingredient : selectedIngredients) {
                    pstmt.setString(paramIndex++, ingredient);
                }
                if (!isOnlyIngredientCountSearch) {
                    pstmt.setInt(paramIndex++, selectedIngredients.size());
                }
            }

            // Maliyet ve malzeme sayısı parametreleri
            if (minIngredientCount != null) {
                pstmt.setInt(paramIndex++, minIngredientCount);
            }
            if (maxIngredientCount != null) {
                pstmt.setInt(paramIndex++, maxIngredientCount);
            }

            ResultSet rs = pstmt.executeQuery();

            // Tarif başına maliyet hesaplaması
            Map<Integer, Recipe> recipeMap = new HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("TarifID");
                String name = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTime = rs.getInt("HazirlamaSuresi");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("ResimYolu");
                int kalori = rs.getInt("Kalori");
                String kisiSayisi = rs.getString("KisiSayisi");

                int totalIngredients = rs.getInt("TotalIngredients");
                int matchingIngredients = rs.getInt("MatchingIngredients");
                double matchPercentage = 0.0;
                if (isIngredientSearch && totalIngredients > 0) {
                    matchPercentage = (double) matchingIngredients / totalIngredients * 100.0;
                }

                Recipe recipe = new Recipe(id, name, category, prepTime, instructions, imagePath, 0.0, matchPercentage, kalori, kisiSayisi);

                recipe.setCost(calculateRecipeCost(conn, id));

                recipeMap.put(id, recipe);
            }

            for (Recipe recipe : recipeMap.values()) {
                if ((minCost == null || recipe.getCost() >= minCost) && (maxCost == null || recipe.getCost() <= maxCost)) {
                    filteredRecipes.add(recipe);
                }
            }

        } catch (SQLException e) {
            System.out.println("Arama sırasında bir hata oluştu: " + e.getMessage());
        }

        if (isIngredientSearch) {
            filteredRecipes = filteredRecipes.stream()
                    .sorted(Comparator.comparingDouble(Recipe::getMatchPercentage).reversed())
                    .collect(Collectors.toList());
        }

        return filteredRecipes;
    }

    private void updateRecipeDisplay(List<Recipe> recipesToDisplay) {
        displayedRecipes = recipesToDisplay;
        mainPanel.removeAll();

        boolean malzemeSecildiMi = ingredientCheckBoxes.stream().anyMatch(JCheckBox::isSelected);

        for (Recipe recipe : recipesToDisplay) {
            JPanel recipePanel;
            if (malzemeSecildiMi) {
                recipePanel = createRecipePanel(recipe, recipe.getMatchPercentage());
            } else {
                recipePanel = createRecipePanel(recipe, -1); 
            }
            mainPanel.add(recipePanel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JPanel createRecipePanel(Recipe recipe, double matchPercentage) {
        JPanel recipePanel = new JPanel();
        recipePanel.setLayout(new BorderLayout());
        recipePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        recipePanel.setPreferredSize(new Dimension(180, 300));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);

        Font titleFont = new Font("Arial Unicode MS", Font.BOLD, 16);

        String originalTitle = recipe.getName(); 
        JLabel titleLabel = new JLabel(originalTitle);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10)); 
        headerPanel.add(Box.createHorizontalStrut(10)); 
        headerPanel.add(titleLabel);

        headerPanel.add(Box.createHorizontalGlue()); 
        headerPanel.add(Box.createHorizontalStrut(10)); 

        double cost = recipe.getCost();

        JButton starButton = new JButton(yildizlananIdleri.contains(recipe.getId()) ? "\u2605" : "\u2606");
        starButton.setFont(titleFont); 
        starButton.setFocusPainted(false);
        starButton.setBorder(BorderFactory.createEmptyBorder());
        starButton.setContentAreaFilled(false);
        headerPanel.add(starButton);
        headerPanel.add(Box.createHorizontalStrut(10));

        starButton.addActionListener(e -> {
            if (!yildizlananIdleri.contains(recipe.getId())) {
                yildizlananIdleri.add(recipe.getId());
                starButton.setText("\u2605");
                addFavoriteToDatabase(recipe);
            } else {
                yildizlananIdleri.remove((Integer) recipe.getId());
                starButton.setText("\u2606");
                removeFavoriteFromDatabase(recipe);
            }
        });

        recipePanel.add(headerPanel, BorderLayout.NORTH);

        JLabel imageLabel = new JLabel();
        if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(250, 167, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            ImageIcon defaultIcon = new ImageIcon("path/to/default-image.jpg");
            Image scaledImage = defaultIcon.getImage().getScaledInstance(250, 167, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        recipePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(matchPercentage > 0.0 ? 3 : 2, 1)); 
        infoPanel.setOpaque(false); 

        // Maliyet etiketi
        JLabel costLabel = new JLabel("Maliyet: " + String.format("%.2f", cost) + " TL");
        costLabel.setHorizontalAlignment(JLabel.CENTER);
        costLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Hazırlama süresi etiketi
        JLabel prepTimeLabel = new JLabel(recipe.getPrepTime() + " dk hazırlık");
        prepTimeLabel.setHorizontalAlignment(JLabel.CENTER);
        prepTimeLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Kalori etiketi, Kişi sayısı etiketi
        JLabel kaloriKisiLabel = new JLabel(recipe.getKalori() + " kcal, " + recipe.getKisiSayisi() + " kişilik");
        kaloriKisiLabel.setHorizontalAlignment(JLabel.CENTER);
        kaloriKisiLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        kaloriKisiLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); 
        infoPanel.add(kaloriKisiLabel);
        kaloriKisiLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        prepTimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); 

        infoPanel.add(prepTimeLabel);
        prepTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        costLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); 

        infoPanel.add(costLabel);
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 

        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 

        if (matchPercentage > 0.0) {
            JLabel matchLabel = new JLabel("Malzeme Uyumu: " + String.format("%.2f", matchPercentage) + "%");
            matchLabel.setHorizontalAlignment(JLabel.CENTER);
            matchLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            infoPanel.add(matchLabel);
        }

        recipePanel.add(infoPanel, BorderLayout.SOUTH);

        if (highlightByCost) {
            if (cost > 0.0) {
                Color redColor = Color.RED;
                titleLabel.setForeground(redColor);
                prepTimeLabel.setForeground(redColor);
                kaloriKisiLabel.setForeground(redColor);
                costLabel.setForeground(redColor);

                recipePanel.setBorder(BorderFactory.createLineBorder(redColor, 2));

                if (!titleLabel.getText().endsWith(" \u2762")) {
                    titleLabel.setText(titleLabel.getText() + " \u2762");
                }
            } else {
                Color greenColor = new Color(0, 128, 0);
                titleLabel.setForeground(greenColor);
                prepTimeLabel.setForeground(greenColor);
                kaloriKisiLabel.setForeground(greenColor);
                costLabel.setForeground(greenColor);

                recipePanel.setBorder(BorderFactory.createLineBorder(greenColor, 2));

                if (titleLabel.getText().endsWith(" \u2762")) {
                    titleLabel.setText(originalTitle);
                }
            }
        } else {
            titleLabel.setForeground(Color.BLACK);
            prepTimeLabel.setForeground(Color.BLACK);
            kaloriKisiLabel.setForeground(Color.BLACK);
            costLabel.setForeground(Color.BLACK);

            recipePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));

            if (titleLabel.getText().endsWith(" \u2762")) {
                titleLabel.setText(originalTitle);
            }
        }

        recipePanel.setBackground(new Color(245, 245, 245));

        recipePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                new Tarif(recipe, AnaEkran.this);
                setVisible(false);
            }

        });

        return recipePanel;
    }

    public void refreshData() {
        recipes = loadRecipes();
        displayedRecipes = new ArrayList<>(recipes);
        updateRecipeDisplay(displayedRecipes);
    }

    public void performSearch(String searchText) {

        List<Recipe> filteredRecipes = fuzzySearchRecipes(searchText, recipes);
        updateRecipeDisplay(filteredRecipes);
        sortCriteriaLabel.setText(" "); 
    }

    public static void main(String[] args) {
        new SliderScreen();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC sürücüsü yüklenemedi: " + e.getMessage());
            return;
        }

        new AnaEkran();
    }
}

class Recipe {

    int id;
    String name;
    String category;
    int prepTime;
    String instructions;
    String imagePath;
    double cost;
    double matchPercentage;
    int kalori;
    String kisiSayisi;

    public Recipe(int id, String name, String category, int prepTime, String instructions, String imagePath, double cost, double matchPercentage, int kalori, String kisiSayisi) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.prepTime = prepTime;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.cost = cost;
        this.matchPercentage = matchPercentage;
        this.kalori = kalori;
        this.kisiSayisi = kisiSayisi;
    }

    // Getter ve setter metodları
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public int getKalori() {
        return kalori;
    }

    public String getKisiSayisi() {
        return kisiSayisi;
    }
}

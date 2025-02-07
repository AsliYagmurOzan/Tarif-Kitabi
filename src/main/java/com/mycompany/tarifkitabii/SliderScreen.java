package com.mycompany.tarifkitabii;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SliderScreen extends JFrame {

    private List<Recipe> sliderRecipes;
    private List<Recipe> rightPanelRecipes;
    private int currentRecipeIndex = 0;
    private Timer sliderTimer;
    private JLabel imageLabel;
    private JButton prevArrowButton;
    private JButton nextArrowButton;
    private String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";
    private JTextField searchField;
    private List<Integer> yildizlananIdleri = new ArrayList<>();
    private static SliderScreen instance;
    private JLabel bottomTitleLabel;

    public SliderScreen() {
        setTitle("Tarifler Slayt Giriş Ekranı");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel mainPanel = new BackgroundPanel("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\sakura.png");
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);
        mainPanel.setOpaque(false);

        createTopPanel();
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        loadStarsRecipes();

        sliderRecipes = loadRecipes(5);
        if (sliderRecipes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hiç tarif bulunamadı.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        rightPanelRecipes = loadRecipes(10);

        createTopPanel();

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false); 

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        sliderPanel.add(layeredPane, BorderLayout.CENTER);

        // Resim etiketini layered pane'e ekliyoruz
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRecipeDetails(sliderRecipes.get(currentRecipeIndex));
            }
        });
        imageLabel.setBounds(100, 0, 800, 533);
        layeredPane.add(imageLabel, Integer.valueOf(0));

        bottomTitleLabel = new JLabel("", JLabel.CENTER);
        bottomTitleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        bottomTitleLabel.setOpaque(true);
        bottomTitleLabel.setBackground(new Color(0, 0, 0, 120));
        bottomTitleLabel.setForeground(Color.WHITE);
        bottomTitleLabel.setBounds(137, 565, 800, 70);
        layeredPane.add(bottomTitleLabel, Integer.valueOf(1));

        sliderPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                imageLabel.setBounds(0, 0, sliderPanel.getWidth(), sliderPanel.getHeight());
            }
        });

        sliderPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int panelWidth = sliderPanel.getWidth();
                int panelHeight = sliderPanel.getHeight();

                int availableWidth = panelWidth - 160;
                int imageWidth = Math.min(availableWidth, 800);
                int imageHeight = (int) (imageWidth * 0.666);

                imageLabel.setBounds((panelWidth - imageWidth - 160) / 2, (panelHeight - imageHeight) / 2, imageWidth, imageHeight);
            }
        });

        prevArrowButton = new JButton();
        prevArrowButton.setPreferredSize(new Dimension(80, 80));
        prevArrowButton.setContentAreaFilled(false);
        prevArrowButton.setBorderPainted(false);
        prevArrowButton.setFocusPainted(false);
        prevArrowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon prevIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\prevarrow.png");
        Image prevImage = prevIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        prevArrowButton.setIcon(new ImageIcon(prevImage));

        prevArrowButton.addActionListener(e -> showPreviousRecipe());
        sliderPanel.add(prevArrowButton, BorderLayout.WEST);

        nextArrowButton = new JButton();
        nextArrowButton.setPreferredSize(new Dimension(80, 80));
        nextArrowButton.setContentAreaFilled(false);
        nextArrowButton.setBorderPainted(false);
        nextArrowButton.setFocusPainted(false);
        nextArrowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon nextIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\nextarrow.png");
        Image nextImage = nextIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        nextArrowButton.setIcon(new ImageIcon(nextImage));

        nextArrowButton.addActionListener(e -> showNextRecipe());
        sliderPanel.add(nextArrowButton, BorderLayout.EAST);

        displayRecipe(currentRecipeIndex);

        sliderTimer = new Timer(6000, e -> showNextRecipe());
        sliderTimer.start();

        mainContentPanel.add(sliderPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setOpaque(false);

        for (Recipe recipe : rightPanelRecipes) {
            JPanel recipeBox = createRecipeBox(recipe);
            rightPanel.add(recipeBox);
        }

        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setOpaque(false);
        rightScrollPane.setPreferredSize(new Dimension(300, getHeight()));

        rightScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        rightScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainContentPanel.add(rightScrollPane, BorderLayout.EAST);
        mainContentPanel.setOpaque(false);

        add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.out.println("Resim yüklenirken hata: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void createTopPanel() {

        CustomButton logoButton = new CustomButton("Ana Sayfa",
                new Color(219, 120, 158), 
                new Color(255, 138, 185)); 
        logoButton.setPreferredSize(new Dimension(120, 40));
        logoButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); 
        logoButton.setForeground(Color.BLACK);

        ImageIcon logoIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\logo.png"); // Resim yolunu ayarlayın
        Image logoImage = logoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        logoButton.setIcon(new ImageIcon(logoImage));

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

        ImageIcon profileIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\profil.png");
        Image profileImage = profileIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        profilButton.setIcon(new ImageIcon(profileImage));

        profilButton.addActionListener(e -> {
            JPopupMenu profilMenu = new JPopupMenu();
            Font menuFont = new Font("Arial", Font.PLAIN, 14);

            JMenuItem malzemelerItem = new JMenuItem("Malzemelerim");
            malzemelerItem.setFont(menuFont);

            JMenuItem yildizlananlarItem = new JMenuItem("Yıldızlananlar");
            yildizlananlarItem.setFont(menuFont);

            profilMenu.add(malzemelerItem);
            profilMenu.addSeparator(); // Çizgi ekledik
            profilMenu.add(yildizlananlarItem);

            malzemelerItem.addActionListener(event -> {
                new MalzemelerEkrani(null);
                setVisible(false);
            });

            yildizlananlarItem.addActionListener(event -> {
                new Yildizlananlar(yildizlananIdleri, null);
                setVisible(false);
            });

            profilMenu.show(profilButton, profilButton.getWidth() / 2, profilButton.getHeight() / 2);
        });

        searchField = new JTextField();
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

        CustomButton allRecipesButton = new CustomButton("",
                new Color(219, 120, 158),
                new Color(255, 138, 185));
        allRecipesButton.setPreferredSize(new Dimension(40, 40));
        allRecipesButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        allRecipesButton.setToolTipText("Tüm Tarifler");

        ImageIcon allRecipesIcon = new ImageIcon("C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\resimler\\tumtarifler.png"); // Resim yolunu ayarlayın
        Image allRecipesImage = allRecipesIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        allRecipesButton.setIcon(new ImageIcon(allRecipesImage));

        allRecipesButton.addActionListener(e -> {
            new AnaEkran();
            setVisible(false);
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
            new TarifEklemeEkrani(null);
            setVisible(false);
        });

        ActionListener searchAction = e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                AnaEkran anaEkran = new AnaEkran();
                anaEkran.performSearch(searchText);
                setVisible(false);
            }
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));

        topPanel.add(logoButton);
        topPanel.add(Box.createHorizontalStrut(10));

        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(10));

        topPanel.add(searchButton);
        topPanel.add(Box.createHorizontalStrut(10));

        topPanel.add(allRecipesButton);
        topPanel.add(Box.createHorizontalStrut(10));

        topPanel.add(Box.createHorizontalGlue());

        topPanel.add(addRecipeButton);
        topPanel.add(Box.createHorizontalStrut(10)); 
        
        topPanel.setBackground(Color.decode("#FFE6F0"));
        topPanel.add(profilButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private JPanel createRecipeBox(Recipe recipe) {
        JPanel recipeBox = new JPanel(new BorderLayout());
        recipeBox.setPreferredSize(new Dimension(300, 100)); 
        recipeBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        recipeBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        JPanel imagePanel = new JPanel();
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(120, 80)); 

        JLabel imageLabel = new JLabel();
        if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            ImageIcon defaultIcon = new ImageIcon("path/to/default-image.jpg");
            Image scaledImage = defaultIcon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }
        imagePanel.add(imageLabel);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 

        JLabel nameLabel = new JLabel(recipe.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        textPanel.add(nameLabel);

        JLabel kaloriLabel = new JLabel(recipe.getKalori() + " kcal");
        kaloriLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        kaloriLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(kaloriLabel);

        JLabel kisiSayisiLabel = new JLabel(recipe.getKisiSayisi() + " kişilik");
        kisiSayisiLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        kisiSayisiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(kisiSayisiLabel);

        contentPanel.add(imagePanel, BorderLayout.WEST);
        contentPanel.add(textPanel, BorderLayout.CENTER);

        recipeBox.add(contentPanel, BorderLayout.CENTER);

        recipeBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRecipeDetails(recipe);
            }
        });

        return recipeBox;
    }

    private void displayRecipe(int index) {
        Recipe recipe = sliderRecipes.get(index);
        bottomTitleLabel.setText(recipe.getName());

        if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(recipe.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(800, 533, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setIcon(new ImageIcon("path/to/default-image.jpg"));
        }
    }

    private void openRecipeDetails(Recipe recipe) {
        new Tarif(recipe, this);
        setVisible(false);
    }

    private void showNextRecipe() {
        currentRecipeIndex = (currentRecipeIndex + 1) % sliderRecipes.size();
        displayRecipe(currentRecipeIndex);
    }

    private void showPreviousRecipe() {
        currentRecipeIndex = (currentRecipeIndex - 1 + sliderRecipes.size()) % sliderRecipes.size();
        displayRecipe(currentRecipeIndex);
    }

    private List<Recipe> loadRecipes(int limit) {
        List<Recipe> recipes = new ArrayList<>();
        String query = "SELECT TarifID, TarifAdi, Kategori, HazirlamaSuresi, Talimatlar, ResimYolu, Kalori, KisiSayisi FROM Tarifler ORDER BY RANDOM() LIMIT ?";

        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("TarifID");
                String name = rs.getString("TarifAdi");
                String category = rs.getString("Kategori");
                int prepTime = rs.getInt("HazirlamaSuresi");
                String instructions = rs.getString("Talimatlar");
                String imagePath = rs.getString("ResimYolu");
                int kalori = rs.getInt("Kalori");
                String kisiSayisi = rs.getString("KisiSayisi");

                recipes.add(new Recipe(id, name, category, prepTime, instructions, imagePath, 0.0, 0.0, kalori, kisiSayisi));
            }

        } catch (SQLException e) {
            System.out.println("Veritabanından tarifler alınırken bir hata oluştu: " + e.getMessage());
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

    public static SliderScreen getInstance() {
        if (instance == null) {
            instance = new SliderScreen();
        }
        return instance;
    }

    public void refreshData() {
        this.getContentPane().removeAll();

        String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {

            String sql = "SELECT * FROM Tarifler";
            ResultSet rs = stmt.executeQuery(sql);

            JPanel slidePanel = new JPanel();
            slidePanel.setLayout(new BoxLayout(slidePanel, BoxLayout.Y_AXIS));

            while (rs.next()) {
                String tarifAdi = rs.getString("TarifAdi");
                String kategori = rs.getString("Kategori");
                String talimatlar = rs.getString("Talimatlar");
                String resimYolu = rs.getString("ResimYolu");

                JLabel recipeLabel = new JLabel("<html><h2>" + tarifAdi + "</h2><p>" + kategori + "</p><p>" + talimatlar + "</p></html>");

                if (resimYolu != null && !resimYolu.isEmpty()) {
                    ImageIcon imageIcon = new ImageIcon(resimYolu);
                    Image scaledImage = imageIcon.getImage().getScaledInstance(420, 280, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                    slidePanel.add(imageLabel);
                }

                slidePanel.add(recipeLabel);
                slidePanel.add(Box.createVerticalStrut(15)); 
            }

            this.add(slidePanel, BorderLayout.CENTER);
            this.revalidate();
            this.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Slayt verileri güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC sürücüsü yüklenemedi: " + e.getMessage());
            return;
        }

        new SliderScreen();
    }
}

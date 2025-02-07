import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class TarifOlusturma {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:C:\\Users\\asliy\\Documents\\NetBeansProjects\\TarifKitabii\\tarifler.db";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {

            if (conn != null) {
                System.out.println("Veritabanı bağlantısı başarılı.");

                String sqlTarifler = "CREATE TABLE IF NOT EXISTS Tarifler ("
                        + "TarifID INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "TarifAdi VARCHAR(100) NOT NULL UNIQUE,"
                        + "Kategori VARCHAR(50),"
                        + "HazirlamaSuresi INTEGER,"
                        + "Talimatlar TEXT,"
                        + "ResimYolu VARCHAR(255),"
                        + "Kalori INTEGER,"
                        + "KisiSayisi VARCHAR(20)" 
                        + ");";
                stmt.execute(sqlTarifler);

                String sqlMalzemeler = "CREATE TABLE IF NOT EXISTS Malzemeler ("
                        + "MalzemeID INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "MalzemeAdi VARCHAR(100) NOT NULL UNIQUE,"
                        + "ToplamMiktar VARCHAR(20)," 
                        + "MalzemeBirim VARCHAR(20),"
                        + "BirimFiyat FLOAT"
                        + ");";
                stmt.execute(sqlMalzemeler);

                String sqlTarifMalzeme = "CREATE TABLE IF NOT EXISTS TarifMalzemeIliskisi ("
                        + "TarifID INTEGER,"
                        + "MalzemeID INTEGER,"
                        + "MalzemeMiktar FLOAT,"
                        + "MalzemeBirim VARCHAR(20)," 
                        + "PRIMARY KEY (TarifID, MalzemeID),"
                        + "FOREIGN KEY (TarifID) REFERENCES Tarifler(TarifID) ON DELETE CASCADE,"
                        + "FOREIGN KEY (MalzemeID) REFERENCES Malzemeler(MalzemeID) ON DELETE CASCADE"
                        + ");";
                stmt.execute(sqlTarifMalzeme);

                String sqlYildizlananlar = "CREATE TABLE IF NOT EXISTS Yildizlananlarim ("
                        + "KullaniciID INTEGER,"
                        + "TarifID INTEGER,"
                        + "PRIMARY KEY (KullaniciID, TarifID),"
                        + "FOREIGN KEY (TarifID) REFERENCES Tarifler(TarifID) ON DELETE CASCADE"
                        + ");";
                stmt.execute(sqlYildizlananlar);

                System.out.println("Veri seti başarıyla oluşturuldu.");
            }
        } catch (SQLException e) {
            System.out.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}

package fakemoney;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Kalici ayarlar: config/fakemoney.properties (UTF-8, Turkce karakterler guvenle saklanir).
 */
public final class FakeMoneyConfig {
   public static final String DEFAULT_FORMAT = "{player} oyuncusundan {amount} aldınız!";
   public static final String DEFAULT_PAY_FORMAT = "{player} oyuncusuna {amount} gönderdiniz!";
   public static final String DEFAULT_PLAYER = "Hexrona";
   public static final String DEFAULT_AMOUNT = "70000000$";

   /** Mod acik/kapali. Kapaliyken ne mesaj dusurulur ne de /pay yakalanir. */
   public static boolean enabled = true;
   /** Mesajdaki {player} degeri. */
   public static String player = DEFAULT_PLAYER;
   /** Mesajdaki {amount} degeri. */
   public static String amount = DEFAULT_AMOUNT;
   /** "Aldiniz" mesaj sablonu. */
   public static String format = DEFAULT_FORMAT;
   /** /pay yakalaninca gosterilen "gonderdiniz" sablonu. */
   public static String payFormat = DEFAULT_PAY_FORMAT;

   private FakeMoneyConfig() {
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("fakemoney.properties");
   }

   public static void load() {
      Path f = file();
      if (!Files.exists(f)) {
         save();
         return;
      }

      Properties p = new Properties();
      try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
         p.load(r);
      } catch (IOException e) {
         FakeMoneyMod.LOGGER.warn("fakemoney.properties okunamadi, varsayilanlar kullaniliyor", e);
         return;
      }

      enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
      player = p.getProperty("player", DEFAULT_PLAYER);
      amount = p.getProperty("amount", DEFAULT_AMOUNT);
      format = p.getProperty("format", DEFAULT_FORMAT);
      payFormat = p.getProperty("payFormat", DEFAULT_PAY_FORMAT);
   }

   public static void save() {
      Properties p = new Properties();
      p.setProperty("enabled", Boolean.toString(enabled));
      p.setProperty("player", nz(player));
      p.setProperty("amount", nz(amount));
      p.setProperty("format", nz(format));
      p.setProperty("payFormat", nz(payFormat));
      try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
         p.store(w, "FakeMoney ayarlari - {player} ve {amount} yer tutucudur");
      } catch (IOException e) {
         FakeMoneyMod.LOGGER.warn("fakemoney.properties yazilamadi", e);
      }
   }

   private static String nz(String s) {
      return s == null ? "" : s;
   }
}

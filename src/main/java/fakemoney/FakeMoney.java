package fakemoney;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * FakeMoney mantigi.
 *
 * 1) send(): sohbete "X oyuncusundan N$ aldiniz!" mesaji dusurur.
 * 2) interceptPay(): "/pay <oyuncu> <miktar>" yazilirsa komut sunucuya GITMEZ; yerine sahte bir
 *    "gonderdiniz" mesaji gorunur (para dusmez).
 *
 * Mesajlar YALNIZCA senin sohbet kutunda gorunur; sunucuya hicbir sey gonderilmez.
 * Renk, sunucunun gercek mesajindaki gibi acik yesildir (§a).
 */
public final class FakeMoney {
   private FakeMoney() {
   }

   /** Sablonu ayarlardaki oyuncu/miktar ile doldurur (onizleme ve gonderim icin ortak). */
   public static String render(String format, String player, String amount) {
      return blankOr(format, FakeMoneyConfig.DEFAULT_FORMAT)
         .replace("{player}", blankOr(player, FakeMoneyConfig.DEFAULT_PLAYER))
         .replace("{amount}", blankOr(amount, FakeMoneyConfig.DEFAULT_AMOUNT));
   }

   public static void send() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (!FakeMoneyConfig.enabled || mc == null || mc.player == null || mc.world == null) {
         return;
      }

      String msg = render(FakeMoneyConfig.format, FakeMoneyConfig.player, FakeMoneyConfig.amount);
      // Yalnizca yerel sohbet kutusuna eklenir; sunucuya paket gitmez.
      mc.inGameHud.getChatHud().addMessage(Text.literal(msg).formatted(Formatting.GREEN));
   }

   /**
    * @param command basinda "/" olmayan komut metni (ornek: "pay Steve 1000")
    * @return true ise komut yutuldu (sunucuya gonderilmemeli)
    */
   public static boolean interceptPay(String command) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (!FakeMoneyConfig.enabled || mc == null || mc.player == null || command == null) {
         return false;
      }

      String[] p = command.trim().split("\\s+");
      if (p.length < 3 || !p[0].equalsIgnoreCase("pay")) {
         return false;
      }

      String msg = blankOr(FakeMoneyConfig.payFormat, FakeMoneyConfig.DEFAULT_PAY_FORMAT)
         .replace("{player}", p[1])
         .replace("{amount}", p[2]);
      mc.inGameHud.getChatHud().addMessage(Text.literal(msg).formatted(Formatting.GREEN));
      return true;
   }

   private static String blankOr(String v, String fallback) {
      return v == null || v.isBlank() ? fallback : v;
   }
}

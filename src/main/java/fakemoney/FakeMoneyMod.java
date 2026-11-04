package fakemoney;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FakeMoney - Minecraft 1.21.11 (Fabric, client-side).
 *
 *  - Sag Shift (degistirilebilir): ayar menusunu acar.
 *  - "Sahte para mesajı gönder" tusu (varsayilan atanmamis): mesaji direkt sohbete dusurur.
 *  - /pay <oyuncu> <miktar>: sunucuya gitmez, yerelde sahte "gonderdiniz" mesaji gosterir.
 */
public final class FakeMoneyMod implements ClientModInitializer {
   public static final String MOD_ID = "fakemoney";
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

   private static KeyBinding openMenuKey;
   private static KeyBinding sendKey;

   @Override
   public void onInitializeClient() {
      FakeMoneyConfig.load();

      KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
      openMenuKey = KeyBindingHelper.registerKeyBinding(
         new KeyBinding("key.fakemoney.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
      sendKey = KeyBindingHelper.registerKeyBinding(
         new KeyBinding("key.fakemoney.send", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));

      ClientTickEvents.END_CLIENT_TICK.register(FakeMoneyMod::onTick);

      // /pay komutunu sunucuya gondermeden yakala (false dondurmek gonderimi iptal eder).
      ClientSendMessageEvents.ALLOW_COMMAND.register(command -> !FakeMoney.interceptPay(command));
   }

   private static void onTick(MinecraftClient mc) {
      while (openMenuKey.wasPressed()) {
         if (mc.currentScreen == null) {
            mc.setScreen(new FakeMoneyScreen(null));
         }
      }

      while (sendKey.wasPressed()) {
         if (mc.currentScreen == null) {
            FakeMoney.send();
         }
      }
   }
}

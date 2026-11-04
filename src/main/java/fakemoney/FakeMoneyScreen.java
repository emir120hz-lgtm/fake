package fakemoney;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * FakeMoney menusu (Sag Shift): oyuncu adi, miktar, "aldiniz" sablonu, "/pay" sablonu,
 * ac/kapat dugmesi ve sohbete test gonderimi. Her sey config/fakemoney.properties dosyasina kaydedilir.
 */
public final class FakeMoneyScreen extends Screen {
   private static final int ROW = 32;

   private final Screen parent;
   private TextFieldWidget player;
   private TextFieldWidget amount;
   private TextFieldWidget format;
   private TextFieldWidget payFormat;
   private ButtonWidget toggle;
   private int fieldX;
   private int fieldY0;
   private int fieldW;

   public FakeMoneyScreen(Screen parent) {
      super(Text.literal("FakeMoney"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      // Pencere yeniden boyutlanınca init() tekrar çalışır; yazılmış ama kaydedilmemiş değerler kaybolmasın.
      if (player != null) {
         apply();
      }

      fieldW = Math.min(340, this.width - 40);
      fieldX = this.width / 2 - fieldW / 2;
      fieldY0 = Math.max(34, this.height / 2 - 112);

      player = field(0, FakeMoneyConfig.player, FakeMoneyConfig.DEFAULT_PLAYER);
      amount = field(1, FakeMoneyConfig.amount, FakeMoneyConfig.DEFAULT_AMOUNT);
      format = field(2, FakeMoneyConfig.format, FakeMoneyConfig.DEFAULT_FORMAT);
      payFormat = field(3, FakeMoneyConfig.payFormat, FakeMoneyConfig.DEFAULT_PAY_FORMAT);

      int by = fieldY0 + ROW * 4 + 30;
      int bw = (fieldW - 8) / 2;
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Sohbete gönder (test)"), b -> {
         apply();
         FakeMoney.send();
      }).dimensions(fieldX, by, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Kaydet ve kapat"), b -> close())
         .dimensions(fieldX + bw + 8, by, bw, 20).build());

      toggle = this.addDrawableChild(ButtonWidget.builder(toggleLabel(), b -> {
         FakeMoneyConfig.enabled = !FakeMoneyConfig.enabled;
         b.setMessage(toggleLabel());
         FakeMoneyConfig.save();
      }).dimensions(fieldX, by + 26, fieldW, 20).build());
   }

   private Text toggleLabel() {
      return Text.literal("FakeMoney: " + (FakeMoneyConfig.enabled ? "AÇIK" : "KAPALI"));
   }

   private TextFieldWidget field(int row, String value, String hint) {
      TextFieldWidget f = new TextFieldWidget(this.textRenderer, fieldX, fieldY0 + row * ROW + 12, fieldW, 20, Text.literal(hint));
      f.setMaxLength(200);
      f.setPlaceholder(Text.literal(hint));
      f.setText(value == null ? "" : value);
      this.addDrawableChild(f);
      return f;
   }

   private void apply() {
      FakeMoneyConfig.player = player.getText();
      FakeMoneyConfig.amount = amount.getText();
      FakeMoneyConfig.format = format.getText();
      FakeMoneyConfig.payFormat = payFormat.getText();
      FakeMoneyConfig.save();
   }

   @Override
   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      super.render(ctx, mouseX, mouseY, delta);
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("FakeMoney ayarları (mesaj sadece sende görünür)"), this.width / 2, fieldY0 - 22, 0xFFFFFFFF);
      String[] labels = {
         "Oyuncu adı ({player})",
         "Miktar ({amount})",
         "Mesaj şablonu (aldınız)",
         "/pay şablonu (gönderdiniz)"
      };
      for (int i = 0; i < labels.length; i++) {
         ctx.drawTextWithShadow(this.textRenderer, labels[i], fieldX, fieldY0 + i * ROW + 1, 0xFFAAAAAA);
      }

      // Canli onizleme: sohbette gorunecek yesil mesaj
      String preview = FakeMoney.render(format.getText(), player.getText(), amount.getText());
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Önizleme: " + preview), this.width / 2, fieldY0 + ROW * 4 + 8, 0xFF55FF55);

      ctx.drawCenteredTextWithShadow(this.textRenderer,
         Text.literal("/pay <oyuncu> <miktar> yazınca sunucuya gitmez, sahte mesaj çıkar. Menü tuşu: Sağ Shift"),
         this.width / 2, fieldY0 + ROW * 4 + 80, 0xFF888888);
   }

   @Override
   public void removed() {
      apply();
   }

   @Override
   public void close() {
      this.client.setScreen(parent);
   }
}

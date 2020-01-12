package carpet_client.gui.entries;

import carpet.settings.ParsedRule;
import carpet_client.gui.ConfigListWidget;
import carpet_client.gui.ServerRulesScreen;
import carpet_client.utils.CarpetSettingsServerNetworkHandler;
import carpet_client.utils.ITooltipEntry;
import carpet_client.utils.RenderHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class StringListEntry extends ConfigListWidget.Entry implements ITooltipEntry
{
    private final ParsedRule<?> settings;
    private final String rule;
    private final ButtonWidget infoButton;
    private final TextFieldWidget textField;
    private final ButtonWidget resetButton;
    private final MinecraftClient client;
    private final ServerRulesScreen gui;
    private boolean invalid;
    
    public StringListEntry(final ParsedRule<?> settings, MinecraftClient client, ServerRulesScreen gui)
    {
        this.settings = settings;
        this.client = client;
        this.gui = gui;
        this.rule = settings.name;
        this.infoButton = new ButtonWidget(0, 0, 14, 20, "i", (button -> {
            button.active = false;
        }));
        TextFieldWidget stringField = new TextFieldWidget(client.textRenderer, 0, 0, 96, 14, "Type a string value");
        stringField.setText(settings.getAsString());
        stringField.setChangedListener(s -> {
            this.checkForInvalid(stringField);
        });
        this.textField = stringField;
        this.resetButton = new ButtonWidget(0, 0, 50, 20, I18n.translate("controls.reset"), (buttonWidget) -> {
            CarpetSettingsServerNetworkHandler.ruleChange(settings.name, settings.defaultAsString, client);
            stringField.setText(settings.defaultAsString);
        });
        gui.getStringFieldList().add(this.textField);
    }
    
    @Override
    public boolean charTyped(char chr, int keyCode)
    {
        return this.textField.charTyped(chr, keyCode);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // ENTER KEY -> 257
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            this.textField.setText(this.textField.getText());
            this.textField.changeFocus(false);
            if (!this.invalid)
                CarpetSettingsServerNetworkHandler.ruleChange(settings.name, this.textField.getText(), client);
        }
        return super.keyPressed(keyCode, scanCode, modifiers) || this.textField.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void render(int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovering, float delta)
    {
        TextRenderer font = client.textRenderer;
        float fontX = (float)(x + 90 - ConfigListWidget.length);
        float fontY = (float)(y + height / 2 - 9 / 2);
        font.draw(this.rule, fontX, fontY, 16777215);
        
        this.resetButton.x = x + 290;
        this.resetButton.y = y;
        this.resetButton.active = !this.settings.getAsString().equals(this.settings.defaultAsString);
        
        this.textField.x = x + 182;
        this.textField.y = y + 3;
        if (this.textField.getText().isEmpty())
        {
            RenderSystem.enableRescaleNormal();
            client.getItemRenderer().renderGuiItemIcon(new ItemStack(Items.BARRIER), this.textField.x + this.textField.getWidth() - 18, this.textField.y- 1);
            RenderSystem.disableRescaleNormal();
        }
        
        this.infoButton.x = x + 156;
        this.infoButton.y = y;
        
        this.infoButton.render(mouseX, mouseY, delta);
        this.textField.render(mouseX, mouseY, delta);
        this.resetButton.render(mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends Element> children()
    {
        return ImmutableList.of(this.infoButton ,this.textField, this.resetButton);
    }
    
    @Override
    public void drawTooltip(int slotIndex, int x, int y, int mouseX, int mouseY, int listWidth, int listHeight, int slotWidth, int slotHeight, float partialTicks)
    {
        if (this.infoButton.isHovered() && !this.infoButton.active)
        {
            String description = this.settings.description;
            RenderHelper.drawGuiInfoBox(client.textRenderer, description, mouseY + 5, listWidth, slotWidth, listHeight, 48);
        }
    }
    
    private void setInvalid(boolean invalid)
    {
        this.invalid = invalid;
        this.gui.setInvalid(invalid);
    }
    
    private void checkForInvalid(TextFieldWidget widget)
    {
        boolean empty = widget.getText().isEmpty();
        if (empty)
        {
            this.gui.setEmpty(true);
            this.setInvalid(true);
        }
        else
        {
            this.setInvalid(false);
        }
    }
}

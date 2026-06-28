package ink.mingyuan.tweelix.mixin.gui;

import ink.mingyuan.tweelix.util.ISignScreenBridge;
import ink.mingyuan.tweelix.util.SignPasteBridge;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin implements ISignScreenBridge {

    @Shadow @Final protected SignBlockEntity sign;
    @Shadow @Final private String[] messages;
    @Shadow @Final private boolean isFrontText;
    @Shadow private SignText text;
    @Shadow private int line;

    // 🆕 获取 signField 以便更新光标
    @Shadow private TextFieldHelper signField;

    // 暂存屏幕用于粘贴（保持原有逻辑）
    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressedHead(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        SignPasteBridge.setScreen((AbstractSignEditScreen) (Object) this);
    }

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void onKeyPressedTail(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        SignPasteBridge.clear();
    }

    @Override
    public int tweelix$getCurrentLine() {
        return this.line;
    }

    @Override
    public String[] tweelix$getMessages() {
        return this.messages;
    }

    @Override
    public boolean tweelix$isBackSide() {
        return !this.isFrontText;
    }

    @Override
    public int tweelix$getMaxLineWidth() {
        // 直接使用 sign 提供的方法
        return this.sign.getMaxTextLineWidth();
    }

    @Override
    public void tweelix$bulkUpdateLines(List<String> lines, int targetCursorLine) {
        // 1. 更新所有行
        for (int i = 0; i < 4; i++) {
            String lineContent = (i < lines.size()) ? lines.get(i) : "";
            this.messages[i] = lineContent;
            this.text = this.text.setMessage(i, Component.literal(lineContent));
        }
        this.sign.setText(this.text, this.isFrontText);

        // 2. 设置光标行
        this.line = Math.min(Math.max(targetCursorLine, 0), 3);

        // 3. 🎯 将光标移到当前行末尾（不重建 GUI）
        if (this.signField != null) {
            String currentLineText = this.messages[this.line];
            int endPos = currentLineText.length();
            this.signField.setCursorPos(endPos);
            this.signField.setSelectionPos(endPos);
        }
    }
}
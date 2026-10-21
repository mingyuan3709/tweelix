package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.util.ISignScreenBridge;
import ink.mingyuan.tweelix.util.SignPasteBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(TextFieldHelper.class)
public class TextFieldHelperMixin {
    @Shadow @Final private Supplier<String> getMessageFn;
    @Shadow @Final private Supplier<String> getClipboardFn;
    @Shadow private int cursorPos;
    @Shadow private int selectionPos;

    @Inject(method = "paste", at = @At("HEAD"), cancellable = true)
    private void onPasteHead(CallbackInfo ci) {
        AbstractSignEditScreen screen = SignPasteBridge.getScreen();
        if (screen == null) return;

        String clipboard = this.getClipboardFn.get();
        if (clipboard == null || clipboard.isEmpty()) return;

        ISignScreenBridge bridge = (ISignScreenBridge) screen;
        boolean isBack = bridge.tweelix$isBackSide();

        if (isBack) {
            if (Generic.SIGN_BACK_PASTE_UNLIMITED.getBooleanValue()) {
                String singleLine = clipboard.replaceAll("\\r?\\n", " ");
                int currentLine = bridge.tweelix$getCurrentLine();
                String[] original = bridge.tweelix$getMessages();
                List<String> newLines = new ArrayList<>(4);
                for (int i = 0; i < 4; i++) {
                    newLines.add(i < original.length ? original[i] : "");
                }
                newLines.set(currentLine, singleLine);
                bridge.tweelix$bulkUpdateLines(newLines, currentLine);
                ci.cancel();
                return;
            }
        }

        if (!isBack && !Generic.SIGN_AUTO_WRAP.getBooleanValue()) {
            return;
        }

        int maxWidth = bridge.tweelix$getMaxLineWidth();
        String currentLineText = this.getMessageFn.get();
        if (currentLineText == null) currentLineText = "";

        int startPos = Math.min(this.cursorPos, this.selectionPos);
        int endPos = Math.max(this.cursorPos, this.selectionPos);
        startPos = Math.max(0, Math.min(startPos, currentLineText.length()));
        endPos = Math.max(0, Math.min(endPos, currentLineText.length()));

        String prefixText = currentLineText.substring(0, startPos);
        String suffixText = currentLineText.substring(endPos);
        String fullCombinedText = prefixText + clipboard + suffixText;

        int currentLineIdx = bridge.tweelix$getCurrentLine();
        List<String> processedLines = new ArrayList<>();

        int finalCursorLine = tweelix$smartSplitTextRowAware(
                bridge, fullCombinedText, currentLineIdx, maxWidth, processedLines
        );

        bridge.tweelix$bulkUpdateLines(processedLines, finalCursorLine);
        ci.cancel();
    }

    @Unique
    private static int tweelix$smartSplitTextRowAware(ISignScreenBridge bridge, String combinedText,
                                                      int targetLine, int maxPixelWidth, List<String> outLines) {
        net.minecraft.client.gui.Font fontRenderer = Minecraft.getInstance().font;
        String[] originalMessages = bridge.tweelix$getMessages();

        // 拷贝当前行之前的行
        for (int i = 0; i < targetLine; i++) {
            if (originalMessages != null && i < originalMessages.length) {
                outLines.add(originalMessages[i]);
            } else {
                outLines.add("");
            }
        }

        int pasteStartRow = outLines.size();

        // 按换行符拆分，并对每行进行宽度切片
        String[] rawLines = combinedText.split("\\r?\\n", -1);
        for (String rawLine : rawLines) {
            if (fontRenderer.width(rawLine) <= maxPixelWidth) {
                outLines.add(rawLine);
            } else {
                StringBuilder lineBuilder = new StringBuilder();
                int currentWidth = 0;
                for (int i = 0; i < rawLine.length(); i++) {
                    char c = rawLine.charAt(i);
                    int charWidth = fontRenderer.width(String.valueOf(c));

                    if (currentWidth + charWidth > maxPixelWidth) {
                        outLines.add(lineBuilder.toString());
                        lineBuilder.setLength(0);
                        currentWidth = 0;
                    }
                    lineBuilder.append(c);
                    currentWidth += charWidth;
                }
                if (!lineBuilder.isEmpty()) {
                    outLines.add(lineBuilder.toString());
                }
            }
            if (outLines.size() >= 4) break;
        }

        int addedRowsCount = outLines.size() - pasteStartRow;
        int calculatedCursorLine = targetLine + Math.max(0, addedRowsCount - 1);
        calculatedCursorLine = Math.min(calculatedCursorLine, 3);

        if (originalMessages != null) {
            while (outLines.size() < 4 && outLines.size() < originalMessages.length) {
                outLines.add(originalMessages[outLines.size()]);
            }
        }

        while (outLines.size() > 4) {
            outLines.removeLast();
        }

        return calculatedCursorLine;
    }
}
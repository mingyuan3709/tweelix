package ink.mingyuan.tweelix.util;

import java.util.List;

public interface ISignScreenBridge {
    /**
     * 跨类批量更新告示牌 1~4 行文本的桥接方法
     * @param lines 切片融合后的全新文本行列表
     */
    /** 批量将切片文本灌入告示牌，并指定刷新后的光标目标行 */
    void tweelix$bulkUpdateLines(List<String> lines, int targetCursorLine);

    int tweelix$getCurrentLine();

    String[] tweelix$getMessages();

    boolean tweelix$isBackSide();

    int tweelix$getMaxLineWidth();
}

package ink.mingyuan.tweelix.util;

import java.util.List;

public interface ISignScreenBridge {

    void tweelix$bulkUpdateLines(List<String> lines, int targetCursorLine);

    int tweelix$getCurrentLine();

    String[] tweelix$getMessages();

    boolean tweelix$isBackSide();

    int tweelix$getMaxLineWidth();
}

package ink.mingyuan.tweelix.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 告示牌命令压缩器 —— 基于 Deflater + Base64，工业级稳健
 * 格式：!U[标志][Base64数据]!K
 * 标志位：0=未压缩，1=已压缩
 * 短命令（≤30字节）直接原样返回，不压缩，不编码
 */
@SuppressWarnings("SpellCheckingInspection")
public final class CommandCompressor {

    private static final String MAGIC = "!U";
    private static final String END_MAGIC = "!K";
    private static final int SHORT_THRESHOLD = 30;   // 字节长度阈值

    // ---------- 对外 API ----------
    public static String compress(String command) {
        if (command == null || command.isEmpty()) return "";
        if (command.startsWith("/")) command = command.substring(1);

        // 1. 预处理（优化：只替换完整单词，避免误伤）
        String preprocessed = preprocess(command);
        byte[] rawBytes = preprocessed.getBytes(StandardCharsets.UTF_8);

        // 2. 短命令直接返回（无任何包装）
        if (rawBytes.length <= SHORT_THRESHOLD) {
            return rawBytes.length == 0 ? "" : new String(rawBytes, StandardCharsets.UTF_8);
        }

        // 3. 压缩
        byte[] compressed = deflate(rawBytes);
        String b64 = Base64.getEncoder().encodeToString(compressed);

        // 4. 打包：!U1{Base64}!K
        return MAGIC + "1" + b64 + END_MAGIC;
    }

    public static String decompress(String encoded) {
        if (encoded == null || encoded.isEmpty()) return null;

        // 检查是否为压缩格式
        if (encoded.startsWith(MAGIC) && encoded.endsWith(END_MAGIC)) {
            // 解析标志
            if (encoded.length() < 4) return null;
            char flag = encoded.charAt(2);
            if (flag != '1') {
                // 若未来扩展其他标志，可忽略
                return null;
            }
            String b64 = encoded.substring(3, encoded.length() - END_MAGIC.length());
            byte[] compressed;
            try {
                compressed = Base64.getDecoder().decode(b64);
            } catch (IllegalArgumentException e) {
                return null; // Base64 非法
            }
            byte[] decompressed = inflate(compressed);
            if (decompressed == null) return null;
            String preprocessed = new String(decompressed, StandardCharsets.UTF_8);
            return postprocess(preprocessed);
        }

        // 若是未压缩的原始命令（短命令），直接返回
        // 注意：此处假定原始命令不含 "!U" 前缀，否则会误判
        return encoded;
    }

    // ---------- 压缩/解压核心 ----------
    private static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        deflater.setInput(input);
        deflater.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int len = deflater.deflate(buffer);
            baos.write(buffer, 0, len);
        }
        deflater.end();
        return baos.toByteArray();
    }

    private static byte[] inflate(byte[] input) {
        Inflater inflater = new Inflater(true);
        inflater.setInput(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length * 2);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int len = inflater.inflate(buffer);
                if (len == 0) {
                    // 若数据异常，提前终止
                    if (inflater.needsInput()) break;
                }
                baos.write(buffer, 0, len);
            }
        } catch (DataFormatException e) {
            return null;
        } finally {
            inflater.end();
        }
        return baos.toByteArray();
    }

    // ---------- 预处理/后处理（增强版：只替换完整单词）----------
    private static String preprocess(String cmd) {
        // 使用正则替换，仅替换独立单词（前后非字母数字下划线）
        return cmd
                .replaceAll("(?<![a-zA-Z0-9_])minecraft:(?![a-zA-Z0-9_])", "\u0001")
                .replaceAll("(?<![a-zA-Z0-9_])@s(?![a-zA-Z0-9_])", "\u0002")
                .replaceAll("(?<![a-zA-Z0-9_])@p(?![a-zA-Z0-9_])", "\u0003")
                .replaceAll("(?<![a-zA-Z0-9_])@a(?![a-zA-Z0-9_])", "\u0004")
                .replaceAll("(?<![a-zA-Z0-9_])@e(?![a-zA-Z0-9_])", "\u0005")
                .replaceAll("(?<![a-zA-Z0-9_])@r(?![a-zA-Z0-9_])", "\u0006")
                .replaceAll("(?<![a-zA-Z0-9_])give(?![a-zA-Z0-9_])", "\u0007")
                .replaceAll("(?<![a-zA-Z0-9_])tp(?![a-zA-Z0-9_])", "\u0008")
                .replaceAll("(?<![a-zA-Z0-9_])effect(?![a-zA-Z0-9_])", "\t")
                .replaceAll("(?<![a-zA-Z0-9_])setblock(?![a-zA-Z0-9_])", "\n")
                .replaceAll("(?<![a-zA-Z0-9_])summon(?![a-zA-Z0-9_])", "\r")
                .replaceAll("(?<![a-zA-Z0-9_])kill(?![a-zA-Z0-9_])", "\f")
                .replaceAll("(?<![a-zA-Z0-9_])gamemode(?![a-zA-Z0-9_])", "\u000F")
                .replaceAll("(?<![a-zA-Z0-9_])say(?![a-zA-Z0-9_])", "\u0010")
                .replaceAll("(?<![a-zA-Z0-9_])tell(?![a-zA-Z0-9_])", "\u0011")
                .replaceAll("(?<![a-zA-Z0-9_])player(?![a-zA-Z0-9_])", "\u0012")
                .replaceAll("(?<![a-zA-Z0-9_])spawn(?![a-zA-Z0-9_])", "\u0013")
                .replaceAll("(?<![a-zA-Z0-9_])diamond(?![a-zA-Z0-9_])", "\u0014")
                .replaceAll("(?<![a-zA-Z0-9_])netherite(?![a-zA-Z0-9_])", "\u0015")
                .replaceAll("(?<![a-zA-Z0-9_])sword(?![a-zA-Z0-9_])", "\u0016")
                .replaceAll("(?<![a-zA-Z0-9_])pickaxe(?![a-zA-Z0-9_])", "\u0017")
                .replaceAll("(?<![a-zA-Z0-9_])axe(?![a-zA-Z0-9_])", "\u0018")
                .replaceAll("(?<![a-zA-Z0-9_])shovel(?![a-zA-Z0-9_])", "\u0019")
                .replaceAll("(?<![a-zA-Z0-9_])hoe(?![a-zA-Z0-9_])", "\u001A")
                .replaceAll("(?<![a-zA-Z0-9_])true(?![a-zA-Z0-9_])", "\u001B")
                .replaceAll("(?<![a-zA-Z0-9_])false(?![a-zA-Z0-9_])", "\u001C")
                .replaceAll("(?<![a-zA-Z0-9_])keep(?![a-zA-Z0-9_])", "\u001D")
                .replaceAll("(?<![a-zA-Z0-9_])destroy(?![a-zA-Z0-9_])", "\u001E")
                .replaceAll("(?<![a-zA-Z0-9_])replace(?![a-zA-Z0-9_])", "\u001F")
                .replaceAll("(?<![a-zA-Z0-9_])64(?![a-zA-Z0-9_])", "\u0020")
                .replaceAll("(?<![a-zA-Z0-9_])1(?![a-zA-Z0-9_])", "\u0021");
    }

    private static String postprocess(String cmd) {
        return cmd
                .replace("\u0001", "minecraft:")
                .replace("\u0002", "@s")
                .replace("\u0003", "@p")
                .replace("\u0004", "@a")
                .replace("\u0005", "@e")
                .replace("\u0006", "@r")
                .replace("\u0007", "give")
                .replace("\u0008", "tp")
                .replace("\t", "effect")
                .replace("\n", "setblock")
                .replace("\r", "summon")
                .replace("\f", "kill")
                .replace("\u000F", "gamemode")
                .replace("\u0010", "say")
                .replace("\u0011", "tell")
                .replace("\u0012", "player")
                .replace("\u0013", "spawn")
                .replace("\u0014", "diamond")
                .replace("\u0015", "netherite")
                .replace("\u0016", "sword")
                .replace("\u0017", "pickaxe")
                .replace("\u0018", "axe")
                .replace("\u0019", "shovel")
                .replace("\u001A", "hoe")
                .replace("\u001B", "true")
                .replace("\u001C", "false")
                .replace("\u001D", "keep")
                .replace("\u001E", "destroy")
                .replace("\u001F", "replace")
                .replace("\u0020", "64")
                .replace("\u0021", "1");
    }

    private CommandCompressor() {}
}
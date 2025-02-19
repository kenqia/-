import java.util.Arrays;

public class BoyerMoore {
    /**
     * BM 算法实现
     *
     * @param text    主串
     * @param pattern 模式串
     * @return 模式串在主串中的起始位置，未找到返回 -1
     */
    public static int bmSearch(String text, String pattern) {
        if (text == null || pattern == null || pattern.length() == 0) {
            return -1;
        }

        int n = text.length(); // 主串长度
        int m = pattern.length(); // 模式串长度

        // 1. 预处理：生成坏字符表和好后缀表
        int[] badCharTable = buildBadCharTable(pattern); // 坏字符表
        int[] goodSuffixTable = buildGoodSuffixTable(pattern); // 好后缀表

        // 2. 匹配过程
        int i = 0; // 主串的起始匹配位置
        while (i <= n - m) {
            int j = m - 1; // 模式串的匹配位置（从后往前匹配）

            // 从后往前匹配字符
            while (j >= 0 && text.charAt(i + j) == pattern.charAt(j)) {
                j--;
            }

            // 匹配成功
            if (j < 0) {
                return i; // 返回匹配的起始位置
            }

            // 匹配失败，计算移动距离
            int badCharShift = j - badCharTable[text.charAt(i + j)]; // 坏字符规则
            int goodSuffixShift = goodSuffixTable[j]; // 好后缀规则

            // 取最大值作为移动距离
            i += Math.max(badCharShift, goodSuffixShift);
        }

        return -1; // 未找到匹配
    }

    /**
     * 构建坏字符表
     *
     * @param pattern 模式串
     * @return 坏字符表
     */
    private static int[] buildBadCharTable(String pattern) {
        int[] badCharTable = new int[256]; // ASCII 字符集
        Arrays.fill(badCharTable, -1); // 初始化为 -1

        // 记录每个字符在模式串中最后出现的位置
        for (int i = 0; i < pattern.length(); i++) {
            badCharTable[pattern.charAt(i)] = i;
        }

        return badCharTable;
    }

    /**
     * 构建好后缀表
     *
     * @param pattern 模式串
     * @return 好后缀表
     */
    private static int[] buildGoodSuffixTable(String pattern) {
        int m = pattern.length();
        int[] goodSuffixTable = new int[m];
        int[] suffix = new int[m];

        // 初始化 suffix 数组
        Arrays.fill(suffix, -1);
        for (int i = 0; i < m - 1; i++) {
            int j = i;
            int k = 0;
            while (j >= 0 && pattern.charAt(j) == pattern.charAt(m - 1 - k)) {
                suffix[++k] = j--;
            }
        }

        // 填充好后缀表
        for (int i = 0; i < m; i++) {
            goodSuffixTable[i] = m; // 默认移动距离为模式串长度
        }

        // 情况 1：好后缀在模式串中有匹配
        for (int i = m - 1; i >= 0; i--) {
            if (suffix[i] != -1) {
                goodSuffixTable[m - 1 - i] = m - 1 - suffix[i];
            }
        }

        // 情况 2：好后缀的部分后缀在模式串的前缀中有匹配
        for (int i = 0; i < m - 1; i++) {
            goodSuffixTable[m - 1 - suffix[i]] = m - 1 - i;
        }

        return goodSuffixTable;
    }

    public static void main(String[] args) {
        String text = "HERE IS A SIMPLE EXAMPLE";
        String pattern = "EXAMPLE";
        int index = bmSearch(text, pattern);

        if (index != -1) {
            System.out.println("Pattern found at index: " + index);
        } else {
            System.out.println("Pattern not found.");
        }
    }
}
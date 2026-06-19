package com.bonsai.util;

import java.util.HashMap;
import java.util.Map;

public class SpeciesNormalizer {

    private static final Map<String, String> ALIAS_MAP = new HashMap<>();

    static {
        ALIAS_MAP.put("五针松", "五针松");
        ALIAS_MAP.put("日本五针松", "五针松");
        ALIAS_MAP.put("五針松", "五针松");

        ALIAS_MAP.put("黑松", "黑松");
        ALIAS_MAP.put("日本黑松", "黑松");
        ALIAS_MAP.put("黑樹松", "黑松");

        ALIAS_MAP.put("罗汉松", "罗汉松");
        ALIAS_MAP.put("土杉", "罗汉松");
        ALIAS_MAP.put("羅漢松", "罗汉松");

        ALIAS_MAP.put("真柏", "真柏");
        ALIAS_MAP.put("真柏桧", "真柏");
        ALIAS_MAP.put("台湾真柏", "真柏");
        ALIAS_MAP.put("系鱼川真柏", "真柏");

        ALIAS_MAP.put("榕树", "榕树");
        ALIAS_MAP.put("小叶榕", "榕树");
        ALIAS_MAP.put("虎皮榕", "榕树");
        ALIAS_MAP.put("气根榕", "榕树");
        ALIAS_MAP.put("榕樹", "榕树");

        ALIAS_MAP.put("榆树", "榆树");
        ALIAS_MAP.put("榔榆", "榆树");
        ALIAS_MAP.put("榔榆树", "榆树");
        ALIAS_MAP.put("小叶榆", "榆树");
        ALIAS_MAP.put("榆樹", "榆树");

        ALIAS_MAP.put("枫树", "枫树");
        ALIAS_MAP.put("红枫", "枫树");
        ALIAS_MAP.put("鸡爪槭", "枫树");
        ALIAS_MAP.put("三角枫", "枫树");
        ALIAS_MAP.put("楓樹", "枫树");

        ALIAS_MAP.put("对接白蜡", "对接白蜡");
        ALIAS_MAP.put("对节白蜡", "对接白蜡");
        ALIAS_MAP.put("白蜡", "对接白蜡");
        ALIAS_MAP.put("对接白腊", "对接白蜡");
        ALIAS_MAP.put("对节白腊", "对接白蜡");

        ALIAS_MAP.put("黄杨", "黄杨");
        ALIAS_MAP.put("瓜子黄杨", "黄杨");
        ALIAS_MAP.put("雀舌黄杨", "黄杨");
        ALIAS_MAP.put("小叶黄杨", "黄杨");
        ALIAS_MAP.put("黃楊", "黄杨");

        ALIAS_MAP.put("三角梅", "三角梅");
        ALIAS_MAP.put("叶子花", "三角梅");
        ALIAS_MAP.put("九重葛", "三角梅");
        ALIAS_MAP.put("三角梅花", "三角梅");

        ALIAS_MAP.put("雀梅", "雀梅");
        ALIAS_MAP.put("雀梅藤", "雀梅");
        ALIAS_MAP.put("酸梅", "雀梅");

        ALIAS_MAP.put("火棘", "火棘");
        ALIAS_MAP.put("火把果", "火棘");
        ALIAS_MAP.put("救军粮", "火棘");

        ALIAS_MAP.put("松柏", "松柏类");
        ALIAS_MAP.put("松柏类", "松柏类");
        ALIAS_MAP.put("杂木", "杂木类");
        ALIAS_MAP.put("杂木类", "杂木类");
        ALIAS_MAP.put("观花观果", "观花观果类");
        ALIAS_MAP.put("观花观果类", "观花观果类");
    }

    private static final String UNKNOWN_SPECIES = "未分类";

    public static String normalize(String speciesName) {
        if (speciesName == null || speciesName.trim().isEmpty()) {
            return UNKNOWN_SPECIES;
        }

        String normalized = speciesName.trim()
                .replaceAll("\\s+", "")
                .replaceAll("　", "");

        String result = ALIAS_MAP.get(normalized);
        if (result != null) {
            return result;
        }

        for (Map.Entry<String, String> entry : ALIAS_MAP.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return normalized;
    }

    public static boolean isUnknown(String speciesName) {
        return UNKNOWN_SPECIES.equals(normalize(speciesName));
    }

    public static String getUnknownSpecies() {
        return UNKNOWN_SPECIES;
    }
}

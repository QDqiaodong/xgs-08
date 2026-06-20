package com.bonsai.util;

import com.bonsai.entity.Bonsai;
import com.bonsai.entity.Category;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class BonsaiValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    private static final Set<String> OLD_STUMP_KEYWORDS = new HashSet<>(Arrays.asList(
        "老桩", "古桩", "老树", "百年", "千年", "古木", "古树", "陈年"
    ));

    private static final Set<String> YOUNG_TREE_KEYWORDS = new HashSet<>(Arrays.asList(
        "幼树", "小苗", "幼苗", "新栽", "新种", "一年生", "两年生", "三年生",
        "籽播", "扦插苗", "嫁接苗", "压条苗"
    ));

    private static final Map<String, Integer> SPECIES_MIN_AGE_FOR_OLD_STUMP = new HashMap<>();
    static {
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("五针松", 30);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("黑松", 25);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("罗汉松", 20);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("真柏", 20);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("榕树", 15);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("榆树", 15);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("枫树", 12);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("对接白蜡", 15);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("黄杨", 20);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("三角梅", 8);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("雀梅", 10);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("火棘", 8);
        SPECIES_MIN_AGE_FOR_OLD_STUMP.put("榔榆", 12);
    }

    private static final Map<String, Set<String>> STYLE_SPECIES_COMPATIBILITY = new HashMap<>();
    static {
        STYLE_SPECIES_COMPATIBILITY.put("直干式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "榕树", "榆树", "枫树", "对接白蜡", "黄杨", "榔榆"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("斜干式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "榕树", "榆树", "枫树", "对接白蜡", "黄杨", "雀梅", "三角梅", "榔榆"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("曲干式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "榆树", "枫树", "对接白蜡", "黄杨", "雀梅", "榔榆"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("悬崖式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "真柏", "罗汉松", "雀梅", "榆树", "对接白蜡", "火棘"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("临水式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "榕树", "榆树", "雀梅", "对接白蜡"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("文人树", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "真柏", "罗汉松", "枫树", "黄杨", "对接白蜡"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("丛林式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "枫树", "榆树", "榉树", "竹类"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("附石式", new HashSet<>(Arrays.asList(
            "五针松", "黑松", "罗汉松", "真柏", "榕树", "榆树", "对接白蜡", "火棘"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("水旱盆景", new HashSet<>(Arrays.asList(
            "五针松", "罗汉松", "真柏", "榕树", "榆树", "枫树", "竹类"
        )));
        STYLE_SPECIES_COMPATIBILITY.put("微型盆景", new HashSet<>(Arrays.asList(
            "真柏", "黄杨", "火棘", "三角梅", "雀梅", "对接白蜡", "枫树"
        )));
    }

    private static final Map<String, Set<String>> POT_TYPE_SPECIES_COMPATIBILITY = new HashMap<>();
    static {
        Set<String> conifers = new HashSet<>(Arrays.asList("五针松", "黑松", "罗汉松", "真柏"));
        Set<String> hardwoods = new HashSet<>(Arrays.asList("榕树", "榆树", "枫树", "对接白蜡", "黄杨", "雀梅", "榔榆"));
        Set<String> flowering = new HashSet<>(Arrays.asList("三角梅", "火棘"));

        POT_TYPE_SPECIES_COMPATIBILITY.put("紫砂盆", union(conifers, hardwoods));
        POT_TYPE_SPECIES_COMPATIBILITY.put("陶盆", union(conifers, hardwoods, flowering));
        POT_TYPE_SPECIES_COMPATIBILITY.put("釉盆", hardwoods);
        POT_TYPE_SPECIES_COMPATIBILITY.put("瓷盆", flowering);
        POT_TYPE_SPECIES_COMPATIBILITY.put("石盆", new HashSet<>(Arrays.asList("五针松", "黑松", "真柏")));
        POT_TYPE_SPECIES_COMPATIBILITY.put("水泥盆", new HashSet<>(Arrays.asList("榕树", "榆树", "对接白蜡")));
    }

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+)(?:年|岁)");

    private static Set<String> union(Set<String>... sets) {
        Set<String> result = new HashSet<>();
        for (Set<String> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    public ValidationResult validate(Bonsai bonsai) {
        ValidationResult result = new ValidationResult();

        if (bonsai == null) {
            result.addError("盆景数据不能为空");
            return result;
        }

        validateTreeAgeKeywords(bonsai, result);
        validatePotInformation(bonsai, result);
        validateStyleSpeciesCompatibility(bonsai, result);
        validatePotTypeSpeciesCompatibility(bonsai, result);
        validateTreeAgeAcquireDate(bonsai, result);

        return result;
    }

    private void validateTreeAgeKeywords(Bonsai bonsai, ValidationResult result) {
        String description = bonsai.getDescription() != null ? bonsai.getDescription() : "";
        String name = bonsai.getName() != null ? bonsai.getName() : "";
        String combinedText = description + name;

        Integer treeAge = bonsai.getTreeAge();
        String speciesName = bonsai.getSpecies() != null ? bonsai.getSpecies().getName() : null;

        boolean mentionsOldStump = containsAnyKeyword(combinedText, OLD_STUMP_KEYWORDS);
        boolean mentionsYoungTree = containsAnyKeyword(combinedText, YOUNG_TREE_KEYWORDS);

        if (treeAge != null && treeAge > 0) {
            if (mentionsOldStump) {
                int minAge = getMinAgeForOldStump(speciesName);
                if (treeAge < minAge) {
                    result.addWarning(String.format(
                        "描述中提到\"老桩/古木\"等词汇，但树龄仅%d年，%s通常需要至少%d年才称得上老桩",
                        treeAge, speciesName != null ? speciesName : "该树种", minAge
                    ));
                }
            }

            if (mentionsYoungTree && treeAge >= 10) {
                result.addWarning(String.format(
                    "描述中提到\"幼树/小苗\"等词汇，但树龄已达%d年，可能描述不准确",
                    treeAge
                ));
            }

            if (treeAge <= 3 && mentionsOldStump) {
                result.addError(String.format(
                    "树龄仅%d年，不应标注为\"老桩\"，幼树通常不具备老桩特征",
                    treeAge
                ));
            }

            int extractedYears = extractYearsFromText(combinedText);
            if (extractedYears > 0 && Math.abs(extractedYears - treeAge) >= 5) {
                result.addWarning(String.format(
                    "描述中提到的年份（约%d年）与填写的树龄（%d年）差异较大，请确认",
                    extractedYears, treeAge
                ));
            }
        }

        if (mentionsOldStump && mentionsYoungTree) {
            result.addError("描述中同时出现\"老桩\"和\"幼树\"类词汇，存在明显矛盾");
        }
    }

    private void validatePotInformation(Bonsai bonsai, ValidationResult result) {
        String potType = bonsai.getPotType();
        String potSurface = bonsai.getPotSurface();
        String description = bonsai.getDescription() != null ? bonsai.getDescription() : "";

        if ((potType == null || potType.trim().isEmpty()) 
            && (potSurface != null && !potSurface.trim().isEmpty())) {
            result.addWarning("已填写盆面信息，但未填写盆器类型，建议补充盆器信息");
        }

        if (description.contains("盆") || description.contains("钵") || description.contains("缸")) {
            if (potType == null || potType.trim().isEmpty()) {
                result.addWarning("简介中提到了盆器相关内容，但未填写盆器类型字段，建议补充");
            }
        }

        if (potType != null && !potType.trim().isEmpty()) {
            String trimmedPot = potType.trim();
            if (trimmedPot.length() == 1) {
                result.addWarning("盆器类型填写过短（仅1个字），信息可能不完整");
            }
            if (trimmedPot.matches("\\d+")) {
                result.addWarning("盆器类型似乎只包含数字，可能填写有误");
            }
        }
    }

    private void validateStyleSpeciesCompatibility(Bonsai bonsai, ValidationResult result) {
        String styleName = bonsai.getStyle() != null ? bonsai.getStyle().getName() : null;
        String speciesName = bonsai.getSpecies() != null ? bonsai.getSpecies().getName() : null;

        if (styleName != null && speciesName != null) {
            Set<String> compatibleSpecies = STYLE_SPECIES_COMPATIBILITY.get(styleName);
            if (compatibleSpecies != null && !compatibleSpecies.contains(speciesName)) {
                result.addWarning(String.format(
                    "%s造型通常不适用于%s树种，该造型更适合松柏类或特定杂木类",
                    styleName, speciesName
                ));
            }
        }

        if (styleName != null) {
            Integer treeAge = bonsai.getTreeAge();
            if ("悬崖式".equals(styleName) && treeAge != null && treeAge < 5) {
                result.addWarning("悬崖式造型需要树干有一定基础，树龄过小（<5年）难以实现");
            }
            if ("文人树".equals(styleName) && treeAge != null && treeAge < 8) {
                result.addWarning("文人树造型需要较高的树干比例和枝干过渡，建议树龄8年以上再尝试");
            }
            if ("丛林式".equals(styleName) && treeAge != null && treeAge > 20) {
                result.addWarning("丛林式一般由多株幼树组合而成，单株树龄过大不适合丛林式组合");
            }
        }
    }

    private void validatePotTypeSpeciesCompatibility(Bonsai bonsai, ValidationResult result) {
        String potType = bonsai.getPotType();
        String speciesName = bonsai.getSpecies() != null ? bonsai.getSpecies().getName() : null;

        if (potType == null || potType.trim().isEmpty() || speciesName == null) {
            return;
        }

        String normalizedPotType = normalizePotType(potType);
        if (normalizedPotType == null) {
            return;
        }

        Set<String> compatibleSpecies = POT_TYPE_SPECIES_COMPATIBILITY.get(normalizedPotType);
        if (compatibleSpecies != null && !compatibleSpecies.contains(speciesName)) {
            result.addWarning(String.format(
                "%s通常不太适合%s，松柏类更推荐紫砂盆，观花观果类可考虑釉盆或瓷盆",
                normalizedPotType, speciesName
            ));
        }
    }

    private void validateTreeAgeAcquireDate(Bonsai bonsai, ValidationResult result) {
        Integer treeAge = bonsai.getTreeAge();
        java.time.LocalDate acquireDate = bonsai.getAcquireDate();

        if (treeAge != null && acquireDate != null) {
            long yearsOwned = java.time.temporal.ChronoUnit.YEARS.between(acquireDate, java.time.LocalDate.now());
            if (treeAge < yearsOwned) {
                result.addWarning(String.format(
                    "树龄（%d年）小于已养护年数（约%d年），数据可能有误，树龄应包含入手前的年份",
                    treeAge, yearsOwned
                ));
            }
            if (treeAge != null && yearsOwned > treeAge * 0.8 && treeAge > 10) {
                result.addWarning(String.format(
                    "您已养护约%d年，占树龄（%d年）的80%%以上，该盆景可能几乎由您从小培养，请确认树龄是否准确",
                    yearsOwned, treeAge
                ));
            }
        }
    }

    private boolean containsAnyKeyword(String text, Set<String> keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int extractYearsFromText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        java.util.regex.Matcher matcher = YEAR_PATTERN.matcher(text);
        int maxYears = 0;
        while (matcher.find()) {
            try {
                int years = Integer.parseInt(matcher.group(1));
                if (years > maxYears && years < 2000) {
                    maxYears = years;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return maxYears;
    }

    private int getMinAgeForOldStump(String speciesName) {
        if (speciesName == null) {
            return 20;
        }
        return SPECIES_MIN_AGE_FOR_OLD_STUMP.getOrDefault(speciesName, 20);
    }

    private String normalizePotType(String potType) {
        if (potType == null) return null;
        String lower = potType.toLowerCase();
        if (lower.contains("紫砂")) return "紫砂盆";
        if (lower.contains("陶")) return "陶盆";
        if (lower.contains("釉")) return "釉盆";
        if (lower.contains("瓷")) return "瓷盆";
        if (lower.contains("石")) return "石盆";
        if (lower.contains("水泥")) return "水泥盆";
        return null;
    }
}

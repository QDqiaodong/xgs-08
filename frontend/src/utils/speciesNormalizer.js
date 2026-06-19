const ALIAS_MAP = {
  '五针松': '五针松',
  '日本五针松': '五针松',
  '五針松': '五针松',

  '黑松': '黑松',
  '日本黑松': '黑松',
  '黑樹松': '黑松',

  '罗汉松': '罗汉松',
  '土杉': '罗汉松',
  '羅漢松': '罗汉松',

  '真柏': '真柏',
  '真柏桧': '真柏',
  '台湾真柏': '真柏',
  '台湾真柏': '真柏',
  '系鱼川真柏': '真柏',

  '榕树': '榕树',
  '小叶榕': '榕树',
  '虎皮榕': '榕树',
  '气根榕': '榕树',
  '榕樹': '榕树',

  '榆树': '榆树',
  '榔榆': '榆树',
  '榔榆树': '榆树',
  '小叶榆': '榆树',
  '榆樹': '榆树',

  '枫树': '枫树',
  '红枫': '枫树',
  '鸡爪槭': '枫树',
  '三角枫': '枫树',
  '楓樹': '枫树',

  '对接白蜡': '对接白蜡',
  '对节白蜡': '对接白蜡',
  '白蜡': '对接白蜡',
  '对接白腊': '对接白蜡',
  '对节白腊': '对接白蜡',

  '黄杨': '黄杨',
  '瓜子黄杨': '黄杨',
  '雀舌黄杨': '黄杨',
  '小叶黄杨': '黄杨',
  '黃楊': '黄杨',

  '三角梅': '三角梅',
  '叶子花': '三角梅',
  '九重葛': '三角梅',
  '三角梅花': '三角梅',

  '雀梅': '雀梅',
  '雀梅藤': '雀梅',
  '酸梅': '雀梅',

  '火棘': '火棘',
  '火把果': '火棘',
  '救军粮': '火棘',

  '松柏': '松柏类',
  '松柏类': '松柏类',
  '杂木': '杂木类',
  '杂木类': '杂木类',
  '观花观果': '观花观果类',
  '观花观果类': '观花观果类'
}

const UNKNOWN_SPECIES = '未分类'

export function normalizeSpecies(speciesName) {
  if (!speciesName || !speciesName.trim()) {
    return UNKNOWN_SPECIES
  }

  let normalized = speciesName.trim()
    .replace(/\s+/g, '')
    .replace(/　/g, '')

  if (ALIAS_MAP[normalized]) {
    return ALIAS_MAP[normalized]
  }

  for (const alias of Object.keys(ALIAS_MAP)) {
    if (normalized.includes(alias)) {
      return ALIAS_MAP[alias]
    }
  }

  return normalized
}

export function isUnknownSpecies(speciesName) {
  return normalizeSpecies(speciesName) === UNKNOWN_SPECIES
}

export function getUnknownSpecies() {
  return UNKNOWN_SPECIES
}

export function getAllStandardSpecies() {
  const standards = new Set()
  for (const value of Object.values(ALIAS_MAP)) {
    standards.add(value)
  }
  return Array.from(standards)
}

export const OLD_STUMP_KEYWORDS = [
  '老桩', '古桩', '老树', '百年', '千年', '古木', '古树', '陈年'
]

export const YOUNG_TREE_KEYWORDS = [
  '幼树', '小苗', '幼苗', '新栽', '新种', '一年生', '两年生', '三年生',
  '籽播', '扦插苗', '嫁接苗', '压条苗'
]

export const SPECIES_MIN_AGE_FOR_OLD_STUMP = {
  '五针松': 30,
  '黑松': 25,
  '罗汉松': 20,
  '真柏': 20,
  '榕树': 15,
  '榆树': 15,
  '枫树': 12,
  '对接白蜡': 15,
  '黄杨': 20,
  '三角梅': 8,
  '雀梅': 10,
  '火棘': 8,
  '榔榆': 12
}

export const STYLE_SPECIES_COMPATIBILITY = {
  '直干式': ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '枫树', '对接白蜡', '黄杨', '榔榆'],
  '斜干式': ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '枫树', '对接白蜡', '黄杨', '雀梅', '三角梅', '榔榆'],
  '曲干式': ['五针松', '黑松', '罗汉松', '真柏', '榆树', '枫树', '对接白蜡', '黄杨', '雀梅', '榔榆'],
  '悬崖式': ['五针松', '黑松', '真柏', '罗汉松', '雀梅', '榆树', '对接白蜡', '火棘'],
  '临水式': ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '雀梅', '对接白蜡'],
  '文人树': ['五针松', '黑松', '真柏', '罗汉松', '枫树', '黄杨', '对接白蜡'],
  '丛林式': ['五针松', '黑松', '罗汉松', '真柏', '枫树', '榆树', '榉树', '竹类'],
  '附石式': ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '对接白蜡', '火棘'],
  '水旱盆景': ['五针松', '罗汉松', '真柏', '榕树', '榆树', '枫树', '竹类'],
  '微型盆景': ['真柏', '黄杨', '火棘', '三角梅', '雀梅', '对接白蜡', '枫树']
}

export const STYLE_TREE_AGE_REQUIREMENTS = {
  '悬崖式': { minAge: 5, message: '悬崖式造型需要树干有一定基础，树龄过小（<5年）难以实现' },
  '文人树': { minAge: 8, message: '文人树造型需要较高的树干比例和枝干过渡，建议树龄8年以上再尝试' },
  '丛林式': { maxAge: 20, message: '丛林式一般由多株幼树组合而成，单株树龄过大不适合丛林式组合' }
}

export const POT_TYPE_SPECIES_COMPATIBILITY = {
  '紫砂盆': { species: ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '枫树', '对接白蜡', '黄杨', '雀梅', '榔榆'], recommend: '松柏类和杂木类通用' },
  '陶盆': { species: ['五针松', '黑松', '罗汉松', '真柏', '榕树', '榆树', '枫树', '对接白蜡', '黄杨', '雀梅', '三角梅', '火棘', '榔榆'], recommend: '各类树种通用' },
  '釉盆': { species: ['榕树', '榆树', '枫树', '对接白蜡', '黄杨', '雀梅', '榔榆'], recommend: '杂木类和观叶类' },
  '瓷盆': { species: ['三角梅', '火棘'], recommend: '观花观果类' },
  '石盆': { species: ['五针松', '黑松', '真柏'], recommend: '松柏类，营造古朴意境' },
  '水泥盆': { species: ['榕树', '榆树', '对接白蜡'], recommend: '大型杂木类' }
}

export const STAGE_LIST = [
  { value: 'MATERIAL_TREE', label: '素材树', icon: '🌱', color: '#67c23a', description: '刚入手或开始培养的原始素材' },
  { value: 'PRUNING', label: '修剪中', icon: '✂️', color: '#e6a23c', description: '造型修剪、蟠扎等过程中的状态' },
  { value: 'AFTER_REPOT', label: '换盆后', icon: '🪴', color: '#f56c6c', description: '换盆、定植后的状态' },
  { value: 'CURRENT', label: '当前状态', icon: '📸', color: '#409eff', description: '最新养护状态展示' }
]

export const TRAINING_STAGE_LIST = [
  { value: 'MATERIAL_CULTIVATION', label: '素材培养', icon: '🌱', color: '#67c23a', description: '刚入手的素材，正在基础培养阶段' },
  { value: 'INITIAL_SHAPING', label: '初步定型', icon: '✂️', color: '#e6a23c', description: '已完成初步造型修剪，正在定型中' },
  { value: 'FINE_BRANCH_TRAINING', label: '细枝培养', icon: '🌿', color: '#409eff', description: '主干已定，正在培养细枝和枝托细节' },
  { value: 'ORNAMENTAL_PERIOD', label: '观赏期', icon: '🌸', color: '#f56c6c', description: '造型成熟，进入最佳观赏期' }
]

export function getTrainingStageInfo(stageValue) {
  return TRAINING_STAGE_LIST.find(s => s.value === stageValue) || null
}

export function containsAnyKeyword(text, keywords) {
  if (!text) return false
  return keywords.some(keyword => text.includes(keyword))
}

export function extractYearsFromText(text) {
  if (!text) return 0
  const regex = /(\d+)(?:年|岁)/g
  let maxYears = 0
  let match
  while ((match = regex.exec(text)) !== null) {
    const years = parseInt(match[1])
    if (years > maxYears && years < 2000) {
      maxYears = years
    }
  }
  return maxYears
}

export function getMinAgeForOldStump(speciesName) {
  if (!speciesName) return 20
  return SPECIES_MIN_AGE_FOR_OLD_STUMP[speciesName] || 20
}

export function normalizePotType(potType) {
  if (!potType) return null
  if (potType.includes('紫砂')) return '紫砂盆'
  if (potType.includes('陶')) return '陶盆'
  if (potType.includes('釉')) return '釉盆'
  if (potType.includes('瓷')) return '瓷盆'
  if (potType.includes('石')) return '石盆'
  if (potType.includes('水泥')) return '水泥盆'
  return null
}

export function calculateYearsOwned(acquireDate) {
  if (!acquireDate) return 0
  const acquire = new Date(acquireDate)
  const now = new Date()
  let years = now.getFullYear() - acquire.getFullYear()
  const monthDiff = now.getMonth() - acquire.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < acquire.getDate())) {
    years--
  }
  return Math.max(0, years)
}

export function validateBonsai(form, speciesName, styleName) {
  const errors = []
  const warnings = []

  const description = form.description || ''
  const name = form.name || ''
  const combinedText = description + name

  const treeAge = form.treeAge ? parseInt(form.treeAge) : null
  const potType = form.potType
  const potSurface = form.potSurface
  const acquireDate = form.acquireDate

  const mentionsOldStump = containsAnyKeyword(combinedText, OLD_STUMP_KEYWORDS)
  const mentionsYoungTree = containsAnyKeyword(combinedText, YOUNG_TREE_KEYWORDS)

  if (treeAge !== null && !isNaN(treeAge) && treeAge > 0) {
    if (mentionsOldStump) {
      const minAge = getMinAgeForOldStump(speciesName)
      if (treeAge < minAge) {
        warnings.push(`描述中提到"老桩/古木"等词汇，但树龄仅${treeAge}年，${speciesName || '该树种'}通常需要至少${minAge}年才称得上老桩`)
      }
    }

    if (mentionsYoungTree && treeAge >= 10) {
      warnings.push(`描述中提到"幼树/小苗"等词汇，但树龄已达${treeAge}年，可能描述不准确`)
    }

    if (treeAge <= 3 && mentionsOldStump) {
      errors.push(`树龄仅${treeAge}年，不应标注为"老桩"，幼树通常不具备老桩特征`)
    }

    const extractedYears = extractYearsFromText(combinedText)
    if (extractedYears > 0 && Math.abs(extractedYears - treeAge) >= 5) {
      warnings.push(`描述中提到的年份（约${extractedYears}年）与填写的树龄（${treeAge}年）差异较大，请确认`)
    }
  }

  if (mentionsOldStump && mentionsYoungTree) {
    errors.push('描述中同时出现"老桩"和"幼树"类词汇，存在明显矛盾')
  }

  if ((!potType || !potType.trim()) && (potSurface && potSurface.trim())) {
    warnings.push('已填写盆面信息，但未填写盆器类型，建议补充盆器信息')
  }

  if ((description.includes('盆') || description.includes('钵') || description.includes('缸')) 
      && (!potType || !potType.trim())) {
    warnings.push('简介中提到了盆器相关内容，但未填写盆器类型字段，建议补充')
  }

  if (potType && potType.trim()) {
    const trimmedPot = potType.trim()
    if (trimmedPot.length === 1) {
      warnings.push('盆器类型填写过短（仅1个字），信息可能不完整')
    }
    if (/^\d+$/.test(trimmedPot)) {
      warnings.push('盆器类型似乎只包含数字，可能填写有误')
    }
  }

  if (styleName && speciesName) {
    const compatibleSpecies = STYLE_SPECIES_COMPATIBILITY[styleName]
    if (compatibleSpecies && !compatibleSpecies.includes(speciesName)) {
      warnings.push(`${styleName}造型通常不适用于${speciesName}树种，该造型更适合松柏类或特定杂木类`)
    }
  }

  if (styleName && treeAge !== null && !isNaN(treeAge)) {
    const ageReq = STYLE_TREE_AGE_REQUIREMENTS[styleName]
    if (ageReq) {
      if (ageReq.minAge && treeAge < ageReq.minAge) {
        warnings.push(ageReq.message)
      }
      if (ageReq.maxAge && treeAge > ageReq.maxAge) {
        warnings.push(ageReq.message)
      }
    }
  }

  if (potType && speciesName) {
    const normalizedPot = normalizePotType(potType)
    if (normalizedPot) {
      const potInfo = POT_TYPE_SPECIES_COMPATIBILITY[normalizedPot]
      if (potInfo && !potInfo.species.includes(speciesName)) {
        warnings.push(`${normalizedPot}通常不太适合${speciesName}，松柏类更推荐紫砂盆，观花观果类可考虑釉盆或瓷盆`)
      }
    }
  }

  if (treeAge !== null && !isNaN(treeAge) && acquireDate) {
    const yearsOwned = calculateYearsOwned(acquireDate)
    if (treeAge < yearsOwned) {
      warnings.push(`树龄（${treeAge}年）小于已养护年数（约${yearsOwned}年），数据可能有误，树龄应包含入手前的年份`)
    }
    if (yearsOwned > treeAge * 0.8 && treeAge > 10) {
      warnings.push(`您已养护约${yearsOwned}年，占树龄（${treeAge}年）的80%以上，该盆景可能几乎由您从小培养，请确认树龄是否准确`)
    }
  }

  return { errors, warnings, isValid: errors.length === 0 }
}

export function getStageInfo(stageValue) {
  return STAGE_LIST.find(s => s.value === stageValue) || {
    label: stageValue,
    icon: '🖼️',
    color: '#909399',
    description: ''
  }
}

export function getStyleName(style) {
  if (!style) return ''
  if (typeof style === 'string') return style
  if (typeof style === 'object') {
    return style.name || style.label || style.title || style.value || ''
  }
  return ''
}

export function getTreeAgeDisplay(treeAge) {
  if (treeAge === null || treeAge === undefined || treeAge === '') return ''
  const age = parseInt(treeAge)
  if (isNaN(age) || age <= 0) return ''
  return age.toString()
}

export function getPotTypeDisplay(potType) {
  if (!potType) return ''
  if (typeof potType === 'string') {
    return potType.trim()
  }
  if (typeof potType === 'object') {
    return potType.name || potType.label || potType.type || ''
  }
  return ''
}

export function normalizeBonsaiDisplayFields(bonsai) {
  if (!bonsai) return null
  return {
    ...bonsai,
    styleName: getStyleName(bonsai.style),
    treeAgeDisplay: getTreeAgeDisplay(bonsai.treeAge),
    potTypeDisplay: getPotTypeDisplay(bonsai.potType)
  }
}

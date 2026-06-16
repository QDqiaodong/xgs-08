const SOLAR_TERMS = [
  { name: '小寒', month: 1, day: 5, season: 'winter' },
  { name: '大寒', month: 1, day: 20, season: 'winter' },
  { name: '立春', month: 2, day: 4, season: 'spring' },
  { name: '雨水', month: 2, day: 19, season: 'spring' },
  { name: '惊蛰', month: 3, day: 6, season: 'spring' },
  { name: '春分', month: 3, day: 21, season: 'spring' },
  { name: '清明', month: 4, day: 5, season: 'spring' },
  { name: '谷雨', month: 4, day: 20, season: 'spring' },
  { name: '立夏', month: 5, day: 6, season: 'summer' },
  { name: '小满', month: 5, day: 21, season: 'summer' },
  { name: '芒种', month: 6, day: 6, season: 'summer' },
  { name: '夏至', month: 6, day: 21, season: 'summer' },
  { name: '小暑', month: 7, day: 7, season: 'summer' },
  { name: '大暑', month: 7, day: 23, season: 'summer' },
  { name: '立秋', month: 8, day: 8, season: 'autumn' },
  { name: '处暑', month: 8, day: 23, season: 'autumn' },
  { name: '白露', month: 9, day: 8, season: 'autumn' },
  { name: '秋分', month: 9, day: 23, season: 'autumn' },
  { name: '寒露', month: 10, day: 8, season: 'autumn' },
  { name: '霜降', month: 10, day: 24, season: 'autumn' },
  { name: '立冬', month: 11, day: 8, season: 'winter' },
  { name: '小雪', month: 11, day: 22, season: 'winter' },
  { name: '大雪', month: 12, day: 7, season: 'winter' },
  { name: '冬至', month: 12, day: 22, season: 'winter' }
]

const SEASONS = {
  spring: { name: '春季', color: '#4CAF50', bgColor: 'rgba(76, 175, 80, 0.1)', icon: '🌱' },
  summer: { name: '夏季', color: '#FF9800', bgColor: 'rgba(255, 152, 0, 0.1)', icon: '☀️' },
  autumn: { name: '秋季', color: '#FF5722', bgColor: 'rgba(255, 87, 34, 0.1)', icon: '🍂' },
  winter: { name: '冬季', color: '#2196F3', bgColor: 'rgba(33, 150, 243, 0.1)', icon: '❄️' }
}

const SEASON_ORDER = ['spring', 'summer', 'autumn', 'winter']

const TERM_CARE_TIPS = {
  '立春': { water: '气温回升，注意补水，萌动期浇水要充足', fertilize: '施基肥，促进萌芽', prune: '修剪枯枝病枝', repot: '适宜换盆的好时节' },
  '雨水': { water: '降水增多，注意排水防涝', fertilize: '适量追肥', prune: '继续整形修剪', repot: '仍可换盆' },
  '惊蛰': { water: '生长开始，保持盆土湿润', fertilize: '追施氮肥为主', prune: '萌芽前修剪完成', repot: '换盆最佳期' },
  '春分': { water: '生长旺盛，充足供水', fertilize: '正常施肥', prune: '修剪整形', repot: '适宜换盆' },
  '清明': { water: '气温升高，增加浇水', fertilize: '追施磷钾肥', prune: '疏芽疏枝', repot: '可以换盆' },
  '谷雨': { water: '降水多，注意通风排水', fertilize: '增加施肥量', prune: '修剪徒长枝', repot: '换盆末期' },
  '立夏': { water: '夏季开始，充足浇水', fertilize: '正常施肥', prune: '夏季修剪开始', repot: '不建议换盆' },
  '小满': { water: '气温高，早晚浇水', fertilize: '减少氮肥', prune: '摘心打顶', repot: '不建议换盆' },
  '芒种': { water: '高温期，增加浇水次数', fertilize: '薄肥勤施', prune: '疏枝通风', repot: '不建议换盆' },
  '夏至': { water: '需水量大，注意遮阴', fertilize: '减少施肥', prune: '修剪过密枝', repot: '不建议换盆' },
  '小暑': { water: '高温酷暑，早晚浇水', fertilize: '停止施肥', prune: '轻剪为主', repot: '不建议换盆' },
  '大暑': { water: '最热时期，充足供水', fertilize: '不施肥', prune: '疏枝通风', repot: '严禁换盆' },
  '立秋': { water: '高温仍高，保持浇水', fertilize: '开始施磷钾肥', prune: '秋季修剪', repot: '可以换盆' },
  '处暑': { water: '气温下降，减少浇水', fertilize: '增加磷钾肥', prune: '修剪徒长枝', repot: '适宜换盆' },
  '白露': { water: '干燥，见干见湿', fertilize: '正常施肥', prune: '整形修剪', repot: '换盆好时机' },
  '秋分': { water: '秋高气爽，适量浇水', fertilize: '施基肥', prune: '修剪病枝弱枝', repot: '适宜换盆' },
  '寒露': { water: '气温下降，控制浇水', fertilize: '减少施肥', prune: '准备越冬修剪', repot: '可以换盆' },
  '霜降': { water: '减少浇水，保持偏干', fertilize: '停止施肥', prune: '修剪完成', repot: '换盆末期' },
  '立冬': { water: '冬季开始，控水', fertilize: '不施肥', prune: '冬季修剪开始', repot: '不建议换盆' },
  '小雪': { water: '减少浇水，宁干勿湿', fertilize: '不施肥', prune: '整形修剪', repot: '不建议换盆' },
  '大雪': { water: '控水为主', fertilize: '不施肥', prune: '重剪整形', repot: '不建议换盆' },
  '冬至': { water: '极少浇水', fertilize: '不施肥', prune: '主要修剪期', repot: '不建议换盆' },
  '小寒': { water: '控水防寒', fertilize: '不施肥', prune: '继续修剪', repot: '不建议换盆' },
  '大寒': { water: '保持干燥', fertilize: '准备春肥', prune: '修剪完成', repot: '不建议换盆' }
}

function getDayOfYear(month, day) {
  const daysBeforeMonth = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334]
  return daysBeforeMonth[month - 1] + day
}

function getSolarTerm(dateStr) {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const dayOfYear = getDayOfYear(month, day)
  
  const termDays = SOLAR_TERMS.map((term, index) => ({
    ...term,
    dayOfYear: getDayOfYear(term.month, term.day),
    index
  }))
  
  let result = termDays[termDays.length - 1]
  
  for (let i = 0; i < termDays.length; i++) {
    if (dayOfYear < termDays[i].dayOfYear) {
      result = i === 0 ? termDays[termDays.length - 1] : termDays[i - 1]
      break
    }
  }
  
  const nextIndex = (result.index + 1) % SOLAR_TERMS.length
  const nextTerm = SOLAR_TERMS[nextIndex]
  
  return {
    name: result.name,
    month: result.month,
    day: result.day,
    season: result.season,
    nextTerm: nextTerm.name,
    seasonInfo: SEASONS[result.season]
  }
}

function getSeasonName(season) {
  return SEASONS[season]?.name || ''
}

function getSeasonColor(season) {
  return SEASONS[season]?.color || '#323233'
}

function getSeasonBgColor(season) {
  return SEASONS[season]?.bgColor || 'rgba(0, 0, 0, 0.05)'
}

function getSeasonIcon(season) {
  return SEASONS[season]?.icon || '🌿'
}

function getCareTips(termName) {
  return TERM_CARE_TIPS[termName] || { water: '', fertilize: '', prune: '', repot: '' }
}

function groupLogsBySolarTerm(logs) {
  const grouped = {}
  
  SEASON_ORDER.forEach(season => {
    grouped[season] = {
      seasonInfo: SEASONS[season],
      terms: {}
    }
    const seasonTerms = SOLAR_TERMS.filter(t => t.season === season)
    seasonTerms.forEach(term => {
      grouped[season].terms[term.name] = {
        ...term,
        logs: []
      }
    })
  })
  
  logs.forEach(log => {
    const termInfo = getSolarTerm(log.logDate)
    if (grouped[termInfo.season] && grouped[termInfo.season].terms[termInfo.name]) {
      grouped[termInfo.season].terms[termInfo.name].logs.push(log)
    }
  })
  
  SEASON_ORDER.forEach(season => {
    Object.keys(grouped[season].terms).forEach(termName => {
      grouped[season].terms[termName].logs.sort((a, b) => new Date(b.logDate) - new Date(a.logDate))
    })
  })
  
  return grouped
}

export {
  SOLAR_TERMS,
  SEASONS,
  SEASON_ORDER,
  TERM_CARE_TIPS,
  getSolarTerm,
  getSeasonName,
  getSeasonColor,
  getSeasonBgColor,
  getSeasonIcon,
  getCareTips,
  groupLogsBySolarTerm
}

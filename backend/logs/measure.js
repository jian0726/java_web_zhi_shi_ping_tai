/**
 * 精确测量：不掺入人为等待，直接测「点击 -> 目标路由组件挂载完成」的真实耗时，
 * 并抓出所有请求（含 404），同时用 CDP 记录长任务（long task）。
 */
const { chromium } = require('playwright-core')
const fs = require('fs')

const BASE = 'http://localhost:5173'
const EXEC = 'C:/Program Files/Google/Chrome/Application/chrome.exe'
const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const exe = fs.existsSync(EXEC) ? EXEC : EDGE

;(async () => {
  const browser = await chromium.launch({ executablePath: exe, headless: true })
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await ctx.newPage()

  const reqs = []
  page.on('response', (r) => {
    const u = r.url()
    if (u.includes('localhost')) reqs.push({ status: r.status(), url: u.replace(BASE, '') })
  })

  // 长任务观测：>50ms 的同步块
  await page.addInitScript(() => {
    window.__long = []
    try {
      new PerformanceObserver((list) => {
        for (const e of list.getEntries()) {
          window.__long.push({ dur: Math.round(e.duration), start: Math.round(e.startTime) })
        }
      }).observe({ entryTypes: ['longtask'] })
    } catch (_) {}
  })

  await page.goto(BASE + '/', { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('roles', JSON.stringify(['ADMIN', 'AUDITOR']))
  })
  await page.goto(BASE + '/admin/audit-knowledge', { waitUntil: 'networkidle' })
  await page.waitForTimeout(500)
  await page.evaluate(() => { window.__long = [] })
  reqs.length = 0

  /** 点击菜单并等待目标页面标题出现，返回真实耗时 */
  async function switchTo(menuText, expectTitle) {
    const t0 = Date.now()
    await page.click(`text=${menuText}`)
    await page.waitForSelector(`text=${expectTitle}`, { timeout: 8000 }).catch(() => {})
    return Date.now() - t0
  }

  console.log('=== 连续切换 10 轮（无人工等待）===')
  for (let i = 1; i <= 10; i++) {
    const a = await switchTo('岗位管理', '创建岗位')
    const b = await switchTo('审核知识库', '待审核')
    console.log(`round ${i}: 岗位=${a}ms 审核=${b}ms`)
  }

  console.log('\n=== 长任务（>50ms）===')
  const longs = await page.evaluate(() => window.__long)
  if (!longs.length) console.log('(无)')
  else longs.slice(0, 15).forEach((l) => console.log(`  ${l.dur}ms @ ${l.start}ms`))
  console.log('长任务总数 =', longs.length, '最长 =', longs.length ? Math.max(...longs.map(l => l.dur)) : 0, 'ms')

  console.log('\n=== 非 200 请求 ===')
  const bad = reqs.filter((r) => r.status >= 400)
  if (!bad.length) console.log('(无)')
  else [...new Set(bad.map((r) => r.status + ' ' + r.url))].slice(0, 20).forEach((s) => console.log('  ' + s))

  await browser.close()
})().catch((e) => console.log('FATAL', e.message))

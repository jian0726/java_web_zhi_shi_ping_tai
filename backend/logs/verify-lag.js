/**
 * 验证管理端「审核知识库 <-> 岗位管理」来回切换是否卡死。
 * 相比旧脚本，本次增加了：真实视口变化 + 表格数据行 + ResizeObserver 回环计数。
 */
const { chromium } = require('playwright-core')

const BASE = 'http://localhost:5173'
const EXEC = 'C:/Program Files/Google/Chrome/Application/chrome.exe'
const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const fs = require('fs')
const exe = fs.existsSync(EXEC) ? EXEC : (fs.existsSync(EDGE) ? EDGE : null)
if (!exe) { console.log('NO_BROWSER'); process.exit(1) }
console.log('browser =', exe)

;(async () => {
  const browser = await chromium.launch({ executablePath: exe, headless: true })
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await ctx.newPage()

  const errs = []
  page.on('console', (m) => {
    if (m.type() === 'error' || m.type() === 'warning') {
      const t = m.text()
      if (/ResizeObserver/i.test(t)) errs.push('RO_LOOP: ' + t)
      else if (m.type() === 'error') errs.push('ERR: ' + t)
    }
  })
  page.on('pageerror', (e) => errs.push('PAGEERROR: ' + e.message))

  // 统计 ResizeObserver 回环次数
  await page.addInitScript(() => {
    window.__roLoop = 0
    window.addEventListener('error', (e) => {
      if (/ResizeObserver loop/i.test(String(e.message))) window.__roLoop++
    })
  })

  await page.goto(BASE + '/', { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('roles', JSON.stringify(['ADMIN', 'AUDITOR']))
  })

  async function clickMenu(text) {
    const t0 = Date.now()
    await page.click(`text=${text}`)
    // 等主线程真的空转 + 内容出现
    await page.waitForTimeout(220)
    return Date.now() - t0
  }

  // 先到审核知识库
  await page.goto(BASE + '/admin/audit-knowledge', { waitUntil: 'networkidle' })
  await page.waitForTimeout(600)

  const nav = await page.evaluate(() => performance.getEntriesByType('navigation').length)
  console.log('--- 开始 6 轮来回切换 ---')

  for (let i = 1; i <= 6; i++) {
    const a = await clickMenu('岗位管理')
    const b = await clickMenu('审核知识库')
    const alive = await page.evaluate(() => 1 + 1).then(() => true).catch(() => false)
    console.log(`round ${i}: 岗位 ${a}ms / 审核 ${b}ms / 主线程存活=${alive}`)
    if (!alive) { console.log('!!! 主线程死亡'); break }
  }

  // 视口抖动 —— 模拟用户拖动窗口 / 滚动条出现，触发 ResizeObserver
  console.log('--- 视口抖动 20 次 ---')
  const t0 = Date.now()
  for (let i = 0; i < 20; i++) {
    await page.setViewportSize({ width: 1400 + (i % 2) * 40, height: 850 + (i % 3) * 30 })
    await page.waitForTimeout(40)
  }
  console.log('视口抖动总耗时 =', Date.now() - t0, 'ms')

  const alive = await page.evaluate(() => 1 + 1).then(() => true).catch(() => false)
  const roLoop = await page.evaluate(() => window.__roLoop)
  console.log('抖动后主线程存活 =', alive, '| ResizeObserver 回环次数 =', roLoop)

  // 前进/后退
  console.log('--- 前进/后退 4 轮 ---')
  for (let i = 1; i <= 4; i++) {
    const t = Date.now()
    await page.goBack().catch(() => {})
    await page.waitForTimeout(150)
    await page.goForward().catch(() => {})
    await page.waitForTimeout(150)
    const ok = await page.evaluate(() => 1 + 1).then(() => true).catch(() => false)
    console.log(`nav ${i}: ${Date.now() - t}ms 主线程存活=${ok}`)
    if (!ok) { console.log('!!! 后退/前进后主线程死亡'); break }
  }

  console.log('--- 控制台异常 ---')
  console.log(errs.length ? errs.slice(0, 12).join('\n') : '(无)')

  await browser.close()
})().catch((e) => { console.log('FATAL', e.message) })

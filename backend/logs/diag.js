const { chromium } = require('playwright-core');
const fs = require('fs');

const OUT = 'D:/zhuomian/好YOU经验知识共享平台/backend/logs/diag.out';
const lines = [];
const log = (m) => { lines.push(String(m)); console.log(String(m)); };
const save = () => fs.writeFileSync(OUT, lines.join('\n'), 'utf8');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  // 全量抓取控制台 + 页面错误 + 请求
  page.on('console', (m) => log('[console.' + m.type() + '] ' + m.text()));
  page.on('pageerror', (e) => log('[PAGEERROR] ' + (e.stack || e.message)));
  page.on('requestfailed', (r) => log('[REQFAIL] ' + r.url().replace('http://localhost:5173', '') + ' :: ' + (r.failure() || {}).errorText));
  page.on('response', (r) => {
    const u = r.url();
    if (u.includes('/api/')) log('[API] ' + r.status() + ' ' + u.replace('http://localhost:5173', ''));
  });

  // 登录
  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('=== 登录态注入完成 ===');

  // 进入管理端
  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(3000);
  log('=== 岗位管理页 标题=' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE') + ' ===');

  // 模拟用户：来回切换 8 次，每次真实点击
  for (let i = 1; i <= 8; i++) {
    // 点"审核知识库"
    const t1 = Date.now();
    const el1 = page.locator('a:has-text("审核知识库"), .side-item:has-text("审核知识库")').first();
    await el1.click({ timeout: 5000 }).catch((e) => log('点击审核失败: ' + e.message.split('\n')[0]));
    await page.waitForTimeout(1500);
    const tt1 = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');

    // 点"岗位管理"
    const t2 = Date.now();
    const el2 = page.locator('a:has-text("岗位管理"), .side-item:has-text("岗位管理")').first();
    await el2.click({ timeout: 5000 }).catch((e) => log('点击岗位失败: ' + e.message.split('\n')[0]));
    await page.waitForTimeout(1500);
    const tt2 = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');

    log('ROUND' + i + ' 审核=' + tt1 + '(' + (Date.now() - t1 - 1500) + 'ms) 岗位=' + tt2 + '(' + (Date.now() - t2 - 1500) + 'ms) url=' + page.url().replace('http://localhost:5173', ''));
  }

  log('=== 页面内存/性能信息 ===');
  const info = await page.evaluate(() => ({
    heapMB: Math.round((performance.memory ? performance.memory.usedJSHeapSize : 0) / 1048576),
    nodes: document.getElementsByTagName('*').length,
    listeners: 'n/a'
  })).catch(() => ({}));
  log(JSON.stringify(info));

  await browser.close();
  save();
})().catch((e) => { log('FATAL ' + e.message); save(); process.exit(1); });

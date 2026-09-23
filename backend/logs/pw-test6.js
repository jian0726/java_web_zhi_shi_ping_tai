const { chromium } = require('playwright-core');

(async () => {
  const log = (m) => console.log(String(m));
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  page.on('pageerror', (e) => log('[pageerror] ' + e.message));
  page.on('console', (m) => { if (m.type() === 'error') log('[console] ' + m.text()); });

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(1500);
  log('就绪: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  // 只在前 12 秒挂起，之后放行
  const blockUntil = Date.now() + 12000;
  await page.route('**/src/views/admin/KnowledgeAudit.vue*', async (route) => {
    const wait = blockUntil - Date.now();
    if (wait > 0) { await new Promise((r) => setTimeout(r, wait)); }
    return route.continue();
  });

  // 关键：click 用 waitUntil 不等待导航完成，避免测试脚本自己被挂住
  log('触发切换（不等待导航完成）');
  page.click('text=审核知识库').catch(() => {});

  for (let i = 1; i <= 25; i++) {
    await page.waitForTimeout(1000);
    const url = page.url();
    const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    log('T+' + i + 's url=' + url.replace('http://localhost:5173', '') + ' 标题=' + title);
    if (title === '审核知识库') { log('>>> 已恢复'); break; }
  }

  await browser.close();
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

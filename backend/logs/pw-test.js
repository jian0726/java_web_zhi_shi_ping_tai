const { chromium } = require('playwright-core');

(async () => {
  const out = [];
  const log = (m) => { out.push(String(m)); console.log(String(m)); };

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  const errors = [];
  page.on('console', (m) => { if (m.type() === 'error') errors.push('[console] ' + m.text()); });
  page.on('pageerror', (e) => errors.push('[pageerror] ' + e.message));
  page.on('requestfailed', (r) => errors.push('[reqfail] ' + r.url()));

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('STEP1 已注入管理员登录态');

  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2500);
  log('STEP2 岗位管理 标题=' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  await page.click('text=审核知识库');
  await page.waitForTimeout(2500);
  log('STEP3 审核知识库 标题=' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  for (let i = 1; i <= 6; i++) {
    let t = Date.now();
    await page.click('text=岗位管理');
    await page.waitForTimeout(1000);
    const a = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    const dA = Date.now() - t;

    t = Date.now();
    await page.click('text=审核知识库');
    await page.waitForTimeout(1000);
    const b = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    const dB = Date.now() - t;

    log('ROUND' + i + ' 岗位=' + a + '(' + dA + 'ms) | 审核=' + b + '(' + dB + 'ms)');
  }

  log('ERRORS_COUNT=' + errors.length);
  errors.slice(0, 20).forEach((e) => log(e));

  await browser.close();
  require('fs').writeFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/test-result.txt', out.join('\n'), 'utf8');
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

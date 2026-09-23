const { chromium } = require('playwright-core');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  page.on('pageerror', (e) => console.log('[PAGEERROR] ' + e.message));

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  await page.goto('http://localhost:5173/admin/audit-knowledge', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(1500);
  console.log('起点: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  console.log('=== 场景1: 反复点击菜单（检测主线程是否存活）===');
  for (let i = 1; i <= 6; i++) {
    const t = Date.now();
    await page.click('.side-item:has-text("岗位管理")').catch((e) => console.log('点击失败:' + e.message.split('\n')[0]));
    await page.waitForTimeout(700);
    await page.click('.side-item:has-text("审核知识库")').catch((e) => console.log('点击失败:' + e.message.split('\n')[0]));
    await page.waitForTimeout(700);
    const alive = await page.evaluate(() => 1 + 1).catch(() => 'DEAD');
    console.log('ROUND' + i + ' 耗时=' + (Date.now() - t) + 'ms 主线程=' + alive);
    if (alive === 'DEAD') { console.log('>>> 页面无响应'); break; }
  }

  console.log('=== 场景2: 前进/后退 ===');
  for (let i = 1; i <= 3; i++) {
    const t = Date.now();
    await page.goBack().catch((e) => console.log('后退失败: ' + e.message.split('\n')[0]));
    await page.waitForTimeout(800);
    const alive1 = await page.evaluate(() => 1 + 1).catch(() => 'DEAD');
    await page.goForward().catch((e) => console.log('前进失败: ' + e.message.split('\n')[0]));
    await page.waitForTimeout(800);
    const alive2 = await page.evaluate(() => 1 + 1).catch(() => 'DEAD');
    console.log('ROUND' + i + ' 后退后=' + alive1 + ' 前进后=' + alive2 + ' 耗时=' + (Date.now() - t) + 'ms');
    if (alive1 === 'DEAD' || alive2 === 'DEAD') { console.log('>>> 页面无响应'); break; }
  }

  const histLen = await page.evaluate(() => history.length).catch(() => 'DEAD');
  console.log('历史栈长度=' + histLen);

  await browser.close();
})().catch((e) => { console.error('FATAL ' + e.message); process.exit(1); });

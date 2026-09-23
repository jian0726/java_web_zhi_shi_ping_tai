const { chromium } = require('playwright-core');
const fs = require('fs');

(async () => {
  const log = (m) => { console.log(String(m)); fs.appendFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/real-test.out', String(m) + '\n'); };
  fs.writeFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/real-test.out', '');

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  page.on('pageerror', (e) => log('[pageerror] ' + e.message));

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(1500);
  log('就绪: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  // 通知外部脚本可以重启 vite 了
  fs.writeFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/ready.flag', 'ready');
  log('已发出重启信号，等待外部重启 vite 并恢复...');

  // 等 20 秒覆盖 vite 重启窗口
  await page.waitForTimeout(20000);

  log('开始来回切换测试');
  for (let i = 1; i <= 4; i++) {
    const t = Date.now();
    page.click('text=审核知识库').catch(() => {});
    let ok = false;
    for (let k = 0; k < 12; k++) {
      await page.waitForTimeout(800);
      const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
      if (title === '审核知识库') { ok = true; break; }
    }
    const d1 = Date.now() - t;

    const t2 = Date.now();
    page.click('text=岗位管理').catch(() => {});
    let ok2 = false;
    for (let k = 0; k < 12; k++) {
      await page.waitForTimeout(800);
      const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
      if (title === '岗位管理') { ok2 = true; break; }
    }
    const d2 = Date.now() - t2;

    log('ROUND' + i + ' 审核=' + (ok ? 'OK' : 'FAIL') + '(' + d1 + 'ms) 岗位=' + (ok2 ? 'OK' : 'FAIL') + '(' + d2 + 'ms)');
  }

  log('FINAL_URL=' + page.url());
  await browser.close();
  fs.writeFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/done.flag', 'done');
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

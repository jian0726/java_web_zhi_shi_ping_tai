const { chromium } = require('playwright-core');
const { execSync } = require('child_process');
const fs = require('fs');

(async () => {
  const log = (m) => console.log(String(m));
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  const errors = [];
  page.on('pageerror', (e) => errors.push('[pageerror] ' + e.message));
  page.on('console', (m) => { if (m.type() === 'error') errors.push('[console] ' + m.text()); });

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('STEP1 登录态就绪，已访问首页（模块已缓存）');

  // 模拟"用户长时间停留"：先加载岗位页，让浏览器缓存好模块
  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2000);
  log('STEP2 岗位页已加载: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  // 标记：杀掉 vite 再重启（这一步由外部脚本完成）
  log('STEP3 等待外部重启 vite...');
  fs.writeFileSync('D:/zhuomian/好YOU经验知识共享平台/backend/logs/ready-for-restart.flag', 'ready');
  await page.waitForTimeout(25000);

  // 重启后继续切换页面，看是否卡死
  log('STEP4 vite 应已重启，开始切换页面');
  const t = Date.now();
  await page.click('text=审核知识库').catch((e) => log('点击异常:' + e.message.split('\n')[0]));
  await page.waitForTimeout(4000);
  const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
  const elapsed = Date.now() - t;
  log('STEP5 切到审核知识库: 标题=' + title + ' 耗时=' + elapsed + 'ms');
  log('结论: ' + (elapsed < 15000 ? '✅ 正常（未卡死）' : '❌ 卡死'));
  log('ERRORS=' + errors.length);
  errors.slice(0, 5).forEach((e) => log(e));

  await browser.close();
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

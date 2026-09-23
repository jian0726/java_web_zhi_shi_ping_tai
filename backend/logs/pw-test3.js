const { chromium } = require('playwright-core');

(async () => {
  const log = (m) => console.log(String(m));
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext();
  const page = await ctx.newPage();

  const reloads = [];
  page.on('framenavigated', (f) => { if (f === page.mainFrame()) reloads.push(f.url()); });
  const errors = [];
  page.on('pageerror', (e) => errors.push('[pageerror] ' + e.message));

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('STEP1 登录态就绪');

  // 模拟 vite 崩溃：JobManage 模块永不响应
  await page.route('**/src/views/admin/JobManage.vue*', () => {});
  log('STEP2 已拦截 JobManage 模块（模拟 vite 崩溃/模块永不返回）');

  const t = Date.now();
  let hung = true;
  try {
    await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded', timeout: 20000 });
    // 等待看是否自动重载（超时为 8s，留 6s 观察余量）
    await page.waitForTimeout(16000);
    const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    const elapsed = Date.now() - t;
    log('STEP3 耗时=' + elapsed + 'ms 标题=' + title);
    log('自动重载次数=' + (reloads.length - 1));
    log('重载URL=' + reloads.join(' -> '));
    hung = elapsed > 30000;
  } catch (e) {
    log('STEP3 超时异常 -> ' + e.message.split('\n')[0]);
  }
  log('是否仍然卡死=' + hung);
  log('ERRORS=' + errors.length);
  errors.slice(0, 5).forEach((e) => log(e));

  await browser.close();
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

const { chromium } = require('playwright-core');

(async () => {
  const log = (m) => console.log(String(m));
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext();
  const page = await ctx.newPage();

  const errors = [];
  page.on('pageerror', (e) => errors.push('[pageerror] ' + e.message));
  page.on('console', (m) => { if (m.type() === 'error') errors.push('[console] ' + m.text()); });

  // 预置登录态
  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('STEP1 登录态就绪');

  // 场景A：拦截懒加载模块，模拟 vite 挂掉（模块请求永不返回 → 旧代码下会卡死）
  await page.route('**/src/views/admin/JobManage.vue*', () => { /* 永不响应，模拟 vite 死亡 */ });
  log('STEP2 已拦截 JobManage 模块请求（模拟 vite 崩溃）');

  const t = Date.now();
  let hung = false;
  try {
    await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded', timeout: 8000 });
    await page.waitForTimeout(3000);
    const title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    log('STEP3 结果: 标题=' + title + ' 耗时=' + (Date.now() - t) + 'ms');
    if (title === 'NONE') hung = true;
  } catch (e) {
    hung = true;
    log('STEP3 结果: 卡住/超时 → ' + e.message.split('\n')[0]);
  }
  log('是否卡死=' + hung);
  log('ERRORS=' + errors.length);

  await browser.close();
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

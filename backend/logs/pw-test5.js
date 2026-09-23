const { chromium } = require('playwright-core');

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
  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(1500);
  log('准备就绪: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  // 关键场景：前 10 秒让模块请求失败（模拟 vite 崩溃），之后放行（模拟 vite 恢复）
  let blockUntil = Date.now() + 10000;
  await page.route('**/src/views/admin/KnowledgeAudit.vue*', async (route) => {
    if (Date.now() < blockUntil) {
      // 模拟 vite 挂掉：请求挂起不返回，直到超时窗口过去
      await new Promise((r) => setTimeout(r, 12000));
      return route.abort().catch(() => {});
    }
    return route.continue();
  });
  log('已设置：KnowledgeAudit 模块前 10s 不可用（模拟 vite 崩溃），之后恢复');

  const t = Date.now();
  await page.click('text=审核知识库').catch((e) => log('点击异常:' + e.message.split('\n')[0]));

  // 轮询观察页面是否自愈
  let healed = false;
  let title = 'NONE';
  for (let i = 0; i < 30; i++) {
    await page.waitForTimeout(1000);
    title = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    if (title === '审核知识库') { healed = true; break; }
  }
  const elapsed = Date.now() - t;
  log('最终标题=' + title + ' 总耗时=' + elapsed + 'ms');
  log('自愈成功=' + healed);
  log('判定: ' + (healed ? '✅ 卡死后自动恢复，无需手动刷新' : '❌ 未能自动恢复'));
  log('ERRORS=' + errors.length);
  errors.slice(0, 5).forEach((e) => log(e));

  await browser.close();
})().catch((e) => { console.error('FATAL', e.message); process.exit(1); });

const { chromium } = require('playwright-core');
const { execSync, spawn } = require('child_process');

const OUT = 'D:/zhuomian/好YOU经验知识共享平台/backend/logs/real-test.out';
const lines = [];
const log = (m) => { lines.push(String(m)); console.log(String(m)); };
const save = () => require('fs').writeFileSync(OUT, lines.join('\n'));

const findVitePid = () => {
  const out = execSync('netstat -ano | grep ":5173 " | head -1', { shell: 'bash' }).toString();
  const m = out.trim().split(/\s+/);
  return m[m.length - 1];
};

(async () => {
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
  log('STEP1 就绪: ' + await page.locator('h1.page-title').first().textContent().catch(() => 'NONE'));

  // ---- 杀掉 vite（模拟崩溃）----
  try {
    const pid = findVitePid();
    execSync(`taskkill /F /PID ${pid}`, { stdio: 'ignore' });
    log('STEP2 已杀掉 vite (PID=' + pid + ')，模拟崩溃');
  } catch (e) { log('STEP2 杀 vite 失败: ' + e.message); }
  await page.waitForTimeout(3000);

  // ---- 在 vite 死亡期间切换页面（这就是用户遇到的卡死时刻）----
  const tDead = Date.now();
  page.click('text=审核知识库').catch(() => {});
  await page.waitForTimeout(3000);
  const deadTitle = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
  log('STEP3 vite 死亡期间切换: 标题=' + deadTitle + ' (耗时' + (Date.now() - tDead) + 'ms)');

  // ---- 重启 vite（模拟用户重开 dev server）----
  const child = spawn('C:/Users/32916/.workbuddy/binaries/node/versions/22.22.2-3/node.exe',
    ['node_modules/vite/bin/vite.js', '--port', '5173'],
    { cwd: 'D:/zhuomian/好YOU经验知识共享平台/frontend', detached: true, stdio: 'ignore' });
  child.unref();
  log('STEP4 已重启 vite，等待就绪...');
  await page.waitForTimeout(14000);

  // ---- 关键验证：不做任何手动刷新，看页面能否自愈 ----
  log('STEP5 开始切换测试（不手动刷新页面）');
  let allOk = true;
  for (let i = 1; i <= 4; i++) {
    const t = Date.now();
    page.click('text=审核知识库').catch(() => {});
    let ok = false;
    for (let k = 0; k < 15; k++) {
      await page.waitForTimeout(800);
      if ((await page.locator('h1.page-title').first().textContent().catch(() => 'NONE')) === '审核知识库') { ok = true; break; }
    }
    const d1 = Date.now() - t;

    const t2 = Date.now();
    page.click('text=岗位管理').catch(() => {});
    let ok2 = false;
    for (let k = 0; k < 15; k++) {
      await page.waitForTimeout(800);
      if ((await page.locator('h1.page-title').first().textContent().catch(() => 'NONE')) === '岗位管理') { ok2 = true; break; }
    }
    const d2 = Date.now() - t2;

    if (!ok || !ok2) allOk = false;
    log('ROUND' + i + ' 审核=' + (ok ? 'OK' : 'FAIL') + '(' + d1 + 'ms) 岗位=' + (ok2 ? 'OK' : 'FAIL') + '(' + d2 + 'ms)');
  }

  log('FINAL_URL=' + page.url().replace('http://localhost:5173', ''));
  log('最终结论: ' + (allOk ? '✅ 全部切换成功，页面可自愈，不再卡死' : '❌ 仍存在卡死'));
  await browser.close();
  save();
})().catch((e) => { console.error('FATAL', e.message); lines.push('FATAL ' + e.message); save(); process.exit(1); });

async page => {
  const out = [];
  const log = (m) => out.push(String(m));

  const errors = [];
  page.on('console', (msg) => { if (msg.type() === 'error') errors.push('[console] ' + msg.text()); });
  page.on('pageerror', (err) => errors.push('[pageerror] ' + err.message));
  page.on('requestfailed', (req) => errors.push('[reqfail] ' + req.url()));

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'test-token-for-ui-check');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });
  log('STEP1 已注入管理员登录态');

  await page.goto('http://localhost:5173/admin/jobs', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(2500);
  const t1 = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
  log('STEP2 岗位管理 loading OK, title=' + t1 + ' url=' + page.url());

  await page.click('text=审核知识库');
  await page.waitForTimeout(2500);
  const t2 = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
  log('STEP3 审核知识库 loading OK, title=' + t2 + ' url=' + page.url());

  for (let i = 1; i <= 5; i++) {
    const tA = Date.now();
    await page.click('text=岗位管理');
    await page.waitForTimeout(1200);
    const a = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    const dA = Date.now() - tA;

    const tB = Date.now();
    await page.click('text=审核知识库');
    await page.waitForTimeout(1200);
    const b = await page.locator('h1.page-title').first().textContent().catch(() => 'NONE');
    const dB = Date.now() - tB;

    log('ROUND' + i + ' 岗位=' + a + ' ' + dA + 'ms | 审核=' + b + ' ' + dB + 'ms');
  }

  log('ERRORS_COUNT=' + errors.length);
  for (const e of errors.slice(0, 15)) log(e);
  return out.join('\n');
}

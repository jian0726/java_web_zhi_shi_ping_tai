const { chromium } = require('playwright-core');
const fs = require('fs');

const pending = []; // CDP 记录的请求
(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  // 用 CDP 精确记录每个网络请求的时序
  const cdp = await page.context().newCDPSession(page);
  await cdp.send('Network.enable');
  const reqMap = new Map();
  const slow = [];
  cdp.on('Network.requestWillBeSent', (e) => reqMap.set(e.requestId, { url: e.request.url, t: e.timestamp }));
  cdp.on('Network.responseReceived', (e) => {
    const r = reqMap.get(e.requestId);
    if (!r) return;
    const ms = Math.round((e.timestamp - r.t) * 1000);
    if (ms > 300) slow.push(ms + 'ms  ' + r.url.replace('http://localhost:5173', ''));
  });

  await page.goto('http://localhost:5173/', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => {
    localStorage.setItem('token', 'ui-check-token');
    localStorage.setItem('nickname', 'admin');
    localStorage.setItem('roles', JSON.stringify(['READER', 'CREATOR', 'AUDITOR', 'ADMIN']));
  });

  // 直接进审核知识库，测量页面可用时间
  console.log('=== 冷启动进入审核知识库 ===');
  const t0 = Date.now();
  await page.goto('http://localhost:5173/admin/audit-knowledge', { waitUntil: 'domcontentloaded' });
  await page.waitForSelector('h1.page-title', { timeout: 20000 });
  console.log('页面标题出现耗时: ' + (Date.now() - t0) + 'ms');

  const t1 = Date.now();
  await page.waitForFunction(() => {
    const el = document.querySelector('.el-table__body-wrapper tbody');
    return el && el.children.length >= 0 && document.querySelector('.el-loading-mask') === null;
  }, { timeout: 20000 }).catch(() => {});
  console.log('表格就绪(loding消失): ' + (Date.now() - t1) + 'ms');

  // 测量切换耗时
  console.log('=== 切换耗时 ===');
  for (let i = 1; i <= 4; i++) {
    let t = Date.now();
    await page.click('text=岗位管理');
    await page.waitForFunction(() => document.querySelector('h1.page-title')?.textContent === '岗位管理', { timeout: 10000 }).catch(() => {});
    const d1 = Date.now() - t;

    t = Date.now();
    await page.click('text=审核知识库');
    await page.waitForFunction(() => document.querySelector('h1.page-title')?.textContent === '审核知识库', { timeout: 10000 }).catch(() => {});
    const d2 = Date.now() - t;

    console.log('ROUND' + i + ' 岗位=' + d1 + 'ms 审核=' + d2 + 'ms');
  }

  console.log('=== 超过 300ms 的请求 ===');
  slow.forEach((s) => console.log(s));

  await browser.close();
})().catch((e) => { console.error('FATAL ' + e.message); process.exit(1); });

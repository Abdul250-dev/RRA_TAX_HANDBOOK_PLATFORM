const { chromium } = require("playwright");

async function main() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const apiBaseUrl = "http://localhost:8081";
  const runSuffix = Date.now();
  const employeeId = `RRA-UI-${runSuffix}`;
  const inviteEmail = `abdul.ui.verifier+${runSuffix}@rra.test`;

  await page.goto("http://localhost:3000/login", { waitUntil: "networkidle" });
  await page.locator('input[autocomplete="username"]').fill("admin");
  await page.locator('input[autocomplete="current-password"]').fill("Admin@123");
  await page.getByRole("button", { name: /login/i }).click();
  await page.waitForURL("**/dashboard", { timeout: 15000 });

  await page.goto("http://localhost:3000/users", { waitUntil: "networkidle" });
  await page.getByRole("button", { name: /invite user/i }).click();
  await page.locator('input[name="employeeId"]').fill(employeeId);
  await page.locator('input[name="firstName"]').fill("Abdul");
  await page.locator('input[name="lastName"]').fill("UiVerifier");
  await page.locator('input[name="email"]').fill(inviteEmail);
  await page.locator('select[name="roleName"]').selectOption("EDITOR");

  const generatedUsername = await page.locator(".invite-user-username-preview strong").textContent();

  await page.getByRole("button", { name: /send invitation/i }).click();
  await page.waitForSelector(".invite-user-modal-success", { timeout: 15000 });
  const successText = await page.locator(".invite-user-modal-success").textContent();

  const loginResponse = await fetch(`${apiBaseUrl}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "admin", password: "Admin@123" }),
  });
  const loginPayload = await loginResponse.json();
  const pendingResponse = await fetch(`${apiBaseUrl}/api/users/invited`, {
    headers: { Authorization: `Bearer ${loginPayload.token}` },
  });
  const pendingInvites = await pendingResponse.json();
  const createdInvite = pendingInvites.find((invite) => invite.email === inviteEmail);

  if (!createdInvite?.inviteToken) {
    throw new Error("Unable to find invite token for the UI verification user.");
  }

  await page.goto(`http://localhost:3000/invitations/accept?token=${encodeURIComponent(createdInvite.inviteToken)}`, {
    waitUntil: "networkidle",
  });
  const acceptUsernameText = await page.locator(".login-panel-intro").textContent();

  console.log(JSON.stringify({
    generatedUsername,
    successText,
    acceptUsernameText,
    finalUrl: page.url(),
  }));

  await browser.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const USER_UID = "rep_123";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read leads", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("leads").get());
});

test("Authenticated user: can read and create valid lead", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertSucceeds(
    authDb.collection("leads").doc("lead_1").set({
      studentName: "Aarav Sharma",
      parentName: "Rajesh Sharma",
      phone: "+919811223344",
      city: "Bengaluru",
      loanAmount: 500000,
      stage: "NEW"
    })
  );
  await assertSucceeds(authDb.collection("leads").get());
});

test("Authenticated user: rejects invalid lead data", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertFails(
    authDb.collection("leads").doc("invalid_lead").set({
      studentName: "",
      loanAmount: -100
    })
  );
});

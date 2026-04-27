const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

// ─── Helpers ────────────────────────────────────────────────────────────────

const REQUIRED_FIELDS = [
  "airline", "airline_name", "source", "destination",
  "departure_time", "arrival_time", "price", "duration",
  "seats_available",
];

function getTimeCategory(hour) {
  if (hour >= 0 && hour <= 11) return "morning";
  if (hour >= 12 && hour <= 17) return "afternoon";
  return "evening";
}

/**
 * Resolve departure_time regardless of whether it's stored as:
 *   - a Firestore Timestamp  { _seconds, _nanoseconds }
 *   - a plain number (ms or seconds)
 */
function resolveTimestamp(value) {
  if (!value) return null;
  // Firestore Timestamp object
  if (typeof value.toDate === "function") return value.toDate();
  // Plain number — detect if milliseconds or seconds
  if (typeof value === "number") {
    return value > 1e12
      ? new Date(value)        // milliseconds
      : new Date(value * 1000); // seconds
  }
  return null;
}

function isUppercaseString(val) {
  return typeof val === "string" && val === val.toUpperCase() && val.length > 0;
}

// ─── Main ────────────────────────────────────────────────────────────────────

async function verifyAndFixFlights() {
  const BATCH_SIZE = 500;
  const collectionRef = db.collection("flights");

  console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  console.log("  SkyPort — Firestore Flights Verifier & Fixer");
  console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

  console.log("📡 Connecting to Firestore and reading 'flights' collection...\n");
  const snapshot = await collectionRef.get();

  // ── STEP 1: Empty check ────────────────────────────────────────────────────
  if (snapshot.empty) {
    console.log("❌ The 'flights' collection is EMPTY.");
    console.log("   → Your queries will always return no results.");
    console.log("   → Please import flight data first.");
    process.exit(0);
  }

  const docs = snapshot.docs;
  console.log(`✅ Found ${docs.size} documents in 'flights'.\n`);

  // ── STEP 2: Print first 5 docs ─────────────────────────────────────────────
  console.log("─── First 5 Documents (Sample) ────────────────────────────────\n");
  docs.slice(0, 5).forEach((doc, idx) => {
    console.log(`[${idx + 1}] ID: ${doc.id}`);
    console.log(JSON.stringify(doc.data(), null, 2));
    console.log();
  });

  // ── STEP 3: Verify + Prepare Updates ──────────────────────────────────────
  console.log("─── Verifying Data Consistency ─────────────────────────────────\n");

  let totalChecked = 0;
  let totalUpdated = 0;
  let issueCount = 0;
  const issues = [];

  let batchNumber = 0;

  for (let i = 0; i < docs.length; i += BATCH_SIZE) {
    const chunk = docs.slice(i, i + BATCH_SIZE);
    const batch = db.batch();
    let batchHasWrites = false;

    for (const doc of chunk) {
      totalChecked++;
      const data = doc.data();
      const docIssues = [];
      const updates = {};

      // --- Check required fields ---
      for (const field of REQUIRED_FIELDS) {
        if (data[field] === undefined || data[field] === null) {
          docIssues.push(`Missing field: "${field}"`);
        }
      }

      // --- Check source / destination are uppercase IATA codes ---
      if (!isUppercaseString(data.source)) {
        docIssues.push(`"source" is not a valid uppercase string: "${data.source}"`);
      }
      if (!isUppercaseString(data.destination)) {
        docIssues.push(`"destination" is not a valid uppercase string: "${data.destination}"`);
      }
      if (data.source === data.destination) {
        docIssues.push(`source and destination are the same: "${data.source}"`);
      }

      // --- Check departure_time is resolvable ---
      const depDate = resolveTimestamp(data.departure_time);
      if (!depDate) {
        docIssues.push(`"departure_time" could not be parsed: ${JSON.stringify(data.departure_time)}`);
      }

      // --- Log issues for this doc ---
      if (docIssues.length > 0) {
        issueCount++;
        issues.push({ id: doc.id, problems: docIssues });
        if (issues.length <= 10) {
          // Only print first 10 problematic docs to avoid flooding console
          console.warn(`⚠️  [${doc.id}]`);
          docIssues.forEach((p) => console.warn(`      ↳ ${p}`));
        }
      }

      // --- Add missing derived fields if needed ---
      let needsUpdate = false;

      if (depDate) {
        const hour = depDate.getHours();

        // time_category
        if (!data.time_category) {
          updates.time_category = getTimeCategory(hour);
          needsUpdate = true;
        }
      }

      // stops & is_non_stop
      if (data.stops === undefined || data.stops === null) {
        const stops = Math.random() < 0.5 ? 0 : 1;
        updates.stops = stops;
        updates.is_non_stop = stops === 0;
        needsUpdate = true;
      } else if (data.is_non_stop === undefined || data.is_non_stop === null) {
        updates.is_non_stop = data.stops === 0;
        needsUpdate = true;
      }

      if (needsUpdate) {
        batch.update(doc.ref, updates);
        batchHasWrites = true;
        totalUpdated++;
      }
    }

    if (batchHasWrites) {
      await batch.commit();
      batchNumber++;
      console.log(`  📦 Batch ${batchNumber} committed (${Math.min(i + BATCH_SIZE, docs.length)}/${docs.length} processed)`);
    }
  }

  // ── STEP 4: Summary ────────────────────────────────────────────────────────
  console.log("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  console.log("  Summary");
  console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  console.log(`  Total documents checked : ${totalChecked}`);
  console.log(`  Documents with issues   : ${issueCount}`);
  console.log(`  Documents updated       : ${totalUpdated}`);

  if (issueCount > 0) {
    console.log(`\n⚠️  ${issueCount} document(s) had data issues.`);
    if (issues.length > 10) {
      console.log(`   (Only first 10 shown above — total: ${issues.length})`);
    }
    console.log("\n💡 Possible reasons queries return empty results:");
    console.log("   → source/destination fields may have wrong casing");
    console.log("   → departure_time may not be a valid Firestore Timestamp");
    console.log("   → Required fields may be missing (null/undefined)");
  } else {
    console.log("\n✅ All documents passed consistency checks.");
    console.log("   If queries still return empty, check:");
    console.log("   → Firestore security rules (read access)");
    console.log('   → Correct collection name ("flights", case-sensitive)');
    console.log("   → Filter values match exactly (e.g. \"DEL\" not \"del\")");
  }

  console.log("\n✅ Done.\n");
}

verifyAndFixFlights().catch((err) => {
  console.error("❌ Fatal error:", err);
  process.exit(1);
});

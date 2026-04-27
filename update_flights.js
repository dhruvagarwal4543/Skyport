const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

// ─── Helpers ────────────────────────────────────────────────────────────────

function getTimeCategory(hour) {
  if (hour >= 0 && hour <= 11) return "morning";
  if (hour >= 12 && hour <= 17) return "afternoon";
  return "evening";
}

function randomStops() {
  return Math.random() < 0.5 ? 0 : 1;
}

// Adjust price by +/- 500–1500 while clamping to ₹2000–₹9000
function adjustPrice(currentPrice) {
  const delta = Math.floor(Math.random() * 1001) + 500; // 500 – 1500
  const direction = Math.random() < 0.5 ? 1 : -1;
  const newPrice = currentPrice + direction * delta;
  return Math.min(9000, Math.max(2000, newPrice));
}

// ─── Main ────────────────────────────────────────────────────────────────────

async function updateFlights() {
  const BATCH_SIZE = 500; // Firestore max per batch commit
  const collectionRef = db.collection("flights");

  console.log("Fetching all flight documents...");
  const snapshot = await collectionRef.get();

  if (snapshot.empty) {
    console.log("No documents found in 'flights' collection.");
    return;
  }

  console.log(`Found ${snapshot.size} documents. Starting batch updates...`);

  const docs = snapshot.docs;
  let totalUpdated = 0;
  let batchCount = 0;

  // Process in chunks of BATCH_SIZE
  for (let i = 0; i < docs.length; i += BATCH_SIZE) {
    const chunk = docs.slice(i, i + BATCH_SIZE);
    const batch = db.batch();

    for (const doc of chunk) {
      const data = doc.data();

      // 1. Derive time_category from departure_time (milliseconds timestamp)
      const departureMs = data.departure_time;
      const departureDate = new Date(departureMs);
      const hour = departureDate.getHours();
      const time_category = getTimeCategory(hour);

      // 2. Randomly assign stops and is_non_stop
      const stops = randomStops();
      const is_non_stop = stops === 0;

      // 3. Optionally adjust price for variation
      const currentPrice = data.price || 5000;
      const price = adjustPrice(currentPrice);

      batch.update(doc.ref, {
        time_category,
        stops,
        is_non_stop,
        price,
      });
    }

    await batch.commit();
    batchCount++;
    totalUpdated += chunk.length;
    console.log(
      `  Batch ${batchCount} committed — ${totalUpdated}/${docs.length} documents updated.`
    );
  }

  console.log(`\n✅ Done! Total documents updated: ${totalUpdated}`);
}

updateFlights().catch((err) => {
  console.error("❌ Error updating flights:", err);
  process.exit(1);
});

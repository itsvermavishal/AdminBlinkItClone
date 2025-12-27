const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotification = functions
  .runWith({ runtime: "nodejs20" })
  .https.onRequest(async (req, res) => {
    try {
      const { token, title, message } = req.body;

      if (!token || !title || !message) {
        return res.status(400).json({ success: false, error: "Missing fields" });
      }

      await admin.messaging().send({
        token: token,
        notification: {
          title: title,
          body: message
        }
      });

      return res.status(200).json({ success: true });
    } catch (error) {
      console.error("FCM Error:", error);
      return res.status(500).json({ success: false });
    }
  });

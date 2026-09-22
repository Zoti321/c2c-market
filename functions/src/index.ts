import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

async function loadTokens(firebaseUid: string): Promise<string[]> {
  const snapshot = await db
    .collection("userMappings")
    .doc(firebaseUid)
    .collection("fcmTokens")
    .get();
  return snapshot.docs
    .map((doc) => doc.get("token") as string | undefined)
    .filter((token): token is string => Boolean(token));
}

async function resolveFirebaseUid(businessUserId: string): Promise<string | null> {
  const snapshot = await db
    .collection("userMappings")
    .where("businessUserId", "==", businessUserId)
    .limit(1)
    .get();
  if (snapshot.empty) return null;
  return snapshot.docs[0].id;
}

async function sendDataMessage(tokens: string[], data: Record<string, string>) {
  if (tokens.length === 0) return;
  await messaging.sendEachForMulticast({ tokens, data });
}

export const onChatMessageCreated = onDocumentCreated(
  "conversations/{convId}/messages/{msgId}",
  async (event) => {
    const message = event.data?.data();
    if (!message) return;
    const convId = event.params.convId;
    const convSnap = await db.collection("conversations").doc(convId).get();
    const conv = convSnap.data();
    if (!conv) return;

    const senderId = message.senderId as string;
    const recipientBusinessId =
      senderId === conv.buyerId ? (conv.sellerId as string) : (conv.buyerId as string);
    if (recipientBusinessId.startsWith("mock-seller-")) return;

    const recipientUid = await resolveFirebaseUid(recipientBusinessId);
    if (!recipientUid) return;

    const tokens = await loadTokens(recipientUid);
    await sendDataMessage(tokens, {
      type: "chat",
      remoteConversationId: convId,
      title: "新消息",
      body: (message.body as string) ?? "",
    });
  },
);

export const onOrderStatusChanged = onDocumentUpdated(
  "orders/{orderId}",
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;
    if (before.status === after.status) return;

    const orderId = event.params.orderId;
    const buyerId = after.buyerId as string;
    const sellerId = after.sellerId as string;
    const status = after.status as string;

    const title = "订单更新";
    const body = `订单状态：${status}`;
    const payload = {
      type: "order",
      remoteOrderId: orderId,
      orderKind: mapOrderKind(status),
      title,
      body,
    };

    const recipientBusinessIds = recipientsForStatus(status, buyerId, sellerId);
    for (const businessId of recipientBusinessIds) {
      const uid = await resolveFirebaseUid(businessId);
      if (!uid) continue;
      await sendDataMessage(await loadTokens(uid), payload);
    }
  },
);

function recipientsForStatus(
  status: string,
  buyerId: string,
  sellerId: string,
): string[] {
  switch (status) {
    case "PENDING":
      return [sellerId];
    case "CONFIRMED":
      return [buyerId];
    case "COMPLETED":
      return [buyerId, sellerId];
    case "CANCELLED":
      return [buyerId, sellerId];
    default:
      return [];
  }
}

function mapOrderKind(status: string): string {
  switch (status) {
    case "PENDING":
      return "PENDING_SELLER";
    case "CONFIRMED":
      return "CONFIRMED_BUYER";
    case "COMPLETED":
      return "COMPLETED";
    default:
      return status;
  }
}

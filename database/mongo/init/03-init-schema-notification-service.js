//Inserito per eager inizilization del database
db = db.getSiblingDB('notification_service');

//Inserito per eager inizilization della collection
db.createCollection("consumed_events", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "eventId", "consumerName", "requestId", "receivedAt"],
      additionalProperties: true,
      properties: {
        _id: { bsonType: "string" },
        eventId: { bsonType: "string" },
        consumerName: { bsonType: "string" },
        requestId: { bsonType: "string" },
        receivedAt: { bsonType: "date" }
      }
    }
  },
  validationLevel: "moderate"
});

//Inserito per eager inizilization della collection
db.createCollection("notification_attempts", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "requestId", "eventId", "requestStatus", "attempt", "occurredAt", "receivedAt"],
      additionalProperties: true,
      properties: {
        _id: { bsonType: "string" },
        requestId: { bsonType: "string" },
        eventId: { bsonType: "string" },
        eventTime: { bsonType: "date" },
        traceId: { bsonType: "string" },
        correlationId: { bsonType: "string" },
        requestStatus: { bsonType: "string" },
        notificationStatus: { bsonType: "string" },
        attempt: { bsonType: "int" },
        occurredAt: { bsonType: "date" },
        receivedAt: { bsonType: "date" },
      }
    }
  },
  validationLevel: "moderate"
});
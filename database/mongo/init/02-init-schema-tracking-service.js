//Inserito per eager inizilization del database
db = db.getSiblingDB('tracking_service');

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
db.createCollection("tracking_events", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "requestId", "eventId", "eventType", "receivedAt"],
      additionalProperties: true,
      properties: {
        _id: { bsonType: "string" },
        requestId: { bsonType: "string" },
        eventId: { bsonType: "string" },
        eventType: { bsonType: "string" },
        eventTime: { bsonType: ["date", "null"] },
        deliveryType: { bsonType: ["string", "null"] },
        status: { bsonType: ["string", "null"] },
        traceId: { bsonType: ["string", "null"] },
        correlationId: { bsonType: ["string", "null"] },
        failureCode: { bsonType: ["string", "null"] },
        failureMessage: { bsonType: ["string", "null"] },
        payload: { bsonType: ["string", "null"] },
        occurredAt: { bsonType: ["date", "null"] },
        receivedAt: { bsonType: "date" },
        providerMessageId: { bsonType: ["string", "null"] }
      }
    }
  },
  validationLevel: "moderate"
});
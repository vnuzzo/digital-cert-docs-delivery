//Inserito per eager inizilization del database
db = db.getSiblingDB('delivery_service');

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
db.createCollection("delivery_attempts", {
validator: {
  $jsonSchema: {
    bsonType: "object",
    required: ["_id", "requestId", "status", "createdAt", "updatedAt"],
    additionalProperties: true,
    properties: {
      _id: { bsonType: "string" },
      requestId: { bsonType: "string" },
      status: {
        bsonType: "string",
        enum: ["CONSUMED", "VALIDATED", "SENT", "FAILED"]
      },
      deliveryType: { bsonType: ["string", "null"] },
      recipients: {
        bsonType: ["array", "null"],
        items: { bsonType: "string" }
      },
      documentRefs: {
        bsonType: ["array", "null"],
        items: { bsonType: "string" }
      },
      providerMessageId: { bsonType: ["string", "null"] },
      failureCode: { bsonType: ["string", "null"] },
      failureMessage: { bsonType: ["string", "null"] },
      createdAt: { bsonType: "date" },
      updatedAt: { bsonType: "date" }
    }
  }
},
validationLevel: "moderate"
});
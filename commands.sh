#!/usr/bin/env bash

#
# Script to exercise the application endpoints over HTTP
# Useful as a smoke test once the platform has been deployed to Docker or Kubernetes or just during general development.
#

if [[ -z "${EVENT_STORE_URL}" || -z "${VIEW_STORE_URL}"  ]]; then
  echo "Environment variables EVENT_STORE_URL and VIEW_STORE_URL must be defined. Use EXPORT to define them before running the script"
  exit 1;
fi

# Command Endpoints (event-store)

ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com", "userName":"testuser12345", "address":"11 Test Lane, Test Town, TT1 1TT"}' $EVENT_STORE_URL/api/v1/bank)
echo "Created account $ID"

curl -X POST -H "Content-Type: application/json" -d '{"email":"test123@test.com"}' $EVENT_STORE_URL/api/v1/bank/email/$ID
echo "Updated email address"

curl -X POST -H "Content-Type: application/json" -d '{"address":"64 Test Ave, Testington, 1TT TT1"}' $EVENT_STORE_URL/api/v1/bank/address/$ID
echo "Updated street address"

curl -X POST -H "Content-Type: application/json" -d '{"amount": 500.00}' $EVENT_STORE_URL/api/v1/bank/deposit/$ID
echo "Deposit of 500"

curl -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' $EVENT_STORE_URL/api/v1/bank/withdraw/$ID
echo "Withdrawal of 100"

sleep 1

# Query Endpoints (view-store)

echo "Account information"
curl -s "$VIEW_STORE_URL/api/v1/bank/$ID" | jq

echo "All accounts"
curl -s "$VIEW_STORE_URL/api/v1/bank/balance?page=0&size=3" | jq
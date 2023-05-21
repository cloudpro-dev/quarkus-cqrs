#!/bin/bash

ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com", "userName":"testuser12345", "address":"11 Test Lane, Test Town, TT1 1TT"}' localhost:9020/api/v1/bank)
echo "$ID"

curl -v -X POST -H "Content-Type: application/json" -d '{"email":"test123@test.com"}' localhost:9020/api/v1/bank/email/$ID

curl -v -X POST -H "Content-Type: application/json" -d '{"address":"64 Test Ave, Testington, 1TT TT1"}' localhost:9020/api/v1/bank/address/$ID

curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 500.00}' localhost:9020/api/v1/bank/deposit/$ID

curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' localhost:9020/api/v1/bank/withdraw/$ID
curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' localhost:9020/api/v1/bank/withdraw/$ID

sleep 2

curl -v "localhost:9010/api/v1/bank/balance?page=0&size=3"

curl -v "localhost:9010/api/v1/bank/$ID"

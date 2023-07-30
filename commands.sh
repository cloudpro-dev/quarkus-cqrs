# Command Endpoints (event-store)

ID=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com", "userName":"testuser12345", "address":"11 Test Lane, Test Town, TT1 1TT"}' localhost:9020/api/v1/bank)
echo "Created account $ID"

curl -v -X POST -H "Content-Type: application/json" -d '{"email":"test123@test.com"}' localhost:9020/api/v1/bank/email/$ID
echo "Updated email address"

curl -v -X POST -H "Content-Type: application/json" -d '{"address":"64 Test Ave, Testington, 1TT TT1"}' localhost:9020/api/v1/bank/address/$ID
echo "Updated street address"

curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 500.00}' localhost:9020/api/v1/bank/deposit/$ID
echo "Deposit of 500"

curl -v -X POST -H "Content-Type: application/json" -d '{"amount": 100.00}' localhost:9020/api/v1/bank/withdraw/$ID
echo "Withdrawal of 100"

sleep 1

# Query Endpoints (view-store)

echo "Account information"
curl -v "localhost:9010/api/v1/bank/$ID"

echo "All accounts"
curl -v "localhost:9010/api/v1/bank/balance?page=0&size=3"
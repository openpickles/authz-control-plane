#!/bin/bash
# reproduce_issue.sh

# Note: This connects to the deployed service. 
# You might want to change URL to localhost:8080 if running locally.
URL="https://openpickles-85640149009.us-central1.run.app/api/v1/policies"

echo "Attempting to create first policy with empty filename..."
# Using a unique name to avoid name collision, focusing on filename collision
NAME1="ReproPolicy_$(date +%s)_1"
curl -X POST "$URL" -v \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$NAME1\",
    \"content\": \"package example\",
    \"status\": \"DRAFT\",
    \"filename\": \"\"
  }"
echo -e "\n\n"

sleep 1

echo "Attempting to create second policy with SAME empty filename..."
NAME2="ReproPolicy_$(date +%s)_2"
curl -X POST "$URL" -v \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$NAME2\",
    \"content\": \"package example\",
    \"status\": \"DRAFT\",
    \"filename\": \"\"
  }"
echo -e "\n"

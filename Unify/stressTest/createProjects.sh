#!/bin/bash

for ((i = 1; i <= 10000; i++)); do

  echo ""
  echo "$(date) : creating project stress_test_project_$i"

  curl -q -X 'POST' \
    'http://dev.movetodata.io/api/kitab/project/create' \
    -H 'accept: */*' \
    -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzZjMzZTBhYS05MWRmLTQ4ZGMtYmVlYy1hMTEyYzBkMTBhY2IiLCJpYXQiOjE2OTkzNjcyMDIsImV4cCI6MTY5OTQxMDQwMn0.aOZtX--mnTW4vUYoyU2SOcF47aHrh2zkFqNIDUtedG9ulj9tT2uW6bhWt-uWCORjUvr7HN8ixcbg1bSE-4AX0Q" \
    -H 'Content-Type: application/json' \
    -d '{
  "name": "stress_test_project_'$i'",
  "groups":true,
  "folders":true,
  "userLanguage":"en",
  "size":0
}'

done

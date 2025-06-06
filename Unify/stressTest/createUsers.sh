#!/bin/bash

for ((i=1;i<=10000;i++)); do

curl -X 'POST' \
  'http://dev.bosler.io/api/passport/users/add' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyYWtlc2giLCJpc3MiOiJodHRwOi8vZGV2LmJvc2xlci5pby9hcGkvcGFzc3BvcnQvdG9rZW4vQ3JlYXRlTG9uZ0xpdmVkIiwiZXhwIjoxNjUwNTg1NjAwfQ.hV5hmBzL3P46SnHk798GEMhK3wUbZJoGOIJQU6zmKaA' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Test1_'$i'",
  "username": "userId'$i'",
  "password": "bosler1234",
  "firstName": "Test'$i'",
  "lastName": "User'$i'",
  "location": "string",
  "profileImageUrl": "string",
  "email": "string"
}'

done

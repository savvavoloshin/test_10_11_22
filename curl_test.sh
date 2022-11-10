#/bin/bash

curl -v -X POST http://localhost:8765 -H 'Content-Type: application/json' \
 -d '{"name":"savva.voloshin","password":"123456"}'

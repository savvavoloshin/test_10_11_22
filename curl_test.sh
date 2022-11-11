#/bin/bash

# curl -v -X POST http://localhost:8765 -H 'Content-Type: application/json' \
#  -d '{"name":"savva.voloshin","password":"123456"}'

curl -v -X POST http://localhost:8765/messages -H 'Content-Type: application/json' \
-H "Authorization: Bearer_Ym9zY236Ym9zY28=" \
 -d '{"name":"savva.voloshin","message":"Папа у Васи силён в математике."}'

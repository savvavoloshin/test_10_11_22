#/bin/bash

curl -v -X POST http://localhost:8765 -H 'Content-Type: application/json' -d '{"login":"my_login","password":"my_password"}'

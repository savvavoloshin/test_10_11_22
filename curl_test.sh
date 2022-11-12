#/bin/bash

# curl -v -X POST http://localhost:8765 -H 'Content-Type: application/json' \
#  -d '{"name":"savva.voloshin","password":"123456"}'

# curl -v -X POST http://localhost:8765 -H 'Content-Type: application/json' \
#  -d '{"name":"savva.voloshin.2","password":"1234567"}'

# curl -v -X POST http://localhost:8765/messages -H 'Content-Type: application/json' \
# -H "Authorization: Bearer_eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbiJ9.B1OTtby9PvGz0EPfV9QtRXWFm81oJUxB-PdjVDA4IVw" \
#  -d '{"name":"savva.voloshin","message":"Папа у Васи силён в математике."}'

# curl -v -X POST http://localhost:8765/messages -H 'Content-Type: application/json' \
# -H "Authorization: Bearer_eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbi4yIn0.SFnICEVVHj5n_-zlPNWNfeqTzL3Tgj6uQAAQzpc3DK4" \
#  -d '{"name":"savva.voloshin.2","message":"Папа у Васи силён в математике, учит уроки за Васю весь год."}'

curl -v -X POST http://localhost:8765/messages -H 'Content-Type: application/json' \
-H "Authorization: Bearer_eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsIm5hbWUiOiJzYXZ2YS52b2xvc2hpbi4yIn0.SFnICEVVHj5n_-zlPNWNfeqTzL3Tgj6uQAAQzpc3DK4" \
 -d '{"name":"savva.voloshin.2","message":"history 10"}'
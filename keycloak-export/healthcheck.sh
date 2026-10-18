#!/bin/bash
# Health check de Keycloak (l'image n'a ni curl ni wget) : interroge /health/ready sur le port de gestion 9000.
exec 3<>/dev/tcp/127.0.0.1/9000 || exit 1
printf 'GET /health/ready HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n' >&3
read -r status <&3
[[ "$status" == *" 200 "* ]]

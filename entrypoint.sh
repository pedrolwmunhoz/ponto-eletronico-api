#!/bin/sh
# Converte DATABASE_URL (postgresql://user:pass@host:port/db) do Render em SPRING_DATASOURCE_URL (jdbc:postgresql://host:port/db)
if [ -n "$DATABASE_URL" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_URL#*@}"
fi
exec java -Dserver.port=${PORT:-8081} -jar app.jar

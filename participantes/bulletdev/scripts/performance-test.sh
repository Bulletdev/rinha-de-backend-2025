#!/bin/bash

echo "=== Rinha Backend Performance Test ==="

echo "Aguardando serviço ficar disponível..."
while ! curl -s http://localhost:8080/payments-summary > /dev/null; do
    sleep 1
done

echo "Serviço disponível! Iniciando testes..."

echo "1. Teste de latência básica..."
curl -w "@curl-format.txt" -o /dev/null -s \
  -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"currency":"BRL","reference":"test-latency"}'

echo "2. Teste de throughput (10k requests, 100 concurrent)..."
hey -n 10000 -c 100 -m POST \
  -d '{"amount":100,"currency":"BRL","reference":"test-throughput"}' \
  -H "Content-Type: application/json" \
  http://localhost:8080/payments

echo "3. Teste de stress (30 segundos, 200 concurrent)..."
hey -z 30s -c 200 -m POST \
  -d '{"amount":100,"currency":"BRL","reference":"test-stress"}' \
  -H "Content-Type: application/json" \
  http://localhost:8080/payments

echo "4. Teste do endpoint summary..."
hey -n 1000 -c 50 -m GET \
  http://localhost:8080/payments-summary

echo "=== Testes concluídos ==="

     time_namelookup:  %{time_namelookup}\n
        time_connect:  %{time_connect}\n
     time_appconnect:  %{time_appconnect}\n
    time_pretransfer:  %{time_pretransfer}\n
       time_redirect:  %{time_redirect}\n
  time_starttransfer:  %{time_starttransfer}\n
                     ----------\n
          time_total:  %{time_total}\n

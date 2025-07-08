# Rinha de Backend - Nerfa mais a JVM

```  
                ⢀⣴⣿⣿⣿⣿⣿⣶⣶⣶⣿⣿⣶⣶⣶⣶⣶⣿⡿⣿⣾⣷⣶⣶⣾⣿⠀                                                                                                                          
             ⣠⣿⣿⢿⣿⣯⠀⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⣿⡇⣿⣿⣿⣿⣿⡇                                                                                                         
         ⠀⣰⣿⣿⣷⡟⠤⠟⠁⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢸⡇⣿⣿⣿⣿⣿⡇ 
         ⠀⣿⣿⣿⣿⣿⣷⣶⣿⣿⡟⠁⣮⡻⣿⣿⣿⣿⣿⣿⣿⣿⢸⡇⣿⣿⣿⣿⣿⡇ 
         ⠘⣿⣿⣿⣿⣿⣿⣿⣿⠏⠀⠀⣿⣿⣹⣿⣿⣿⣿⣿⣿⡿⢸⡇⣿⣿⣿⣿⣿⡇ 
         ⠀⠙⢿⣿⣿⣿⡿⠟⠁⣿⣿⣶⣿⠟⢻⣿⣿⣿⣿⣿⣿⡇⣼⡇⣿⣿⣿⣿⣿⠇
         ⠀⠀⠈⠋⠉⠁⣶⣶⣶⣿⣿⣿⣿⢀⣿⣿⣿⣿⣿⣿⣿⣇⣿⢰⣿⣿⣿⣿⣿⠀ 
         ⠀⠀⠀⠀⠀⠙⠿⣿⣿⣿⡄⢀⣠⣾⣿⣿⣿⣿⣿⣿⣿⣽⣿⣼⣿⣿⣿⣿⠇⠀ 
         ⠀⠀⠀⠀⠀⠀⠀⠈⠉⠒⠚⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠛⠿⠿⠿⠿⠿⠋⠀⠀ 
         ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀ 
         ⠀⠀⠀⣿⣙⡆⠀⠀⡇⠀⢸⠀⠀⢸⠀⠀ ⢸⡇⠀⠀⢸⣏⡉  ⠙⡏⠁⠀ 
         ⠀⠀⠀⣿⣉⡷⠀⠀⢧⣀⣼ ⠀⢸⣀  ⢸⣇⡀ ⢸⣏⣁⠀ ⠀⡇⠀ 
```
## Arquitetura e Otimizações

### Framework e Tecnologias
- **Netty**: Framework HTTP de alta performance para baixa latência
- **AsyncHttpClient**: Cliente HTTP assíncrono para integração com processadores
- **Caffeine**: Cache LRU otimizado para JVM
- **Jackson**: Serialização JSON rápida
- **Java 21**: Aproveitando as otimizações mais recentes da JVM

### Estratégias de Performance

#### 1. **Cache LRU Inteligente**
```java
Cache<String, PaymentSummary> summaryCache = Caffeine.newBuilder()
    .maximumSize(100)
    .expireAfterWrite(Duration.ofSeconds(10))
    .build();
```

#### 2. **Estruturas Thread-Safe**
- `AtomicLong` para contadores de métricas
- `ConcurrentHashMap` para rate limiting de health checks
- Pool de threads otimizado no Netty

#### 3. **Fallback Strategy**
```java
return processWithProcessor(request, primaryProcessor)
    .exceptionally(throwable -> {
        return processWithProcessor(request, fallbackProcessor)
            .join();
    });
```

#### 4. **Health Check Rate Limiting**
- Máximo 1 check por processador a cada 5 segundos
- Cache de 30 segundos para status de saúde
- Checks assíncronos em background

### Otimizações JVM

#### Configurações de Memória
```bash
-Xmx512m -Xms512m          # Heap fixo para evitar realocações
-XX:+UseG1GC               # G1 GC para baixa latência
-XX:MaxGCPauseMillis=100   # Máximo 100ms de pausa para GC
-XX:+UseStringDeduplication # Reduz uso de memória com strings
```

#### Otimizações Netty
```bash
-Dnetty.allocator.type=pooled     # Pool de buffers reutilizáveis
-Dnetty.allocator.maxOrder=9      # Tamanho máximo de chunks
```

### Benchmarks Esperados

#### Latência
- **P50**: < 10ms para `/payments`
- **P95**: < 50ms para `/payments`
- **P99**: < 100ms para `/payments`

#### Throughput
- **RPS**: > 10,000 requests/second
- **Concurrent Connections**: > 1,000

### Monitoramento

#### Métricas Coletadas
```json
{
  "total_payments": 1000,
  "total_amount": 50000,
  "successful_payments": 950,
  "failed_payments": 50,
  "processor_default_usage": 800,
  "processor_fallback_usage": 150
}
```

### Como Executar

```bash

./gradlew build

./gradlew run

docker-compose up --build

hey -n 10000 -c 100 -m POST -d '{"amount":100,"currency":"BRL","reference":"test"}' \
  -H "Content-Type: application/json" \
  http://localhost:8080/payments
```


### Pontos de Otimização Avançada

1. **Object Pooling**: Reutilização de objetos para reduzir GC
2. **Direct Memory**: Uso de ByteBuffers off-heap
3. **Native Transport**: Epoll no Linux para melhor performance
4. **Connection Pooling**: Pool de conexões HTTP reutilizáveis
5. **Batch Processing**: Agrupamento de métricas para reduzir contenção

Esta implementação está otimizada para alta performance e baixa latência, seguindo as melhores práticas para sistemas de alta escala em Java.

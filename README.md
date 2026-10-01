# Plataforma de Monitoramento de APIs

API REST em Java 25 e Spring Boot para acompanhar disponibilidade e latência de APIs públicas. Os resultados são persistidos em H2 e agregados em relatórios diários.

## Execução

```powershell
mvn spring-boot:run
```

A aplicação inicia em `http://localhost:8080`. Na primeira inicialização, 22 APIs públicas são incluídas na lista. Os dados locais são armazenados em `./data/api-health`.

## Rotas da API

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/api/targets` | Lista as APIs monitoradas |
| `POST` | `/api/targets` | Cadastra uma API (`name`, `url`, `method`) |
| `PUT` | `/api/targets/{id}` | Atualiza uma API monitorada |
| `DELETE` | `/api/targets/{id}` | Desativa uma API sem apagar o histórico |
| `POST` | `/api/targets/seed` | Reinsere APIs padrão ausentes |
| `POST` | `/api/monitoring/run` | Executa uma verificação de todas as APIs ativas |
| `POST` | `/api/monitoring/run/{targetId}` | Verifica uma API |
| `GET` | `/api/monitoring/checks?targetId={id}` | Consulta as últimas 100 verificações |
| `GET` | `/api/reports/latest` | Consulta o relatório diário mais recente |
| `GET` | `/api/reports/{yyyy-MM-dd}` | Consulta um relatório por data |
| `POST` | `/api/reports/{yyyy-MM-dd}/generate` | Gera ou atualiza o relatório de uma data |
| `GET` | `/actuator/health` | Estado da aplicação |

As verificações são executadas a cada cinco minutos por padrão. Um relatório do dia anterior é gerado diariamente às 00:05. O intervalo e os tempos limite podem ser configurados em `application.properties`.

Exemplo de cadastro:

```json
{
  "name": "Minha API",
  "url": "https://example.com/health",
  "method": "GET"
}
```

Uma resposta HTTP abaixo de 500 é considerada disponível; erros de rede, tempos limite excedidos e respostas 5xx são registrados como falhas.# api-health-platform

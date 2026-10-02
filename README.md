# Plataforma de Monitoramento de APIs

API REST em Java 25 e Spring Boot para acompanhar disponibilidade e latência de APIs públicas. Os resultados são persistidos em H2 e agregados em relatórios diários.

## Execução

```powershell
mvn spring-boot:run
```

A aplicação inicia em `http://localhost:8080`. Na primeira inicialização, 22 APIs públicas são incluídas na lista. Os dados locais são armazenados em `./data/api-health`.

## Esteira no GitHub Actions

O workflow em `.github/workflows/maven.yml` é executado em pushes e pull requests para `main`. A execução é dividida em três jobs:

1. **Validate project**: valida a configuração Maven com `mvn validate`.
2. **Run tests**: compila o projeto e executa os testes com `mvn test`.
3. **Package and smoke check**: empacota a aplicação. No disparo manual, também pode iniciá-la e verificar `/actuator/health`.

Para iniciar uma execução manual, acesse **Actions**, selecione **Build, test, and smoke check**, clique em **Run workflow** e escolha o escopo:

| Opção | Execução |
| --- | --- |
| `application` | Valida o projeto, empacota sem executar os testes e verifica a saúde da aplicação iniciada no runner. |
| `tests` | Valida o projeto e executa os testes. |
| `all` | Executa validação, testes, empacotamento e smoke check. É a opção padrão. |

O workflow usa Java 25 e Ubuntu 24.04. O smoke check roda em um runner temporário do GitHub; ele não publica nem mantém a API disponível para acesso externo. Para testar localmente pelo Postman, inicie a aplicação com `mvn spring-boot:run` e use `http://localhost:8080`.

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

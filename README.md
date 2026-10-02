# Plataforma de Monitoramento de APIs

API REST em Java 25 e Spring Boot para acompanhar disponibilidade e latência de APIs públicas. Os resultados são persistidos em H2 e agregados em relatórios diários.

## Execução

```powershell
mvn spring-boot:run
```

A aplicação inicia em `http://localhost:8080`. Na primeira inicialização, 22 APIs públicas são incluídas na lista. Os dados locais são armazenados em `./data/api-health`.

## Esteira no GitHub Actions

O workflow em `.github/workflows/maven.yml` é executado em pushes e pull requests para `main` e diariamente às 07:00 no horário de Brasília (10:00 UTC). A execução agendada roda validação, testes e empacotamento; o smoke check da aplicação continua disponível nos modos manuais `application` e `all`. O GitHub pode atrasar o início devido à fila de execuções. A esteira é dividida em três jobs:

1. **Validate project**: valida a configuração Maven com `mvn validate`.
2. **Run unit and integration tests**: compila o projeto e executa todos os testes com `mvn test`.
3. **Package and smoke check**: empacota a aplicação. No disparo manual, também pode iniciá-la e verificar `/actuator/health`.

O job de testes inclui testes unitários e de integração, descobertos automaticamente pelo Maven:

- `DailyReportServiceTest`: 7 casos unitários para cálculo, criação e atualização de relatórios e respostas `404`.
- `ApiHttpIntegrationTest`: 4 casos de integração para cadastro, atualização e desativação de alvos, validação HTTP e geração/consulta de relatórios.

Os testes de integração iniciam a aplicação em uma porta aleatória, usam H2 em memória e verificam a persistência por requisições HTTP. O agendador de monitoramento é isolado para evitar chamadas a APIs públicas. Atualmente, a suíte tem 11 casos e é executada pelo comando `mvn test` no GitHub Actions.

Para iniciar uma execução manual, acesse **Actions**, selecione **Build, test, and smoke check**, clique em **Run workflow** e escolha o escopo:

| Opção | Execução |
| --- | --- |
| `application` | Valida o projeto, empacota sem executar os testes e verifica a saúde da aplicação iniciada no runner. |
| `tests` | Valida o projeto e executa os testes. |
| `all` | Executa validação, testes, empacotamento e smoke check. É a opção padrão. |

O workflow usa Java 25 e Ubuntu 24.04. O smoke check roda em um runner temporário do GitHub; ele não publica nem mantém a API disponível para acesso externo. Para testar localmente pelo Postman, inicie a aplicação com `mvn spring-boot:run` e use `http://localhost:8080`.

## Testando a API localmente

Inicie a aplicação em um terminal e deixe-o aberto enquanto testa:

```powershell
mvn spring-boot:run
```

A base URL é `http://localhost:8080`. No Postman, crie uma variável de ambiente `baseUrl` com esse valor e monte as requisições usando os métodos e endpoints da tabela abaixo. Para `POST` e `PUT` com corpo, selecione **Body > raw > JSON**; o Postman adicionará `Content-Type: application/json`.

No PowerShell, use `curl.exe` para chamar o cURL nativo do Windows. Os exemplos a seguir verificam a saúde, listam os alvos, cadastram uma API e usam o ID retornado para executar uma verificação:

```powershell
$base = "http://localhost:8080"

curl.exe -i "$base/actuator/health"
curl.exe "$base/api/targets"

$target = curl.exe -sS -X POST "$base/api/targets" `
  -H "Content-Type: application/json" `
  --data-raw '{"name":"API de teste","url":"https://example.com","method":"GET"}' |
  ConvertFrom-Json

$id = $target.id
$target | Format-List

curl.exe -i -X POST "$base/api/monitoring/run/$id"
curl.exe "$base/api/monitoring/checks?targetId=$id"
```

O cadastro retorna HTTP `201 Created`; copie o `id` da resposta se não estiver usando a variável `$id`. O DTO aceita somente os métodos `GET` ou `HEAD` e URLs iniciadas por `http://` ou `https://`. O endereço `https://example.com` é apenas um exemplo; a verificação precisa de acesso à internet e consulta a URL cadastrada.

Para atualizar o alvo, verificar todos os alvos ativos ou desativar o alvo de teste:

```powershell
curl.exe -i -X PUT "$base/api/targets/$id" `
  -H "Content-Type: application/json" `
  --data-raw '{"name":"API de teste atualizada","url":"https://example.com","method":"HEAD"}'

curl.exe -i -X POST "$base/api/monitoring/run"
curl.exe -i -X DELETE "$base/api/targets/$id"
```

O monitoramento em lote consulta todos os alvos ativos e pode demorar mais, especialmente com os 22 alvos padrão. O `DELETE` desativa o alvo sem apagar o histórico. Os dados ficam no H2 local em `./data/api-health` e persistem entre execuções.

Para gerar e consultar o relatório do dia:

```powershell
$date = Get-Date -Format "yyyy-MM-dd"
curl.exe -i -X POST "$base/api/reports/$date/generate"
curl.exe "$base/api/reports/latest"
```

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

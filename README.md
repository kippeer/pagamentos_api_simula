# Sistema de Processamento de Pagamentos

## Descrição
Sistema de processamento de pagamentos que suporta múltiplos métodos de pagamento (PIX, Cartão de Crédito e QR Code), com funcionalidades de notificação e monitoramento.

## Tecnologias Utilizadas
- Java 17
- Spring Boot 3.2.2
- PostgreSQL
- Lombok
- Micrometer (para métricas)

## Funcionalidades Principais

### Métodos de Pagamento
- PIX
  - Geração de QR Code
  - Chave PIX
  - Expiração configurável
- Cartão de Crédito
  - Validação do número do cartão
  - Processamento de parcelas
  - Tokenização do cartão
- QR Code (preparado para implementação futura)

### Notificações
- Email
- SMS
- Webhook
- Notificações assíncronas
- Histórico de notificações

### Monitoramento
- Métricas de processamento
- Monitoramento de status
- Logs detalhados
- Rastreamento de transações

## Configuração do Ambiente

### Pré-requisitos
- JDK 17
- PostgreSQL
- Maven

### Variáveis de Ambiente
```properties
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

### Banco de Dados
O sistema utiliza PostgreSQL. Configure a conexão em `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:adm}
```

## Instalação e Execução

1. Clone o repositório
```bash
git clone [url-do-repositorio]
```

2. Configure o banco de dados PostgreSQL

3. Execute a aplicação
```bash
./mvnw spring-boot:run
```

## Endpoints da API

### Pagamentos

#### Criar Pagamento
```http
POST /api/payments
```

Exemplo de requisição PIX:
```json
{
  "amount": 100.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "paymentDetails": {
    "pixKey": "email@exemplo.com",
    "description": "Pagamento de teste"
  },
  "notificationPreferences": {
    "emailNotification": true,
    "smsNotification": false
  }
}
```

Exemplo de requisição Cartão de Crédito:
```json
{
  "amount": 100.00,
  "currency": "BRL",
  "paymentMethod": "CREDIT_CARD",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "cardHolderName": "João Silva",
    "expirationDate": "12/25",
    "cvv": "123",
    "installments": 1
  },
  "notificationPreferences": {
    "emailNotification": true
  }
}
```

#### Consultar Pagamento
```http
GET /api/payments/{id}
```

#### Reembolsar Pagamento
```http
POST /api/payments/{id}/refund
```

#### Buscar Pagamentos
```http
GET /api/payments/search?status=COMPLETED&method=PIX
```

### Webhooks

#### Callback PIX
```http
POST /api/webhooks/pix/{paymentId}
```

## Segurança

- Validação de entrada de dados
- Criptografia de dados sensíveis
- Políticas de segurança para cartões
- Proteção contra ataques comuns

## Monitoramento

### Métricas Disponíveis
- Taxa de sucesso/falha de pagamentos
- Tempo de processamento
- Volume de transações
- Status das notificações

### Endpoints de Monitoramento
```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/prometheus
```

## Tratamento de Erros

O sistema possui tratamento centralizado de exceções, retornando respostas padronizadas:

```json
{
  "message": "Mensagem de erro",
  "code": "CÓDIGO_ERRO",
  "timestamp": "2024-02-10T10:30:00Z"
}
```

## Boas Práticas

- Clean Code
- SOLID Principles
- Testes unitários e de integração
- Documentação clara
- Logs estruturados
- Tratamento de exceções

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT.
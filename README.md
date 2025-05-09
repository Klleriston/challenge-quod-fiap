# Challenge FIAP-QUOD - Sistema de Detecção de Fraudes Biométricas

## Visão Geral

Este projeto é um sistema de detecção de fraudes biométricas desenvolvido utilizando Spring Boot e MongoDB. A aplicação fornece uma API RESTful para validação biométrica facial e digital, detecção de fraudes em documentos, e notificação de eventos fraudulentos.

## Especificações Técnicas

### Tecnologias Principais

- **Java 17** - Linguagem de programação
- **Spring Boot 3.4.4** - Framework para desenvolvimento de aplicações Java
- **Spring Security** - Módulo para autenticação e autorização
- **MongoDB** - Banco de dados NoSQL para persistência
- **JWT (JSON Web Token)** - Para autenticação stateless
- **OpenCV** - Biblioteca para processamento e análise de imagens
- **Apache Commons Imaging** - Para análise de metadados EXIF em imagens
- **Docker/Docker Compose** - Para containerização da aplicação e serviços

### Arquitetura

O projeto segue uma arquitetura em camadas baseada em princípios de Domain-Driven Design (DDD):

1. **Camada de Apresentação** (`presentation`) - Contém os controladores REST que recebem as requisições HTTP
2. **Camada de Aplicação** (`application`) - Contém os DTOs (Data Transfer Objects) para comunicação entre camadas
3. **Camada de Domínio** (`domain`) - Contém a lógica de negócio, modelos e serviços
4. **Camada de Infraestrutura** (`infrastructure`) - Contém configurações, segurança e componentes de suporte

## Funcionalidades Principais

### Autenticação e Autorização

- **Registro de usuários** - Criação de novos usuários com nome, email e senha
- **Login** - Autenticação via email e senha, gerando um token JWT
- **Autorização baseada em token** - Proteção de endpoints usando JWT

### Validação Biométrica

- **Biometria Facial** - Análise de imagens faciais para validação de identidade
- **Biometria Digital** - Validação de impressões digitais
- **Detecção de Fraudes** - Identificação automática de possíveis fraudes em biometrias

### Análise de Documentos

- **Validação de RG e CPF** - Verificação da validade de documentos
- **Detecção de Manipulação** - Identificação de documentos potencialmente adulterados

### Notificação de Fraudes

- **Registro de Fraudes** - Armazenamento de informações sobre ocorrências fraudulentas
- **Listagem de Notificações** - Consulta de notificações de fraude registradas
- **Metadados Contextuais** - Armazenamento de informações sobre dispositivo, localização e outros dados relevantes

## Endpoints da API

### Autenticação

- `POST /api/auth/register` - Registra um novo usuário
- `POST /api/auth/login` - Autentica um usuário e retorna um token JWT

### Biometria

- `POST /api/biometria/facial` - Valida biometria facial
- `POST /api/biometria/digital` - Valida biometria digital

### Notificações de Fraude

- `POST /api/notificacoes/fraude` - Registra uma nova notificação de fraude
- `GET /api/notificacoes` - Lista todas as notificações de fraude
- `GET /api/notificacoes/{id}` - Busca uma notificação de fraude pelo ID

### Documentos

- `POST /api/documentos/validar` - Valida documentos (RG, CPF)

## Segurança

A aplicação implementa as seguintes medidas de segurança:

- **Autenticação JWT** - Tokens com prazo de expiração para acesso seguro
- **Senhas Criptografadas** - Utilização de BCrypt para armazenamento seguro de senhas
- **HTTPS** - Suporte a comunicação criptografada (quando configurado)
- **Proteção CSRF** - Desativada para APIs RESTful stateless
- **Sessão Stateless** - Não armazena estado de sessão no servidor

## Estrutura do Banco de Dados

O projeto utiliza MongoDB como banco de dados principal, com as seguintes coleções:

- **Users** - Armazena informações de usuários
- **NotificacaoFraude** - Registra ocorrências de fraudes detectadas
- **Documents** - Armazena informações sobre documentos analisados
- **AnalysisRecord** - Registra resultados de análises de validação
- **FingerprintRecord** - Armazena dados de biometria digital
- **Image** - Armazena imagens ou referências a imagens analisadas

## Detecção de Fraudes

O sistema utiliza várias técnicas para detectar possíveis fraudes:

- **Análise Facial** - Detecta presença de rostos, múltiplos rostos, orientação e qualidade da imagem
- **Análise de Metadados EXIF** - Verifica dados de origem da imagem
- **Verificação de Liveness** - Determina se uma imagem contém um rosto "vivo" ou apenas uma foto
- **Validação de Documentos** - Verifica consistência e validade de documentos

## Implantação

### Requisitos

- Java 17 ou superior
- Docker e Docker Compose
- Maven

### Configuração

As principais configurações estão no arquivo `application.properties`:

```properties
spring.application.name=Challenge-FIAP-QUOD
spring.data.mongodb.uri=mongodb://root:root@localhost:27017/quodnosql?authSource=admin
jwt.secret=M3ea99fbJNVw/tbKGy6SP8FtgzOU5LzWQ7tXy/UnRt87RTET2IXUdLQtf6nf9OvcfU/bPlILOShXh5F3nk/ufg==
jwt.expiration=3600000
```

### Passos para Execução

1. **Iniciar o MongoDB**:
   ```bash
   docker-compose up -d
   ```

2. **Compilar e executar a aplicação**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Acesso à aplicação**:
   - API: http://localhost:8080/api
   - Interface de administração do MongoDB: http://localhost:8081
   
## Desenvolvimento

### Estrutura do Projeto

```
src/main/java/com/fiap/challengefiapquod/
├── ChallengeFiapQuodApplication.java
├── application/
│   └── dto/          # Data Transfer Objects
├── domain/
│   ├── model/        # Entidades e objetos de valor
│   ├── repository/   # Interfaces de repositório
│   └── service/      # Lógica de negócio
├── infrastructure/
│   ├── config/       # Configurações
│   └── security/     # Componentes de segurança
└── presentation/
    ├── controller/   # Controladores REST
    └── exception/    # Manipuladores de exceção
```

### Bibliotecas Principais

- **Spring Boot Starter Data MongoDB**: Integração com MongoDB
- **Spring Boot Starter Data REST**: Suporte a APIs RESTful
- **Spring Boot Starter Security**: Segurança e autenticação
- **Spring Boot Starter Web**: Desenvolvimento web MVC
- **Lombok**: Redução de código boilerplate
- **JJWT**: Implementação JWT
- **OpenCV**: Processamento de imagens
- **Apache Commons Imaging**: Análise de metadados EXIF

## Monitoramento e Administração

- MongoDB Express está disponível em http://localhost:8081 (usuário: user, senha: pass)
- Para acessar, use as credenciais configuradas no Docker Compose

## Considerações sobre Segurança

- A chave JWT definida em `application.properties` deve ser alterada em ambientes de produção
- Considere adicionar HTTPS em ambientes de produção
- As credenciais do MongoDB devem ser alteradas em ambientes de produção

## Fluxo de Validação Biométrica

1. O usuário se autentica na API
2. Envia uma imagem para validação biométrica
3. O sistema analisa a imagem usando algoritmos de detecção de fraude
4. Um resultado é retornado, indicando se a biometria é válida
5. Em caso de fraude, uma notificação é registrada automaticamente

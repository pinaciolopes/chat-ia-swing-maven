# Chat IA

Aplicação desktop de chat com Inteligência Artificial, desenvolvida em Java com JavaFX, MongoDB Atlas, RAG por palavras-chave e a API da Groq.

---

## 1. Sobre o projeto

O **Chat IA** permite que diferentes usuários usem o mesmo sistema, cada um com o seu próprio ambiente de conversação. Principais funcionalidades:

- Animação de abertura feita com **GSAP**, executada em um `WebView` antes da tela de login.
- Cadastro, login e logout de usuários, com senha armazenada em hash.
- Recuperação de senha por **token temporário** (15 minutos, uso único).
- Criação, renomeação e exclusão (com confirmação) de conversas.
- Envio de perguntas para a IA da Groq e apresentação da resposta em balões de chat.
- Histórico persistido no MongoDB Atlas: é possível fechar a aplicação e recuperar as conversas depois.
- **Isolamento de dados por usuário**: cada usuário só enxerga, altera e exclui as próprias conversas.
- **RAG por palavras-chave**: a pergunta é processada, e documentos relevantes da base de conhecimento entram como contexto na chamada à IA.
- Tema claro e escuro, controlados por JavaFX CSS (a escolha é lembrada).
- Interface 100% construída por código, sem Scene Builder.

## 2. Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem |
| JavaFX 21 (`controls` e `web`) | Interface gráfica e `WebView` do splash |
| JavaFX CSS | Estilização e temas claro/escuro |
| GSAP 3 (HTML + CSS + JavaScript) | Animação de entrada |
| Maven | Gerenciamento do projeto e das dependências |
| MongoDB Atlas (driver Java Sync 5.2.0) | Banco de dados na nuvem |
| API da Groq | Inteligência Artificial |
| jBCrypt | Hash de senhas (BCrypt) |
| Gson | JSON da API da Groq |
| Lombok | Redução de código repetitivo nos models |
| JUnit 5 | Testes unitários |
| IntelliJ IDEA | Ambiente de desenvolvimento |
| Git / GitHub | Versionamento |
| jpackage | Instalador para Windows |

## 3. Arquitetura

A aplicação segue a arquitetura em camadas **MVC + Service + Repository**:

```
View
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
MongoDB
```

- **View:** monta a interface e expõe os componentes. Não tem regra de negócio.
- **Controller:** recebe as ações da interface e as encaminha para os Services. Operações que acessam banco ou IA rodam em outra thread (`Task`), para a janela não congelar.
- **Service:** concentra as regras de negócio (validações, hash de senha, isolamento por usuário, fluxo do chat).
- **Repository:** é a única camada que fala com o MongoDB.
- **Model:** classes que representam os documentos do banco.

Fluxo de uma pergunta ao chat:

```
ChatView → ChatController → ChatService → RAGService → GroqService → API da Groq
                                  │
                                  └→ MensagemRepository → MongoDB (pergunta e resposta)
```

Fluxo de abertura:

```
Main → SplashView → WebView → HTML + CSS + JavaScript → GSAP → animação → LoginView
```

## 4. Estrutura de pastas

```
src/
├── main/
│   ├── java/br/com/projeto/
│   │   ├── Main.java            Aplicação JavaFX
│   │   ├── Launcher.java        Ponto de entrada (necessário ao rodar pelo classpath e pelo jpackage)
│   │   ├── config/              Leitura de variáveis de ambiente e conexão com o MongoDB
│   │   ├── controller/          Controllers das telas, rotas de navegação e controle de tema
│   │   ├── model/               Usuario, Conversa, Mensagem, DocumentoConhecimento, TokenRecuperacaoSenha
│   │   ├── repository/          Acesso ao MongoDB (um repository por coleção)
│   │   ├── service/             Regras de negócio, sessão e integração com a Groq
│   │   ├── rag/                 Processamento de texto: normalização, tokenização e stop words
│   │   ├── view/                Telas JavaFX montadas por código e o Navegador
│   │   └── util/                AppException e ErrorHandler
│   └── resources/
│       ├── css/                 light.css e dark.css (temas do JavaFX)
│       ├── images/
│       └── splash/              Animação de entrada
│           ├── html/splash.html
│           ├── css/splash.css
│           └── js/              gsap.min.js e splash.js
└── test/java/                   Testes JUnit (TextoProcessador)
pom.xml
README.md
```

## 5. Pré-requisitos

- JDK 21
- Maven (ou o Maven integrado ao IntelliJ)
- Conta no **MongoDB Atlas** com um cluster (o plano gratuito M0 serve)
- Conta na **Groq** com uma API Key
- Windows (sistema alvo do instalador)
- IntelliJ IDEA com **Annotation Processing** habilitado, por causa do Lombok (`Settings > Build, Execution, Deployment > Compiler > Annotation Processors`)

## 6. Configuração

As credenciais **não ficam no código nem no repositório**. A aplicação lê duas variáveis de ambiente:

| Variável | Conteúdo |
|---|---|
| `MONGODB_URI` | String de conexão do Atlas (`mongodb+srv://...`) |
| `GROQ_API_KEY` | Chave da API da Groq (`gsk_...`) |

### 6.1 MongoDB Atlas

1. Crie um cluster gratuito (M0).
2. Em **Security > Database Access**, crie um usuário e senha com a permissão *Read and write to any database*.
3. Em **Security > Network Access**, libere o acesso de rede. Para desenvolvimento, foi usada a entrada `0.0.0.0/0` (veja as [Limitações](#12-limitações)).
4. Em **Database > Connect > Drivers**, copie a string de conexão e troque `<db_password>` pela senha do usuário do passo 2. Caracteres especiais na senha precisam ser codificados (`@` vira `%40`).

A conexão com o Atlas usa `mongodb+srv://`, que trafega por TLS. O banco usado é o `chat_ia`, criado automaticamente na primeira gravação.

### 6.2 Variáveis no IntelliJ

`Run > Edit Configurations...` > configuração da classe `Launcher` > **Modify options > Environment variables**, e adicione:

```
MONGODB_URI=mongodb+srv://USUARIO:SENHA@SEU_CLUSTER.mongodb.net/?appName=Cluster0
GROQ_API_KEY=gsk_sua_chave_aqui
```

O IntelliJ guarda isso na pasta `.idea/`, que está no `.gitignore`.

### 6.3 Variáveis no Windows (para a aplicação instalada)

No prompt de comando, substituindo pelos seus valores:

```
setx MONGODB_URI "mongodb+srv://USUARIO:SENHA@SEU_CLUSTER.mongodb.net/?appName=Cluster0"
setx GROQ_API_KEY "gsk_sua_chave_aqui"
```

Feche e abra novamente o terminal (ou a aplicação) para as variáveis valerem. **O instalador não contém credenciais**: em cada computador, elas precisam ser definidas dessa forma.

## 7. Banco de dados

Banco `chat_ia`, com cinco coleções:

| Coleção | Campos | Índices |
|---|---|---|
| `users` | `_id`, `name`, `email`, `passwordHash`, `createdAt` | `email` (único) |
| `conversations` | `_id`, `ownerId`, `title`, `createdAt`, `updatedAt` | `ownerId` |
| `messages` | `_id`, `userId`, `conversationId`, `role`, `content`, `createdAt` | `conversationId` |
| `documents` | `_id`, `title`, `content`, `keywords`, `createdAt`, `updatedAt` | `keywords` |
| `password_reset_tokens` | `_id`, `userId`, `tokenHash`, `expiresAt`, `used` | `tokenHash` |

- `ownerId` (conversas) e `userId` (mensagens) guardam o `_id` do usuário dono do registro.
- `role` da mensagem é `user` ou `assistant`, os mesmos valores esperados pela API da Groq.
- `keywords` guarda as palavras-chave **já normalizadas** (minúsculas e sem acento).
- `tokenHash` guarda apenas o **hash** do código de recuperação, nunca o código.

## 8. RAG (recuperação de informações)

A primeira versão faz recuperação **por palavras-chave**:

```
Pergunta
   ↓
Normalização        minúsculas e sem acentos ("Herança" → "heranca")
   ↓
Tokenização         separa o texto em palavras, descartando pontuação
   ↓
Remoção de stop words
   ↓
Palavras relevantes (mínimo de 3 letras, sem repetição)
   ↓
Busca na coleção documents, pelo campo keywords
   ↓
Ranking pela quantidade de palavras em comum (até 3 documentos)
   ↓
Contexto → enviado à Groq junto com a pergunta e o histórico recente
```

- Ao cadastrar um documento, o `RAGService` gera as `keywords` a partir do título e do conteúdo, usando o mesmo processamento aplicado às perguntas.
- Se nenhum documento for encontrado, a pergunta segue para a IA normalmente, sem contexto adicional.
- Além do contexto, a IA recebe as **10 últimas mensagens** da conversa.

### Por que as stop words são removidas?

Palavras como "de", "para", "que", "o" e "em" aparecem em praticamente todos os textos e não indicam o assunto da pergunta. Se fossem usadas na busca, quase todos os documentos casariam com elas, produzindo resultados irrelevantes. Removê-las deixa apenas as palavras que identificam o tema (por exemplo, "encapsulamento" e "java"), o que torna a busca mais precisa e mais rápida. A lista usada está na classe `TextoProcessador`.

### Base de conhecimento

Os documentos ficam na coleção `documents`. Nesta versão, eles são cadastrados por código (`RAGService.cadastrarDocumento`), ou diretamente no Atlas. Para testar o RAG, foram cadastrados documentos com informações fictícias (por exemplo, o prazo de devolução da "Biblioteca Aurora"), que a IA não teria como saber sem o contexto recuperado.

## 9. Execução

### Pelo IntelliJ

1. Abra a pasta do projeto (`File > Open`) e aguarde o Maven baixar as dependências (*Reload All Maven Projects*, se necessário).
2. Configure as variáveis de ambiente, conforme a seção 6.2.
3. Execute a classe **`br.com.projeto.Launcher`**. Não execute a `Main` diretamente: uma classe que estende `Application` não inicia pelo classpath sem os módulos do JavaFX.

### Testes unitários

```
mvn test
```

Executa os testes do `TextoProcessador` (normalização, remoção de stop words, palavras duplicadas e texto vazio).

## 10. Instalação

> **A COMPLETAR após gerar e testar o instalador com o jpackage.**

1. Execute o instalador **[NOME-DO-INSTALADOR].exe**.
2. Siga o assistente. O instalador cria atalho na área de trabalho e entrada no menu Iniciar.
3. Defina as variáveis `MONGODB_URI` e `GROQ_API_KEY` no Windows (seção 6.3).
4. Abra o **Chat IA** pelo atalho ou pelo menu Iniciar.

Comando usado para gerar o instalador:

```
[COMANDO DO JPACKAGE]
```

## 11. Segurança

- **Senhas:** armazenadas apenas como hash **BCrypt** (custo 12, com salt automático). Não existe campo nem rotina que guarde ou recupere a senha em texto puro. A senha mínima é de 8 caracteres.
- **Login:** a mensagem de erro é a mesma para e-mail inexistente e senha incorreta ("E-mail ou senha inválidos."), para não revelar quais e-mails estão cadastrados.
- **API Keys e credenciais:** a chave da Groq e a URI do MongoDB são lidas de variáveis de ambiente. Nenhuma credencial é versionada: o `.gitignore` exclui `.idea/`, `target/` e `.env`.
- **Sessão:** o usuário autenticado fica guardado em memória (`SessionManagerService`) enquanto a aplicação está aberta. O logout encerra a sessão e volta ao login, permitindo que outro usuário entre depois.
- **Isolamento de dados:** o dono de toda conversa vem **da sessão**, nunca de dados enviados pela interface. Todas as consultas, alterações e exclusões de conversas filtram por `ownerId` e as de mensagens por `userId`, direto no banco. Um usuário que tente acessar a conversa de outro recebe a mesma mensagem de "Conversa não encontrada", sem revelar que ela existe.
- **Recuperação de senha:** em vez de exibir a senha, o sistema gera um código aleatório (`SecureRandom`, 256 bits). O banco guarda só o hash SHA-256 do código, que expira em 15 minutos, vale uma única vez e é invalidado quando outro código é solicitado. A resposta ao pedido é neutra, exista o e-mail ou não.
- **Validação de entrada:** campos obrigatórios, formato de e-mail, tamanho mínimo da senha, confirmação da senha, e-mail duplicado (verificação e índice único no banco), título de conversa e tamanho máximo da mensagem (4000 caracteres).
- **Tratamento de erros:** erros técnicos (banco indisponível, sem internet, chave da Groq inválida ou ausente, limite de requisições, falha ao salvar) viram mensagens compreensíveis. O detalhe técnico fica apenas no console.
- **Conexão com o banco:** TLS, por meio da string `mongodb+srv://` do Atlas.

## 12. Limitações

- A **recuperação de senha não envia e-mail**: o código é exibido na própria tela (caixa de demonstração). Em um sistema real, ele seria enviado por e-mail (por exemplo, via SMTP).
- O acesso de rede do Atlas está aberto a qualquer IP (`0.0.0.0/0`) para facilitar o desenvolvimento e a instalação em outros computadores. Em produção, deve ser restrito aos IPs necessários.
- O RAG usa **palavras-chave exatas**: não reconhece sinônimos, plurais ou variações da palavra, e não usa embeddings.
- A base de conhecimento não tem tela de cadastro: os documentos são inseridos por código ou pelo Atlas.
- A sessão existe só em memória: ao fechar a aplicação, é preciso fazer login de novo.
- As respostas da IA são exibidas como texto puro. Marcações Markdown (como `**negrito**`) aparecem sem formatação.
- Se a IA falhar, a pergunta continua salva no histórico, sem resposta.
- A IA recebe apenas as 10 últimas mensagens de cada conversa.
- A escolha do tema é guardada por computador (preferências do Java), e não por usuário.
- O título de uma conversa nova é "Nova conversa" até ser renomeado pelo usuário.
- As credenciais precisam ser configuradas manualmente em cada computador onde a aplicação for instalada.
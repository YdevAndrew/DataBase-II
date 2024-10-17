PixelArt To-Do List


📋 Descrição
O PixelArt To-Do List é uma aplicação interativa desenvolvida em Java com JavaFX que permite organizar suas criações de PixelArt em um sistema de tarefas. As tarefas são categorizadas como "To-Do", "In Progress", e "Done", e exibidas em um Kanban board interativo. Animações divertidas de personagens como Snorlax e Saitama ilustram o progresso das suas criações.

Os dados são armazenados no banco de dados MongoDB usando MongoDB Atlas. Para garantir a segurança, os dados sensíveis (como nomes e status das PixelArts) são protegidos com criptografia AES.

🚀 Funcionalidades
Adicionar, Editar e Remover PixelArts: Crie e gerencie suas PixelArts diretamente na lista de tarefas.
Kanban Board: Organize suas PixelArts em um quadro Kanban, com status "To-Do", "In Progress", "Done".
Criptografia AES: As informações sensíveis são criptografadas ao serem salvas no MongoDB.
Customizações Visuais: Fontes personalizadas e cursores customizados para melhorar a experiência visual.

🛠️ Tecnologias Utilizadas
Java 8+
JavaFX
MongoDB Atlas
Criptografia AES
Maven (para gerenciamento de dependências)

📋 Pré-requisitos
Antes de rodar o projeto, certifique-se de ter o seguinte instalado:

Java 8+
Maven
Uma conta no MongoDB Atlas com uma instância criada

🔧 Como rodar o projeto
1. Clonar o repositório
   bash
   Copiar código
   git clone https://gitlab.com/andrehugo201203/bd-ii/-/tree/master?ref_type=heads
   cd pixelart-todo-list

2. Configurar o MongoDB Atlas
   Crie um cluster no MongoDB Atlas.
   Configure a string de conexão do MongoDB Atlas no código (ou preferencialmente, usando variáveis de ambiente).
   Nota: Certifique-se de armazenar suas credenciais de conexão de forma segura, por exemplo, usando variáveis de ambiente.

3. Rodar o projeto
   Compile e execute o projeto usando Maven:


mvn clean javafx:run
Isso irá abrir a interface gráfica da aplicação PixelArt To-Do List.

📁 Estrutura do Projeto

├── src
│   ├── main
│   │   ├── java
│   │   │   ├── Controller
│   │   │   │   └── PixelArtController.java    # Lógica para conexão e operações no MongoDB
│   │   │   ├── Main
│   │   │   │   ├── MainApp.java               # Interface gráfica da aplicação
│   │   │   │   └── PixelArt.java              # Modelo da PixelArt com nome, status e datas
│   │   ├── resources
│   │   │   ├── images                         # Imagens de background, gifs e cursores
│   │   │   ├── fonts                          # Fontes personalizadas
│   │   │   └── style.css                      # Estilos customizados
│   └── test                                   # Testes unitários (futuros)
├── pom.xml                                    # Configuração do Maven
└── README.md

# Documentação do projeto
🗂️ Explicação dos Arquivos Principais
MainApp.java: Gerencia a interface gráfica, layouts, botões e interações de arrastar e soltar (Drag & Drop).
PixelArt.java: Modelo de dados que define uma PixelArt com nome, status, data de início e data de término.
PixelArtController.java: Responsável pela conexão com o banco de dados MongoDB e pelas operações CRUD (Create, Read, Update, Delete). Implementa também a criptografia AES para proteger dados sensíveis.

🔒 Criptografia AES
O projeto implementa a criptografia AES para proteger os dados sensíveis (como o nome e o status das PixelArts) armazenados no MongoDB.

Como Funciona:
Criptografia: Antes de salvar os dados no MongoDB, os campos sensíveis são criptografados com a chave AES.
Descriptografia: Ao buscar os dados do MongoDB, esses campos são descriptografados para serem exibidos corretamente na interface.
Exemplo de Código de Criptografia:

private String encrypt(String data) throws Exception {
Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
SecretKey secretKey = new SecretKeySpec(encryptionKey, ENCRYPTION_ALGORITHM);
cipher.init(Cipher.ENCRYPT_MODE, secretKey);
byte[] encryptedBytes = cipher.doFinal(data.getBytes());
return Base64.getEncoder().encodeToString(encryptedBytes);
}

private String decrypt(String encryptedData) throws Exception {
Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
SecretKey secretKey = new SecretKeySpec(encryptionKey, ENCRYPTION_ALGORITHM);
cipher.init(Cipher.DECRYPT_MODE, secretKey);
byte[] decryptedBytes = Base64.getDecoder().decode(encryptedData);
return new String(cipher.doFinal(decryptedBytes));
}

🛠️ Melhorias Futuras
Testes Unitários: Adicionar testes unitários para validar a integridade das operações do banco de dados e da criptografia.
Autenticação de Usuários: Implementar um sistema de login para usuários.
Melhorias Visuais: Tornar o layout mais responsivo e adicionar novas animações e interações visuais.
Rotação de Chave de Criptografia: Implementar um sistema de rotação de chaves AES para garantir segurança de longo prazo.

🤝 Contribuindo
Sinta-se à vontade para contribuir com o projeto! Para contribuir:

Faça um fork do repositório.
Crie uma branch para sua feature (git checkout -b minha-nova-feature).
Commit suas mudanças (git commit -am 'Adiciona nova feature').
Faça push para a branch (git push origin minha-nova-feature).
Crie um Pull Request.
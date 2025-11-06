# ☕ Grão da Villa - Aplicativo de Cafeteria

O **Grão da Villa** é um aplicativo Android desenvolvido para simular o funcionamento de uma cafeteria moderna.  
O projeto demonstra o fluxo completo de **cadastro, login, navegação por produtos, adição ao carrinho e finalização de pedidos**, com integração ao **Firebase**.

---

## 🚀 Funcionalidades Principais

### Autenticação
- Registro de novos usuários com **Firebase Authentication**.  
- Login seguro com **email e senha**.  
- Login com **Google** totalmente integrado.  

### Catálogo de Produtos
- Exibição de **bebidas quentes, geladas, salgados e doces** com:
  - Nome do produto  
  - Descrição detalhada  
  - Imagem ilustrativa  
  - Preço  
- Adição de produtos ao carrinho com quantidade personalizada.

### Carrinho de Compras
- Visualização dos produtos adicionados.  
- Atualização automática do **valor total** do pedido.  
- Opção de remover produtos ou alterar quantidades.  

### Finalização do Pedido
- Seleção da **forma de pagamento**: Pix, crédito, débito ou dinheiro.  
- Inserção do **número da mesa** antes de confirmar o pedido.  
- Armazenamento de pedidos no **Firebase Realtime Database**.  

### Histórico de Pedidos
- Histórico completo por usuário.  
- Visualização de pedidos anteriores com **data, valor e status**.  

### Área de Administrador
- Administradores podem **gerenciar o catálogo**:
  - Adicionar novos produtos  
  - Editar informações de produtos existentes  
  - Excluir produtos do catálogo  
- Controle completo sobre bebidas quentes, geladas, salgados e doces, incluindo nome, descrição, preço e imagem.  
- Permissões restritas apenas a usuários com nível de administrador.

---

## 🧠 Tecnologias Utilizadas

| Tecnologia | Uso |
|------------|-----|
| **Java (Android)** | Lógica e estrutura principal do app |
| **Firebase Authentication** | Login e registro de usuários |
| **Firebase Realtime Database** | Armazenamento de pedidos |
| **Firebase Firestore** | Armazenamento de dados de usuários e catálogo de produtos |
| **Cloudinary** | Armazenamento e gerenciamento de imagens de produtos |
| **RecyclerView + Adapter** | Exibição dos produtos e pedidos |
| **Material Design Components** | Layout moderno e responsivo |
| **Google Sign-In** | Login com conta Google |

---

## ⚙️ Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/giualbuq/GraoDaVilla.git
2. Abra o projeto no Android Studio.
3. Configure o Firebase:
- Crie um projeto no Firebase Console
- Adicione o app Android e insira o google-services.json na pasta app/.
- Ative Authentication (Email/Password) e Realtime Database.
4. Compile e execute no emulador ou dispositivo físico.


### 👨‍💻 Desenvolvido por
- Amanda Silva Soares - [MandaSoares](https://github.com/MandaSoares)
- Giulia Albuquerque - [Giualbuq](https://github.com/giualbuq)
- Marisol Marques - [Mmarques04](https://github.com/mmarques04)
- Nathan Tanzi - [Ntanzi07](https://github.com/Ntanzi07)


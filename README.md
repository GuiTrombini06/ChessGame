# ♟️ ChessGame

**ChessGame** é um projeto de desenvolvimento de um jogo de xadrez, implementado em **Java**. A aplicação utiliza a biblioteca Swing para sua interface de usuário, proporcionando uma experiência completa para partidas de dois jogadores locais. Os recursos incluem a visualização de movimentos, um registro do histórico de jogadas e a aplicação das regras padrão do xadrez.

---

## 🚀 Principais Funcionalidades
- 📌 **Tabuleiro interativo**: Interface gráfica que exibe o tabuleiro 8x8, com peças representadas por imagens.  
- ♟️ **Movimentação de peças**: Permite selecionar e mover peças conforme as regras do xadrez.  
- ✅ **Validação de movimentos**: Apenas movimentos legais são permitidos, incluindo captura, xeque e xeque-mate.  
- 📝 **Histórico de jogadas**: Área dedicada para exibir todos os lances realizados na partida.  
- 🎨 **Temas de cores**: Possibilidade de alternar entre diferentes temas visuais para o tabuleiro.  
- 🔦 **Destaques visuais**: Casas de seleção, movimentos possíveis e último lance são destacados para facilitar a visualização.  
- 🔄 **Controle de turno**: Indicação de qual jogador deve jogar (Vermelhas ou Amarelas).  
- 🆕 **Novo jogo**: Opção para reiniciar a partida a qualquer momento.  
- 🤖 **Inteligência Artificial (IA)** com **3 níveis de dificuldade**: fácil, médio e difícil.  

---

## 📦 Estrutura do Projeto


src/view/ChessGUI.java        # Interface gráfica principal do jogo

src/controller/Game.java      # Lógica central do jogo, controle de regras e estado

src/model/board/Board.java    # Representação do tabuleiro e manipulação de peças

src/model/pieces/             # Classes das peças (King, Queen, Rook, Bishop, Knight, Pawn)

resources/                    # Imagens das peças utilizadas na interface

out/                          # Arquivos compilados (.class)

README.md                     # Documentação do projeto

---

## ▶️ Como Executar

No terminal *PowerShell*, execute os comandos:

powershell
# Compilar todos os arquivos Java
Remove-Item -Recurse -Force .\out -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force .\out | Out-Null

$files = Get-ChildItem -Recurse -Path .\src -Filter *.java | ForEach-Object FullName

javac -Xlint:all -encoding UTF-8 -d out $files

# Executar o jogo
java -cp "out;resources" view.ChessGUI
---

## 🔮 Melhorias Futuras

* Implementação de partidas **online**
* Animações e efeitos visuais
* Internacionalização da interface (suporte a múltiplos idiomas)
* Testes automatizados para maior robustez

---

## 👨‍🎓 Autor

Desenvolvido por **Guilherme Trombini de Castro**
📚 Projeto acadêmico – Faculdade Unicesumar
Sinta-se à vontade para contribuir ou sugerir melhorias! ✨


---

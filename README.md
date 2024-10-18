
# PD-Splitwise

**Descrição do Projeto**:  
Este projeto tem como objetivo criar um sistema distribuído para o registo e gestão de despesas partilhadas, inspirado na aplicação Splitwise. Utiliza Java, com um servidor principal, servidores de backup, clientes e uma base de dados SQLite.

## Equipa de Desenvolvimento

- **Afonso da Silva**
- **Frederico Quelhas**
- **Eduardo**

## Tecnologias Utilizadas

- **Java** (versão 21)
- **SQLite** para a gestão da base de dados
- **GitHub** para controle de versão e colaboração
- **IntelliJ IDEA** como IDE

## Funcionalidades

- Registo de despesas partilhadas entre utilizadores.
- Gestão de grupos para partilha de despesas.
- Sincronização de dados entre servidor principal e servidores de backup.
- Interface intuitiva para criação e visualização de despesas.
- Armazenamento seguro das informações utilizando SQLite.

## Instalação

1. Clone o repositório do GitLab:
   ```bash
   git clone https://gitlab.com/link-para-o-repositorio.git
   ```
2. Certifique-se de que o Java 21 está instalado e configurado:
   ```bash
   java -version
   ```
3. Abra o projeto no IntelliJ IDEA.
4. Configure a base de dados SQLite, criando as tabelas necessárias.
5. Execute a aplicação.

## Estrutura do Projeto

- **/src**: Código-fonte do projeto.
- **/db**: Scripts de inicialização da base de dados SQLite.
- **/docs**: Documentação e diagramas do projeto.
- **/tests**: Testes unitários e de integração.

## Como Utilizar

1. Registe-se no sistema e crie um grupo.
2. Adicione despesas ao grupo, especificando os membros e os valores.
3. Visualize as despesas partilhadas e os saldos pendentes.
4. Sincronize os dados com os servidores de backup para garantir segurança.

## Licença

Este projeto está licenciado sob a Licença MIT - veja o ficheiro [LICENSE](LICENSE) para mais detalhes.

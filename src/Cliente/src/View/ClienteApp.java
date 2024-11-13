package Cliente.src.View;

import Cliente.src.Controller.ClienteService;

import java.util.Scanner;

public class ClienteApp {
    private ClienteService clienteService; // Classe que gerencia a comunicação com o servidor

    public ClienteApp(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Registrar-se");
            System.out.println("2. Login");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir o newline

            switch (opcao) {
                case 1:
                    registrar(scanner);
                    break;
                case 2:
                    login(scanner);
                    break;
                case 3:
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void registrar(Scanner scanner) {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        // Enviar dados para o servidor
        if (clienteService.registrar(nome, telefone, email, senha)) {
            System.out.println("Registro bem-sucedido!");
        } else {
            System.out.println("Erro ao registrar.");
        }
    }

    private void login(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        // Enviar dados para o servidor
        if (clienteService.login(email, senha)) {
            System.out.println("Login bem-sucedido!");
            // Redirecionar para menu de operações autenticadas
            menuAutenticado(scanner);
        } else {
            System.out.println("Email ou senha incorretos.");
        }
    }

    private void menuAutenticado(Scanner scanner) {
        // Implementação das operações após o login
    }
}

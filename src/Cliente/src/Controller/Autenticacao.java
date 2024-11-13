package Cliente.src.Controller;

import Cliente.src.Entidades.Utilizador;
import Cliente.src.recursos.Comunicacao;

import java.util.Scanner;

public class Autenticacao {
//    private Cliente cliente;
//
//    public Autenticacao(Cliente cliente) {
//        this.cliente = cliente;
//    }
//
//    public void iniciarAutenticacao() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Digite seu email: ");
//        String email = scanner.nextLine();
//        System.out.print("Digite sua senha: ");
//        String senha = scanner.nextLine();
//
//        Utilizador utilizador = new Utilizador(email, senha);
//        Comunicacao requisicao = new Comunicacao(Comunicacao.Comando.AUTENTICACAO, utilizador);
//
//        Comunicacao resposta = cliente.enviarRequisicao(requisicao);
//        if (resposta.getMensagemServidor().equals("Autenticado com sucesso")) {
//            System.out.println("Autenticado!");
//            // Continuar para o menu principal
//        } else {
//            System.out.println("Falha na autenticação.");
//        }
//    }
//
//    public void iniciarRegistro() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Digite seu nome: ");
//        String nome = scanner.nextLine();
//        System.out.print("Digite seu telefone: ");
//        String telefone = scanner.nextLine();
//        System.out.print("Digite seu email: ");
//        String email = scanner.nextLine();
//        System.out.print("Digite sua senha: ");
//        String senha = scanner.nextLine();
//
//        Utilizador novoUtilizador = new Utilizador(nome, telefone, email, senha);
//        Comunicacao requisicao = new Comunicacao(Comunicacao.Comando.REGISTRO, novoUtilizador);
//
//        Comunicacao resposta = cliente.enviarRequisicao(requisicao);
//        System.out.println(resposta.getMensagemServidor());
//    }
}


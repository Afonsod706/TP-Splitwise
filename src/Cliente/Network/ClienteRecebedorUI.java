package Cliente.Network;

import Cliente.Controller.Comandos;
import Cliente.Controller.Comunicacao;
import Cliente.View.UI.Controller.AppController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClienteRecebedorUI implements Runnable {
    private final ObjectInputStream in;
    private final AppController appController;
    private final AtomicBoolean running;

    public ClienteRecebedorUI(ObjectInputStream in, AppController appController, AtomicBoolean running) {
        this.in = in;
        this.appController = appController;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                // Recebe comunicação do servidor
                Comunicacao comunicacao = (Comunicacao) in.readObject();
                System.out.println("Comando recebido (UI): " + comunicacao.getComando());

                if (comunicacao != null) {
                    //appController.mostrarNotificacao("Resposta do servidor: " + comunicacao.getResposta());
                    processarComunicacao(comunicacao);
                    appController.atualizarDashboard();
                }

                if (comunicacao.getComando() == Comandos.SAIR) {
                    running.set(false); // Finaliza a execução ao receber encerramento do servidor
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Conexão perdida com o servidor (UI): " + e.getMessage());
        } finally {
            System.out.println("Encerrando thread de recepção (UI).");
            appController.finalizarAplicacao(); // Solicita que a aplicação seja encerrada
        }
    }

    private void processarComunicacao(Comunicacao comunicacao) {
        String resposta = comunicacao.getResposta(); // Mensagem do servidor
        switch (comunicacao.getComando()) {
            case LOGIN:
                if (comunicacao.getAutenticado()) {
                    appController.atualizarComunicacao(comunicacao);
                    appController.mostrarMenuPrincipal();
                } else {
                    appController.mostrarErro("Erro de Login", resposta);
                }
                break;

            case REGISTRAR:
                if (comunicacao.getAutenticado()) {
                    appController.atualizarComunicacao(comunicacao);
                    appController.mostrarMenuPrincipal();
                } else {
                    appController.mostrarErro("Erro de Registro", resposta);
                }
                break;
            case EDITAR_DADOS:
                appController.atualizarComunicacao(comunicacao);
                appController.atualizarDashboard();
                break;

            case LISTAR_GRUPOS,CRIAR_GRUPO,SELECIONAR_GRUPO,SAIR_GRUPO,EDITAR_GRUPO,VISUALIZAR_SALDOS_GRUPO:
                appController.mostrarMensagemNoPainelGRUPO(resposta);
                appController.atualizarComunicacao(comunicacao);
                appController.atualizarDashboard();
                break;

            case INSERIR_DESPESA,EDITAR_DESPESA,ELIMINAR_DESPESA,EXPORTAR_DESPESAS_CSV,VISUALIZAR_HISTORICO_DESPESAS:
                appController.mostrarMensagemNoPainelDESPESA( resposta);
                appController.atualizarDashboard();
                break;

            case ELIMINAR_PAGAMENTO,INSERIR_PAGAMENTO,LISTAR_PAGAMENTOS:
                appController.mostrarMensagemNoPainelPAGAMENTO(resposta);
                appController.atualizarDashboard();
                break;

            case CRIAR_CONVITE,RESPONDER_CONVITE,VISUALIZAR_CONVITES:
                appController.mostrarMensagemNoPainelCONVITE( resposta);
                appController.atualizarDashboard();
                break;

            case NOTIFICACAO:
                appController.mostrarNotificacao(resposta);
                appController.atualizarDashboard();
                break;
            case LOGOUT:
                appController.mostrarNotificacao(resposta);
                appController.resetarEstado(); // Limpa o estado após logout
                appController.mostrarLogin();

            case SAIR:
                appController.finalizarAplicacao();
                break;

            default:
                appController.mostrarNotificacao("Comando desconhecido: " + comunicacao.getComando());
                break;
        }
    }
}

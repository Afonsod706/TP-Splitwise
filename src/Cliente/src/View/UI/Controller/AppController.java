package Cliente.src.View.UI.Controller;

import Cliente.src.Controller.Comandos;
import Cliente.src.Controller.Comunicacao;
import Cliente.src.Entidades.*;
import Cliente.src.Network.ClienteComunicacao;
import Cliente.src.View.UI.View.MenuBoard.DashboardView;
import Cliente.src.View.UI.View.MenuBoard.LoginView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class AppController {
    private final Stage stage;
    private final ClienteComunicacao clienteComunicacao;
    private Comunicacao comunicacao;
    private DashboardView dashboardView;
    private BorderPane root; // Layout principal

    public AppController(Stage stage, ClienteComunicacao comunicacao) {
        this.stage = stage;
        this.clienteComunicacao = comunicacao;
        this.comunicacao = new Comunicacao();
        this.root = new BorderPane(); // Inicializa o layout principal
    }

    // Redireciona para a tela de menu principal
    public void mostrarMenuPrincipal() {
        Platform.runLater(() -> {
            dashboardView = new DashboardView(stage, clienteComunicacao, this);
            root.setCenter(dashboardView.getView()); // Atualiza a área central do layout principal
            stage.setScene(new javafx.scene.Scene(root, 900, 600)); // Configura a cena principal
            stage.show();
        });

    }

    public BorderPane getRoot() {
        return root;
    }

    // Redireciona para a tela de login
    public void mostrarLogin() {
        Platform.runLater(() -> new LoginView(stage, clienteComunicacao));
    }

    // Exibe um alerta de erro
    public void mostrarErro(String titulo, String mensagem) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle(titulo);
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        });
    }

    public void mostrarAviso(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    // Mostra uma notificação ao usuário
    public void mostrarNotificacao(String mensagem) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Notificação");
            alerta.setHeaderText(null);
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        });
    }


    // Finaliza a aplicação
    public void finalizarAplicacao() {
        Platform.runLater(() -> {
            System.out.println("Encerrando aplicação...");
            stage.close();
        });
    }


    public void atualizarComunicacao(Comunicacao novaComunicacao) {
        if (novaComunicacao == null) return;

        // Atualizar apenas os campos relevantes
        if (novaComunicacao.getUtilizador() != null) {
            this.comunicacao.setUtilizador(novaComunicacao.getUtilizador());
            System.out.println("Atualizando dados do utilizador: " + novaComunicacao.getUtilizador().getNome());
        }

        if (novaComunicacao.getGrupo() != null) {
            this.comunicacao.setGrupo(novaComunicacao.getGrupo());
            System.out.println("Grupo atualizado: " + novaComunicacao.getGrupo().getNome());
        }

        if (novaComunicacao.getGrupos() != null && !novaComunicacao.getGrupos().isEmpty()) {
            this.comunicacao.setGrupos(novaComunicacao.getGrupos());
            System.out.println("Lista de grupos atualizada. Total de grupos: " + novaComunicacao.getGrupos().size());
        }

        if (novaComunicacao.getUtilizadorGrupos() != null && !novaComunicacao.getUtilizadorGrupos().isEmpty()) {
            this.comunicacao.setUtilizadorGrupos(novaComunicacao.getUtilizadorGrupos());
            System.out.println("Lista de membros do grupo atualizada.");
        }

        if (novaComunicacao.getResposta() != null && !novaComunicacao.getResposta().isEmpty()) {
            this.comunicacao.setResposta(novaComunicacao.getResposta());
            System.out.println("Resposta do servidor atualizada: " + novaComunicacao.getResposta());
        }

        // Preserve outros campos, como despesas, convites e pagamentos, de forma similar
        if (novaComunicacao.getDespesa() != null) {
            this.comunicacao.setDespesa(novaComunicacao.getDespesa());
            System.out.println("Dados de despesa atualizados.");
        }

        if (novaComunicacao.getConvite() != null) {
            this.comunicacao.setConvite(novaComunicacao.getConvite());
            System.out.println("Dados de convite atualizados.");
        }

        if (novaComunicacao.getPagamento() != null) {
            this.comunicacao.setPagamento(novaComunicacao.getPagamento());
            System.out.println("Dados de pagamento atualizados.");
        }
    }


    public Comunicacao getComunicacao() {
        return comunicacao;
    }

    public void setComunicacao(Comunicacao comunicacao) {
        this.comunicacao = comunicacao;
    }

    public void setDashboardView(DashboardView dashboardView) {
        this.dashboardView = dashboardView;
    }

    public void atualizarDashboard() {
        Platform.runLater(() -> {
            if (dashboardView != null) {
                dashboardView.atualizarDados(); // Atualiza os dados do painel ativo
            } else {
                System.err.println("DashboardView não está inicializada.");
            }
        });
    }


    public Object getDashboardView() {
        return dashboardView;
    }


    public void solicitarDadosGruposUI() {
        if (comunicacao != null && comunicacao.getUtilizador() != null) {
            Comunicacao solicitacao = new Comunicacao();
            solicitacao.setComando(Comandos.GRUPOS_UI);
            solicitacao.getUtilizador().setId(comunicacao.getUtilizador().getId());
            clienteComunicacao.enviarMensagem(solicitacao);
        } else {
            System.err.println("Erro: Utilizador não autenticado ou comunicação não inicializada.");
        }
    }

    public void solicitarDadosGrupoEspecifico(Grupo grupoSelecionado) {
        Comunicacao solicitacao = new Comunicacao();
        solicitacao.setComando(Comandos.GRUPOS_UI);
        solicitacao.setGrupo(grupoSelecionado);
        clienteComunicacao.enviarMensagem(solicitacao);
    }


    public void solicitarCriacaoGrupo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Grupo");
        dialog.setHeaderText("Digite o nome do novo grupo:");
        dialog.setContentText("Nome do Grupo:");

        dialog.showAndWait().ifPresent(nomeGrupo -> {
            if (!nomeGrupo.isEmpty()) {
                Comunicacao solicitacao = new Comunicacao();
                solicitacao.setComando(Comandos.CRIAR_GRUPO);
                Grupo novoGrupo = new Grupo(nomeGrupo, comunicacao.getUtilizador().getId());
                solicitacao.setGrupo(novoGrupo);

                try {
                    clienteComunicacao.enviarMensagem(solicitacao);
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível enviar a solicitação de criação do grupo: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O nome do grupo não pode ser vazio.");
            }
        });
    }

    public void solicitarListagemGrupos() {
        Comunicacao solicitacao = new Comunicacao();
        solicitacao.setComando(Comandos.LISTAR_GRUPOS);
        try {
            clienteComunicacao.enviarMensagem(solicitacao);
        } catch (Exception e) {
            mostrarErro("Erro", "Não foi possível solicitar a lista de grupos: " + e.getMessage());
        }
    }
/// GRUPOS
    public void solicitarSelecaoGrupo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Selecionar Grupo");
        dialog.setHeaderText("Digite o nome do grupo a ser selecionado:");
        dialog.setContentText("Nome do Grupo:");

        dialog.showAndWait().ifPresent(nomeGrupo -> {
            if (!nomeGrupo.isEmpty()) {
                try {
                    Comunicacao solicitacao = new Comunicacao();
                    solicitacao.setComando(Comandos.SELECIONAR_GRUPO);
                    Grupo grupo = new Grupo(nomeGrupo); // Cria o grupo com o nome fornecido
                    solicitacao.setGrupo(grupo);

                    clienteComunicacao.enviarMensagem(solicitacao);
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível enviar a solicitação de seleção do grupo: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O nome do grupo não pode estar vazio.");
            }
        });
    }

    public void solicitarEdicaoGrupo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Editar Nome do Grupo");
        dialog.setHeaderText("Digite o novo nome do grupo:");
        dialog.setContentText("Novo Nome:");

        dialog.showAndWait().ifPresent(novoNome -> {
            if (!novoNome.trim().isEmpty()) {
                try {
                    // Criação da solicitação para editar o grupo
                    Comunicacao solicitacao = new Comunicacao();
                    solicitacao.setComando(Comandos.EDITAR_GRUPO);

                    // Define o novo nome do grupo na solicitação
                    Grupo grupo = new Grupo(novoNome.trim());

                    solicitacao.setGrupo(grupo);

                    // Envia a solicitação ao servidor
                    clienteComunicacao.enviarMensagem(solicitacao);
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível enviar a solicitação de edição do grupo: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O novo nome do grupo não pode estar vazio.");
            }
        });
    }

    public void solicitarSaidaGrupo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sair do Grupo");
        dialog.setHeaderText("Digite o nome do grupo que deseja sair:");
        dialog.setContentText("Nome do Grupo:");

        dialog.showAndWait().ifPresent(nomeGrupo -> {
            if (!nomeGrupo.isEmpty()) {
                try {
                    Comunicacao solicitacao = new Comunicacao();
                    solicitacao.setComando(Comandos.SAIR_GRUPO);
                    Grupo grupo = new Grupo(nomeGrupo); // Cria o grupo com o nome fornecido
                    solicitacao.setGrupo(grupo);

                    clienteComunicacao.enviarMensagem(solicitacao);
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível enviar a solicitação para sair do grupo: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O nome do grupo não pode estar vazio.");
            }
        });
    }

    public void solicitarEliminacaoGrupo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Eliminar Grupo");
        dialog.setHeaderText("Digite o nome do grupo que deseja eliminar:");
        dialog.setContentText("Nome do Grupo:");

        dialog.showAndWait().ifPresent(nomeGrupo -> {
            if (!nomeGrupo.isEmpty()) {
                try {
                    Comunicacao solicitacao = new Comunicacao();
                    solicitacao.setComando(Comandos.ELIMINAR_GRUPO);
                    Grupo grupo = new Grupo(nomeGrupo); // Cria o grupo com o nome fornecido
                    solicitacao.setGrupo(grupo);

                    clienteComunicacao.enviarMensagem(solicitacao);
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível enviar a solicitação de eliminação do grupo: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O nome do grupo não pode estar vazio.");
            }
        });
    }

    public void solicitarVisualizacaoSaldosGrupo() {
        try {
            Comunicacao solicitacao = new Comunicacao();
            solicitacao.setComando(Comandos.VISUALIZAR_SALDOS_GRUPO);
            Grupo grupoAtual = getComunicacao().getGrupo();

            if (grupoAtual != null) {
                solicitacao.setGrupo(grupoAtual);
                clienteComunicacao.enviarMensagem(solicitacao);
            } else {
                mostrarAviso("Grupo não selecionado", "Por favor, selecione um grupo antes de visualizar os saldos.");
            }
        } catch (Exception e) {
            mostrarErro("Erro", "Não foi possível enviar a solicitação de visualização dos saldos: " + e.getMessage());
        }
    }

    public void mostrarMensagemNoPainelGRUPO(String mensagem) {
        Platform.runLater(() -> {
            if (dashboardView != null && dashboardView.getGrupoUI() != null) {
                dashboardView.getGrupoUI().adicionarMensagem(mensagem);
            } else {
                System.err.println("Erro: GrupoUI não inicializado para exibir mensagens.");
            }
        });
    }


    public void mostrarMensagemNoPainelDESPESA(String resposta) {
        Platform.runLater(() -> {
            if (dashboardView != null && dashboardView.getDespesaUI() != null) {
                // Adiciona a mensagem no painel de despesas
                dashboardView.getDespesaUI().adicionarMensagem(resposta);
            } else {
                System.err.println("Erro: DespesaUI não inicializado ou painel não ativo.");
            }
        });
    }


    public void mostrarMensagemNoPainelPAGAMENTO(String resposta) {
        Platform.runLater(() -> {
            if (dashboardView != null && dashboardView.getPagamentoUI() != null) {
                // Adiciona a mensagem no painel de pagamentos
                dashboardView.getPagamentoUI().adicionarMensagem(resposta);
            } else {
                System.err.println("Erro: PagamentoUI não inicializado ou painel não ativo.");
            }
        });
    }

    public void mostrarMensagemNoPainelCONVITE(String resposta) {
        Platform.runLater(() -> {
            if (dashboardView != null && dashboardView.getConviteUI() != null) {
                // Adiciona a mensagem no painel de convites
                dashboardView.getConviteUI().adicionarMensagem(resposta);
            } else {
                System.err.println("Erro: ConviteUI não inicializado ou painel não ativo.");
            }
        });
    }


/// DESPESA

    /// DESPESA

    public void solicitarVisualizarHistoricoDespesas() {
        try {
            if (comunicacao.getGrupo() == null) {
                mostrarAviso("Erro", "Nenhum grupo selecionado. Por favor, selecione um grupo para visualizar o histórico de despesas.");
                return;
            }

            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.VISUALIZAR_HISTORICO_DESPESAS);
            novoComunicacao.setGrupo(comunicacao.getGrupo()); // Define o grupo atual

            clienteComunicacao.enviarMensagem(novoComunicacao);
            //mostrarNotificacao("Solicitação para visualizar o histórico de despesas enviada com sucesso.");
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao enviar solicitação para visualizar o histórico de despesas: " + e.getMessage());
        }
    }

    public void solicitarExportarDespesas() {
        try {
            if (comunicacao.getGrupo() == null) {
                mostrarAviso("Erro", "Nenhum grupo selecionado. Por favor, selecione um grupo para exportar despesas.");
                return;
            }

            Comunicacao novoComunicacao = new Comunicacao();
            novoComunicacao.setComando(Comandos.EXPORTAR_DESPESAS_CSV);
            novoComunicacao.setGrupo(comunicacao.getGrupo()); // Define o grupo atual

            clienteComunicacao.enviarMensagem(novoComunicacao);
            //mostrarNotificacao("Solicitação para exportar despesas enviada com sucesso.");
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao enviar solicitação para exportar despesas: " + e.getMessage());
        }
    }

    public void solicitarEliminarDespesa() {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Eliminar Despesa");
            dialog.setHeaderText("Digite o ID da despesa que deseja eliminar:");
            dialog.setContentText("ID da Despesa:");

            dialog.showAndWait().ifPresent(idDespesaInput -> {
                try {
                    int idDespesa = Integer.parseInt(idDespesaInput);

                    Despesa despesa = new Despesa();
                    despesa.setId(idDespesa);

                    Comunicacao novoComunicacao = new Comunicacao();
                    novoComunicacao.setComando(Comandos.ELIMINAR_DESPESA);
                    novoComunicacao.setDespesa(despesa);

                    clienteComunicacao.enviarMensagem(novoComunicacao);
                    //mostrarNotificacao("Solicitação para eliminar despesa enviada com sucesso.");
                } catch (NumberFormatException e) {
                    mostrarErro("Erro", "ID inválido. Operação cancelada.");
                }
            });
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao solicitar eliminação de despesa: " + e.getMessage());
        }
    }

    public void solicitarEditarDespesa() {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Editar Despesa");
            dialog.setHeaderText("Digite o ID da despesa que deseja editar:");
            dialog.setContentText("ID da Despesa:");

            dialog.showAndWait().ifPresent(idDespesaInput -> {
                try {
                    int idDespesa = Integer.parseInt(idDespesaInput);

                    TextInputDialog descricaoDialog = new TextInputDialog();
                    descricaoDialog.setTitle("Editar Despesa");
                    descricaoDialog.setHeaderText("Digite a nova descrição da despesa:");
                    descricaoDialog.setContentText("Descrição:");

                    descricaoDialog.showAndWait().ifPresent(descricao -> {
                        TextInputDialog valorDialog = new TextInputDialog();
                        valorDialog.setTitle("Editar Despesa");
                        valorDialog.setHeaderText("Digite o novo valor da despesa:");
                        valorDialog.setContentText("Valor:");

                        valorDialog.showAndWait().ifPresent(valorInput -> {
                            try {
                                double valor = Double.parseDouble(valorInput);

                                Despesa despesa = new Despesa();
                                despesa.setId(idDespesa);
                                despesa.setDescricao(descricao);
                                despesa.setValor(valor);

                                Comunicacao novoComunicacao = new Comunicacao();
                                novoComunicacao.setComando(Comandos.EDITAR_DESPESA);
                                novoComunicacao.setDespesa(despesa);

                                clienteComunicacao.enviarMensagem(novoComunicacao);
                               // mostrarNotificacao("Solicitação para editar despesa enviada com sucesso.");
                            } catch (NumberFormatException e) {
                                mostrarErro("Erro", "Valor inválido. Operação cancelada.");
                            }
                        });
                    });
                } catch (NumberFormatException e) {
                    mostrarErro("Erro", "ID inválido. Operação cancelada.");
                }
            });
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao solicitar edição de despesa: " + e.getMessage());
        }
    }

    public void solicitarAdicionarDespesa() {
        try {
            TextInputDialog descricaoDialog = new TextInputDialog();
            descricaoDialog.setTitle("Adicionar Despesa");
            descricaoDialog.setHeaderText("Digite a descrição da despesa:");
            descricaoDialog.setContentText("Descrição:");

            descricaoDialog.showAndWait().ifPresent(descricao -> {
                TextInputDialog valorDialog = new TextInputDialog();
                valorDialog.setTitle("Adicionar Despesa");
                valorDialog.setHeaderText("Digite o valor da despesa:");
                valorDialog.setContentText("Valor:");

                valorDialog.showAndWait().ifPresent(valorInput -> {
                    try {
                        double valor = Double.parseDouble(valorInput);

                        TextInputDialog emailDialog = new TextInputDialog();
                        emailDialog.setTitle("Adicionar Despesa");
                        emailDialog.setHeaderText("Digite o email do pagador:");
                        emailDialog.setContentText("Email:");

                        emailDialog.showAndWait().ifPresent(emailPagador -> {
                            Despesa despesa = new Despesa();
                            despesa.setDescricao(descricao);
                            despesa.setValor(valor);
                            despesa.setEmailPagante(emailPagador);

                            Comunicacao novoComunicacao = new Comunicacao();
                            novoComunicacao.setComando(Comandos.INSERIR_DESPESA);
                            novoComunicacao.setDespesa(despesa);

                            clienteComunicacao.enviarMensagem(novoComunicacao);
                           // mostrarNotificacao("Solicitação para adicionar despesa enviada com sucesso.");
                        });
                    } catch (NumberFormatException e) {
                        mostrarErro("Erro", "Valor inválido. Operação cancelada.");
                    }
                });
            });
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao solicitar adição de despesa: " + e.getMessage());
        }
    }

/// CONVITE
public void solicitarAceitarConvite() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Aceitar Convite");
    dialog.setHeaderText("Digite o ID do convite que deseja aceitar:");
    dialog.setContentText("ID do Convite:");

    dialog.showAndWait().ifPresent(idConvite -> {
        try {
            int conviteId = Integer.parseInt(idConvite.trim());

            // Cria o objeto convite com a resposta "aceitar"
            Convite convite = new Convite();
            convite.setIdConvite(conviteId);
            convite.setEstado("aceitar");

            // Configura a comunicação
            Comunicacao solicitacao = new Comunicacao();
            solicitacao.setComando(Comandos.RESPONDER_CONVITE);
            solicitacao.setConvite(convite);

            // Envia ao servidor
            clienteComunicacao.enviarMensagem(solicitacao);
          //  mostrarNotificacao("Convite aceito com sucesso!");
        } catch (NumberFormatException e) {
            mostrarErro("Erro", "O ID do convite deve ser um número.");
        } catch (Exception e) {
            mostrarErro("Erro", "Não foi possível aceitar o convite: " + e.getMessage());
        }
    });
}

    public void solicitarRecusarConvite() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recusar Convite");
        dialog.setHeaderText("Digite o ID do convite que deseja recusar:");
        dialog.setContentText("ID do Convite:");

        dialog.showAndWait().ifPresent(idConvite -> {
            try {
                int conviteId = Integer.parseInt(idConvite.trim());

                // Cria o objeto convite com a resposta "recusar"
                Convite convite = new Convite();
                convite.setIdConvite(conviteId);
                convite.setEstado("recusar");

                // Configura a comunicação
                Comunicacao solicitacao = new Comunicacao();
                solicitacao.setComando(Comandos.RESPONDER_CONVITE);
                solicitacao.setConvite(convite);

                // Envia ao servidor
                clienteComunicacao.enviarMensagem(solicitacao);
               // mostrarNotificacao("Convite recusado com sucesso!");
            } catch (NumberFormatException e) {
                mostrarErro("Erro", "O ID do convite deve ser um número.");
            } catch (Exception e) {
                mostrarErro("Erro", "Não foi possível recusar o convite: " + e.getMessage());
            }
        });
    }

    public void solicitarListarConvites() {
        try {
            // Configura a comunicação
            Comunicacao solicitacao = new Comunicacao();
            solicitacao.setComando(Comandos.VISUALIZAR_CONVITES);

            // Envia ao servidor
            clienteComunicacao.enviarMensagem(solicitacao);
          //  mostrarNotificacao("Solicitação para listar convites enviada ao servidor.");
        } catch (Exception e) {
            mostrarErro("Erro", "Não foi possível listar os convites: " + e.getMessage());
        }
    }

    public void solicitarCriarConvite() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Convite");
        dialog.setHeaderText("Digite o email do utilizador que deseja convidar:");
        dialog.setContentText("Email:");

        dialog.showAndWait().ifPresent(email -> {
            if (!email.trim().isEmpty()) {
                try {
                    // Configura o convite
                    Convite convite = new Convite();
                    convite.setEmailConvidado(email.trim());

                    // Configura a comunicação
                    Comunicacao solicitacao = new Comunicacao();
                    solicitacao.setComando(Comandos.CRIAR_CONVITE);
                    solicitacao.setConvite(convite);

                    // Envia ao servidor
                    clienteComunicacao.enviarMensagem(solicitacao);
                //    mostrarNotificacao("Convite criado com sucesso!");
                } catch (Exception e) {
                    mostrarErro("Erro", "Não foi possível criar o convite: " + e.getMessage());
                }
            } else {
                mostrarErro("Erro", "O email do convidado não pode estar vazio.");
            }
        });
    }

    public void solicitarRegistrarPagamento() {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Registrar Pagamento");
        emailDialog.setHeaderText("Digite o email do recebedor:");
        emailDialog.setContentText("Email do Recebedor:");

        emailDialog.showAndWait().ifPresent(emailRecebedor -> {
            if (!emailRecebedor.trim().isEmpty()) {
                TextInputDialog despesaDialog = new TextInputDialog();
                despesaDialog.setTitle("Registrar Pagamento");
                despesaDialog.setHeaderText("Digite o ID da despesa:");
                despesaDialog.setContentText("ID da Despesa:");

                despesaDialog.showAndWait().ifPresent(idDespesaInput -> {
                    try {
                        int idDespesa = Integer.parseInt(idDespesaInput);

                        TextInputDialog valorDialog = new TextInputDialog();
                        valorDialog.setTitle("Registrar Pagamento");
                        valorDialog.setHeaderText("Digite o valor do pagamento:");
                        valorDialog.setContentText("Valor:");

                        valorDialog.showAndWait().ifPresent(valorInput -> {
                            try {
                                double valor = Double.parseDouble(valorInput);

                                // Configura os dados do pagamento
                                Pagamento pagamento = new Pagamento();
                                pagamento.setEmailRecebedor(emailRecebedor);
                                pagamento.setIdDespesa(idDespesa);
                                pagamento.setValor(valor);

                                // Cria a comunicação e envia
                                Comunicacao comunicacao = new Comunicacao();
                                comunicacao.setComando(Comandos.INSERIR_PAGAMENTO);
                                comunicacao.setPagamento(pagamento);

                                clienteComunicacao.enviarMensagem(comunicacao);
                     //           mostrarNotificacao("Solicitação de registro de pagamento enviada com sucesso.");
                            } catch (NumberFormatException e) {
                                mostrarErro("Erro", "Valor inválido. Operação cancelada.");
                            }
                        });
                    } catch (NumberFormatException e) {
                        mostrarErro("Erro", "ID de despesa inválido. Operação cancelada.");
                    }
                });
            } else {
                mostrarErro("Erro", "O email do recebedor não pode estar vazio.");
            }
        });
    }

    public void solicitarListarPagamentos() {
        try {
            // Configura a comunicação
            Comunicacao comunicacao = new Comunicacao();
            comunicacao.setComando(Comandos.LISTAR_PAGAMENTOS);

            // Envia ao servidor
            clienteComunicacao.enviarMensagem(comunicacao);
         //   mostrarNotificacao("Solicitação para listar pagamentos enviada ao servidor.");
        } catch (Exception e) {
            mostrarErro("Erro", "Erro ao enviar solicitação para listar pagamentos: " + e.getMessage());
        }
    }

    public void solicitarEliminarPagamento() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Eliminar Pagamento");
        dialog.setHeaderText("Digite o ID do pagamento que deseja eliminar:");
        dialog.setContentText("ID do Pagamento:");

        dialog.showAndWait().ifPresent(idPagamentoInput -> {
            try {
                int idPagamento = Integer.parseInt(idPagamentoInput);

                // Configura o pagamento
                Pagamento pagamento = new Pagamento();
                pagamento.setIdPagamento(idPagamento);

                // Configura a comunicação e envia
                Comunicacao comunicacao = new Comunicacao();
                comunicacao.setComando(Comandos.ELIMINAR_PAGAMENTO);
                comunicacao.setPagamento(pagamento);

                clienteComunicacao.enviarMensagem(comunicacao);
             //   mostrarNotificacao("Solicitação para eliminar pagamento enviada com sucesso.");
            } catch (NumberFormatException e) {
                mostrarErro("Erro", "ID inválido. Operação cancelada.");
            }
        });
    }

}

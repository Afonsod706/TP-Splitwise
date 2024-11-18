package Cliente.src.recursos;

public enum Comandos {
    REGISTRAR, LOGIN, SAIR, CRIAR_GRUPO, SELECIONAR_GRUPO,
    LISTAR_GRUPOS, SAIR_GRUPO, CRIAR_CONVITE,EDITAR_GRUPO ,ELIMINAR_GRUPO, LISTAR_CONVITES,
    ACEITAR_CONVITE, RECUSAR_CONVITE, INSERIR_DESPESA, TOTAL_GASTOS_GRUPO,
    HISTORICO_DESPESAS, EXPORTAR_DESPESAS, EDITAR_DESPESA, ELIMINAR_DESPESA,
    INSERIR_PAGAMENTO, LISTAR_PAGAMENTOS, ELIMINAR_PAGAMENTO,
    SALDOS_GRUPO, INFORMACOES, EDITAR_DADOS, LOGOUT;

    public static Comandos fromString(String comando) {
        try {
            return Comandos.valueOf(comando.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null; // Retorna null para comandos inválidos
        }
    }
}

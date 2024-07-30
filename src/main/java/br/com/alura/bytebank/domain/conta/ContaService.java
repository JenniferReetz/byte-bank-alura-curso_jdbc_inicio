package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class ContaService {



    private ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        BigDecimal novoValor = conta.getSaldo().subtract(valor);
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(conta.getNumero(), novoValor);
        if (!conta.getEstaAtiva()){
        throw new RegraDeNegocioException("Conta não esta ativa");
        }
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }
        BigDecimal novoValor = conta.getSaldo().add(valor);
        alterar(conta, novoValor);
        if (!conta.getEstaAtiva()){
            throw new RegraDeNegocioException("Conta não esta ativa");
        }
    }
    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino,
                                      BigDecimal valor) {
        this.realizarSaque(numeroDaContaOrigem, valor);
        this.realizarDeposito(numeroDaContaDestino, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).deletar(numeroDaConta);
    }

    public void encerrarLogico(Integer numeroDaConta){
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterarlogico(numeroDaConta);
    }



    private Conta buscarContaPorNumero(Integer numeroDaConta) {
        Connection conn = connection.recuperarConexao();
        Conta conta = new ContaDAO(conn).listarPorNumero(numeroDaConta);
        if(conta != null) {
            return conta;
        } else {
            throw new RegraDeNegocioException("Não existe conta cadastrada com esse número!");
        }
    }

    private void alterar(Conta conta, BigDecimal valor) {
        PreparedStatement ps;
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(conta.getNumero(), valor);
    }

    public Set<Conta> listarContasAbertas() {
        Connection conn = connection.recuperarConexao();
        return new ContaDAO(conn).listar();
    }
}

/* ***************************************************************
* Autor............: Carlos Gil Martins da Silva
* Matricula........: 202110261
* Inicio...........: 09/11/2023
* Ultima alteracao.: 14/11/2023
* Nome.............: Controle de Fluxo
* Funcao...........: Realiza Toda a parte de controle de fluxo
tanto envio quando recepcao e o inverso tambem ja que agora temos
transmissor/receptor em um apenas, controla todo o fluxo de tempo
de threads e tudo mais que se faz ncessario, enviando os quadros
para o meio de comunicacao
****************************************************************/
package model;
import java.util.concurrent.Semaphore;

import control.controllerPrincipal;
import javafx.application.Platform;


public class ControleDeFluxo {
    int ContAcks = 0;
    int qtdReenvios = 0;
    int tamanhoJanela =0;
    private Semaphore semaforo = new Semaphore(1);
    private Semaphore semaforoJanelaValue = new Semaphore(1);
    controllerPrincipal cG = new controllerPrincipal(); // Instanciando e Criando o Controller

    // Metodo Utilizado para Setar um Controlador em Comum em Todas Thread
    public void setControlador(controllerPrincipal controle) {
        this.cG = controle;
    }

    public void CamadaEnlaceDadosTransmissoraControleDeFluxo(int quadro[][]) {
        int tipoDeControleDeFluxo = cG.getControleFluxo(); // alterar de acordo com o teste
        switch (tipoDeControleDeFluxo) {
            case 0: // protocolo de janela deslizante de 1 bit
                CamadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit(quadro);
                break;
            case 1: // protocolo de janela deslizante go-back-n
                CamadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN(quadro);
                break;
            case 2: // protocolo de janela deslizante com retransmissão seletiva
                CamadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva(quadro);
                break;
        }// fim do switch/case
    }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeFluxo

    public void CamadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit(int quadro[][]) {
        ContAcks = 0;
        // Setando o Timer Com base no Tipo de Codificacao (Manchester Dobro da mensagem)
        //Temporizador com Maior Folga
            if(cG.getCodificacao() != 0){
                cG.setMiliTemporizador(1500);
            }
            else{
                cG.setMiliTemporizador(1000);
            }
            new Thread(() -> { // Abrindo as Threads 
            int[] qtdBits = cG.getQtdBitsControleFluxo();
            //Recebendo o Quadro e Colocando a Informacao de Controle
            for (int i = 0; i < quadro.length; i++) {
                int[] arrayQuadro = new int[(quadro[i].length + 1)];
                for (int j = 0; j < quadro[i].length; j++) {
                    arrayQuadro[j] = quadro[i][j];
                }

                arrayQuadro[quadro[i].length] = alternaACK(i);
                cG.setQtdBitsInsercaoBits(qtdBits[i]);
                Platform.runLater(() -> {
                    cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                });
                
                try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                } catch (InterruptedException e) {}
                
                // Caso o ACK nao tenha Chegado, entra aqui infinitamente, ate chegar
                while(cG.getBooleanACK(i) != arrayQuadro[quadro[i].length]){
                    cG.setQtdBitsInsercaoBits(qtdBits[i]);
                    cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                    try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                    } catch (InterruptedException e) {}
                }
                if(i != quadro.length-1){
                System.out.println("\n||----------------> Quadro Enviado com Sucesso, partindo para o proximo! <----------------||");                
                }
            }
            cG.disableButtons();
            ContAcks = 0;
        }).start(); // Fim thread
    }// fim do metodo CamadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit

    public void CamadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN(int quadro[][]) {
        ContAcks = 0; 
        // Setando o Timer Com base no Tipo de Codificacao (Manchester Dobro da mensagem)
        //Temporizador com Maior Folga
            if(cG.getCodificacao() != 0){
                cG.setMiliTemporizador(1500);
            }
            else{
                cG.setMiliTemporizador(1000);
            }
            int[] qtdBits = cG.getQtdBitsControleFluxo();
            Thread[] threads = new Thread[quadro.length];
            cG.setTamanhoEnquadros(quadro.length);
            //Recebendo o Quadro e Colocando a Informacao de Controle
            for (int i = 0; i < quadro.length; i++) {
                final int indexExterno = i;
                int[] arrayQuadro = new int[(quadro[i].length + 1)];
                for (int j = 0; j < quadro[i].length; j++) {
                    arrayQuadro[j] = quadro[i][j];
                }
                threads[i] = new Thread(() -> { //Criando as Threads Todas Juntas
                setTamanhoJanela(getTamanhoJanela()+1); //Setando a Janela Deslizante
                System.out.println("Quadro ["+ indexExterno+"] Entrou na Janela");  
                arrayQuadro[quadro[indexExterno].length] = indexExterno;
                cG.setQtdBitsInsercaoBits(qtdBits[indexExterno]);

                Platform.runLater(() -> {
                    cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                });
                try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                } catch (InterruptedException e) {}
                
                // Caso o ACK nao tenha Chegado, entra aqui infinitamente, ate chegar
                while(cG.getBooleanACK(indexExterno) != arrayQuadro[quadro[indexExterno].length]){
                    cG.setQtdBitsInsercaoBits(qtdBits[indexExterno]);
                    Platform.runLater(() -> {
                        cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                    });
                    try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                    } catch (InterruptedException e) {}
                }
                System.out.println("||----------------> Quadro ["+ indexExterno+"] Recebido com Sucesso <----------------||\n");  
                if(indexExterno == quadro.length-1){
                    cG.disableButtons();       
                }
                });
            }
            new Thread(() -> {
            for (int i = 0; i < threads.length; i++) {
                while(getTamanhoJanela() == 4){
                    //Esperando para entrar no espaco da janela Deslizante de 4 quados por Vez
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
                threads[i].start();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            }).start();
    }// fim do metodo CamadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN

    public void CamadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva(int quadro[][]) {
        ContAcks = 0; 
                // Setando o Timer Com base no Tipo de Codificacao (Manchester Dobro da mensagem)
        //Temporizador com Maior Folga
            if(cG.getCodificacao() != 0){
                cG.setMiliTemporizador(1500);
            }
            else{
                cG.setMiliTemporizador(1000);
            }

            int[] qtdBits = cG.getQtdBitsControleFluxo();
            Thread[] threads = new Thread[quadro.length];

            cG.setTamanhoEnquadros(quadro.length);
            cG.setTamanhoFluxoRetransmissaoSeletiva(quadro.length);
            //Recebendo o Quadro e Colocando a Informacao de Controle
            for (int i = 0; i < quadro.length; i++) {
                final int indexExterno = i;
                int[] arrayQuadro = new int[(quadro[i].length + 1)];
                for (int j = 0; j < quadro[i].length; j++) {
                    arrayQuadro[j] = quadro[i][j];
                }
                threads[i] = new Thread(() -> { //Criando as Threads Todas Juntas

                setTamanhoJanela(getTamanhoJanela()+1); //Setando a Janela Deslizante
                System.out.println("Quadro ["+ indexExterno+"] Entrou na Janela");  

                arrayQuadro[quadro[indexExterno].length] = indexExterno;
                cG.setQtdBitsInsercaoBits(qtdBits[indexExterno]);

                Platform.runLater(() -> {
                    cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                });
                try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                } catch (InterruptedException e) {}
                
                // Caso o ACK nao tenha Chegado, entra aqui infinitamente, ate chegar
                while(cG.getBooleanACK(indexExterno) != arrayQuadro[quadro[indexExterno].length]){
                    cG.setQtdBitsInsercaoBits(qtdBits[indexExterno]);
                    Platform.runLater(() -> {
                        cG.getTs().CamadaFisicaTransmissora(arrayQuadro);
                    });
                    try {Thread.sleep(cG.getMiliTemporizador()); // TEMPO DE ESPERA
                    } catch (InterruptedException e) {}
                }
                System.out.println("||----------------> Quadro ["+ indexExterno+"] Recebido com Sucesso <----------------||\n");  
                if(indexExterno == quadro.length-1){
                    cG.disableButtons();
                }
                });
            }
            new Thread(() -> {
            for (int i = 0; i < threads.length; i++) {
                while(getTamanhoJanela() == 4){
                    //Esperando para entrar no espaco da janela Deslizante de 4 quados por Vez
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
                threads[i].start();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            }).start();
    }// fim do CamadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva

    ////////////////////////////////////////////////////
    // //
    // FINALIZAÇÃO DO CONTROLE DE FLUXO TRANSMISSORA //
    // INICIO DO CONTROLE DE FLUXO ENLACE RECEPETORA //
    // //
    //////////////////////////////////////////////////

    public int[] CamadaEnlaceDadosReceptoraControleDeFluxo(int quadro[]) {
        /*
        try {
            semaforoRecepcao.acquire();
        } catch (InterruptedException e) {}*/
        int tipoDeControleDeFluxo = cG.getControleFluxo(); // alterar de acordo com o teste
        int[] quadroFluxo;
        switch (tipoDeControleDeFluxo) {
            case 0: // protocolo de janela deslizante de 1 bit
                quadroFluxo = CamadaEnlaceDadosReceptoraJanelaDeslizanteUmBit(quadro);
                break;
            case 1: // protocolo de janela deslizante go-back-n
                quadroFluxo = CamadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN(quadro);
                break;
            case 2: // protocolo de janela deslizante com retransmissão seletiva
                quadroFluxo = CamadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva(quadro);
                break;
            default:
            quadroFluxo = quadro;
            break;
        }// fim do switch/case
        //semaforoRecepcao.release();
        return quadroFluxo;
    }// fim do metodo CamadaEnlaceDadosReceptoraControleDeFluxo

    public int[] CamadaEnlaceDadosReceptoraJanelaDeslizanteUmBit(int quadro[]) {
        if( quadro == null){
            qtdReenvios++;
            System.out.println("Quadro Chegou com Erro, Reenviando...");
            return null;
        }
        else{
                cG.setvalueACK(ContAcks, quadro[quadro.length-1]);
                System.out.println("Valor do Ackermann: "+ quadro[quadro.length-1]);
                System.out.println("Quantidade de Reenvios do Quadro: "+ qtdReenvios);
                qtdReenvios = 0;
                ContAcks++;
            return quadro;
        }
    }// fim do metodo CamadaEnlaceDadosReceptoraJanelaDeslizanteUmBit

    public int[] CamadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN(int quadro[]) {
        if( quadro == null){ // Quadro Chegou com Erro
            System.out.println("Quadro [CHEGOU COM ERRO], Reenviando ele e seus Subsequentes...");
            return null;
        }
        else{ //Verifica Se os Quadros Anteriores chegaram Corretamente
            boolean AcceptQuadro = true;
            int[] AcksTotais = cG.getBooleanACKvalue();
            if(quadro[quadro.length-1] != 0){
                for(int i = 0; i < quadro[quadro.length-1]; i++){
                    if(AcksTotais[i] != i){
                        AcceptQuadro = false;
                    }
                }
            }
            if(AcceptQuadro){ // Caso tenham Chegado ele Envia o  Ack de volta ao Transmissor e  envia o Quadro
                    try {
                        semaforo.acquire();
                        cG.setvalueACK(ContAcks, quadro[quadro.length-1]); // Setando valor do Ack
                        semaforo.release();
                        System.out.println("Valor do Ackermann: "+ quadro[quadro.length-1]);
                        setTamanhoJanela(getTamanhoJanela()-1); // liberando a janela Deslizante

                        if(quadro[quadro.length-1] == cG.getTamanhoEnquadros()-1){
                            cG.setRetSELET(true); // Ultimo Quadro Confirmado, Exibir Mensagem na tela
                        }
                    } 
                    catch (InterruptedException e) {
                    }
                    ContAcks++;
                    return quadro;
                }
                else{ // Caso n tenha Chegado, Descarta o Quadro
                    System.out.println("Quadro ["+ quadro[quadro.length-1] + "] Chegou Correto Mas seu/seus Anterior Não e foi descartado\n");
                    return null;
                }
        }
    }// fim do metodo CamadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN

    public int[] CamadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva(int quadro[]) {
        if( quadro == null){
            System.out.println("Quadro Chegou com Erro, Reenviando...");
            return null;
        }
        else{
                try {
                    semaforo.acquire();
                } catch (InterruptedException e) {
                }
                cG.setvalueACK(quadro[quadro.length-1], quadro[quadro.length-1]); // Setando valor do Ack
                cG.setindiceFluxoRetransmissaoSeletiva(ContAcks, quadro[quadro.length-1]);
                System.out.println("Valor do Ackermann: "+ quadro[quadro.length-1]);
                setTamanhoJanela(getTamanhoJanela()-1); // liberando a janela Deslizante
                ContAcks++;
                if(ContAcks == cG.getTamanhoEnquadros()){
                    cG.setRetSELET(true);
                }
                semaforo.release();
            return quadro;
        }
    }// fim do CamadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva

    // metodo para alternar os valores do ACK no Deslizante 1 Bit
    public int alternaACK(int i){
        if(i%2==0){return 0;}
        else{return 1;}
    }

    public int getTamanhoJanela() {
        return tamanhoJanela;
    }
    public void setTamanhoJanela(int tamanhoJanela) {
        try {
            semaforoJanelaValue.acquire();
        } catch (InterruptedException e) {
        }
        this.tamanhoJanela = tamanhoJanela;
        semaforoJanelaValue.release();
    }

}

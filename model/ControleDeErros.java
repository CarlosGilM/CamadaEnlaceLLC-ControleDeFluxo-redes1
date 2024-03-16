/* ***************************************************************
* Autor............: Carlos Gil Martins da Silva
* Matricula........: 202110261
* Inicio...........: 31/10/2023
* Ultima alteracao.: 04/11/2023
* Nome.............: Controle de Erros
* Funcao...........: Camada Controle de erros, faz toda a parte
de controle, desde a transmissao ate a recepcao, controlando tudo
que eh necessario para o mesmo, em todos os metodos pedidos no
trabalho
****************************************************************/
package model;
import java.util.concurrent.Semaphore;

import control.controllerPrincipal;


public class ControleDeErros {
  private Semaphore SemaforoTransmissorErro = new Semaphore(1);
  private Semaphore SemaforoReceptorErro = new Semaphore(1);
  int TamanhoQuadro = 0;
  controllerPrincipal cG = new controllerPrincipal(); // Instanciando e Criando o Controller

  // Metodo Utilizado para Setar um Controlador em Comum em Todas Thread
  public void setControlador(controllerPrincipal controle) {
    this.cG = controle;
  }

  public int[][] CamadaEnlaceDadosTransmissoraControleDeErro(int quadro[]) {
    try {
      SemaforoTransmissorErro.acquire();
    } catch (InterruptedException e) {
    }
    int tipoDeControleDeErro = cG.getControleErro(); // alterar de acordo com o teste
    TamanhoQuadro = cG.setNumBITScontroleErro(tipoDeControleDeErro);
    int[][] quadroControleErro;
    switch (tipoDeControleDeErro) {
      case 0: // bit de paridade par
        quadroControleErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: // bit de paridade impar
        quadroControleErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroControleErro = CamadaEnlaceDadosTransmissoraControleDeErroCRC(quadro);
        break;
      case 3: // codigo de Hamming
        quadroControleErro = CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(quadro);
        // codigo
        break;
      default:
        quadroControleErro = null;
        break;
    }// fim do switch/case
    cG.setBooleanACK(quadroControleErro.length);
    SemaforoTransmissorErro.release();
    return quadroControleErro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErro

  int[][] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(int quadro[]) {
    StringBuilder Temporaria = new StringBuilder();
    // quadro com cada enquadramento separado manipulado por bits
    int[][] enquadrosSeparados = new int[cG.getQtdEnquadros()][];

    // quadro q armazena a qnt de bits de cada enquadramento
    int[] qtdBitsControleFluxo = new int[cG.getQtdEnquadros()];
    
    int[] quadroParcial = new int[2]; // quadro parcial
    StringBuilder str = new StringBuilder(cG.ExibirBinarioControleErro(quadro)); // mensagem completa
    int forInterno = 0;
    int positionBit = 0;
    int indexQuadro = 0;
    int countsBits1 = 0;
    // Percorre toda mensagem separando cada enquadramento e inserindo a informacao de controle
    // em cada enquadramento separadamente para enviar para proxima camada
    for (int i = str.length() - 1; i >= 0; i--) {
      positionBit = i;
      if(i >=31){
        forInterno = 31;
      }else{
        forInterno = i;
      }

      for(int j = 0; j <= forInterno; j++){
        if (str.toString().charAt(positionBit) == '1') {
        countsBits1++;
        quadroParcial[0] = quadroParcial[0] | (1 << j);
        }
      positionBit--;
      }
      int bitQuadro = (forInterno+1) % 32;
        if (countsBits1 % 2 != 0) { // Se nao tiver Quantidade Par de Bits, Insere um Bit 1 no ultimo Bit da
          quadroParcial[quadroParcial.length-1] = quadroParcial[quadroParcial.length-1]| (1 << bitQuadro);
        }
        enquadrosSeparados[indexQuadro] = quadroParcial;
        countsBits1 = 0;
        qtdBitsControleFluxo[indexQuadro] = forInterno+2;
        System.out.println(("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro])));
        String aux = ("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        Temporaria.append(aux).append("\n");
        indexQuadro++;
        i = i - forInterno;
        if (i < 32){
          quadroParcial = new int[1];
        }else{
          quadroParcial = new int[2];
        }
      }
      cG.setQtdBitsControleFluxo(qtdBitsControleFluxo);
      System.out.println("<----------------------------------------------------------------------->");
      cG.setBinaryArea(Temporaria.toString());
    return enquadrosSeparados;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControledeErroBitParidadePar

  int[][] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(int quadro[]) {
    StringBuilder Temporaria = new StringBuilder();
        // quadro com cada enquadramento separado manipulado por bits
    int[][] enquadrosSeparados = new int[cG.getQtdEnquadros()][];

    // quadro q armazena a qnt de bits de cada enquadramento
    int[] qtdBitsControleFluxo = new int[cG.getQtdEnquadros()];
    
    int[] quadroParcial = new int[2]; // quadro parcial
    StringBuilder str = new StringBuilder(cG.ExibirBinarioControleErro(quadro)); // mensagem completa
    int forInterno = 0;
    int positionBit = 0;
    int indexQuadro = 0;
    int countsBits1 = 0;
    // Percorre toda mensagem separando cada enquadramento e inserindo a informacao de controle
    // em cada enquadramento separadamente para enviar para proxima camada
    for (int i = str.length() - 1; i >= 0; i--) {
      positionBit = i;
      if(i >=31){
        forInterno = 31;
      }else{
        forInterno = i;
      }

      for(int j = 0; j <= forInterno; j++){
        if (str.toString().charAt(positionBit) == '1') {
        countsBits1++;
        quadroParcial[0] = quadroParcial[0] | (1 << j);
        }
      positionBit--;
      }
      int bitQuadro = (forInterno+1) % 32;
        if (countsBits1 % 2 == 0) { // Se nao tiver Quantidade Par de Bits, Insere um Bit 1 no ultimo Bit da
          quadroParcial[quadroParcial.length-1] = quadroParcial[quadroParcial.length-1]| (1 << bitQuadro);
        }
        enquadrosSeparados[indexQuadro] = quadroParcial;
        countsBits1 = 0;
        qtdBitsControleFluxo[indexQuadro] = forInterno+2;
        System.out.println("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        String aux = ("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        Temporaria.append(aux).append("\n");
        indexQuadro++;
        i = i - forInterno;
        if (i < 32){
          quadroParcial = new int[1];
        }else{
          quadroParcial = new int[2];
        }
      }
      cG.setQtdBitsControleFluxo(qtdBitsControleFluxo);
      cG.setBinaryArea(Temporaria.toString());
            System.out.println("<----------------------------------------------------------------------->");
    return enquadrosSeparados;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControledeErroBitParidadeImpar

  int[][] CamadaEnlaceDadosTransmissoraControleDeErroCRC(int quadro[]) {
    StringBuilder Temporaria = new StringBuilder();
    /*
     * EXEMPLO DE FUNCIONAMENTO DO CRC, COM UM DE 4 BITS
     * 11010011101100 000 <--- entrada deslocada para a direita com 3 zeros
     * 1011 <--- divisor (4 bits) = x³ + x + 1
     * ------------------
     * 01100011101100 000 <--- resultado
     */

    // Polinomio CRC-32 = x32, x26, x23, x22, x16, x12, x11, x10, x8, x7, x5, x4,
    // x2, x1 + 1
    // Logo um Polinomio de 33 bits com 32 bits de resto, assim deslocamos a
    // mensagem
    // 32 Bits 0 A direita e dividos o crc e inserimos nesse lugar

    // quadro com cada enquadramento separado manipulado por bits
    int[][] enquadrosSeparados = new int[cG.getQtdEnquadros()][];

    // quadro q armazena a qnt de bits de cada enquadramento
    int[] qtdBitsControleFluxo = new int[cG.getQtdEnquadros()];
    
    int[] quadroParcial = new int[2]; // quadro parcial
    StringBuilder str = new StringBuilder(cG.ExibirBinarioControleErro(quadro)); // mensagem completa
    String PolinomioCRC32 = "100000100110000010001110110110111";
    // 32 Bits 0 a serem inseridos no final da mensagem para calcular o crc
    String zerosInseridos = "00000000000000000000000000000000";

    int forInterno = 0;
    int positionBit = 0;
    int indexQuadro = 0;

    // Percorre toda mensagem separando cada enquadramento e inserindo a informacao de controle
    // em cada enquadramento separadamente para enviar para proxima camada
    for (int i = str.length() - 1; i >= 0; i--) {
      StringBuilder stringQuadro = new StringBuilder();
      positionBit = i;
      if(i >=31){
        forInterno = 31;
      }else{
        forInterno = i;
      }
      // Percorre apenas o tamanho definiddo do quadro
      //Concatenando os 0 e 1 na string
      for(int j = 0; j <= forInterno; j++){
        if (str.toString().charAt(positionBit) == '1') {
          stringQuadro.insert(0,'1');
        }
        else{
          stringQuadro.insert(0,'0');
        }
      positionBit--;
      }
      // Insere os 32 bits 0 e calcula o crc e insere na string
      String aux = stringQuadro.toString();
      aux = aux + zerosInseridos;
      String Resto = cG.divisaoBinariaResto(aux, PolinomioCRC32);
      stringQuadro.append(Resto);

      //Passa a string para o array de int manipulando os bits
      positionBit = stringQuadro.length()-1;
      for(int j = 0; j <= stringQuadro.length()-1; j++){
        if (stringQuadro.toString().charAt(positionBit) == '1') {
        quadroParcial[j/32] = quadroParcial[j/32] | (1 << j);
        }
      positionBit--;
      }
      // o quadro de quadros recebe o quadro na posicao
        enquadrosSeparados[indexQuadro] = quadroParcial;
        qtdBitsControleFluxo[indexQuadro] = stringQuadro.length();
        System.out.println("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        String aux2 = ("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        Temporaria.append(aux2).append("\n");
        indexQuadro++;
        i = i - forInterno;
        quadroParcial = new int[2]; // tamanho do novo quadro
      }
      cG.setQtdBitsControleFluxo(qtdBitsControleFluxo);
      System.out.println("<----------------------------------------------------------------------->");
      cG.setBinaryArea(Temporaria.toString());
    return enquadrosSeparados;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControledeErroCRC

  int[][] CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int quadro[]) {
    StringBuilder Temporaria = new StringBuilder();
        // quadro com cada enquadramento separado manipulado por bits
    int[][] enquadrosSeparados = new int[cG.getQtdEnquadros()][];
    // quadro q armazena a qnt de bits de cada enquadramento
    int[] qtdBitsControleFluxo = new int[cG.getQtdEnquadros()];
    StringBuilder str = new StringBuilder(cG.ExibirBinarioControleErro(quadro)); // mensagem completa

    int forInterno = 0;
    int positionBit = 0;
    int indexQuadro = 0;

    // Percorre toda mensagem separando cada enquadramento e inserindo a informacao de controle
    // em cada enquadramento separadamente para enviar para proxima camada
    for (int i = str.length() - 1; i >= 0; i--) {
      StringBuilder stringQuadro = new StringBuilder();
      positionBit = i;
      if(i >=31){
        forInterno = 31;
      }else{
        forInterno = i;
      }
      // Percorre apenas o tamanho definiddo do quadro
      //Concatenando os 0 e 1 na string
      for(int j = 0; j <= forInterno; j++){
        if (str.toString().charAt(positionBit) == '1') {
          stringQuadro.insert(0,'1');
        }
        else{
          stringQuadro.insert(0,'0');
        }
      positionBit--;
      }
      // Insere os 32 bits 0 e calcula o crc e insere na string
      StringBuilder mensagemCodificada = new StringBuilder(cG.codificarHamming(stringQuadro.toString()));
      int[] quadroParcial;
      if(mensagemCodificada.length() % 32 == 0){
        quadroParcial = new int[(mensagemCodificada.length()/32)];
      }
      else{
        quadroParcial = new int[((mensagemCodificada.length()/32)+1)];
      }
      //Passa a string para o array de int manipulando os bits
      positionBit = mensagemCodificada.length()-1;
      for(int j = 0; j <= mensagemCodificada.length()-1; j++){
        if (mensagemCodificada.toString().charAt(positionBit) == '1') {
        quadroParcial[j/32] = quadroParcial[j/32] | (1 << j);
        }
      positionBit--;
      }
      // o quadro de quadros recebe o quadro na posicao
        enquadrosSeparados[indexQuadro] = quadroParcial;
        qtdBitsControleFluxo[indexQuadro] = mensagemCodificada.length();
        System.out.println("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
         String aux2 = ("Quadro[" + indexQuadro + "]: " + cG.ExibirBinarioControleFluxo(enquadrosSeparados[indexQuadro], qtdBitsControleFluxo[indexQuadro]));
        Temporaria.append(aux2).append("\n");
        indexQuadro++; //aumenta o index do enquadrosSeparados
        i = i - forInterno; // diminui o valor de iteracao do I do valor do for interno que eh a (qtd de bits do quadro)
      }
      cG.setQtdBitsControleFluxo(qtdBitsControleFluxo);
            System.out.println("<----------------------------------------------------------------------->");
            cG.setBinaryArea(Temporaria.toString());
    return enquadrosSeparados;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCodigoDehamming

  ////////////////////////////////////////////////////
  // //
  // FINALIZAÇÃO DO CONTROLE DE ERROS TRANSMISSORA //
  // INICIO DO CONTROLE DE ERROS ENLACE RECEPETORA //
  // //
  //////////////////////////////////////////////////

  int[] CamadaEnlaceDadosReceptoraControleDeErro(int quadro[]) {
    try {
      SemaforoReceptorErro.acquire();
    } catch (InterruptedException e) {
    }
    int tipoDeControleDeErro = cG.getControleErro(); // alterar de acordo com o teste
    int[] quadroSemErro;
    switch (tipoDeControleDeErro) {
      case 0: // bit de paridade par
        quadroSemErro = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(quadro);
        break;
      case 1: // bit de paridade impar
        quadroSemErro = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroSemErro = CamadaEnlaceDadosReceptoraControleDeErroCRC(quadro);
        break;
      case 3: // codigo de hamming
        quadroSemErro = CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
      default:
        quadroSemErro = quadro;
        break;
    }// fim do switch/case
    SemaforoReceptorErro.release();
    return quadroSemErro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErro

  int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(int quadro[]) {
    int posicaoRemocao = ((cG.getQtdBitsInsercaoBits() % 32) - 1);
    int indexArray = 0;
    if (cG.getQtdBitsInsercaoBits() % 32 == 0)
      indexArray = (cG.getQtdBitsInsercaoBits() / 32);
    else
      indexArray = (cG.getQtdBitsInsercaoBits() / 32) + 1;

    int somatorioBITS1 = 0;
    // For até o tamanho da Mensagem
    for (int i = cG.getQtdBitsInsercaoBits() - 1; i >= 0; i--) {
      int bitQuadro = i % 32;
      int mascara = 1 << bitQuadro;
      int Bit = (quadro[i / 32] & mascara) >> bitQuadro; // Pega o Bit na posicao da Mascara&Quadro na posicao desloc
                                                         // Bit
      // Estrutura de IF que manipula bit por Bit
      if (Bit == 1 || Bit == -1) {
        somatorioBITS1++;
      }
    } // Fim For Bits
    if (somatorioBITS1 % 2 == 0) { // Se Tiver Quantidade Par de Bits, Insere um Bit 1 no ultimo Bit da mensagem
      // Tira a Informação de Controle do Array de Bits
      quadro[indexArray - 1] = quadro[indexArray - 1] | (0 << posicaoRemocao);
      cG.setQtdBitsInsercaoBits(cG.getQtdBitsInsercaoBits() - 1);
      return quadro;
    } else {
      return null;
    }
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar

  int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(int quadro[]) {
    int posicaoRemocao = ((cG.getQtdBitsInsercaoBits() % 32) - 1);
    int indexArray = 0;
    if (cG.getQtdBitsInsercaoBits() % 32 == 0)
      indexArray = (cG.getQtdBitsInsercaoBits() / 32);
    else
      indexArray = (cG.getQtdBitsInsercaoBits() / 32) + 1;

    int somatorioBITS1 = 0;
    // For até o tamanho da Mensagem
    for (int i = cG.getQtdBitsInsercaoBits() - 1; i >= 0; i--) {
      int bitQuadro = i % 32;
      int mascara = 1 << bitQuadro;
      int Bit = (quadro[i / 32] & mascara) >> bitQuadro; // Pega o Bit na posicao da Mascara&Quadro na posicao desloc
                                                         // Bit
      // Estrutura de IF que manipula bit por Bit
      if (Bit == 1 || Bit == -1) {
        somatorioBITS1++;
      }
    } // Fim For Bits
    if (somatorioBITS1 % 2 != 0) { // Se Tiver Quantidade Par de Bits, Insere um Bit 1 no ultimo Bit da mensagem
      // Tira a Informação de Controle do Array de Bits
      quadro[indexArray - 1] = quadro[indexArray - 1] | (0 << posicaoRemocao);
      cG.setQtdBitsInsercaoBits(cG.getQtdBitsInsercaoBits() - 1);
      return quadro;
    } else {
      return null;
    }
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar

  int[] CamadaEnlaceDadosReceptoraControleDeErroCRC(int quadro[]) {
    int[] quadroSemControle;
    String PolinomioCRC32 = "100000100110000010001110110110111";
    String verifyRESTO = cG.divisaoBinariaResto(cG.ExibirBinarioControleErro(quadro), PolinomioCRC32);
    // Validacao se A mensagem Chegou Corretamente (Sem ERROS)
    for (int i = 0; i < 32; i++) {
      if (verifyRESTO.charAt(i) != '0') {
        return null;
      }
    }
    // Definindo Tamanho do Array sem o Controle de Erro
    if ((cG.getQtdBitsInsercaoBits() - 32) % 32 == 0) {
      quadroSemControle = new int[((cG.getQtdBitsInsercaoBits() - 32) / 32)+1];
    } else {
      quadroSemControle = new int[(((cG.getQtdBitsInsercaoBits() - 32) / 32) + 1)+1];
    }
    int bitsAnterior = cG.getQtdBitsInsercaoBits() - 33;

    for (int i = cG.getQtdBitsInsercaoBits() - 1; i >= 32; i--) {
      int bitQuadro = i % 32;
      int mascara = 1 << bitQuadro;
      int Bit = (quadro[i / 32] & mascara) >> bitQuadro; // Pega o Bit na posicao da Mascara&Quadro na posicao desloc
                                                         // Bit
      if (Bit == 1 || Bit == -1) {
        int bitQuadroSemControle = bitsAnterior % 32;
        quadroSemControle[bitsAnterior / 32] = quadroSemControle[bitsAnterior / 32] | (1 << bitQuadroSemControle);
      }
      bitsAnterior--;
    }
    quadroSemControle[quadroSemControle.length-1] = quadro[quadro.length-1];
    cG.setQtdBitsInsercaoBits((cG.getQtdBitsInsercaoBits() - 32));
    return quadroSemControle;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCRC

  int[] CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(int quadro[]) {
    String StringBinariaRecebida = cG.ExibirBinarioControleErro(quadro);

    if (cG.decodificarHamming(StringBinariaRecebida) == null) {
      return null;
    }

    StringBuilder mensagemBinaria = new StringBuilder(cG.decodificarHamming(StringBinariaRecebida));
    mensagemBinaria.reverse(); // Invertendo os bits para inserir corretamente no array

    // Definindo o tamanho do array sem Controle de Erro
    int tamanhoQuadro;
    if (mensagemBinaria.length() % 32 == 0) {
      tamanhoQuadro = (mensagemBinaria.length() / 32);
    } else {
      tamanhoQuadro = (mensagemBinaria.length() / 32) + 1;
    }

    // Criando o Array com o novo Tamanho e Inserindo os Bits
    // Por meio de Mascara nele
    int[] arraySemControleERRO = new int[tamanhoQuadro+1];
    for (int i = (mensagemBinaria.length() - 1); i >= 0; i--) {
      int bitQuadro = i % 32;
      if (mensagemBinaria.charAt(i) == '1') {
        arraySemControleERRO[i / 32] = arraySemControleERRO[i / 32] | (1 << bitQuadro);
      }
    }
    arraySemControleERRO[arraySemControleERRO.length-1] = quadro[quadro.length-1];
    cG.setQtdBitsInsercaoBits(mensagemBinaria.length());
    return arraySemControleERRO;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming

}

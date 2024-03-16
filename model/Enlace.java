/* ***************************************************************
* Autor............: Carlos Gil Martins da Silva
* Matricula........: 202110261
* Inicio...........: 08/10/2023
* Ultima alteracao.: 13/10/2023
* Nome.............: Enlace
* Funcao...........: Camada de Enlace de Dados, faz toda a parte
de enlace, desde Transmissao ate a Recepcao, No qual tem os 4
metodos propostos para o trabalho funcionando todos perfeitamente
****************************************************************/
// Camada de ENQUADRAMENTO, Nome Incorreto
package model;

import java.util.concurrent.Semaphore;

import control.controllerPrincipal;

public class Enlace {

  private Semaphore SemaforoEnquad = new Semaphore(1);
  private Semaphore SemaforoDesenquad = new Semaphore(1);
  controllerPrincipal cG = new controllerPrincipal(); // Instanciando e Criando o Controller
  int caracteresAnterior;
  int tamanhoDesenquadro;
  // Metodo Utilizado para Setar um Controlador em Comum em Todas Thread
  public void setControlador(controllerPrincipal controle) {
    this.cG = controle;
  }

  void CamadaEnlaceDadosTransmissora(int quadro[]) {
   int quadroEnquadrado[] = CamadaEnlaceDadosTransmissoraEnquadramento(quadro);
   int quadroControleERRO[][] = cG.getCtr().CamadaEnlaceDadosTransmissoraControleDeErro(quadroEnquadrado);
   cG.getCfx().CamadaEnlaceDadosTransmissoraControleDeFluxo(quadroControleERRO);
  }// fim do metodo CamadaEnlaceDadosTransmissora

  public int[] CamadaEnlaceDadosTransmissoraEnquadramento(int quadro[]) {
    try {
      SemaforoEnquad.acquire();
    } catch (InterruptedException e) {
    }
    int tipoDeEnquadramento = cG.getEnquadramento(); // alterar de acordo com o teste
    caracteresAnterior = cG.getNumCaracteres();

    cG.setNumCaracteresEnquadramento(cG.getEnquadramento(), quadro); // Novo Numero de Caracteres
    int quadroEnquadrado[] = new int[cG.setTamanhoArray(cG.getCodificacao())];

    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
    }// fim do switch/case
    SemaforoEnquad.release();
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceTransmissoraEnquadramento

  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(int quadro[]) {

    // Criando Novo quadro com o novo tamanho
    int quadroEnquadrado[] = new int[cG.setTamanhoArray(cG.getCodificacao())]; // Novo Array Quadro

    int controllerBits = 0; // Controlador Deslocamento de Bits Array
    int controllerBitsENQUADRAMENTO = 0; // Controlador Deslocamento de Bits Array Enquadramento
    int deslocBit = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois eh o ultimo Bit de um Caractere)
    int deslocBitENQUAD = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois eh o ultimo Bit de um Caractere)
    int indexQuadro = 0; // Index do Array ENQUADRADO
    
    // For até o tamanho da Mensagem
    for (int i = 0; i < caracteresAnterior; i++) {
      if (i % 3 == 0 && i != 0) { // Aumenta o indice do Array Enquadrado quando tiver mais de 3 carac (pois com o controle fica 4 carac)
        indexQuadro++;
      }
      deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);
      deslocBit = cG.setDeslocamentoBIT(controllerBits, deslocBit);

      if (i % 3 == 0) { // Insere o byte de Controle (Contagem de Caractere)
        int enquad = Math.min(3, caracteresAnterior - i); // Pega o Tamanho do Quadro
        char intChar = Integer.toString(enquad).charAt(0); // Int para Char
        String aux = cG.charParaBinario(intChar); // String com os Binarios

        for (int j = 0; j < 8; j++) { // For para cada Bit
          // Estrutura de IF que manipula bit por Bit
          if (aux.charAt(j) == '1') {
            // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1, mantendo os outros bits inalterados.
            quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
          }
          deslocBitENQUAD--;
        }
        controllerBitsENQUADRAMENTO++;
        deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);

      } // Fim If Controle do Enquadramento (Contagem de Caracteres)

      for (int j = 0; j < 8; j++) { // For para cada caractere
        int mascara = 1 << deslocBit;
        int Bit = (quadro[i / 4] & mascara) >> deslocBit; // Pega o Bit na posicao da Mascara&Quadro na posicao deslocBit
        // Estrutura de IF que manipula bit por Bit
        if (Bit == 1 || Bit == -1) {
          quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
        }
        deslocBit--;
        deslocBitENQUAD--;
      } // Fim For Caractere
      controllerBits++;
      controllerBitsENQUADRAMENTO++;

    } // Fim For Mensagem Completa
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraContagemDeCaracteres


  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(int quadro[]) {
    // Criando Novo quadro com o novo tamanho
    int quadroEnquadrado[] = new int[cG.setTamanhoArray(cG.getCodificacao())]; // Novo Array Quadro
    int controllerBits = 0; // Controlador Deslocamento de Bits Array
    int controllerBitsENQUADRAMENTO = 0; // Controlador Deslocamento de Bits Array Enquadramento
    int deslocBit = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois o ultimo Bit de um Caractere)
    int deslocBitENQUAD = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois eh o ultimo Bit de um Caractere)
    
    int indexQuadro = 0; // Index do Array ENQUADRADO
    int contCaracteresInseridos = 0; // caracteres inseridos no array para conseguir atualizar o index corretamente

    String STX = "01010011"; // -> Flag = S
    String ETX = "01000101"; // -> Flag = E
    String valueEscape = "01111100"; // -> Escape = |

    // For até o tamanho da Mensagem
    for (int i = 0; i < caracteresAnterior; i++) {

      // Seta o valor dos Deslocamentos dos Bits Para Insercao na posicao devida
      deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);
      deslocBit = cG.setDeslocamentoBIT(controllerBits, deslocBit);

      if (i % 3 == 0) { // Insere o byte de Controle (Insercao de Bytes)
        if (i == 0) { // INSERE O CONTROLE INICIAL S
          for (int j = 0; j < 8; j++) { // For para cada Bit
            // Estrutura de IF que manipula bit por Bit
            if (STX.charAt(j) == '1') {
              // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
              quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
            }
            deslocBitENQUAD--;
          }
          contCaracteresInseridos++;
          indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);

          controllerBitsENQUADRAMENTO++;
          deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);

        } else { // INSERE OS CONTROLES INTERMEDIARIOS ES
          for (int t = 0; t < 2; t++) {
            String aux;
            if (t == 0)
              aux = ETX;
            else
              aux = STX;
            for (int j = 0; j < 8; j++) { // For para cada Bit
              // Estrutura de IF que manipula bit por Bit
              if (aux.charAt(j) == '1') {
                // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
                quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
              }
              deslocBitENQUAD--;
            }
            contCaracteresInseridos++;
            indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
            controllerBitsENQUADRAMENTO++;
            deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);

          }
        }
      } // Fim If Controle do Enquadramento (insercao de bytes)

      // Recupera os bits para verficar se eh uma fake flag ou fakeesc ou nao
      String BitComparation = cG.recuperaBitArray(quadro, i, (deslocBit-7));
      if (BitComparation.equals(valueEscape) || BitComparation.equals(ETX) || BitComparation.equals(STX)) {
        for (int j = 0; j < 8; j++) { // For para cada Bit
          // Estrutura de IF que manipula bit por Bit
          if (valueEscape.charAt(j) == '1') {
            // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
            quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
          }
          deslocBitENQUAD--;
        }
        contCaracteresInseridos++;
        indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
        controllerBitsENQUADRAMENTO++;
        deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);
      }
      for (int j = 0; j < 8; j++) { // For para cada caractere
        int mascara = 1 << deslocBit;
        int Bit = (quadro[i / 4] & mascara) >> deslocBit; // Pega o Bit na posicao da Mascara&Quadro na posicao desloc Bit
        // Estrutura de IF que manipula bit por Bit
        if (Bit == 1 || Bit == -1) {
          quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
        }
        deslocBit--;
        deslocBitENQUAD--;
      } // Fim For Caractere
      contCaracteresInseridos++;
      indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
      controllerBits++;
      controllerBitsENQUADRAMENTO++;

      if (i == caracteresAnterior - 1) { // IF PARA VERIFICAR SE É O FIM Da MENSAGEM e inserir o ETX
        deslocBitENQUAD = cG.setDeslocamentoBIT(controllerBitsENQUADRAMENTO, deslocBitENQUAD);
        for (int j = 0; j < 8; j++) { // For para cada Bit
          // Estrutura de IF que manipula bit por Bit
          if (ETX.charAt(j) == '1') {
            // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
            quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBitENQUAD);
          }
          deslocBitENQUAD--;
        }
        contCaracteresInseridos++;
        controllerBitsENQUADRAMENTO++;
        indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
      }
    } // Fim For Mensagem Completa
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBytes

  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(int quadro[]) {
    // Criando Novo quadro com o novo tamanho
    int quadroEnquadrado[] = new int[cG.setTamanhoArray(cG.getCodificacao())]; // Novo Array Quadro
    int indexQuadro;
    int flagCount = 0;
    if(cG.getNumCaracteres() % 4 == 0)
      indexQuadro = cG.getNumCaracteres()/4; // Index do Array ENQUADRADO
    else
      indexQuadro = (cG.getNumCaracteres()/4) +1;

    int bitsENQUAD = cG.getQtdBitsInsercaoBits()-1;
    String Flag = "01111110"; // flag inicial e final
    int somatorioBITS1 = 0;

    // For até o tamanho da Mensagem
    for (int i = (caracteresAnterior*8)-1; i >= 0; i--) {
      bitsENQUAD = bitsENQUAD % 32;
      //Quadros de Tamanho 3
      if (flagCount == 0 || flagCount == 24) { // Insere os bytes de Controle inicial e final de cada quadro (Insercao de Bits)
          for (int j = 0; j < 8; j++) { // For para cada Bit
            if (Flag.charAt(j) == '1') {
              // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
              quadroEnquadrado[indexQuadro-1] = quadroEnquadrado[indexQuadro-1] | (1 << bitsENQUAD);
            }
            bitsENQUAD--;
          if(bitsENQUAD == -1){
            bitsENQUAD = 31;
            indexQuadro--;}
          }
        flagCount = 0;
      } // Fim If Controle do Enquadramento (insercao de bytes)

        int bitQuadro = i%32;
        int mascara = 1 << bitQuadro;
        int Bit = (quadro[i / 32] & mascara) >> bitQuadro; // Pega o Bit na posicao da Mascara&Quadro na posicao desloc Bit
        // Estrutura de IF que manipula bit por Bit
        if (Bit == 1 || Bit == -1) {
          quadroEnquadrado[indexQuadro-1] = quadroEnquadrado[indexQuadro-1] | (1 << bitsENQUAD);
          somatorioBITS1++;
        }else{
          somatorioBITS1 = 0;
        }
        if(somatorioBITS1 == 5){
          bitsENQUAD--;
            if(bitsENQUAD == -1){
            bitsENQUAD = 31;
            indexQuadro--;}
          quadroEnquadrado[indexQuadro-1] = quadroEnquadrado[indexQuadro-1] | (0 << bitsENQUAD);
          somatorioBITS1 = 0;
        }
        bitsENQUAD--;
            if(bitsENQUAD == -1){
            bitsENQUAD = 31;
            indexQuadro--;}
        
        flagCount++;
        if (i==0) { // Insere os bytes de Controle inicial e final de cada quadro (Insercao de Bits)
      for (int j = 0; j < 8; j++) { // For para cada Bit
        if (Flag.charAt(j) == '1') {
          // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
          quadroEnquadrado[indexQuadro-1] = quadroEnquadrado[indexQuadro-1] | (1 << bitsENQUAD);
        }
        bitsENQUAD--;
          if(bitsENQUAD == -1){
            bitsENQUAD = 31;
            indexQuadro--;}    
      }
    } // Fim If Controle do Enquadramento (insercao de bytes)
      } // Fim For Bits
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBits

  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {
    // Codificacao na Camada Fisica
    // Uma vez que ele viola a camada fisica, a codificacao deve ser feita la
    // quando passamos o algoritmo de binario para manchester ou Manchester Differencial
    return quadro;
  }// fim do metodo CamadaEnlaceDadosTransmissoraViolacaoDaCamadaFisica

  ////////////////////////////////////////////////
  //                                            //
  // FINALIZAÇÃO DA CAMADA ENLACE TRANSMISSORA  //
  // INICIO DA CAMADA ENLACE RECEPETORA        //
  //                                           //
  ///////////////////////////////////////////////

  public void CamadaEnlaceDadosReceptora (int quadro []) {
    int quadroControleErro[] = cG.getCtr().CamadaEnlaceDadosReceptoraControleDeErro(quadro);
    if(quadroControleErro == null){
      cG.getRt().AplicacaoReceptora(null);
      int quadroFLUXO[] = cG.getCfx().CamadaEnlaceDadosReceptoraControleDeFluxo(null);
    }
    else{
      int quadroDesenquadrado[] = CamadaEnlaceDadosReceptoraEnquadramento(quadroControleErro);
      int quadroFLUXO[] = cG.getCfx().CamadaEnlaceDadosReceptoraControleDeFluxo(quadroDesenquadrado);
      //chama proxima camada
      cG.getRt().CamadaDeAplicacaoReceptora(quadroFLUXO);
    }
    }//fim do metodo CamadaEnlaceDadosReceptora

  public int[] CamadaEnlaceDadosReceptoraEnquadramento (int quadro []) {
    try {
      SemaforoDesenquad.acquire();
    } catch (InterruptedException e) {
    }
    int tipoDeEnquadramento = cG.getEnquadramento(); //alterar de acordo com o teste
    if (caracteresAnterior % 4 == 0)
     tamanhoDesenquadro = (caracteresAnterior / 4);
  else
    tamanhoDesenquadro =((caracteresAnterior / 4) + 1);
    int quadroDesenquadrado [] = new int[tamanhoDesenquadro];

    switch (tipoDeEnquadramento) {
    case 0 : //contagem de caracteres
    quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
    break;
    case 1 : //insercao de bytes
    quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
    break;
    case 2 : //insercao de bits
    quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
    break;
    case 3 : //violacao da camada fisica
    quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(quadro);
    break;
    }//fim do switch/case
    SemaforoDesenquad.release();
    return quadroDesenquadrado;
    }//fim do metodo CamadaEnlaceDadosReceptoraEnquadramento

  public int[] CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres (int quadro []) {
    int positionBit;
    StringBuilder stringBinaria = new StringBuilder(cG.ExibirBinarioControleErro(quadro));
    StringBuilder Return = new StringBuilder();
    int verify = stringBinaria.length() - 8;
    for(int i = stringBinaria.length()-1; i>=0; i--){
      if(i >= verify){
        //Informacao de Controle
      }
      else{
        Return.insert(0, stringBinaria.toString().charAt(i));
      }
    }

    int[] quadroParcial = new int[(Return.length()/8)+1];
    positionBit = Return.length()-1;
    for(int j = 0; j <= Return.length()-1; j++){
      if (Return.toString().charAt(positionBit) == '1') {
      quadroParcial[j/32] = quadroParcial[j/32] | (1 << j);
      }
    positionBit--;
    }
    cG.setQtdBitsInsercaoBits(Return.length());
    quadroParcial[quadroParcial.length-1] = quadro[quadro.length-1];
    
    return quadroParcial;
  }//fim do metodo CamadaEnlaceDadosReceptoraContagemDeCaracteres

  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes (int quadro []) {
    // Criando Novo quadro com o novo tamanho
    int quadroDesenquadrado[] = new int[tamanhoDesenquadro]; // Novo Array Quadro
    cG.setQtdBitsInsercaoBits(caracteresAnterior*8);
    int controllerBits = 0; // Controlador Deslocamento de Bits Array
    int controllerBitsDESENQUADRAMENTO = 0; // Controlador Deslocamento de Bits Array Enquadramento
    int deslocBit = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois o ultimo Bit de um Caractere)
    int deslocbitDESENQUAD = 0; // Variavel define qual bit vai deslocar para manipulacaoo (Inicia do 7 pois eh o ultimo Bit de um Caractere)
    
    int indexQuadro = 0; // Index do Array ENQUADRADO
    int contCaracteresInseridos = 0; // caracteres inseridos no array para conseguir atualizar o index corretamente
    String BitComparation;
    boolean controlFlagFAKE = false;
    boolean positionFlags = false;
    int auxFlags = 0;

    String STX = "01010011"; // -> Flag = S
    String ETX = "01000101"; // -> Flag = E
    String valueEscape = "01111100"; // -> Escape = |

    // For até o tamanho da Mensagem
    for (int i = 0; i < cG.getCaracAux(); i++) {
      // Seta o valor dos Deslocamentos dos Bits Para Insercao na posicao devida
      deslocBit = cG.setDeslocamentoBIT(controllerBits, deslocBit);
      // Recupera os bits para verficar se eh uma fake flag ou fakeesc ou nao
      BitComparation = cG.recuperaBitArray(quadro, i, (deslocBit - 7));
      deslocBit = deslocBit - 8;
      controllerBits++;
      if (BitComparation.equals(ETX) || BitComparation.equals(STX) || controlFlagFAKE || auxFlags % 4 == 0 || i == (cG.getCaracAux()-1) || positionFlags == true) {
        // Byte de Controle de Quadros
        if (controlFlagFAKE) { // Significa que o Bit de comparacao foi um ESCAPE (se entrar eh uma Flag Fake)
          deslocbitDESENQUAD = cG.setDeslocamentoBIT(controllerBitsDESENQUADRAMENTO, deslocbitDESENQUAD);
          for (int j = 0; j < 8; j++) { // For para cada Bit
            // Estrutura de IF que manipula bit por Bit
            if (BitComparation.charAt(j) == '1') {
              // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
              quadroDesenquadrado[indexQuadro] = quadroDesenquadrado[indexQuadro] | (1 << deslocbitDESENQUAD);
            }
            deslocbitDESENQUAD--;
          }
          contCaracteresInseridos++;
          controllerBitsDESENQUADRAMENTO++;
          controlFlagFAKE = false;
          indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
          auxFlags--;
        }else{
        if(positionFlags){
          positionFlags = false;
          auxFlags--;
        }
        if(auxFlags%4 == 0 && auxFlags != 0){
          auxFlags = 0;
          positionFlags = true;
        }
        }
        } else if (BitComparation.equals(valueEscape)) {
          controlFlagFAKE = true;
        } else {
          deslocbitDESENQUAD = cG.setDeslocamentoBIT(controllerBitsDESENQUADRAMENTO, deslocbitDESENQUAD);
          for (int j = 0; j < 8; j++) { // For para cada Bit
            // Estrutura de IF que manipula bit por Bit
            if (BitComparation.charAt(j) == '1') {
              // define o bit na posicaoo deslocBit do quadro[indexQuadro] como 1
              quadroDesenquadrado[indexQuadro] = quadroDesenquadrado[indexQuadro] | (1 << deslocbitDESENQUAD);
            }
            deslocbitDESENQUAD--;
          }
          contCaracteresInseridos++;
          controllerBitsDESENQUADRAMENTO++;
          controlFlagFAKE = false;
          indexQuadro = cG.atualizaIndiceArrayEnquadrado(contCaracteresInseridos, indexQuadro);
        }
        auxFlags++;
    } // Fim For Mensagem Completa
    return quadroDesenquadrado;
  }//fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBytes

  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {
    // Criando Novo quadro com o novo tamanho
    int qtdBits = cG.getQtdBitsInsercaoBits();

    int quadroEnquadrado[] = new int[tamanhoDesenquadro]; // Novo Array Quadro
    int indexQuadro;
    int flagCount = 0;
    indexQuadro = tamanhoDesenquadro - 1;
    int bitsENQUAD = qtdBits -1;
    String Flag = "01111110"; // flag inicial e final
    int somatorioBITS1 = 0;
    int deslocBits = (cG.getCaracteresAnterior() * 8) - 1;

    // For até o tamanho da Mensagem
    for (int i = (qtdBits - 1); i >= 0; i--) {
      bitsENQUAD = bitsENQUAD % 32;
      // Quadros de Tamanho 3
      if (flagCount == 0 || flagCount == 24) { //Posicao Onde Tem os Flags Bits
        String BitComparation = "";
        for (int j = 0; j < 8; j++) { // For para cada Bit
          int mascara = 1 << bitsENQUAD;
          int Bit = (quadro[i / 32] & mascara) >> bitsENQUAD; // Recupera o Bit
          if (Bit == 1) {
            BitComparation += "1";
          } else {
            BitComparation += "0";
          }
          bitsENQUAD--;
          if (bitsENQUAD == -1) { // Verifica o Deslocamento dos Bits
            bitsENQUAD = 31;
          }
        }
        if (BitComparation.equals(Flag)) {
          // Flag de Verificacao Inicial ou Final do Quadro
        }
        flagCount = 0;
        i = i - 8;
      } // Fim If Controle do Enquadramento (insercao de bytes)
      deslocBits = deslocBits % 32;
      int mascara = 1 << bitsENQUAD;
      int Bit = (quadro[i / 32] & mascara) >> bitsENQUAD; //Recupera o Bit
      // Estrutura de IF que manipula bit por Bit
      if (Bit == 1 || Bit == -1) {
        quadroEnquadrado[indexQuadro] = quadroEnquadrado[indexQuadro] | (1 << deslocBits);
        somatorioBITS1++;
      } else {
        somatorioBITS1 = 0;
      }
      if (somatorioBITS1 == 5) {
        bitsENQUAD--; // Ignora o Proximo Bit, pois eh de Controle para Tirar as Flags Fakes
        i--;
        somatorioBITS1 = 0;
      }
      bitsENQUAD--;
      deslocBits--;
      if (bitsENQUAD == -1) {
        bitsENQUAD = 31;
      }
      if (deslocBits == -1) {
        deslocBits = 31;
        indexQuadro--;
      }
      flagCount++;

      if (i-7 == 0) { //Posicao Onde Tem os Flags Bits
        String BitComparation = "";
        for (int j = 0; j < 8; j++) { // For para cada Bit
          int mascara2 = 1 << bitsENQUAD;
          int Bit2 = (quadro[i / 32] & mascara2) >> bitsENQUAD; // Recupera o Bit
          if (Bit2 == 1) {
            BitComparation += "1";
          } else {
            BitComparation += "0";
          }
          bitsENQUAD--;
          if (bitsENQUAD == -1) { // Verifica o Deslocamento dos Bits
            bitsENQUAD = 31;
          }
        }
        if (BitComparation.equals(Flag)) {
          // Flag de Verificacao Inicial ou Final do Quadro
        }
        flagCount = 0;
        i = i - 8;
      } // Fim If Controle do Enquadramento (insercao de bytes)
    } // Fim For Bits
    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBits


  public int[] CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(int quadro []) {
    // Decodificacao realizada na Camada Fisica
    // Uma vez que ele viola a camada fisica, a decodificacao deve ser feita la
    // quando passamos o algoritmo de binario para manchester ou Manchester Differencial
  return quadro;
  }//fim do metodo CamadaEnlaceDadosReceptoraViolacaoDaCamadaFisica
}

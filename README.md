# Projeto SNMP
### **Gerência de Redes de Computadores - Turma 030 - 2022/1 - Prof. Cristina Moreira Nunes**
**Grupo:**
- Bruno Simm
- João Gabriel Bergamaschi
- Rafael Coll

**<div align="center"><h2>Enunciado do Trabalho Final</h2></div>**

### **Objetivo**

O objetivo deste trabalho é implementar um gerente SNMP que busque 
informações de objetos da MIB II e mostre os resultados encontrados. O gerente SNMP 
deverá permitir a execução de um conjunto de operações básicas e estendidas e deverá 
utilizar o protocolo SNMPv2C para a comunicação com o agente SNMP. 
### **Descrição e Execução**
O trabalho consiste em implementar um sistema de gerência usando o protocolo 
SNMP. O sistema deve implementar as seguintes operações:
- *GET*: faz uma requisição de GET a um agente usando a comunidade indicada com 
o objeto selecionado e instância especificada, e apresenta o resultado ao usuário;
- *GETNEXT*: faz uma requisição de GETNEXT a um agente usando a comunidade 
indicada com o objeto selecionado e instância especificada (pode ser nula), e 
apresenta o resultado ao usuário;
- *SET*: pede para o usuário indicar o valor a ser colocado no objeto, faz uma 
requisição de SET a um agente usando a comunidade indicada com o objeto 
selecionado, instância especificada e valor a ser atribuído, e apresenta o resultado 
ao usuário;
- *GETBULK*: solicita ao usuário os valores para os parâmetros N (non-repeaters) e 
M (max-repetitions), faz uma requisição de GETBULK a um agente usando a 
comunidade indicada com o objeto selecionado, instância especificada e 
parâmetros indicados, e apresenta o resultado ao usuário;
- *WALK*: implementa o funcionamento do comando snmpwalk, realizando, a partir 
de um objeto selecionado, diversas requisições GETNEXT até que retorne um 
objeto que "saia" da sub-árvore indicada.
- *GETTABLE*: através da indicação de um objeto do tipo tabela da MIB, obtém todo 
o conteúdo da tabela e apresenta ao usuário as informações em um formato de 
tabela;
- *GETDELTA*: pede ao usuário os parâmetros tempo e amostras e realiza N 
requisições GET, sendo N o valor do parâmetro amostras; entre cada requisição 
deve haver um intervalo de M segundos, onde M é o valor do parâmetro tempo; o 
resultado a ser apresentado ao usuário é a diferença entre o resultado de uma 
operação atual e o resultado da operação anterior.
Para cada operação, será necessário indicar o endereço IP do agente, a 
comunidade e a instância do objeto, quando necessário. 
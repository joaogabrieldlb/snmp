package com.snmp.gerente;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.snmp.util.TextColor;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import de.vandermeer.asciitable.AsciiTable;

public class GerenteApp {

    private CommunityTarget target;
    private Snmp snmp;
    private PDU requestPDU;
    private ResponseEvent response;
    private int nonRepeaters = 0;
    private int maxRepetitions = 0;
    private int tempo = 0;
    private int amostras = 0;

    public GerenteApp(String ip_agente, String comunidade) throws IOException {
        // community settings
        this.target = new CommunityTarget();
        this.target.setCommunity(new OctetString(comunidade));
        this.target.setAddress(new UdpAddress(InetAddress.getByName(ip_agente), 161));
        this.target.setVersion(SnmpConstants.version2c);

        this.snmp = new Snmp(new DefaultUdpTransportMapping());
        this.snmp.listen();

        this.requestPDU = new PDU();
    }

    public void run(String[] gerente_args) throws Exception {
        switch (gerente_args[0].toUpperCase()) {
            case "GET":
                if (gerente_args.length != 2)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 1 parametro.");
                executeGet(gerente_args[1]); // oid
                break;
            case "GETNEXT":
                if (gerente_args.length > 2)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir no maximo 1 parametro.");
                if (gerente_args.length == 1) {
                    executeGetNext(""); // oid null
                } else {
                    executeGetNext(gerente_args[1]); // oid
                }
                break;
            case "GETBULK":
                if (gerente_args.length < 4)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir no minimo 3 parametros.");
                try {
                    nonRepeaters = Integer.parseInt(gerente_args[1]);
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("Valor de non-repeaters (N) deve ser inteiro.");
                }
                if (nonRepeaters < 0)
                    throw new InvalidParameterException("Valor de non-repeaters (N) deve ser maior ou igual a zero.");
                try {
                    maxRepetitions = Integer.parseInt(gerente_args[2]);
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("Valor de max-repetitions (M) deve ser inteiro.");
                }
                if (maxRepetitions < 0)
                    throw new InvalidParameterException("Valor de max-repetitions (M) deve ser maior ou igual a zero.");
                executeGetBulk(nonRepeaters, maxRepetitions, Arrays.copyOfRange(gerente_args, 3, gerente_args.length)); // non-repeaters,
                                                                                                                        // max_repetitions,
                                                                                                                        // oid(s)
                break;
            case "GETTABLE":
                if (gerente_args.length != 2)
                throw new InvalidParameterException(
                        "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 1 parametro.");
                executeGetTable(gerente_args[1]); // oid
                break;
            case "GETDELTA":
                if (gerente_args.length != 4)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 3 parametros.");
                try {
                    tempo = Integer.parseInt(gerente_args[1]);
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("Valor de tempo (M) deve ser inteiro.");
                }
                if (tempo <= 0)
                    throw new InvalidParameterException("Valor de tempo (M) deve ser positivo.");
                try {
                    amostras = Integer.parseInt(gerente_args[2]);
                } catch (NumberFormatException e) {
                    throw new InvalidParameterException("Valor de amostras (N) deve ser inteiro.");
                }
                if (amostras <= 0)
                    throw new InvalidParameterException("Valor de amostras (N) deve ser positivo.");
                executeGetDelta(tempo, amostras, gerente_args[3]); // tempo, amostras, oid(s)
                break;
            case "WALK":
                if (gerente_args.length != 2)
                throw new InvalidParameterException(
                    "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 1 parametro.");
                executeWalk(gerente_args[1]);
                break;
            case "SET":
                if (gerente_args.length != 3)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 2 parametros.");
                executeSet(gerente_args[1], gerente_args[2]); // oid, content
                break;
            default:
                throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
        }
    }

    private void executeGet(String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        this.response = this.snmp.get(requestPDU, target);
        imprimeResponse(this.response);
    }

    private void executeGetNext(String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        this.response = this.snmp.getNext(requestPDU, target);
        imprimeResponse(this.response);
    }

    private void executeSet(String oid, String conteudo) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid), new OctetString(conteudo)));
        // Quando for SET, precisa setar a comunidade! (private)
        this.response = this.snmp.set(requestPDU, target);
        imprimeResponse(this.response);
    }

    // non_repeaters, max_repetitions
    private void executeGetBulk(int nonRepeaters, int maxRepetitions, String[] oids) throws IOException {
        for (String oid : oids) {
            this.requestPDU.add(new VariableBinding(new OID(oid)));
        }
        this.requestPDU.setNonRepeaters(nonRepeaters);
        this.requestPDU.setMaxRepetitions(maxRepetitions);
        this.response = this.snmp.getBulk(requestPDU, target);
        imprimeResponse(this.response);
    }

    private void executeWalk(String oid) throws IOException {
        TreeUtils treeUtils = new TreeUtils(this.snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.walk(target, new OID[] { new OID(oid) });

        for (TreeEvent event : events) {
            VariableBinding[] list = event.getVariableBindings();
            if (event.isError() || list == null || list.length == 0) {
                continue;
            }
            
            for(VariableBinding vb: event.getVariableBindings()) {
                System.out.println(vb.toString());
            }
        }
        System.out.println("Fim da operação WALK.");
    }

    private void executeGetTable(String oid) {
        if (!oid.startsWith("."))
            oid = "." + oid;
        TableUtils tUtils = new TableUtils(this.snmp, new DefaultPDUFactory());
        List<TableEvent> events = tUtils.getTable(this.target, new OID[] { new OID(oid) }, null, null);

        int firstIndex = events.get(0).getIndex().toIntArray()[1];
        int countColumns = 0;
        for (int index = 0; index <= events.size(); index++) {
            int actualIndex = events.get(index).getIndex().toIntArray()[1];
            if(actualIndex == firstIndex){
                countColumns++;
            } else {
                break;
            }
        }
        int larguraTabela = 35 * countColumns;

        AsciiTable titulo = new AsciiTable();
        titulo.addRule();
        titulo.addRow("Table = " + oid);
        titulo.addRule();
        String rendTitulo = titulo.render(larguraTabela);
        
        AsciiTable tabela = new AsciiTable();
        List<String> rowData = new ArrayList<>();
        tabela.addRule();
        for (int i = 0; i < events.size(); i = i + countColumns) {
            rowData.clear();
            for (int j = i; j < i + countColumns; j++) {
                String celula;
                if (i == 0) {
                    celula = "Indice = " + events.get(j).getColumns()[0].toValueString();
                } else {
                    celula = events.get(j).getIndex() + " = " + events.get(j).getColumns()[0].toValueString();
                }
                rowData.add(celula);
            }
            tabela.addRow(rowData);
            tabela.addRule();
        }
        String rendTabela = tabela.render(larguraTabela);
        
        System.out.println(rendTitulo);
        System.out.println(rendTabela);
    }
        
    private void executeGetDelta(int tempo, int amostras, String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        this.response = this.snmp.get(requestPDU, target);
        System.out.println(this.response.getResponse().toString());

        Long resultLong = null;
        Long deltaLong = null;
        Double resultDouble = null;
        Double deltaDouble = null;
        try {
            resultLong = Long.parseLong(this.response.getResponse().get(0).toValueString());
        } catch (NumberFormatException e1) {
            try {
                resultDouble = Double.parseDouble(this.response.getResponse().get(0).toValueString());
            } catch (NumberFormatException e2) {
                throw new InvalidParameterException("Valor do objeto (OID) deve retornar um valor numerico.");
            }
        }
        
        StringBuilder output;
        for (int i = 1; i <= amostras; i++) {
            try {
                TimeUnit.SECONDS.sleep(tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            output = new StringBuilder();
            output.append("[T" + i + "]\t");
            this.response = snmp.get(requestPDU, target);
            if (this.response.getResponse() == null) {
                // request timed out
                output.append(TextColor.red + "timeout" + TextColor.defaultColor);
            } else {
                output.append("Delta = ");
                if (resultLong != null) {
                    deltaLong = Long.parseLong(this.response.getResponse().get(0).toValueString()) - resultLong;
                    resultLong = Long.parseLong(this.response.getResponse().get(0).toValueString());
                    output.append(deltaLong);
                } else if (resultDouble != null) {
                    deltaDouble = Double.parseDouble(this.response.getResponse().get(0).toValueString()) - resultDouble;
                    resultDouble = Double.parseDouble(this.response.getResponse().get(0).toValueString());
                    output.append(deltaDouble);
                }
            }

            System.out.println(output);
        }
    }

    private void imprimeResponse(ResponseEvent response) {
        StringBuilder output = new StringBuilder();
        if (response.getResponse() == null) {
            // request timed out
            output.append("[" + TextColor.red + "ERRO" + TextColor.defaultColor + "] ");
            output.append("Envio do request expirou (timeout).");
        } else {
            System.out.println("Response recebido de: " + response.getPeerAddress());
            // System.out.println("lenght: " + response.getResponse().size());
            // dump response PDU
            if (response.getResponse().getErrorStatus() == 0) {
                output.append("[" + TextColor.lightGreen + "OK" + TextColor.defaultColor + "] ");
            } else {
                output.append("[" + TextColor.red + "ERRO" + TextColor.defaultColor + "] ");
            }
            output.append(responseString(response.getResponse()));
        }
        System.out.println(output);
    }

    private String responseString(PDU pdu) {
        StringBuilder responseString = new StringBuilder();
        responseString.append(PDU.getTypeString(pdu.getType()));
        responseString.append("[requestID=");
        responseString.append(pdu.getRequestID());
        responseString.append(", errorStatus=");
        responseString.append(pdu.getErrorStatusText()).append("(").append(pdu.getErrorStatus()).append(")");
        responseString.append(", errorIndex=");
        responseString.append(pdu.getErrorIndex());
        responseString.append("] Objeto(s):\n");
        for (int i = 0; i < pdu.size(); i++) {
            responseString.append("[" + (i + 1) + "]\t").append(pdu.get(i)).append("\n");
        }
        return responseString.toString();
    }
}

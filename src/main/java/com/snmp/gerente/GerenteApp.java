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
                executeWalk(gerente_args[1]);
                break;
            case "SET":
                if (gerente_args.length != 3)
                    throw new InvalidParameterException(
                            "Operacao " + gerente_args[0].toUpperCase() + " deve possuir 2 parametros.");
                executeSet(gerente_args[1], gerente_args[2]); // oid, content
                break;
            // // test case
            // case "TEST":
            // executeTest(oid);
            // break;
            default:
                throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
        }
    }

    // private static void verifyArguments(List<String> args) {
    // switch(args.get(3).toUpperCase()) {
    // case "GET":
    // case "GETTABLE":
    // case "GETNEXT": // args = "gerente", ip_target, comunidade_target, command,
    // oid
    // if (args.size() != 5) App.printHelpMessageAndExitProgram(args);
    // break;
    // case "GETDELTA": // args = "gerente", ip_target, comunidade_target, command,
    // time, oid(s)
    // if (args.size() < 6) App.printHelpMessageAndExitProgram(args);
    // break;
    // case "GETBULK": // args = "gerente", ip_target, comunidade_target, command,
    // non-repeaters, max_repetitions, oid(s)
    // if (args.size() < 7) App.printHelpMessageAndExitProgram(args);
    // break;
    // case "WALK":
    // //TODO: verify WALK operation
    // break;
    // case "SET": // args = "gerente", ip_target, comunidade_target, command, oid,
    // content
    // if (args.size() != 6) App.printHelpMessageAndExitProgram(args);
    // break;
    // default:
    // throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
    // }
    // }

    private void executeGet(String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        this.response = this.snmp.get(requestPDU, target);
        imprimeResponse(response);
    }

    private void executeGetNext(String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        this.response = this.snmp.getNext(requestPDU, target);
        imprimeResponse(response);
    }

    private void executeSet(String oid, String conteudo) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid), new OctetString(conteudo)));
        // Quando for SET, precisa setar a comunidade! (private)
        this.response = this.snmp.set(requestPDU, target);
        imprimeResponse(response);
    }

    // non_repeaters, max_repetitions
    private void executeGetBulk(int nonRepeaters, int maxRepetitions, String[] oids) throws IOException {
        for (String oid : oids) {
            this.requestPDU.add(new VariableBinding(new OID(oid)));
        }
        this.requestPDU.setNonRepeaters(nonRepeaters);
        this.requestPDU.setMaxRepetitions(maxRepetitions);
        this.response = this.snmp.getBulk(requestPDU, target);
        imprimeResponse(response);
    }

    private void executeWalk(String oid) throws IOException {
        TreeUtils treeUtils = new TreeUtils(this.snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.walk(target, new OID[] { new OID(oid) });

        for (TreeEvent event : events) {
            VariableBinding[] list = event.getVariableBindings();
            if (event.isError() || list == null || list.length == 0) {
                System.out.println("Fim da operação WALK");
                continue;
            }
            
            for(VariableBinding vb: event.getVariableBindings()) {
                System.out.println(vb.toString());
            }
        }
    }

    private void executeGetTable(String oid) {
        if (!oid.startsWith("."))
            oid = "." + oid;
        TableUtils tUtils = new TableUtils(this.snmp, new DefaultPDUFactory());
        List<TableEvent> events = tUtils.getTable(this.target, new OID[] { new OID(oid) }, null, null);

        AsciiTable asciiTable = new AsciiTable();

        int countIndexes = 0;
        int maxIndex = 0;
        for (int index = 0; index <= events.size(); index++) {
            int actualIndex = events.get(index).getIndex().last();
            // System.out.println(events.get(index).getIndex().toIntArray()[1]);
            //int actualIndex = events.get(index).getIndex().toIntArray()[1];
            if(actualIndex > maxIndex){
                countIndexes++;
                maxIndex = actualIndex;
            } else {
                break;
            }
        }

        System.out.println(countIndexes);
        System.out.println(maxIndex);
        System.out.println("--------------------");

        List<String> rowData = new ArrayList<>();
        asciiTable.addRule();
        for (int i = 0; i < events.size(); i = i + countIndexes) {
            rowData.clear();
            for (int j = i; j < i + countIndexes; j++) {
                System.out.println(events.get(j).getIndex().toIntArray()[1]);
                String celula = events.get(j).getIndex() + " = " + events.get(j).getColumns()[0].toValueString();
                rowData.add(celula);
            }
            asciiTable.addRow(rowData);
            asciiTable.addRule();
        }

        String rend = asciiTable.render(140);
        System.out.println("Table = " + oid);
        System.out.println(rend);



        // int maxValue = 0;
        // for (TableEvent event : events) {
        //     if (event.isError()) {
        //         continue;
        //         // throw new RuntimeException(event.getErrorMessage());
        //     }
        //     // String[] args = event.getIndex().toString().split(".");
        //     int index = event.getIndex().last();
        //     System.out.println("INDICE LAST> " + index); 
        //     //Math.max(a, b);
        //     //1,2,3,4
        //     //oid.1 = 2, oid.5 = 3, oid.7 = 43, oid.88 = 56 

            
           

        //     System.out.println();
        //     // teste = new List<String>();
        //     // for (VariableBinding vb : event.getColumns()) {
                
        //     //     // String key = vb.getOid().toString();
        //     //     // String value = vb.getVariable().toString();            
        //     //     // System.out.println(key + " - " + value);
        //     // }
        //     System.out.println("===================");
        // }
    }
        
    private void executeGetTable2(String oid) {
        if (!oid.startsWith("."))
            oid = "." + oid;
        TableUtils tUtils = new TableUtils(this.snmp, new DefaultPDUFactory());
        List<TableEvent> events = tUtils.getTable(this.target, new OID[] { new OID(oid) }, null, null);
                
        AsciiTable asciiTable = new AsciiTable();
        int count = 0;
        int maxIndex = 0;
        for (int index = 0; index <= events.size(); index++) {
            int actualIndex = events.get(index).getIndex().last();
            if(actualIndex > maxIndex){
                count++;
                maxIndex = actualIndex;
            } else {
                break;
            }
        }

        for (TableEvent event : events) {
            if (event.isError()) {
                continue;
                // throw new RuntimeException(event.getErrorMessage());
            }
           
            //1,2,3,4
            //oid.1 = 2, oid.5 = 3, oid.7 = 43, oid.88 = 56 
            // teste = new List<String>();
            // for (VariableBinding vb : event.getColumns()) {
                
            //     // String key = vb.getOid().toString();
            //     // String value = vb.getVariable().toString();            
            //     // System.out.println(key + " - " + value);
            // }
            System.out.println("===================");
        }
    }

    private void executeGetDelta(int tempo, int amostras, String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        ResponseEvent response;
        response = this.snmp.get(requestPDU, target);
        System.out.println(response.getResponse().toString());

        Long resultLong = null;
        Long deltaLong = null;
        Double resultDouble = null;
        Double deltaDouble = null;
        try {
            resultLong = Long.parseLong(response.getResponse().get(0).toValueString());
        } catch (NumberFormatException e1) {
            try {
                resultDouble = Double.parseDouble(response.getResponse().get(0).toValueString());
            } catch (NumberFormatException e2) {
                throw new InvalidParameterException("Valor do objeto (OID) deve retornar um valor numerico.");
            }
        }
        
        StringBuilder msg;
        for (int i = 0; i < amostras; i++) {
            try {
                TimeUnit.SECONDS.sleep(tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            msg = new StringBuilder("[T" + (i + 1) + "]\t");
            response = snmp.get(requestPDU, target);
            if (response.getResponse() == null) {
                // request timed out
                msg.append(TextColor.red + "timeout" + TextColor.defaultColor);
            } else {
                if (resultLong != null) {
                    deltaLong = Long.parseLong(response.getResponse().get(0).toValueString()) - resultLong;
                    resultLong = Long.parseLong(response.getResponse().get(0).toValueString());
                    msg.append(deltaLong);
                } else if (resultDouble != null) {
                    deltaDouble = Double.parseDouble(response.getResponse().get(0).toValueString()) - resultDouble;
                    resultDouble = Double.parseDouble(response.getResponse().get(0).toValueString());
                    msg.append(deltaDouble);
                }
            }

            System.out.println(msg);
        }
    }

    private void imprimeResponse(ResponseEvent response) {
        String result = "";
        if (response.getResponse() == null) {
            // request timed out
            result += "[" + TextColor.red + "ERRO" + TextColor.defaultColor + "] ";
            result += "Envio do Request expirou (time out).";
        } else {
            System.out.println("Response recebido de: " + response.getPeerAddress());
            System.out.println("lenght: " + response.getResponse().size());
            // dump response PDU
            if (response.getResponse().getErrorStatus() == 0) {
                result += "[" + TextColor.lightGreen + "OK" + TextColor.defaultColor + "] ";
            } else {
                result += "[" + TextColor.red + "ERRO" + TextColor.defaultColor + "] ";
            }
            result += responseString(response.getResponse());
        }
        System.out.println(result);
    }

    private String responseString(PDU pdu) {
        StringBuilder buf = new StringBuilder();
        buf.append(PDU.getTypeString(pdu.getType()));
        buf.append("[requestID=");
        buf.append(pdu.getRequestID());
        buf.append(", errorStatus=");
        buf.append(pdu.getErrorStatusText()).append("(").append(pdu.getErrorStatus()).append(")");
        buf.append(", errorIndex=");
        buf.append(pdu.getErrorIndex());
        buf.append("] Objeto(s):\n");
        for (int i = 0; i < pdu.size(); i++) {
            buf.append("[" + (i + 1) + "]\t" + pdu.get(i) + "\n");
        }
        return buf.toString();
    }

    // private void executeTest(String oid) throws IOException {
    // // community settings
    // CommunityTarget target = new CommunityTarget();
    // target.setCommunity(new OctetString("private"));
    // target.setAddress(new UdpAddress(InetAddress.getByName("localhost"), 161));
    // target.setVersion(SnmpConstants.version2c);

    // Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
    // snmp.listen();

    // PDU requestPDU = new PDU();

    // requestPDU.add(new VariableBinding(new OID(oid), new OctetString("TESTE
    // FOI2")));
    // //Quando for SET, precisa setar a comunidade! (private)
    // this.response = snmp.set(requestPDU, target);
    // // snmp.getNext(pdu, target)
    // // snmp.getBulk(pdu, target)
    // // snmp.get(pdu, target)
    // if (response.getResponse() == null) {
    // // request timed out

    // }
    // else {
    // System.out.println("Received response from: " + response.getPeerAddress());

    // // dump response PDU
    // System.out.println(response.getResponse().toString());
    // }
    // }
}

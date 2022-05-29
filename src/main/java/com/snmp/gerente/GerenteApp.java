package com.snmp.gerente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.snmp.App;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class GerenteApp {

    private CommunityTarget target;
    private Snmp snmp;
    private PDU requestPDU;

    private int DEFAULT_MAX_REPETITIONS_VALUE = 0;
    private int DEFAULT_NON_REPETERS_VALUE = 0;

    public GerenteApp(String ip_target, String comunidade_target) throws IOException{
        // community settings
        this.target = new CommunityTarget();
        this.target.setCommunity(new OctetString(comunidade_target));
        this.target.setAddress(new UdpAddress(InetAddress.getByName(ip_target), 161));
        this.target.setVersion(SnmpConstants.version2c);
        
        this.snmp = new Snmp(new DefaultUdpTransportMapping());
        this.snmp.listen();
    
        this.requestPDU = new PDU();
    }

    public void run(List<String> args) throws Exception {
        verifyArguments(args);
        switch (args.get(3).toUpperCase()) {
            case "GET":
                executeGet(args.get(4));        // oid
                break;
            case "GETNEXT":
                executeGetNext(args.get(4));    // oid
                break;
            case "GETBULK":
                executeGetBulk(args.get(4), args.get(5), args.subList(6, args.size() - 1));   // non-repeaters, max_repetitions, oid(s)
                break;
            case "GETTABLE":
                executeGetTable(args.get(4));   // oid
                break;
            case "GETDELTA":
                executeGetDelta(args.get(4), args.subList(5, args.size() - 1)); // time, oid(s)
                break;
            case "WALK":
                executeWalk(oid);
                break;
            case "SET":
                executeSet(args.get(4), args.get(5));   // oid, content
                break;
            // // test case
            // case "TEST":
            //     executeTest(oid);
            //     break;
            default:
                throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
        }
    }

    private static void verifyArguments(List<String> args) {
        switch(args.get(3).toUpperCase()) {
            case "GET":
            case "GETTABLE":
            case "GETNEXT":     // args = "gerente", ip_target, comunidade_target, command, oid
                if (args.size() != 5) App.printHelpMessageAndExitProgram(args);
                break;
            case "GETDELTA":    // args = "gerente", ip_target, comunidade_target, command, time, oid(s)
                if (args.size() < 6) App.printHelpMessageAndExitProgram(args);
                break;
            case "GETBULK":     // args = "gerente", ip_target, comunidade_target, command, non-repeaters, max_repetitions, oid(s)
                if (args.size() < 7) App.printHelpMessageAndExitProgram(args);
                break;
            case "WALK":
                //TODO: verify WALK operation
                break;
            case "SET":     // args = "gerente", ip_target, comunidade_target, command, oid, content
                if (args.size() != 6) App.printHelpMessageAndExitProgram(args);
                break;
            default:
                throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
        }
    }

    private void executeGet(String oid) throws IOException {
        this.requestPDU.add(new VariableBinding(new OID(oid)));
        ResponseEvent response = this.snmp.get(requestPDU, target);
        if (response.getResponse() == null) {
            // request timed out
            
        }
        else {
            System.out.println("Received response from: " + response.getPeerAddress());
            
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }
    
    private void executeGetNext(String oid) throws IOException {
        requestPDU.add(new VariableBinding(new OID(oid)));
        ResponseEvent response = snmp.getNext(requestPDU, target);
        if (response.getResponse() == null) {
            // request timed out
            
        }
        else {
            System.out.println("Received response from: " + response.getPeerAddress());
            
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }
    
    private void executeSet(String oid, String conteudo) throws IOException {
        requestPDU.add(new VariableBinding(new OID(oid), new OctetString(conteudo)));
        //Quando for SET, precisa setar a comunidade! (private)
        ResponseEvent response = this.snmp.set(requestPDU, target);
        if (response.getResponse() == null) {
            // request timed out
            
        }
        else {
            System.out.println("Received response from: " + response.getPeerAddress());
            
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }
    // non-repeaters, max_repetitions
    private void executeGetBulk(String nonRepeaters, String maxRepetitions, List<String> oids) throws IOException {

        requestPDU.add(new VariableBinding(new OID(oid)));
        ResponseEvent response = snmp.getBulk(requestPDU, target);
        if (response.getResponse() == null) {
            // request timed out
            
        }
        else {
            System.out.println("Received response from: " + response.getPeerAddress());
            
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }
    
    private void executeWalk(String oid) {
    }
    
    private void executeGetTable(String oid) {
    }
    
    private void executeGetDelta(String time, List<String> oids) {
    }

    // private void executeTest(String oid) throws IOException {
    //     // community settings
    //     CommunityTarget target = new CommunityTarget();
    //     target.setCommunity(new OctetString("private"));
    //     target.setAddress(new UdpAddress(InetAddress.getByName("localhost"), 161));
    //     target.setVersion(SnmpConstants.version2c);
        
    
    //     Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
    //     snmp.listen();
    
    //     PDU requestPDU = new PDU();
        
    //     requestPDU.add(new VariableBinding(new OID(oid), new OctetString("TESTE FOI2")));
    //     //Quando for SET, precisa setar a comunidade! (private)
    //     ResponseEvent response = snmp.set(requestPDU, target);
    //     // snmp.getNext(pdu, target)
    //     // snmp.getBulk(pdu, target)
    //     // snmp.get(pdu, target)
    //     if (response.getResponse() == null) {
    //         // request timed out
            
    //     }
    //     else {
    //         System.out.println("Received response from: " + response.getPeerAddress());
            
    //         // dump response PDU
    //         System.out.println(response.getResponse().toString());
    //     }
    // }
}

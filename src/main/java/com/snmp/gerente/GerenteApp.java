package com.snmp.gerente;

import java.security.InvalidParameterException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class GerenteApp {
    
    public void run(String command, String oid) throws Exception {
        switch (command.toUpperCase()) {
            case "GET":
                executeGet(oid);
                break;
            case "GETNEXT":
                executeGetNext(oid);
                break;
            case "SET":
                executeSet(oid);
                break;
            case "GETBULK":
                executeGetBulk(oid);
                break;
            case "WALK":
                executeWalk(oid);
                break;
            case "GETTABLE":
                executeGetTable(oid);
                break;
            case "GETDELTA":
                executeGetDelta(oid);
                break;
            // test case
            case "TEST":
                executeTest(oid);
                break;
            default:
                throw new InvalidParameterException("\n\t[!] Operacao invalida!\n");
        }
    }

    private void executeGet(String oid) {
    }
    
    private void executeGetNext(String oid) {
    }
    
    private void executeSet(String oid) {
    }

    private void executeGetBulk(String oid) {
    }
    
    private void executeWalk(String oid) {
    }
    
    private void executeGetTable(String oid) {
    }
    
    private void executeGetDelta(String oid) {
    }

    private void executeTest(String oid) {
        // community settings
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(IpAddress.parse("127.0.0.1"));
        target.setVersion(SnmpConstants.version2c);
    
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen();
    
        PDU requestPDU = new PDU();
        requestPDU.add(new VariableBinding(new OID(oid)));
        ResponseEvent response = snmp.send(requestPDU, target);
        // snmp.getNext(pdu, target)
        // snmp.getBulk(pdu, target)
        // snmp.get(pdu, target)
        if (response.getResponse() == null) {
            // request timed out
            
        }
        else {
            System.out.println("Received response from: " + response.getPeerAddress());
            // dump response PDU
            System.out.println(response.getResponse().toString());
        }
    }
}

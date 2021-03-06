package com.snmp.util;

import java.io.IOException;

import com.snmp.agente.AgenteApp;
import com.snmp.gerente.GerenteApp;

public class SnmpFactory {
    
    public static AgenteApp criaAgente() {
        return new AgenteApp();
    }

    public static GerenteApp criaGerente(String ip_agente, String comunidade) throws IOException {
        return new GerenteApp(ip_agente, comunidade);
    }
}

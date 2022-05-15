package com.snmp.util;

import com.snmp.agente.AgenteApp;
import com.snmp.gerente.GerenteApp;

public class SnmpFactory {
    
    public static AgenteApp criaAgente() {
        return new AgenteApp();
    }

    public static GerenteApp criaGerente() {
        return new GerenteApp();
    }
}

package com.snmp;

import com.snmp.util.SnmpFactory;

public class App {
    public static void main(String[] args) {
        if (args.length != 1 || args.length != 3 ) {
            printHelpMessage();
            System.exit(1);
        }

        switch (args[0].toUpperCase()) {
            case "AGENTE":
                try {
                    SnmpFactory.criaAgente().run();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                break;
            case "GERENTE":
                try {
                    if (args.length == 3)
                        SnmpFactory.criaGerente().run(args[1], args[2]);
                    else
                        printHelpMessage();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                break;
            default:
                printHelpMessage();
        }

        System.exit(0);
    }

    public static void printHelpMessage() {
        System.out.println(
                "\n\tUso: java App agente | java App gerente operacao_snmpv2c oid\n");
    }
}
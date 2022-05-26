package com.snmp;

import com.snmp.util.SnmpFactory;

public class App {
    public static void main(String[] args) {
        if (!(args.length == 5 || args.length == 6)) {
            printHelpMessageAndExitProgram(args);
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
                    if (args.length <= 6){
                        if(args[3].equalsIgnoreCase("set") && args.length < 6){ //set sem conteudo
                            printHelpMessageAndExitProgram(args);
                        }
                        if(args[3].equalsIgnoreCase("set")){ //set
                            SnmpFactory.criaGerente(args[1], args[2]).run(args[3], args[4], args[5]); //ip_target, comunidade_target, command, oid, conteudo
                        }
                        SnmpFactory.criaGerente(args[1], args[2]).run(args[3], args[4]); //ip_target, comunidade_target, command, oid
                    } else {
                        printHelpMessageAndExitProgram(args);
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                break;
            default:
                printHelpMessageAndExitProgram(args);
        }

        System.exit(0);
    }

    public static void printHelpMessageAndExitProgram(String[] args) {
        System.out.print("\n[\u001B[31mERROR\u001B[0m] \u001B[37mNao foi possivel executar o programa ");
        if(args.length != 0) {
            System.out.print("com o(s) argumento(s): ");
            for (int i = 0; i < args.length; i++) {
                System.out.print("\"" + args[i] + "\"" + (i == (args.length - 1) ? "\u001B[0m\n" : ", "));
            }
        } else {
            System.out.print("sem os argumentos\n");
        }
        System.out.println("\n\t\u001B[32mFormas de uso:\u001B[0m");
        System.out.println("\t\u001B[32m 1. java App agente | java App gerente operacao_snmpv2c oid\u001B[0m");
        System.out.println("\t\u001B[32m 2. .\\run agente | .\\run gerente ip_target comunidade_target operacao_snmpv2c oid conteudo\u001B[0m\n");
        System.exit(1);
    }
}
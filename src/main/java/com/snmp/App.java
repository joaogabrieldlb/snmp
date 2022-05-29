package com.snmp;

import java.util.Arrays;
import java.util.List;

import com.snmp.util.SnmpFactory;
import com.snmp.util.TextColor;

public class App {
    public static void main(String[] args) {
        List<String> arguments = Arrays.asList(args);

        arguments.forEach(x => x.);

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
                    SnmpFactory.criaGerente(args[1], args[2]).run(arguments);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                break;
            default:
                printHelpMessageAndExitProgram(arguments);
        }

        System.exit(0);
    }

    public static void printHelpMessageAndExitProgram(List<String> args) {
        System.out.print("\n[" + TextColor.red + "ERROR"+ TextColor.defaultColor +"] \u001B[37mNao foi possivel executar o programa ");
        if(args.size() != 0) {
            System.out.print("com o(s) argumento(s): ");
            for (int i = 0; i < args.size(); i++) {
                System.out.print("\"" + args.get(i) + "\"" + (i == (args.size() - 1) ? TextColor.defaultColor + "\n" : ", "));
            }
        } else {
            System.out.print("sem os argumentos\n");
        }
        System.out.println("\n\t" + TextColor.lightGreen +"Formas de uso:"+ TextColor.defaultColor);
        System.out.println("\t" + TextColor.lightGreen +" 1. java App agente | java App gerente operacao_snmpv2c oid"+ TextColor.defaultColor);
        System.out.println("\t" + TextColor.lightGreen +" 2. .\\run agente | .\\run gerente ip_target comunidade_target operacao_snmpv2c oid conteudo"+ TextColor.defaultColor + "\n");
        System.exit(1);
    }
}
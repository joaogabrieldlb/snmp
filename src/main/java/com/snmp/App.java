package com.snmp;

import java.security.InvalidParameterException;
import java.util.Arrays;
import com.snmp.util.SnmpFactory;
import com.snmp.util.TextColor;

public class App {
    public static void main(String[] args) {
        // List<String> arguments = Arrays.asList(args);
        // arguments.forEach(x -> System.out.println("> " + x));
        if (args.length < 4) {
            printHelpMessageAndExitProgram(args);
        }

        switch (args[0].toUpperCase()) {
            case "AGENTE":
                try {
                    SnmpFactory.criaAgente().run();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    printHelpMessageAndExitProgram(args);
                }
                break;
            case "GERENTE":
                try {
                    String[] gerente_args = Arrays.copyOfRange(args, 3, args.length);
                    SnmpFactory.criaGerente(args[1], args[2]).run(gerente_args);
                } catch (InvalidParameterException ex) {
                    System.out.println(ex.getMessage());
                    printHelpMessageAndExitProgram(args);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    printHelpMessageAndExitProgram(args);
                }
                break;
            default:
                printHelpMessageAndExitProgram(args);
        }

        System.exit(0);
    }

    private static void printHelpMessageAndExitProgram(String[] args) {
        String helpMessage = "";
        helpMessage += "\n[" + TextColor.red + "ERRO" + TextColor.defaultColor + "] Nao foi possivel executar o programa ";
        if(args.length != 0) {
            helpMessage += "com o(s) argumento(s): ";
            for (int i = 0; i < args.length; i++) {
                helpMessage += "\"" + args[i] + "\"";
                helpMessage += (i == (args.length - 1) ? "\n" : ", ");
            }
        } else {
            helpMessage += "sem argumentos.";
        }
        helpMessage += "\n\t" + TextColor.lightGreen +"Formas de uso:";
        helpMessage += "\n\t- Modo GERENTE: [ java App gerente | ./gerente.sh ] <ip_agente> <comunidade> <operacao_snmpv2c> <oid(s)> <conteudo>\n" + TextColor.defaultColor;
        //helpMessage += "\n\t- Modo Agente: [ java App agente | ./agente.sh ] ";
        System.out.println(helpMessage);
        System.exit(1);
    }
}
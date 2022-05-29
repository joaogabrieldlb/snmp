#!/bin/sh
arguments="gerente "

for i in $@
do
    arguments=$arguments"$i "
done

mvn exec:java -Dexec.mainClass=com.snmp.App -Dexec.args="$arguments"
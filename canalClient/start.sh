#!/usr/bin/env bash
nohup java -jar  -Xms8g -Xmx8g -XX:NewRatio=2 -XX:+UseG1GC -XX:+UseCompressedOops -Dfile.encoding=UTF-8 canalClient-1.0-allinone.jar -XX:+HeapDumpAfterFullGC  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/booking/communicate/logs  &
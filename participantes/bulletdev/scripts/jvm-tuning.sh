#!/bin/bash

export JAVA_OPTS="
-Xmx512m
-Xms512m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1
-XX:+UnlockExperimentalVMOptions
-XX:+UseShenandoahGC
-XX:ShenandoahGCHeuristics=compact
-XX:+AlwaysPreTouch
-XX:+DisableExplicitGC
-XX:+PerfDisableSharedMem
-Dnetty.allocator.type=pooled
-Dnetty.allocator.maxOrder=9
-Dnetty.allocator.numHeapArenas=4
-Dnetty.allocator.numDirectArenas=2
-Dnetty.allocator.pageSize=8192
-Dnetty.allocator.maxCachedBufferCapacity=32768
-Dnetty.allocator.cacheTrimInterval=600
-Dnetty.recycler.maxCapacity=4096
-Dnetty.recycler.maxSharedCapacityFactor=2
-Dnetty.recycler.linkCapacity=16
-Dnetty.recycler.ratio=8
-Djava.awt.headless=true
-Dfile.encoding=UTF-8
-Duser.timezone=UTC
"

java $JAVA_OPTS -jar target/rinha-backend-1.0.0.jar

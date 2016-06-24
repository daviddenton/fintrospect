#!/bin/bash
./sbt clean coverage test
rm -rf target/*/scoverage*
./sbt coverageReport
./sbt coverageAggregate

#!/usr/bin/env bash

jarFile="$(ls -1 hyperledger-examples-chainexplorer-*-shaded.jar)"
java -jar "${jarFile}" server hyperLedger-dropwizard.yml

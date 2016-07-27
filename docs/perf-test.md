# How to run performance test

## Prepare fabric

The documentation for setting up a network can be found
[here](http://hyperledger-fabric.readthedocs.io/en/latest/dev-setup/devnet-setup/).
Setting up a network with 4 PBFT nodes on one machine is described here. It is
assumed, that you have fabric development environment including vagrant set up.
You have to change the consensus plugin in `peer/core.yaml` to `pbft`, and then
create the docker image of the peer with `make peer-image`. Make sure you
rebuild the image after changing code or config. Also make sure, that the
`CORE_PEER_DISCOVERY_ROOTNODE` points to the address of `vp0` (first node in
terminal 1), the node will print its address out on startup.

```bash
cd $GOPATH/src/github.com/hyperledger/fabric

#modfiy plugin to pbft
vim peer/core.yaml

make peer-image

# vagrant terminal 1
docker run --rm -it -e CORE_VM_ENDPOINT=http://172.17.0.1:2375 \
                    -e CORE_PEER_ID=vp0 \
                    -e CORE_PEER_ADDRESSAUTODETECT=true \
                    -p 30303:30303 \
                    -p 31315:31315 \
                    hyperledger/fabric-peer peer node start \
                    --logging-level=debug

# vagrant terminal 2
docker run --rm -it -e CORE_VM_ENDPOINT=http://172.17.0.1:2375 \
                    -e CORE_PEER_ID=vp1 \
                    -e CORE_PEER_DISCOVERY_ROOTNODE=172.17.0.2:30303 \
                    -e CORE_PEER_ADDRESSAUTODETECT=true \
                    hyperledger/fabric-peer peer node start

# vagrant terminal 3
docker run --rm -it -e CORE_VM_ENDPOINT=http://172.17.0.1:2375 \
                    -e CORE_PEER_ID=vp2 \
                    -e CORE_PEER_DISCOVERY_ROOTNODE=172.17.0.2:30303 \
                    -e CORE_PEER_ADDRESSAUTODETECT=true \
                    hyperledger/fabric-peer peer node start

# vagrant terminal 4
docker run --rm -it -e CORE_VM_ENDPOINT=http://172.17.0.1:2375 \
                    -e CORE_PEER_ID=vp3 \
                    -e CORE_PEER_DISCOVERY_ROOTNODE=172.17.0.2:30303 \
                    -e CORE_PEER_ADDRESSAUTODETECT=true \
                    hyperledger/fabric-peer peer node start
```

Now, you have the network set up, and can move on to setting up fabric-api

### Important settings for performance testing

* peer/core.yaml
 * `peer.validator.consensus.plugin:` plugin used for consensus, should be set to pbft
* consensus/pbft/config.yaml
 * `general.batchsize:` how much transactions are grouped for doing consensus

## Prepare fabric-api

There is a `PerfTest` class you can run for performance testing. By default,
the tests use in-memory implementation of the ledger, to speed up development.
For this reason, in the `PerfTest` class, the default `HLAPI` implementation is
the `DummyFabric`. You have to change this to `GRPCClient`. The top of the
class contains a few parameters for the test (like number of transactions), you
can tune these if needed. After you have this, you can run the test from your
IDE, or from a terminal:

```bash
# switch HLAPI implementation
vim api/src/test/java/org/hyperledger/api/connector/PerfTest.java

mvn test -Dtest=PerfTest
```

It will print the results at the end, something like this:

```
====== Test results ======

Total: 10000 transactions in 353.49 sec
Average transaction/s: 28.29
Average transaction process time: 15027.30 ms
Listener not called for 0 transactions
Listener called multiple times for 2 transactions
0 transactions not found in the ledger
0 transactions rejected
Distribution:
   0 -   1000:    tx/sec=34.83    avg_tx_time=1792.79 ms
1000 -   2000:    tx/sec=30.65    avg_tx_time=7089.22 ms
2000 -   3000:    tx/sec=26.52    avg_tx_time=13959.66 ms
3000 -   4000:    tx/sec=26.18    avg_tx_time=19009.87 ms
4000 -   5000:    tx/sec=26.60    avg_tx_time=18913.06 ms
5000 -   6000:    tx/sec=26.61    avg_tx_time=19227.61 ms
6000 -   7000:    tx/sec=27.60    avg_tx_time=18181.01 ms
7000 -   8000:    tx/sec=26.87    avg_tx_time=18792.81 ms
8000 -   9000:    tx/sec=26.39    avg_tx_time=19007.33 ms
9000 -  10000:    tx/sec=38.16    avg_tx_time=14299.58 ms
```

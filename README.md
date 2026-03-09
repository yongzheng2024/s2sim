# What is S2Sim
S2Sim serves as a plugin of [Batfish](https://github.com/batfish/batfish/tree/master) to diagnosing and repairing distributed routing configurations using *selective symbolic simulation*

For more details, please refer to our [paper](https://yrll.github.io/files//nsdi26spring-final155.pdf)

S2Sim is accepted by **NSDI 2026** 🎉


# Build Instruction

Clone the repository via `git`.

```bash
git clone https://github.com/sngroup-xmu/S2Sim.git
cd S2Sim
```

Build the project:

```bash
cd code/project
mvn package -Dmaven.test.skip=true
```

It might take a while. Our project is baselined on Batfish (2022) and uses Maven, while the current upstream has migrated to Bazel.



# Run S2Sim

Once you built the project, you can run the allinone-bundle-*.jar with the following command:

```bash
java -jar allinone/target/allinone-bundle-*.jar -runmode interactive -loglevel info -batfishmode workservice -coordinatorargs "-templatedirs questions"
```

## Dataset

The sythesized errorneous configurations we used in this paper are under `dataset-new` dictionary, and the corresponding errors for each configuration are listed in our paper (Appendix B, Table 4):

```text
 ── dataset-new
    ├── fattree-cfg         (fattree configs)
    ├── Topoplogy-zoo-cfg   (TopologyZoo configs synthesized by Netcomplete)
    └── multi-protocol      (synthesized from real configurations)
```

However, due to confidentiality agreements and collaboration constraints, we are unable to release the real network configurations.


## User Intents

For each network, we can set the intent (e.g., reachability) list in the `requirements.json` file. 

For example, this JSON snippet defines a reachability intent: node "ormoz" must reach network "128.0.1.0/24" in node "sezana". 

```bash
[{
    "srcNode": ["ormoz"],
    "dstNode": "sezana",
    "reqDstPrefixString": "128.0.1.0/24",
    "failure":0,
    "configRootPath": "dataset-new/Topology-zoo-cfg/Arnes/configs" # replace with your local absolute path before running
}]

```


**Field-by-Field Breakdown:**

srcNode: Source Node(s).

dstNode: Destination Node. 

reqDstPrefixString: Target IP Prefix.

failure: a failure scenario specifier that requires the intent to hold under up to arbitrary K link failures (0 means no failure).

configRootPath: The absolute configuration path.

## Diagnose and Repair

After starting the batfish service, you can diagnose a configuration following these steps:

1. initiate a network configuration

```bash
init-snapshot dataset-new/Topology-zoo-cfg/Arnes # replace with your local absolute path before runnin
```

2. first simulation (concrete simulation to get the erroneous dataplane)
```bash
get routes
```

3. second simulation (selective symbolic simulation)
```bash
get routes symbolic
```

After that, there will generate a `result.txt` under the configuration dictionary, showing the configuration lines that violate the user intents.

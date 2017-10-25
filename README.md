# Blockchain benchmarking framework 
The main purpose of this project is measuring the performance of various implementations of blockchain technology.

## Use cases of framework
#### Use case 1 - Blockchain implementation selection

According to the chosen case of technology use, the most suitable for that case implementation of blockchain is selected.
Tests are conducted for different version and further the user receives information about key metrics for each implementation.
This case is interesting to those who wants to select the most appropriate implementation of blockchain to solve a specific problem 
without a full-fledged prototype development.

#### Use case 2 - Blockchain profiling

The same scenario is running on different versions of the same implementation of blockchain and results are compared. 
This case may be interesting to blockchain developers for solving optimization problems.

#### Use case 3 - Scalability tests
Determination of the dependence of performance on the growth of the number of blockchain's nodes. Simplification is achieved
through the use of automatic network deployment.

## Current progress
1. Created basic version of framework for load testing.
2. Hyperledger Fabric 0.6 and Ethereum supported.
3. The basic metrics were chosen.
4. Experiments were carried out with measurements of data and their subsequent analysis.


## Quick start
#### Run benchmark for Ethereum blockchain
All you need right now is Intellij IDEA, Java 1.8+, Gradle (>= 1.8), and remote Ubuntu instances, for example on Amazon EC2.
1. Open and this project in Intellij IDEA from build.gradle. 
2. Set up test-manager.properties in ethereum folder (choose needed properties for your testnet set up).
3. Create file with name **instances** in **ethereum** folder (file, which contains IP addresses to connect).
4. Run TestManagerMain class with program arguments **ethereum/test-manager.properties**.
5. You will get .csv files in **ethereum/logs** folder. That data can be analyzed.

## The tasks to be solved
1. **Standardization** of various blockchain's implementations - single generalized API with connectors to various implementations development.
2. Determination of blockchain **key performance indicators**: latency,  delay in the distibution of data over P2P network, etc.
3. **Automatic search for optimal parameters** for blockchain's launch and network topology, which allow to achieve optimal results for 
a particular case.
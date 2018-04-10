## Load generator module
Load generator is a module which pushes transactions to blockchain.

### Configuration
Load manager can be configured with properties in `test-manager.properties` file, 
which you pass to `TestManagerMain` when starting a benchmark.

* `amount.transactions.per.target` defines maximal count of transactions which can be sent for every node in blockchain  
* `amount.threads.per.target` defines count of threads which generates transactions for each node in parallel
* `message.length.min` defines minimal count of symbols in transaction
* `message.length.max` defines maximal count of symbols in transaction
* `load_generator.load_config` defines path to plan of load generation, see details here

### Configuring a test plan
There are different types of load generation schemes in our project. You can specify different plans for different nodes, 
same plan for all nodes, or mix these 2 types. Load generation plan is specified in JSON file, with parameters:
* `type` defines is this plan is the same for all nodes or specific. Possible values are `LoadPlanPerNode` and `SameLoadPlan` 

If `type` is `SameLoadPlan` then you should specify only one inner object: `loadAndDuration`. It is an array of objects, 
which represents periods of load generation plan.

Each _period_ (or _section_) of load generation plan has duration (field `durationMillis`) and type of load (field `load`).
At this moment we support this types of loads:
* ConstantLoad (has one parameter: `intensity`: count of events per second)
* ExponentialLoad (has )
* LinearLoad
* LinearDecreaseLoad

You can view examples of load plans ...

Correctness of syntax of load plan can be checked on your local machine via `InputLoadPlanChecker.java` class.
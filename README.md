# IoT Use case Miner
Backend for identification of IoT use cases in massive Twitter datasets using text mining.


## Quickstart

### Setup dataset

- Extract the dataset in `tweets.zip` from `resources/dataset` in `iot-miner-lib` module.
- Import the dataset to a new database. A table `tweets` should be created with around 500000 entries.
- Configure database connection in `app.properties`.

### Mine use cases

Run `UseCaseIdentifier` main method in `io-miner-lib` module. Default settings should work.

This will perform following operations
 
 - Train a model for classification
    - Prepare training data set and convert it into `weka` format
    - Train the model and save it in the file system
 - Pre-process tweets
    - Retrieve tweets with related keywords
    - Clean up tweets that are too short
    - Discard non-English tweets
    - Remove near-duplicates (such as re-tweets) using edit-distance
    - Remove stop words
    - Perform stemming
 - Discover IoT use cases using classification
    - Run weka's J48 algorithm by default (can be easily changed)
 - Categorize discovered use cases 
    - Use using LDA topic modelling with given number of topics
 - Save results
    - Automatically creates two tables `industry` and `use-case`
    - Insert the results into the tables so they can be accessed using rest-api
    
### Deploy backend

- Build with maven: `mvn install`.
- Deploy `iot-miner.war` file in tomcat. 
- `IotMinerApplication` can also be run within IDE instead of deploying in a server.

### Try out REST-API

- After the application is deployed, REST-API documentation can be accessed at `http://localhost:8080/swagger-ui.html`

### Try out front-end

- Front-end is available here: [iotuc-explorer](https://github.com/vmath89/iotuc-explorer)

## Scheduled non-blocking fetch

Service  which fetches data from preconfigured endpoint A at x second 
intervals and caches the results in memory.

Requirements: Java 8, SBT 1.1.1

1. ###Start application

    ```bash
    sbt run
    ```
    
    By default server listens port 9000.   
    
2. ###Configuration
    Please check 
    
    `main/resources/apllication.conf`

3.  ###API

    `GET: /{index}`

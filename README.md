## Scheduled non-blocking fetch

Service  which fetches data from preconfigured endpoint A at x second 
intervals and caches the results in memory.


1. ### Scheduled non-blocking fetch 

    Create a service which fetches data from endpoint `A` at `x` second intervals 
    and caches the results in memory. After each fetch, it should clear the
    existing cache and populate it with new items. The constant x should be 
    configurable in `reference.conf` or `application.conf`.  
    
    The endpoint returns a list of items separated by the new-line character. 
    It’s available at: http://challenge.carjump.net/A 

2. ### REST / HTTP interface 

    Create a REST / HTTP interface that allows clients to access the data 
    at a given index. You can use a framework of your choice. It should provide a 
    single endpoint to return an item at a given index: GET /(index) 

3. ### Actors 

    Separate fetching and storage into 2 actors. 
    
4. ###  Compression 

   Items returned by our endpoint will contain repeated duplicates at high frequencies. 
   Modify your cache to use Run-length encoding (RLE) compression for internal storage. 
   Your compression and decompression should be some concrete implementation of the 
   following trait.  
   
   ```scala
       trait Compressor {  
         def compress[A]: Seq[A] => Seq[Compressed[A]]  
         def decompress[A]: Seq[Compressed[A]] => Seq[A] 
       } 
       
       sealed trait Compressed[+A] 
       case class Single[A](element: A) extends Compressed[A] 
       case class Repeat[A](count: Int, element: A) extends Compressed[A]
   ``` 

##

1. ### Start application

    Requirements: Java 8, Scala 2.12.4, SBT 1.1.1
    
    ```bash
    sbt run
    ```
    
    By default server listens port 9000.   
    
2. ### Configuration
    Please check 
    
    `main/resources/apllication.conf`

3.  ### API

    `GET: /{index}`

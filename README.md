# Realistic Search Engine Query Log Generation
  
## Introduction  
Query logs are critical to conduct research in information retrieval area. Researchers in this area use query logs extensively to do empirical studies and prove their proposed theories in real-world applications. However, query logs from real search engines are hard to find. Companies usually do not share query logs due to privacy concerns even if they have been anonymized. As an example, AOL published a set of anonymous query logs in 2006 but deleted it after some users could be identified through associating some information with each other. In another example, Wikipedia published an anonymous query log dataset in 2012 but removed it after several days, stating that they no longer plann to publish any query log in the near future. Therefore, the only option for researchers is to find query logs that are released for specific and small systems; in which they can investigate queries carefully and remove any data that might be problematic in terms of privacy concerns.

Given these issues, our goal is to generate  realistic search engine query logs in large scale. Given that this goal seems ambitious and to our best knowledge no previous work has done it before, we think having even a small contribution in this direction still does matters.
        
In this project, we aim to build a tool that its input is a corpus containing a list of documents for general or specific domains. The tool will generate query logs based on this input corpus that follows real-world search engine query log characteristics. 

## Requirements
 * Java 1.8 (or newer)
 

## Getting Started
 Clone the repository (or download the zip file)  
```bash 
git clone https://github.com/Aleyasen/GenQL.git
 ```
 
 ### Parameters


| Parameter         | Description                                                               |
|-------------------|---------------------------------------------------------------------------|
| -d,--dir          | The directory for the corpus                                                      |
| -e,--eval         | Evaluate generated queries based on a ground-truth query log                                                |
| -g,--gen       | Generate search query log              |
| -h,--help | Help |
| -n,--count       | Numbers of queries to generate                                               |
| -o,--output         | Output file for the generated queries                           |
| -q,--query          | The file for the generated queries using -g option (only for evaluation)                                                |
| -t,--gtruth        | The file for the ground-truth query log (only for evaluation). Each line of the file contains a query                                               |
| -m,--min        | Minimum query length (default: 2)                                               |
| -x,--max        | Maximum query length (default: 7)                                               |



## Contributors

- Amirhossein Aleyasen (aleyase2@illinois.edu)
- Chen Zhang (czhang49@illinois.edu)

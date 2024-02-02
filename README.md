# How to start developing:
1. build the image and install dependencies on container

```sh
docker build . -f Dockerfile.dev -t java-webscraper && docker run -d --name=java-webscraper --rm java-webscraper
```
2. copy the .m2 folder to the project root 
```sh
docker cp java-webscraper:/root/.m2 .m2
```
3. stop the running container (`docker rm $(docker ps -qa) -f`), as it was only meant to generate the dependencies
4. run the app in development mode 
```sh
docker run --rm -e BASE_URL=WEBSITE_YOU_WANT_TO_SCRAPE -p 4567:4567 java-webscraper
```
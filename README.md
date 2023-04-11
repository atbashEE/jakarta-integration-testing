# Jakarta Integration testing

Integration testing with Testcontainers for Jakarta EE

You can easily test your application through the provided endpoints as the application is deployed, as is, into a Docker container.

Current version : 1.2.0 (Jakarta EE 8)

- Application runnable on Payara Micro, OpenLiberty, Wildfly, and Glassfish.
- Define version of the runtime or use default values.
- Easy to call application endpoints by using MicroProfile Rest Client generated proxies that hide all complexity calling the endpoints.
- JSON support for endpoints out of the box as you only need to use Java Objects (no manual JSON).
- Support for databases and automatically configure JPA datasource
- Customizable creation of Docker Build script files. 
- Remote Debug of application possible during test.
- Define response of remote services through WireMock
- Various options to access log of the container process
- Volume mapping between host running the application and the container.

Using Jakarta EE 10? Use version 2.2.0

Planned in future versions

- Support for View testing (Jakarta Faces)
- ...

## Blogs

[Basic principles](https://www.atbash.be/2022/07/24/jakarta-ee-integration-testing/)

[WireMock and image customizations](https://www.atbash.be/2022/11/08/atbash-jakarta-ee-integration-testing-version-1-1-released/)

[Support for Database and Jakarta EE 10](https://www.atbash.be/2023/04/10/support-for-database-and-jakarta-ee-10-within-jakarta-ee-integration-testing-framework/)
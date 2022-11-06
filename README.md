# Jakarta Integration testing

Integration testing with Testcontainers for Jakarta EE

You can easily test your application through the provided endpoints as the application is deployed, as is, into a Docker container.

Current version : 1.1.0

- Application runnable on Payara Micro, OpenLiberty, Wildfly, and Glassfish.
- Define version of the runtime or use default values.
- Easy to call application endpoints by using MicroProfile Rest Client generated proxies that hide all complexity calling the endpoints.
- JSON support for endpoints out of the box as you only need to use Java Objects (no JSON).
- Remote Debug of application possible during test.
- Define response of remote services through WireMock
- Various options to access log of the container process
- Volume mapping between host running the application and the container.

Planned in future versions

- Support for databases with definition of dataset that needs to be used.
- Support for View testing (Jakarta Faces)
- ...


package test_war;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class TestEndpoint {
	@GET
	public String get() {
		return "test";
	}
}
package test_war;

import java.util.Optional;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationPath("")
@Path("")
public class TestEndpoint extends javax.ws.rs.core.Application {
	
	public TestEndpoint() {
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response get() {
		return Response.ok().build();
	}
	
}

package de.i3mainz.ibr.openid;

import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.connections.Identification;
import de.i3mainz.ibr.visibility.MainVisibility;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class OpenidResource {
	
	@GET
	@Path("/visibility/start")
	@Produces(MediaType.APPLICATION_XML)
	public Response doVisibilityAnalysis(@Context HttpServletRequest req,
			@QueryParam("objects") String objects,
			@QueryParam("sightLevel") String sightLevel,
			@DefaultValue("5") @QueryParam("ignoreOffset") String ignoreOffset,
			@QueryParam("project") String project,
			@DefaultValue("FF000000") @QueryParam("wallColor") String wallColor,
			@DefaultValue("FFFF0000") @QueryParam("fromColor") String fromColor,
			@DefaultValue("FF00FF00") @QueryParam("toColor") String toColor) {
		Identification user = Identification.getUser(req);
		if (user.getState() != 2) {
			user.setState(2);
			(new Thread(new MainVisibility(user,MainVisibility.getPoints(objects), Double.parseDouble(sightLevel), Double.parseDouble(ignoreOffset), project, wallColor, fromColor, toColor))).start();
		}
		return Identification.getUserData(req);
	}

	@GET
	@Path("/visibility.png")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getVisibilityFile(@Context HttpServletRequest req) {
		// TODO: create Image dependent on width and height
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(Config.getProperty("dsfpath") + "visibility.png"))) {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            stream.close();
			Identification.getUser(req).setState(0);
            return Config.getResult(data);
        } catch (Exception e) {
            return Config.getResult(e);
        }
	}
	
	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	public Response login(@Context HttpServletRequest req) {
		return Identification.LoginPage(req);
	}
	
	@GET
	@Path("/identify")
	@Produces(MediaType.APPLICATION_XML)
	public Response identify(@Context HttpServletRequest req) {
		return Identification.identify(req);
	}
	
	@GET
	@Path("/logout")
	@Produces(MediaType.APPLICATION_XML)
	public Response logout(@Context HttpServletRequest req) {
		return Identification.logout(req);
	}
	
	@GET
	@Path("/data")
	@Produces(MediaType.APPLICATION_XML)
	public Response data(@Context HttpServletRequest req) {
		return Identification.getUserData(req);
	}
	
	@GET
	@Path("/login/{provider}")
	@Produces(MediaType.APPLICATION_XML)
	public Response setLogin(@Context HttpServletRequest req, @PathParam("provider") String provider) {
		return Identification.providerLogin(req,provider);
	}
}

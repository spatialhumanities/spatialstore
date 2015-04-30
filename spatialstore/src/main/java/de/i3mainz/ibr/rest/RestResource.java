package de.i3mainz.ibr.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import de.i3mainz.ibr.connections.*;
import de.i3mainz.ibr.getfeature.Export;
import de.i3mainz.ibr.getfeature.Information;
import de.i3mainz.ibr.getfeature.InputXML;
import de.i3mainz.ibr.getfeature.Manipulation;
import de.i3mainz.ibr.project.ClearProject;
import de.i3mainz.ibr.project.ProjectXML;
import de.i3mainz.ibr.visibility.MainVisibility;
import de.i3mainz.ibr.xml.SpatialcontextXML;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jdom.JDOMException;

/**
 * Servlet zum Erzeugen von XML Response aus PostGIS Database (HTTP) via
 * Jersey-Application
 */
@Path("")
public class RestResource {

	private static int toInt(String value) {
		return (value == null || value.isEmpty()) ? 0 : Integer.parseInt(value);
	}

	private static int toInt(String value1, String value2) {
		return (value1 == null || value1.isEmpty()) ? ((value2 == null || value2.isEmpty()) ? 0 : Integer.parseInt(value2)) : Integer.parseInt(value1);
	}

	// <editor-fold defaultstate="collapsed" desc="GET">

	@GET
	@Path("/") //rest
	@Produces(MediaType.APPLICATION_XML)
	public Response getRest(@Context HttpServletRequest req) {
		return SpatialcontextXML.getSpatialcontexts();
	}

	@GET
	@Path("/{spatialcontext}") //oberwesel
	@Produces(MediaType.APPLICATION_XML)
	public Response getSpatialcontext(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext) {
		return SpatialcontextXML.getSpatialcontext(spatialcontext);
	}

	@GET
	@Path("/{spatialcontext}/viewerdata")
	@Produces(MediaType.APPLICATION_XML)
	public Response getViewerdata(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext) {
		return SpatialcontextXML.getSpatialcontext(spatialcontext);
	}

	@GET
	@Path("/{spatialcontext}/point")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPoint(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext,
			@QueryParam("viewpoint") String viewpoint, @QueryParam("azim") String azim, @QueryParam("elev") String elev) {
		return Information.getPoint(spatialcontext, viewpoint, Double.parseDouble(azim), Double.parseDouble(elev));
	}

	@GET
	@Path("/{spatialcontext}/info")
	@Produces(MediaType.APPLICATION_XML)
	public Response getInfo(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext,
			@QueryParam("xml") String xml) {
		try {
			InputXML input = new InputXML(xml, null);
			return Manipulation.getFeature(spatialcontext, input);
		} catch (IOException | JDOMException e) {
			// TODO Replace
			return Response.status(400).entity("<error>" + e.getMessage() + "</error>").header("Access-Control-Allow-Origin", "*").build();
		}
	}

	@GET
	@Path("/{spatialcontext}/features")
	@Produces(MediaType.APPLICATION_XML)
	public Response getFeatures(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext,
			@QueryParam("srid") String srid,
			@QueryParam("fids") String fids, @QueryParam("viewpoint") String viewpoint, @QueryParam("creator") String creator) {
		Identification user = Identification.getUser(req);
		return Export.getFeatures(user, spatialcontext, fids, viewpoint, creator, null, srid, -1, 0);
	}

	@GET
	@Path("/{spatialcontext}/features.{format}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportFeatures(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext,
			@PathParam("format") String format, @QueryParam("srid") String srid,
			@QueryParam("fids") String fids, @QueryParam("viewpoint") String viewpoint, @QueryParam("creator") String creator, @QueryParam("deci") String deci) {
		Identification user = Identification.getUser(req);
		return Export.getFeatures(user, spatialcontext, fids, viewpoint, creator, format, srid, toInt(deci, null), 0);
	}

	@GET
	@Path("/{spatialcontext}/features/{feature}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getFeature(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@QueryParam("srid") String srid) {
		Identification user = Identification.getUser(req);
		return Export.getFeatures(user, spatialcontext, "" + toInt(feature), null, null, null, srid, -1, 0);
	}

	@GET
	@Path("/{spatialcontext}/features/{feature}.{format}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportFeature(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@PathParam("format") String format, @QueryParam("srid") String srid, 
                        @QueryParam("deci") String deci, @QueryParam("maxPoints") String maxPoints, @QueryParam("width") String width, @QueryParam("height") String height) {
                Identification user = Identification.getUser(req);
                return Export.getFeatures(user, spatialcontext, "" + toInt(feature), null, null, format, srid, toInt(deci, width), toInt(maxPoints, height));
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="POST">
	@POST
	@Path("/{spatialcontext}/viewpoints/{viewpoint}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response postFeature(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("viewpoint") String viewpoint,
			@FormParam("feature") String feature, @FormParam("xml") String xml, @FormParam("img") String img, @FormParam("width") String width, @FormParam("height") String height) {
		try {
			int fid = toInt(feature);
			Identification user = Identification.getUser(req);
			InputXML input = new InputXML(xml, user.getID(fid));
			if (fid == 0) {
				return Manipulation.addFeature(spatialcontext, viewpoint, input, img, toInt(width), toInt(height));
			} else {
				return Manipulation.editFeature(fid, spatialcontext, viewpoint, input, img, toInt(width), toInt(height));
			}
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	public Response importFeatures(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext,
			@FormDataParam("file") InputStream fileIS, @FormDataParam("file") FormDataContentDisposition fileDetail
        ) {
		try (Scanner scanner = new Scanner(fileIS).useDelimiter("\\A")) {
			String file = scanner.hasNext() ? scanner.next() : "";
			String format = fileDetail.getFileName().substring(fileDetail.getFileName().lastIndexOf(".") + 1);
			if (format.equals("wkt") || format.equals("ewkt")) {
				return Manipulation.importFeatures(spatialcontext, file, format, "0", Identification.getUser(req, 0), "");
			}
			throw new ClientException("format not supported: " + format, 400);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}/features/{feature}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response setVisibility(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@FormParam("featurevisiblefrom") String viewpoint, @FormParam("type") String type) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			if (type.equals("input")) {
				return Manipulation.setVisibility(spatialcontext, viewpoint, fid, true);
			}
			if (type.equals("delete")) {
				return Manipulation.setVisibility(spatialcontext, viewpoint, fid, false);
			}
			throw new ClientException("wrong type: " + type, 400);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}/features/{feature}/setvisible")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response setVisibility(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@FormParam("viewpoint") String viewpoint) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			return Manipulation.setVisibility(spatialcontext, viewpoint, fid, true);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}/features/{feature}/setinvisible")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response setInvisibility(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@FormParam("viewpoint") String viewpoint) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			return Manipulation.setVisibility(spatialcontext, viewpoint, fid, false);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}/features/{feature}/visibility")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response visibilityAnalysis(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature, @FormParam("viewpoint") String viewpoint) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			return Manipulation.setVisibility(spatialcontext, fid, viewpoint);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	@POST
	@Path("/{spatialcontext}/features/{feature}/screenshot")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response addScreenshot(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature,
			@FormParam("img") String img, @FormParam("width") String width, @FormParam("height") String height) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			return Manipulation.setScreenshot(spatialcontext, fid, img, toInt(width), toInt(height));
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="DELETE">
	@POST
	@Path("/{spatialcontext}/features/{feature}/delete")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteFeature(@Context HttpServletRequest req,
			@PathParam("spatialcontext") String spatialcontext, @PathParam("feature") String feature) {
		try {
			int fid = toInt(feature);
			Identification.getUser(req, fid);
			return Manipulation.deleteFeature(spatialcontext, fid);
		} catch (ClientException e) {
			return Config.getResult(e);
		} catch (Exception e) {
			return Config.getResult(e);
		}
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="PROJECT">
	@POST
	@Path("/")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_XML)
	public Response postProjectfileOrDeleteIt(@FormParam("projectXML") String XML_Request,
			@FormParam("clearMode") String _clearMode) {
		try {
			String StringOut = "";
			String StringXMLOut;
			if (XML_Request != null) { // Neues Projekt anlegen
				// String von "unnÃƒÆ’Ã‚Â¶tigen" Zeichen entfernen
				XML_Request = XML_Request.replace("  ", "");
				XML_Request = XML_Request.replace("\t", "");
				XML_Request = XML_Request.replace("\n", "");
				XML_Request = XML_Request.replace("\r", "");
				// Datenbankverbindung erstellen und Daten parsen/eintragen
				StringXMLOut = ProjectXML.parseXMLandPutInDatabase(XML_Request);
				// XML Output
				StringOut = "<log>";
				StringOut += StringXMLOut;
				StringOut += "</log>";
			} else if (_clearMode.equals("all")) {
				// Gesamte Datenbank lÃƒÆ’Ã‚Â¶schen und neu erzeugen
				// Datenbankverbindung erstellen und Datenbank lÃƒÆ’Ã‚Â¶schen

				//TODO Zur Sicherheit auskomentiert
				//StringXMLOut = ClearDatabase.All(); 
				// XML Output
				StringOut = "<log>";
				StringOut += "Diese Funktion ist zur Sicherheit deaktiviert";
				//StringOut += StringXMLOut;
				StringOut += "</log>";
			} else if (!_clearMode.equals("all")) {
				// Nur ein Projekt lÃƒÆ’Ã‚Â¶schen
				StringXMLOut = ClearProject.ClearDatabaseSC(_clearMode);
				// XML Output
				StringOut = "<log>";
				StringOut += StringXMLOut;
				StringOut += "</log>";
			}
			// XML Output
			return Response.ok(StringOut).header("Access-Control-Allow-Origin", "*").build();
		} catch (Exception e) {
			return Response.status(404).entity("<error>" + e.toString() + "</error>").build();
		}
	}
	// </editor-fold>
}

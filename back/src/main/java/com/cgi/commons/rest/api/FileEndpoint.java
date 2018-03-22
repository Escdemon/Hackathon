package com.cgi.commons.rest.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.WordUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.cgi.commons.db.DB;
import com.cgi.commons.db.FileDbManager;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.rest.auth.WsUserMgr;
import com.cgi.commons.utils.TmpFileManager;
import com.cgi.commons.utils.reflect.DomainUtils;

@Path("/file")
public class FileEndpoint {

    private static final String FILE = "file";
    public static final String CUSTOM_DL = "IS_CUSTOM_DOWNLOAD";

    /**
     * Saves a file onto the server.
     * <p>
     * It saves the file into a temporary directory. The original file name is added into the beginning of the file
     * content and the file is renamed with an UUID.
     * </p>
     * 
     * @param is
     *            File to save.
     * @param infos
     *            File's informations (file name).
     * @return A response which contains the file name and the UUID.
     * @see TmpFileManager#createFile(boolean, boolean)
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam(FILE) InputStream is,
            @FormDataParam(FILE) FormDataContentDisposition infos) {

    	// File name may be encoding with Base64 to preserve Latin characters.
    	String name;
    	byte[] bytes = infos.getFileName().getBytes(StandardCharsets.UTF_8);

    	if (Base64.isBase64(bytes)) {
    		name = new String(Base64.decodeBase64(bytes));	
    	} else {
    		name = infos.getFileName();
    	}
        FileContainer container = new TmpFileManager(is, name).createFile(false, true);
        return Response.ok(container).build();
    }

    /**
     * Retrieves a temporary file using its UUID.
     * 
     * @param uuid
     *            File's UUID.
     * @param rm
     *            Indicates whether the file should be deleted after the download. It is {@code true} by default.
     * @param attachment
     *            Indicates whether the HTTP header {@code Content-Disposition: attachment; filename=} should be
     *            written. It is {@code true} by default.
     * @return A response with the file content.
     * @throws IOException
     *             If an error occurs while reading file.
     * @see TmpFileManager#getTemporaryFile(String)
     * @see TmpFile#TmpFile(File, InputStream, boolean)
     */
    @GET
    @Path("/dl/{uuid}")
    public Response downloadFile(
            @PathParam("uuid") String uuid,
            @QueryParam("rm") @DefaultValue("true") boolean rm,
            @QueryParam("attachment") @DefaultValue("true") boolean attachment) throws IOException {

        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return notFound();
        }

        File file = TmpFileManager.getTemporaryFile(uuid);

        if (!file.exists()) {
            return notFound();
        }
        FileInputStream fis = new FileInputStream(file);
        TmpFileManager manager = new TmpFileManager(fis);
        FileContainer container = new FileContainer();
        container.setName(manager.extractName());
        String contentType = container.contentType();
        ResponseBuilder rb = Response
                .ok(new TmpFile(file, fis, rm))
                .header(HttpHeaders.CONTENT_LENGTH, file.length() - manager.getFileLengthDelta())
                .header(HttpHeaders.CONTENT_TYPE, contentType);

        if (attachment) {
            rb.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + container.getName() + "\"");
        }
        return rb.build();
    }

    /**
     * Retrieves a Lob variable.
     * 
     * @param entityName
     *            Entity name.
     * @param pk
     *            Entity primary key.
     * @param varName
     *            Variable name.
     * @param httpRequest
     *            HTTP request to retrieve the user.
     * @return A response which contains the file name and the UUID.
     */
    @GET
    @Path("/download/{entityName}/{pk}/{varName}")
    public Response download(
            @PathParam("entityName") String entityName,
            @PathParam("pk") String pk,
            @PathParam("varName") String varName,
            @Context HttpServletRequest httpRequest) {

		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			entityName = convertToCamel(entityName);
			varName = convertToCamel(varName);
			FileContainer container = getFileContainer(entityName, pk, varName, true, context);

			if (container == null || container.getUuid() == null) {
				return notFound();
			}
			File file = TmpFileManager.getTemporaryFile(container.getUuid());

			if (!file.exists()) {
				return notFound();
			}

			// Ajouter un jeton quelconque avec un timeout ?
			return Response.ok(container).build();
		}
    }

    /**
     * Retrieves an image.
     * 
     * @param entityName
     *            Entity name.
     * @param pk
     *            Entity primary key.
     * @param varName
     *            Variable name.
     * @param attachment
     *            Indicates whether the HTTP header {@code Content-Disposition: attachment; filename=} should be
     *            written. It is {@code true} by default.
     * @return A response with the file content.
     * @throws IOException
     *             If an error occurs while reading file.
     */
    @GET
    @Path("/image/{entityName}/{pk}/{varName}")
    public Response downloadImage(
            @PathParam("entityName") String entityName,
            @PathParam("pk") String pk,
            @PathParam("varName") String varName,
            @QueryParam("attachment") @DefaultValue("true") boolean attachment) throws IOException {

        // Ce serait pas mal de passer directement de la bdd à la réponse HTTP sans le fichier tmp ?
        // Quid de l'utilisateur ?
		try (RequestContext context = new RequestContext(null)) {
			entityName = convertToCamel(entityName);
			varName = convertToCamel(varName);
			FileContainer container = getFileContainer(entityName, pk, varName, false, context);

			if (container == null || container.getUuid() == null) {
				return notFound();
			}
			File file = TmpFileManager.getTemporaryFile(container.getUuid());

			if (!file.exists()) {
				return notFound();
			}
			FileInputStream fis = new FileInputStream(file);
			String contentType = container.contentType();
			ResponseBuilder rb = Response
					.ok(new TmpFile(file, fis, true))
					.header(HttpHeaders.CONTENT_LENGTH, file.length())
					.header(HttpHeaders.CONTENT_TYPE, contentType);

			if (attachment) {
				rb.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + container.getName() + "\"");
			}
			return rb.build();
		}
    }

    private String convertToCamel(String name) {
    	String upName = WordUtils.capitalizeFully(name, new char[]{'-'}).replaceAll("-", "");
    	return Character.toLowerCase(upName.charAt(0)) + upName.substring(1);
	}

	private FileContainer getFileContainer(String entityName, String pk, String varName,
            boolean appendName, RequestContext context) {

        EntityModel model = EntityManager.getEntityModel(entityName);
        EntityField field = model.getField(varName);
        FileContainer container = null;

        if (field.isFromDatabase()) {
            Entity entity = DomainUtils.newDomain(entityName);
            entity.setPrimaryKey(new Key(entityName, pk));
            FileDbManager manager = new FileDbManager(context, entity, varName);
            container = manager.getFile(appendName);

        } else {
        	// Attribut pour permettre au code custom de savoir quand calculer le contenu du fichier
        	context.getAttributes().put(CUSTOM_DL, Boolean.TRUE);
            Entity bean = DB.get(entityName, new Key(entityName, pk), context);
            container = (FileContainer) bean.invokeGetter(varName);
        }
        return container;
    }

    private Response notFound() {
        return Response.status(Status.NOT_FOUND).build();
    }
}

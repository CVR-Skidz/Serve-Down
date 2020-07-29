package com.cvrskidz.servedown;

/**
 * The state of an com.cvrskidz.servedown.HTTPResponse can be sent as a valid response to an HTTP request.
 * Instances store the protocol (HTTP or HTTPS), header, content, and content type of the response.
 */
public class HTTPResponse {
    private String header, content, protocol, contentType;
    private boolean hasContent; //error responses such as 404 will not contain content and se this to false

    //the status of a 'normal' response
    private final String STATUS_200 = "200 OK";

    /**
     * Enumerations representing a secure HTTP connection or not.
     */
    public static enum PROTOCOL {
        HTTP, HTTPS;
    }

    /**
     * Returns a new instance, automatically determining the content type of the response from the file extension.
     *
     * @param extension The extension of the requested file.
     * @param content The contents of the response
     * @param p The response protocol (this.PROTOCOL.HTTP or this.PROTOCOL.HTTPS)
     * @return
     */
    public static HTTPResponse newResponse(String extension, String content, PROTOCOL p) {
        String contentType = CacheHandler.IMAGE_EXTENSIONS.contains(extension) ? "image/" : "text/";
        contentType += extension.substring(extension.indexOf('.') + 1);
        return new HTTPResponse(contentType, content, p);
    }

    /**
     * Returns a new instance of a com.cvrskidz.servedown.HTTPResponse object. This is the default constructor,
     * requiring the content of the response to be set after.
     *
     * @param contentType The content type specifier to include in the response header.
     * @param p The protocol of the response (this.PROTOCOL.HTTP or this.PROTOCOL.HTTPS)
     */
    public HTTPResponse(String contentType, PROTOCOL p) {
        this.protocol = p.toString();
        this.contentType = contentType;
    }

    /**
     * Returns a new instance of a com.cvrskidz.servedown.HTTPResponse object containing the specified contents.
     *
     * @param contentType The content type specifier to include in the response header.
     * @param content The contents of the response
     * @param p The protocol of the response (this.PROTOCOL.HTTP or this.PROTOCOL.HTTPS)
     */
    public HTTPResponse(String contentType, String content, PROTOCOL p) {
        this(contentType, p);
        this.content = content;
        this.hasContent = true;
        writeHeader(STATUS_200);
    };

    /**
     * Returns a new instance of an com.cvrskidz.servedown.HTTPResponse object without contents, and specifying the response status.
     *
     * @param contentType The type specifier to be included in the header
     * @param name The string representation of the status code
     * @param status The status code to respond with
     * @param p The protocol of the response (this.PROTOCOL.HTTP or this.PROTOCOL.HTTPS)
     */
    public HTTPResponse(String contentType, String name, int status, PROTOCOL p) {
        this(contentType, p);
        writeHeader(Integer.toString(status) + " " + name);
    }

    /**
     * Fills the fields of a http header with the corresponding instance values.
     *
     * @param response The status of the response in the following format "${name} ${status code}"
     */
    private void writeHeader(String response) {
        StringBuilder out = new StringBuilder(protocol + " " + response); //first line of a HTTP response e.g. HTTP 200 OK

        //fill the required fields if the response contains contents
        if(hasContent) {
            out.append("\nContent-Type: " + contentType +"\n");
            out.append("Content-Length: " + content.length() + "\n\n");
        }

        header = out.toString();
    }

    /**
     * Returns the complete, usable, http response.
     *
     * @return
     */
    @Override
    public String toString() {
        return header + content;
    }
}

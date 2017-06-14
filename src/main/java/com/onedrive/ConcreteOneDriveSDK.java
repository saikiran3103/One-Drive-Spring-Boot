package com.onedrive;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


/**
 * This class provides the functionality to authenticate to OneDrive and handles the communication.
 */
public class ConcreteOneDriveSDK implements OneDriveSDK {
    private static final Logger logger = Logger.getLogger(OneDriveSession.class);
    private static final Gson gson = new Gson();

    private String baseUrl = "https://api.onedrive.com/v1.0/";
    private OneDriveSession session;

    /**
     * Instantiates a new ConcreteOneDriveSDK.
     *
     * @param session
     */
    private ConcreteOneDriveSDK(OneDriveSession session) {
        this.session = session;

    }


    /**
     * Instantiates a new ConcreteOneDriveSDK.
     *
     * @param clientId
     * @param clientSecret
     * @param scopes
     * @return OneDriveSDK
     */
    public static OneDriveSDK createOneDriveConnection(String clientId, String clientSecret,String redirect_uri,ExceptionEventHandler handler, OneDriveScope[] scopes) {
        OkHttpClient cli = new OkHttpClient();
        cli.setFollowRedirects(false);
        OneDriveSession session = OneDriveSession.initializeSession(cli, clientId, clientSecret,redirect_uri, scopes);
        return new ConcreteOneDriveSDK(session);
    }

    static OneDriveSDK createFromSession(OneDriveSession session) {
        return new ConcreteOneDriveSDK(session);
    }

    @Override
    public List<OneDrive> getAllDrives() throws IOException, OneDriveException {
        String requestURL = "drives/";

        PreparedRequest request = new PreparedRequest(requestURL, PreparedRequestMethod.GET);
        String json = this.makeRequest(request).getBodyAsString();
        List<OneDrive> drives = null;
        try {
            drives = ConcreteOneDrive.parseDrivesFromJson(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }

        for (OneDrive drive : drives) {
            ((ConcreteOneDrive)drive).setApi(this);
        }

        return drives;
    }

    @Override
    public OneDrive getDefaultDrive() throws IOException, OneDriveException {
        String requestURL = "drive/";

        PreparedRequest request = new PreparedRequest(requestURL, PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();

        ConcreteOneDrive oneDrive = null;
        try {
            oneDrive = ConcreteOneDrive.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        oneDrive.setApi(this);

        return oneDrive;
    }

    @Override
    public OneDrive getDrive(String driveId) throws IOException, OneDriveException {
        String requestURL = "drives/%s";

        PreparedRequest request = new PreparedRequest(String.format(requestURL, driveId), PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();

        ConcreteOneDrive oneDrive = null;
        try {
            oneDrive = ConcreteOneDrive.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        oneDrive.setApi(this);

        return oneDrive;
    }

    @Override
    public OneFolder getFolderById(String id) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        String requestURL = "drive/items/%s";

        PreparedRequest request = new PreparedRequest(String.format(requestURL, id), PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();
        ConcreteOneFolder oneFolder = null;
        try {
            oneFolder = (ConcreteOneFolder) ConcreteOneFolder.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        oneFolder.setApi(this);

        return oneFolder;
    }

    @Override
    public OneFolder getFolderByPath(String pathToFolder) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return getFolderByPath(pathToFolder, null);
    }

    @Override
    public OneFolder getRootFolder() throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return getRootFolder(null);
    }

    @Override
    public OneFolder getRootFolder(OneDrive drive) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        PreparedRequest request;
        if (drive == null) {
            request = new PreparedRequest("drive/root", PreparedRequestMethod.GET);
        } else {
            request = new PreparedRequest(String.format("drives/%s/root/", drive.getId()), PreparedRequestMethod.GET);
        }

        String json = this.makeRequest(request).getBodyAsString();

        ConcreteOneFolder oneFolder = null;
        try {
            oneFolder = (ConcreteOneFolder) ConcreteOneFolder.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        oneFolder.setApi(this);

        return oneFolder;
    }

    @Override
    public OneFile getFileById(String id) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        String requestURL = "drive/items/%s";

        PreparedRequest request = new PreparedRequest(String.format(requestURL, id), PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();
        ConcreteOneFile file = null;
        try {
            file = (ConcreteOneFile) ConcreteOneFile.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        file.setApi(this);
        return file;
    }

    @Override
    public OneFile getFileByPath(String pathToFile) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return getFileByPath(pathToFile, null);
    }

    @Override
    public OneFolder getFolderByPath(String pathToFolder, OneDrive drive) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {

        String requestURL = (drive == null) ? "drive" : String.format("drives/%s", drive.getId());
        requestURL += "/%s";

        PreparedRequest request = new PreparedRequest(String.format(requestURL, this.convertPathToApiPath(pathToFolder)), PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();
        ConcreteOneFolder oneFolder = null;
        try {
            oneFolder = (ConcreteOneFolder) ConcreteOneFolder.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        oneFolder.setApi(this);

        return oneFolder;
    }

    @Override
    public OneFile getFileByPath(String pathToFile, OneDrive drive) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        String requestURL = (drive == null) ? "drive" : String.format("drives/%s", drive.getId());
        requestURL += "/%s";

        PreparedRequest request = new PreparedRequest(String.format(requestURL, this.convertPathToApiPath(pathToFile)), PreparedRequestMethod.GET);

        String json = this.makeRequest(request).getBodyAsString();
        ConcreteOneFile file = null;
        try {
            file = (ConcreteOneFile) ConcreteOneFile.fromJSON(json);
        } catch (ParseException e) {
            throw new OneDriveException("API - response could not be processed", e);
        }
        file.setApi(this);

        return file;
    }

    @Override
    public void authenticate(String oAuthCode) throws IOException, OneDriveException {
        OneDriveSession.authorizeSession(this.session, oAuthCode);
    }

    @Override
    public void authenticateWithRefreshToken(String refreshToken) throws IOException, OneDriveException {
        OneDriveSession.refreshSession(this.session, refreshToken);
    }

    @Override
    public String getRefreshToken() throws OneDriveException {
        if(!this.session.isAuthenticated() || this.session.getRefreshToken() == null
                || this.session.getRefreshToken().isEmpty()){
            throw new OneDriveException("can not return a valid refreshToken");
        }
        return this.session.getRefreshToken();
    }

    @Override
    public void disconnect() throws IOException {
        this.session.terminate();
    }

    @Override
    public String getAuthenticationURL() {
        return session.getAccessURL();
    }

    @Override
    public boolean isAuthenticated() {
        return session.isAuthenticated();
    }

    /**
     * Create a new upload session in preparation of a file upload.
     *
     * @param folder   on OneDrive
     * @param fileName on OneDrive
     * @return UploadSession
     * @throws IOException
     */
    public UploadSession createUploadSession(ConcreteOneFolder folder,
                                             String fileName) throws IOException, OneDriveAuthenticationException {
        String requestURL = "drive/items/%s:/%s:/upload.createSession";

        String url = String.format(requestURL, folder.getId(), fileName);
        PreparedRequest request = new PreparedRequest(url, PreparedRequestMethod.POST);

        String json = this.makeRequest(request).getBodyAsString();

        return gson.fromJson(json, UploadSession.class);
    }

    /**
     * Gets all children of the given folder depending on the type.
     *
     * @param concreteOneFolder
     * @param type
     * @return children
     * @throws IOException
     * @throws OneDriveException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public List<OneItem> getChildren(ConcreteOneFolder concreteOneFolder, OneItemType type) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException, ParseException {
        String requestURL = String.format("drive/items/%s/children", concreteOneFolder.getId());

        PreparedRequest request = new PreparedRequest(requestURL, PreparedRequestMethod.GET);
        String json = this.makeRequest(request).getBodyAsString();

        List<OneItem> items = null;
        items = OneItem.parseItemsFromJson(json, type);
        for (OneItem item : items) {
            item.setApi(this);
        }
        return items;
    }

    /**
     * Gets all child folder of the specified folder.
     *
     * @param concreteOneFolder
     * @return children
     * @throws IOException
     * @throws OneDriveException
     * @throws ParseException 
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public List<OneFolder> getChildFolder(ConcreteOneFolder concreteOneFolder) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException, ParseException {
        List<OneFolder> folder = new ArrayList<>();
        for (OneItem item : this.getChildren(concreteOneFolder, OneItemType.FOLDER)) {
            folder.add((OneFolder) item);
        }

        return folder;
    }

    /**
     * Gets all child files of the specified folder.
     *
     * @param concreteOneFolder
     * @return children
     * @throws IOException
     * @throws OneDriveException
     * @throws ParseException 
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public List<OneFile> getChildFiles(ConcreteOneFolder concreteOneFolder) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException, ParseException {
        List<OneFile> files = new ArrayList<>();
        for (OneItem item : this.getChildren(concreteOneFolder, OneItemType.FILE)) {
            files.add((OneFile) item);
        }

        return files;
    }

    /**
     * Perform the HTTP request to the OneDrive API with a json body.
     *
     * @param url
     * @param method
     * @param json   body of the request
     * @return OneResponse
     * @throws IOException
     */
    public OneResponse makeRequest(String url, PreparedRequestMethod method, String json) throws IOException, OneDriveAuthenticationException {
        PreparedRequest request = new PreparedRequest(url, method);
        request.addHeader("Content-Type", "application/json");
        request.setBody(json.getBytes());
        return makeRequest(request);
    }

    /**
     * Perform the HTTP request to the OneDrive API.
     *
     * @param preparedRequest
     * @return OneResponse
     * @throws IOException
     */
    public OneResponse makeRequest(PreparedRequest preparedRequest) throws IOException, OneDriveAuthenticationException {

        if(!this.session.isAuthenticated()){
            throw new OneDriveAuthenticationException("Session is no longer valid. Look for a failure of the refresh Thread in the log.");
        }

        String url;
        RequestBody body = null;

        if (preparedRequest.getBody() != null) {
            body = RequestBody.create(null, preparedRequest.getBody());
        }

        if (isCompleteURL(preparedRequest.getPath())) {
            url = preparedRequest.getPath();
        } else {
            url = String.format("%s%s?access_token=%s", this.baseUrl, preparedRequest.getPath(), session.getAccessToken());
        }

        logger.debug(String.format("making request to %s",url));

        Request.Builder builder = new Request.Builder().method(preparedRequest.getMethod(), body).url(url);

        for (String key : preparedRequest.getHeader().keySet()) {
            builder.addHeader(key, preparedRequest.getHeader().get(key));
        }

        //replace 
        String access_token= "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFCbmZpRy1tQTZOVGFlN0NkV1c3UWZkbTU3ZkpvWGIxY0Q4cTVrdTJTMkRFdElVWnYteldwNm5lMVU3NGtoSmZmRUpmVUUzaVBja0tqdS0wUk10QVF3NEQ5VkNLR29CMGt6UWNnMkdMS1lFelNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3Iiwia2lkIjoiOUZYRHBiZk1GVDJTdlF1WGg4NDZZVHdFSUJ3In0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lMDc5M2QzOS0wOTM5LTQ5NmQtYjEyOS0xOThlZGQ5MTZmZWIvIiwiaWF0IjoxNDk3MzkyODk5LCJuYmYiOjE0OTczOTI4OTksImV4cCI6MTQ5NzM5Njc5OSwiYWNyIjoiMSIsImFpbyI6IlkyWmdZQkEwRk56Ymt6MWJUdkdwcTgzbXVNL0hIVjZrSyt6dmY2ajliOTVUazJiLzgrWUEiLCJhbXIiOlsicHdkIiwibWZhIl0sImFwcF9kaXNwbGF5bmFtZSI6InRlc3QgQXBwbGljYXRpb24gMiIsImFwcGlkIjoiYzAwYTRjMjYtZTY0Yi00NTliLTkxZjYtMzE1NzFiODAyYWU0IiwiYXBwaWRhY3IiOiIwIiwiZmFtaWx5X25hbWUiOiJBa2tpcmVkZHkiLCJnaXZlbl9uYW1lIjoiU2FpIEtpcmFuIiwiaXBhZGRyIjoiMTcwLjI1MS4xOTYuMTg3IiwibmFtZSI6IkFra2lyZWRkeSwgU2FpIEtpcmFuIiwib2lkIjoiMGM5ODViZDgtZmIyMS00YTcyLWFiMGYtNGMzMGEzNjJmMTc5Iiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTMyOTA2ODE1Mi0xNDU0NDcxMTY1LTE0MTcwMDEzMzMtNTg1MTAzOSIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMDAwMEEwOTUxNzkxIiwic2NwIjoiRmlsZXMuUmVhZC5BbGwgRmlsZXMuUmVhZFdyaXRlIEZpbGVzLlJlYWRXcml0ZS5BbGwgU2l0ZXMuUmVhZFdyaXRlLkFsbCIsInN1YiI6IndsYjVUQWpKWkltREdLeDhYOXFITFQ3Y3lKX0lyOUhzZ0ViWEVxOWx6OFUiLCJ0aWQiOiJlMDc5M2QzOS0wOTM5LTQ5NmQtYjEyOS0xOThlZGQ5MTZmZWIiLCJ1bmlxdWVfbmFtZSI6InNhaS5raXJhbi5ha2tpcmVkZHlAYWNjZW50dXJlLmNvbSIsInVwbiI6InNhaS5raXJhbi5ha2tpcmVkZHlAYWNjZW50dXJlLmNvbSIsInV0aSI6InhUV25pMGZGWDB5cmpEbHZMVUJHQUEiLCJ2ZXIiOiIxLjAifQ.K1UNrzaUjy3lUDI09DX7cmFFq59TAvhpV2-RZECngdn547IPcMCNzjc_BNYMvVlfAnwnboRf8BFmV3b2BmARHu4DiPuNm-X5-igp7oR8YIgtlCan3XY29Dil1i_bEt5qW1FFmzlcOKw-oQ7wQ6jzlUTprOf0oJFRrEb8is0JVQehc5sd1GlN8FL6SGedGERK27RtXkRIUCxMcXcvQXeNN30hEta-K4t9tSqyjgfkpJRc4-89kRdbyMJBlyKpeDr5HItC5NXtNB7KjnZLSe_ljncyqEfb3WK9I0pZEtAx2wuwfccobYZ3ac4HTnAlWyOtXlJE1NlzVTqhDusELv9qTg";
        //Add auth permanently to header with redirection
        builder.header("Authorization", "bearer " + access_token);
        Request request = builder.build();

        Response getDrivesResponse = session.getClient().newCall(request).execute();
        return new ConcreteOneResponse(getDrivesResponse);
    }

    /**
     * Create a new folder in OneDrive.
     *
     * @param folder the parent folder
     * @param name   name of the new folder
     * @return OneFolder the newly created folder
     * @throws IOException
     * @throws OneDriveException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public OneFolder createFolder(OneFolder folder, String name) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return createFolder(folder, name, ConflictBehavior.RENAME);
    }

    /**
     * Create a new folder in OneDrive and define the behavior on folder name conflict.
     *
     * @param folder   the parent folder
     * @param name
     * @param behavior
     * @return OneFolder the newly created folder
     * @throws IOException
     * @throws OneDriveException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public OneFolder createFolder(OneFolder folder, String name, ConflictBehavior behavior) throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        String requestURL = String.format("drive/items/%s/children", "sai");

        String createFolderJson = "{\"name\": \"" + name + "\", \"folder\": { }, \"@name.conflictBehavior\": \"" + behavior.name + "\"}";

        OneResponse response = makeRequest(requestURL, PreparedRequestMethod.POST, createFolderJson);

        if (response.getStatusCode() == 201) {
            ConcreteOneFolder createdFolder = null;
            try {
                createdFolder = (ConcreteOneFolder) ConcreteOneFolder.fromJSON(response.getBodyAsString());
            } catch (ParseException e) {
                throw new OneDriveException("API - response could not be processed", e);
            }
            createdFolder.setApi(this);
            return createdFolder;
        } else {
            throw new OneDriveException(response.toString());
        }

    }

    /**
     * Checks if the given URL is a syntactic correct url.
     *
     * @param url
     * @return boolean
     */
    private boolean isCompleteURL(String url) {
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI();// does the extra checking required for validation of URI
        } catch (URISyntaxException | MalformedURLException e) {
            // if exception then no url
            return false;
        }
        return true;
    }

    /**
     * Deletes a OneDriveItem form OneDrive.
     *
     * @param oneItem to delete
     * @return true on success
     * @throws IOException
     * @throws OneDriveException
     */
    public boolean deleteItem(OneItem oneItem) throws IOException, OneDriveException {
        String requestURL = String.format("drive/items/%s", oneItem.getId());

        PreparedRequest request = new PreparedRequest(requestURL, PreparedRequestMethod.DELETE);
        OneResponse response = this.makeRequest(request);

        if (response.getStatusCode() == 204) {
            return true;
        } else {
            throw new OneDriveException(response.toString());
        }
    }

    /**
     * Converts a path to api specific path.
     * "/" -> "root/"
     * "/folder" -> "root:/folder:/"
     *
     * @param path
     * @return String
     */
    private String convertPathToApiPath(String path) {
        if (path.equals("/")) {
            return "root/";
        } else {
            path = this.removeSlashes(path);
            return "root:/" + path + ":/";
        }
    }

    /**
     * Remove slashes from the beginning and the end.
     *
     * @param path
     * @return String
     */
    private String removeSlashes(String path) {
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }


    /**
     * Download a file from OneDrive by id and returns the byte[].
     *
     * @param fileID the OneDrive file id
     * @return byte[]
     * @throws IOException
     */
    public byte[] download(String fileID) throws IOException, OneDriveAuthenticationException {
        session.getClient().setFollowRedirects(false);

        String url = "drive/items/%s/content";
        url = String.format(url, fileID);

        PreparedRequest downloadRequest = new PreparedRequest(url, PreparedRequestMethod.GET);
        OneResponse getResponse = makeRequest(downloadRequest);
        PreparedRequest contentRequest = new PreparedRequest(getResponse.getHeader("Location"), PreparedRequestMethod.GET);
        OneResponse contentResponse = makeRequest(contentRequest);

        return contentResponse.getBodyAsBytes();
    }

    /**
     * Copy a file in OneDrive to a location in OneDrive.
     *
     * @param id            OneDrive item id of the file to be copied
     * @param destinationId id of the target folder
     * @return OneFile the copied file
     * @throws IOException
     * @throws OneDriveException
     * @throws ParseException
     * @throws InterruptedException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public OneFile copy(String id, String destinationId) throws IOException, OneDriveException, ParseException, InterruptedException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return this.copy(id, destinationId, null);
    }

    /**
     * Copy and rename a file in OneDrive to a location in OneDrive.
     *
     * @param id            OneDrive item id of the file to be copied
     * @param destinationId id of the target folder
     * @param newName       the new name of the copied file
     * @return OneFile the copied file
     * @throws IOException
     * @throws OneDriveException
     * @throws ParseException
     * @throws InterruptedException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public OneFile copy(String id, String destinationId, String newName) throws IOException, OneDriveException, ParseException, InterruptedException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        ParentReference reference = new ParentReference();
        reference.setId(destinationId);

        OneDestinationItem destination = new OneDestinationItem(reference);

        if (newName != null)
            destination.setName(newName);

        String url = String.format("drive/items/%s/action.copy", id);
        String json = gson.toJson(destination);

        PreparedRequest request = new PreparedRequest(url, PreparedRequestMethod.POST);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Prefer", "respond-async");
        request.setBody(json.getBytes());

        OneResponse response = this.makeRequest(request);
        if (response.getStatusCode() != 202) {
            OneDriveError error = gson.fromJson(response.getBodyAsString(), OneDriveError.class);
            throw new OneDriveException("Request error: " + response.getStatusCode() + " " + error);
        } else {
            String redirectUrl = response.getHeader("Location");

            do {
                PreparedRequest contentRequest = new PreparedRequest(redirectUrl, PreparedRequestMethod.GET);
                response = makeRequest(contentRequest);
                Thread.sleep(500);
            } while (response.getStatusCode() != 200 && response.getStatusCode() != 500);

            if (response.getStatusCode() == 500) {
                HashMap<String, String> copyStatus = gson.fromJson(response.getBodyAsString(), HashMap.class);
                throw new OneDriveException("Item copy operation status: " + copyStatus.get("status"));
            }
        }

        return (OneFile) ConcreteOneFile.fromJSON(response.getBodyAsString()).setApi(this);
    }

    /**
     * Move a file in OneDrive.
     *
     * @param id            OneDrive item id of the file to be moved
     * @param destinationId id of the target folder
     * @return OneFile
     * @throws IOException
     * @throws OneDriveException
     * @throws ParseException
     * @throws InterruptedException
     * @throws org.apache.taglibs.standard.lang.jstl.parser.ParseException 
     */
    public OneFile move(String id, String destinationId) throws IOException, OneDriveException, ParseException, InterruptedException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        ParentReference reference = new ParentReference();
        reference.setId(destinationId);

        OneDestinationItem destination = new OneDestinationItem(reference);

        String url = String.format("drive/items/%s", id);
        String json = gson.toJson(destination);

        PreparedRequest request = new PreparedRequest(url, PreparedRequestMethod.PATCH);
        request.addHeader("Content-Type", "application/json");
        request.setBody(json.getBytes());

        OneResponse response = this.makeRequest(request);
        if (response.getStatusCode() != 200) {
            OneDriveError error = gson.fromJson(response.getBodyAsString(), OneDriveError.class);
            throw new OneDriveException("Request error: " + response.getStatusCode() + " " + error);
        }

        return (OneFile) ConcreteOneFile.fromJSON(response.getBodyAsString()).setApi(this);
    }

    @Override
    public void startSessionAutoRefresh() {
        this.session.startRefreshThread();
    }
}

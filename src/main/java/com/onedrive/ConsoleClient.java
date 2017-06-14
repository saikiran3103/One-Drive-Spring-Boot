package com.onedrive;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import asg.cliche.Command;
import asg.cliche.Param;

/**
 * Example Client for testing the OneDrive SDK
 */
public class ConsoleClient {
   private  OneFolder currentFolder;
   
   public ConsoleClient(OneFolder currentFolder){
	 this.currentFolder = currentFolder;
   }
//    private final String html_response = "HTTP/1.x 200 OK\n" +
//            "Connection: close\n" +
//            "Pragma: public\n" +
//            "Cache-Control: max-age=3600, public\n" +
//            "Content-Type: text/html; charset=UTF-8\n" +
//            "Vary: Accept-Encoding, Cookie, User-Agent\n" +
//            "\n" +
//            "<!DOCTYPE html><html><head><title>This Message will autodestroy itself in 10 seconds</title></head><body><h1 id='shit'></h1><script type='text/javascript'>var x=location.search;document.getElementById(\"shit\").innerHTML=x.substr(x.indexOf(\"code=\")+5);/script></body></html>";
   ExecutorService executor = Executors.newFixedThreadPool(5);
//    private OneDriveSDK api;
//    private Map<String, OneFile> currentFolderFiles = Maps.newHashMap();
//    private Map<String, OneFolder> currentFolderFolders = Maps.newHashMap();
//    private Map<String, OneItem> currentFolderItems = Maps.newHashMap();
//
//    public ConsoleClient() throws IOException, InterruptedException,
//            OneDriveException {
//
//        api = OneDriveFactory.createOneDriveSDK(OneDriveCredentials.getClientId(), OneDriveCredentials.getClientSecret(), "http://localhost:8080/onedrive/redirect"
//                , OneDriveScope.OFFLINE_ACCESS);
//
//        openWebpage(api.getAuthenticationURL());
//
//        //intercepts redirect end automatically enters the oAuth Code
//        ServerSocket serverSocket = null;
//        try {
//            serverSocket = new ServerSocket(8080);
//            while (!api.isAuthenticated()) {
//                Socket s = serverSocket.accept();
//                BufferedReader bs = new BufferedReader(new InputStreamReader(s.getInputStream()));
//                String line;
//                while ((line = bs.readLine()) != null) {
//                    Matcher m = Pattern.compile("\\?code=([^ ]+) HTTP").matcher(line);
//                    if (m.find()) {
//                        api.authenticate(m.group(1));
//                        OutputStream os = s.getOutputStream();
//                        os.write(new String(html_response).getBytes());
//                        os.close();
//                        break;
//                    }
//                }
//
//                s.close();
//            }
//            serverSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//   //     currentFolder = api.getRootFolder();
//
//
//        api.startSessionAutoRefresh();
//
//    }
//
//    private static void openWebpage(URI uri) {
//        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
//        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
//            try {
//                desktop.browse(uri);
//            } catch (Exception e) {}
//        }
//    }
//
//    private static void openWebpage(String url) {
//        try {
//            openWebpage(new URL(url).toURI());
//        } catch (Exception e) {}
//    }
//
////    public static void main(String[] args) throws IOException, InterruptedException, OneDriveException {
////        ShellFactory.createConsoleShell("OneDrive",
////                        "To list all available commands enter ?list or ?list-all, the latter will also show you system commands.\nTo get detailed info on a command enter ?help command-name",
////                        new ConsoleClient()).commandLoop();
////
////    }
//
//    private static String printCurrentFolder() {
//        if (currentFolder != null) {
//            return currentFolder.toString();
//        } else {
//            return "/";
//        }
//    }
//
//    @Command(description = "Change the current directory")
//    public void changeDirectory(
//            @Param(name = "index", description = "Index of folder you want to switch to, OR '..' to go back")
//            String index) throws IOException, OneDriveException {
//
//        OneFolder newCurrentFolder;
//        if (index.equals("..")) {
//  //          newCurrentFolder = currentFolder.getParentFolder();
//        } else {
//            newCurrentFolder = currentFolderFolders.get(index);
//        }
//
//   //     if (newCurrentFolder != null) {
//     //       currentFolder = newCurrentFolder;
//       // }
//
//  //      System.out.println("Changing folder to: " + currentFolder.getName());
//    }
//
//    @Command(name = "list children", abbrev = "ls")
//    public void listSubItems() throws IOException, OneDriveException, ParseException, org.json.simple.parser.ParseException {
//        System.out.println("Listing children");
//
//        this.currentFolderFiles = new HashMap<>();
//        this.currentFolderFolders = new HashMap<>();
//        this.currentFolderItems = convertToMap(currentFolder.getChildren(), OneFile.class);
//
//        for (String s : this.currentFolderItems.keySet()) {
//            OneItem item = this.currentFolderItems.get(s);
//            if (item.isFile())
//                this.currentFolderFiles.put(s, (OneFile) item);
//            if (item.isFolder())
//                this.currentFolderFolders.put(s, (OneFolder) item);
//        }
//
//        printItemList(currentFolderItems);
//    }
//
//    @Command(name = "list Directories", abbrev = "ls-d")
//    public void listSubFolders() throws IOException, OneDriveException, ParseException, org.json.simple.parser.ParseException {
//        System.out.println("Listing sub Folders");
//        this.currentFolderFolders = convertToMap(currentFolder.getChildFolder(), OneFolder.class);
//        printItemList(currentFolderFolders);
//    }
//
//    @Command(name = "list Files", abbrev = "ls-f")
//    public void listSubFiles() throws IOException, OneDriveException, ParseException, org.json.simple.parser.ParseException {
//        System.out.println("Listing sub files");
//        this.currentFolderFiles = convertToMap(currentFolder.getChildFiles(), OneFile.class);
//        printItemList(currentFolderFiles);
//    }

    @Command(name = "upload Files", abbrev = "uf")
    public void uploadFile(
            @Param(name = "path", description = "Path of the File you want to upload to the current Folder")
            String path,  @Param(name = "uploadUrl", description = "url of the File you want to upload to the current Folder") String url
    ) throws IOException,
            InterruptedException, ExecutionException, OneDriveException {
        File file = new File(path);
        OneUploadFile upload = currentFolder.uploadFile(file,url);
        Future<OneFile> futureUpload = executor.submit(upload);
        System.out.println(futureUpload.get().getCreatedDateTime());
        System.out.println(futureUpload.get().getDownloadUrl());
        
    }

//    @Command(name = "remove",abbrev = "rm",description = "Deletes a file")
//    public void deleteItem(
//            @Param(name = "index", description = "Index of file you want to delete")
//            String index) throws IOException, OneDriveException {
//        OneItem item = null;
//
//        if (this.currentFolderFiles.containsKey(index))
//            item = (OneItem) this.currentFolderFiles.get(index);
//
//        if (this.currentFolderFolders.containsKey(index))
//            item = (OneItem) this.currentFolderFolders.get(index);
//
//        if (item != null) {
//            System.out.println(String.format("Deleting %s", item.getName()));
//            item.delete();
//        } else {
//            System.out.println("Can not find item with index '" + index + "'");
//        }
//    }
//
//    @Command(description = "Creates a subfolder in the currentFolder")
//    public void createFolder(
//            @Param(name = "folderName", description = "The name of the new Folder that should be created")
//            String folderName) throws IOException, OneDriveException {
//        System.out.println(String.format("Creating %s in %s", folderName, currentFolder.getName()));
//    //    currentFolder.createFolder(folderName);
//    }
//
//    @Command
//    public void downloadItem(
//            @Param(name = "index", description = "Index of the file you want to download")
//            String index,
//            @Param(name = "targetFileName", description = "path where to download to")
//            String pathToDownload)
//            throws IOException {
//        System.out.println(String.format("Downloading %s to %s", currentFolderFiles.get(index).getName(), pathToDownload));
//        OneFile tmpFile = currentFolderFiles.get(index);
//        try {
//            tmpFile.download(new File(pathToDownload)).startDownload();
//        } catch (OneDriveAuthenticationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Command
//    public void exit() throws IOException {
//        api.disconnect();
//        System.exit(0);
//    }
//
//    private <T> Map<String, T> convertToMap(List<T> listToConvert, Type T) {
//        Map<String, T> tmpMap = Maps.newHashMapWithExpectedSize(listToConvert
//                .size());
//        for (int i = 0; i < listToConvert.size(); i++) {
//            tmpMap.put(i + "", listToConvert.get(i));
//        }
//        return tmpMap;
//    }
//
//    private void printItemList(Map<String, ?> map) {
//        List<String> itemKeys = new ArrayList<>(map.keySet());
//        Collections.sort(itemKeys);
//        for (String key : itemKeys) {
//            System.out.println(String.format("Item %s = %s", key, map.get(key)));
//        }
//    }
}
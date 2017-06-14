package com.onedrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

/**
 * Implementation of OneFile using methods from ConcreteOneDriveSDK
 */
public class ConcreteOneFile extends OneItem implements OneFile {

    private FileProperty file;

    private ConcreteOneFile() {
    }

//    public static ConcreteOneFile fromJSON(String json) throws ParseException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
//        return (ConcreteOneFile) OneItem.fromJSON(json).setRawJson(json);
//    }

    @Override
    public String toString() {
        return "(F) " + name;
    }

    public OneDownloadFile download(File targetFile) throws FileNotFoundException {
        return new ConcreteOneDownloadFile(this,api,targetFile);
    }

    @Override
    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Override
    public OneFile refresh() throws OneDriveException, IOException, org.apache.taglibs.standard.lang.jstl.parser.ParseException, ParseException {
        return (OneFile) super.refreshItem();
    }

    @Override
    public String getCRC32Hash() {
        return this.file.hashes.get("crc32Hash");
    }

    @Override
    public String getSHA1Hash() {
        return this.file.hashes.get("sha1Hash");
    }

    @Override
    public String getMimeType() {
        return this.file.mimeType;
    }

//    @Override
//    public OneFolder getParentFolder() throws IOException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
//        return super.getParentFolder();
//    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public OneFile copy(OneFolder targetFolder) throws IOException, OneDriveException, ParseException, InterruptedException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return this.copy(targetFolder, null);
    }

    @Override
    public OneFile copy(OneFolder targetFolder, String name) throws IOException, OneDriveException, ParseException, InterruptedException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return null;
    }

    @Override
    public OneFile move(OneFolder targetFolder) throws InterruptedException, OneDriveException, ParseException, IOException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
        return null;
    }
}

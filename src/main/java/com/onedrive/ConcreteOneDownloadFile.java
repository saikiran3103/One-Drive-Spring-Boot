package com.onedrive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

/**
 * Implementation of OneDownloadFile
 * Blocking download operation
 */
public class ConcreteOneDownloadFile implements OneDownloadFile {

    private static final Logger logger = Logger.getLogger(ConcreteOneDownloadFile.class);

    private final ConcreteOneFile metadata;
    private final ConcreteOneDriveSDK api;
    private final File destinationFile;

    public ConcreteOneDownloadFile(ConcreteOneFile metadata, ConcreteOneDriveSDK api, File destinationFile) throws FileNotFoundException {
        this.metadata = metadata;
        this.api = api;
        this.destinationFile = destinationFile;
    }

    @Override
    public OneFile getMetaData() {
        return metadata;
    }

    @Override
    public void startDownload() throws IOException, OneDriveAuthenticationException {
        RandomAccessFile destination = new RandomAccessFile(this.destinationFile, "rw");
        try {
            logger.info("Starting download of "+metadata.getName());
            destination.write(api.download(metadata.getId()));
        } finally {
            logger.info("Finished download of "+this.metadata.getName());
            try {
                destination.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public File getDownloadedFile() {
        return destinationFile;
    }
}

package com.onedrive;

import java.io.File;
import java.io.IOException;
/**
 * This Interface provides all Methods to handle a File download
 *
 */
public interface OneDownloadFile {

    /**
     * Gets the meta data of the downloaded file.
     *
     * @return meta data
     */
    OneFile getMetaData();

    /**
     * Starts Download, blocks until finished.
     *
     * @throws IOException
     */
    void startDownload() throws IOException, OneDriveAuthenticationException;

    /**
     * Gets the file handel of the downloaded file.
     *
     * @return downloaded file
     */
    File getDownloadedFile();
}

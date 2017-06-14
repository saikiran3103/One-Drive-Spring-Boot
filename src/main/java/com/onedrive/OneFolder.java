package com.onedrive;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.taglibs.standard.lang.jstl.parser.ParseException;
/**
 * This Interface provides all Methods to handle a Folder
 *
 */
public interface OneFolder {

    
    
    OneUploadFile uploadFile(File file, String url) throws IOException, OneDriveException;

    


    /**
     * Deletes the current folder.
     *
     * @return true if the deletion was successful, false otherwise
     * @throws OneDriveException
     * @throws IOException
     */
   

    /**
     * Gets the raw JSON which is received from the OneDrive API.
     *
     * @return raw json
     */
    
}

package com.onedrive;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

/**
 * Here goes everything that is needed for every resource
 * Like all the communication with the server and handling the requests and parsing them to simple JSONObjects
 * The childClasses then will form themselves according to the JSONObject by picking what they need and ignoring what isn't relevant for them
 * Like If this item represents a folder, a folder child will use the JSONObject with all the folder data to fill it's own fields
 *
 * @author timmeey
 */
public class ConcreteOneFolder extends OneItem implements OneFolder {

    private FolderProperty folder;

    private ConcreteOneFolder() {
    }

//    public static ConcreteOneFolder fromJSON(String json) throws ParseException, OneDriveException, org.apache.taglibs.standard.lang.jstl.parser.ParseException{
//        return (ConcreteOneFolder) OneItem.fromJSON(json).setRawJson(json);
//    }

  //  @Override
 //   public OneFolder getParentFolder() throws IOException, OneDriveException, ParseException, org.apache.taglibs.standard.lang.jstl.parser.ParseException {
 //       return super.getParentFolder();
 //   }

  
    @Override
    public OneUploadFile uploadFile(File file, String url) throws IOException, OneDriveException {
        return new ConcreteOneUploadFile(url, file, api);
    }

	@Override
	public boolean isFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFolder() {
		// TODO Auto-generated method stub
		return false;
	}

  

}

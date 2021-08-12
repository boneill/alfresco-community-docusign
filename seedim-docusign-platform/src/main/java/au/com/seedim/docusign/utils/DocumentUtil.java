package au.com.seedim.docusign.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;

import com.sun.jersey.core.util.Base64;

import au.com.seedim.docusign.service.DSHelper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocumentUtil {

    public static final String PREFIX = "SingatureFile";
    public static final String SUFFIX = ".tmp";

    // Create a temp file from the InputStram
    public static File stream2file (NodeRef docRef, ContentService contentService) throws IOException {
        
    	ContentReader reader = contentService.getReader(docRef, ContentModel.PROP_CONTENT);
        InputStream in = reader.getContentInputStream();
//      
    	
    	final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }
    
    private static byte[] readContent(File file) throws IOException {

        InputStream is = new DataInputStream(new FileInputStream(file));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;

        byte[] data = new byte[1024];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

	public static String base64String(File docFile) throws IOException {
		// TODO Auto-generated method stub
		
		return new String(Base64.encode(readContent(docFile)));
	}
	
	/** 
	 * Get unique file name in a folder
	 */
	public static String getUniqueNodeNameInFolder(String filename, NodeRef targetFolder, NodeService nodeService ) {
		NodeRef nodeRef = null;
		String newFilename = filename;
	    int i = 0;
	    do {
	      newFilename = getFileNameVersion(filename, i);
	      nodeRef = nodeService.getChildByName(targetFolder, ContentModel.ASSOC_CONTAINS, newFilename);
	      i++;
	    } while (nodeRef != null);
	    
	    return newFilename;
	}
	
	/** utility method to get the next numbered name of a document **/
	private static String getFileNameVersion(String filename, int number) {
	    if (number == 0) {
	      return filename;
	    }
	    int position = filename.lastIndexOf(".");
	    if (position == -1) {
	      position = filename.length();
	    }
	    String name  = filename.substring(0, position);
	    String extension = filename.substring(position);
	    
	    return String.format("%s_%d%s", name, number, extension);
	  }
    
}
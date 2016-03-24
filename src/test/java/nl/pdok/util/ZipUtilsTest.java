package nl.pdok.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author HoogmaN
 */
public class ZipUtilsTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testNestedDirectories() throws IOException {

		try (InputStream is = ZipUtilsTest.class.getResourceAsStream("/nl/pdok/java/util/ziptest.zip")) {
			ZipUtils.unzip(is, testFolder.getRoot());

			assertTrue(new File(testFolder.getRoot(), "a/1.shp").exists());
			assertTrue(new File(testFolder.getRoot(), "a/b/2.shp").exists());
			assertTrue(new File(testFolder.getRoot(), "c/3.shp").exists());
			assertTrue(new File(testFolder.getRoot(), "c/4.txt").exists());
		}
	}
    
    @Test
    public void isZipFileTest() throws URISyntaxException, IOException {
        File f = fileFromResource("/input/dataset_vin_1.zip");
        assertTrue(ZipUtils.isZip(f));
        
    }
    
    @Test
    public void isNoZipFileTest() throws IOException {
        File f = fileFromResource("/input/vin_pakbon.json");
        assertFalse(ZipUtils.isZip(f));
    }
    
    @Test
    public void isZipStreamTest() throws URISyntaxException, IOException {
        try (BufferedInputStream in = streamFromResource("/input/dataset_vin_1.zip")) {
            assertTrue(ZipUtils.isZip(in));
        }
    }
    
    @Test
    public void isNoZipStreamTest() throws IOException {
        try (BufferedInputStream in = streamFromResource("/input/vin_pakbon.json")) {
            assertFalse(ZipUtils.isZip(in));
        }
    }
    
    
    private BufferedInputStream streamFromResource(String location) {
        return new BufferedInputStream(ZipUtilsTest.class.getResourceAsStream(location));
    }
    
    private File fileFromResource(String location) throws IOException {
        File tmpFile = File.createTempFile("zipUtilsTest", null, null);
        URL resource = ZipUtilsTest.class.getResource(location);
        
        IOUtils.copy(resource.openStream(),
            FileUtils.openOutputStream(tmpFile));
        
        return tmpFile;
    }
    
}

import com.excelsiorjet.TestUtils

return !TestUtils.crossCompilation && System.getProperty("maven.exec")!=null && TestUtils.PGOSupported
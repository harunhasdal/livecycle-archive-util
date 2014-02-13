import com.github.harunhasdal.LCAGenerator;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

public class LCAGeneratorTest {

	@Test
	public void throwsExceptionWhenNoBaseDirectorySpecified() throws Exception {
		try {
			new LCAGenerator(null);
			Assert.fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {

		}
	}


	@Test
	public void throwsExceptionWhenBaseDirectoryInvalid() throws Exception {
		try {
			new LCAGenerator(new File("NonExistent"));
			Assert.fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {

		}
	}
}

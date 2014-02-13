import com.github.harunhasdal.LCAGenerator;
import junit.framework.Assert;
import org.junit.Test;

public class LCAGeneratorTest {

	@Test
	public void throwsExceptionWhenNoBaseDirectorySpecified() throws Exception {
		try {
			new LCAGenerator(null);
			Assert.fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {

		}
	}
}

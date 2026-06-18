package coverage;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "common",
        "controllers",
        "integration",
        "models",
        "run",
        "system",
        "views"
})
@IncludeClassNamePatterns(".*(Test|IT)")
public class AllCoverageSuite {
}

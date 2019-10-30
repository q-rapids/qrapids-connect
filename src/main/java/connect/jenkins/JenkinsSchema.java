package connect.jenkins;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class JenkinsSchema {
	
	
	public static String FIELD_CREATED = "created";
	
	// Jenkins
	public static String FIELD_JENKINS_URL = "jenkinsUrl";
	public static String FIELD_JENKINS_CLASS = "jenkinsClazz";
	public static String FIELD_MODE = "mode";
	public static String FIELD_NODE_DESCRIPTION = "nodeDescription";
	public static String FIELD_NODE_NAME = "nodeName";
	public static String FIELD_NUM_EXECUTORS = "numExecutors";
	public static String FIELD_DESCRIPTION = "description";
	public static String FIELD_QUIETING_DOWN = "quietingDown";
	public static String FIELD_USE_CRUMBS = "useCrumbs";
	public static String FIELD_USE_SECURITY = "useSecurity";
	
	
	// jobs
	public static String FIELD_JOB_URL = "jobUrl";
	public static String FIELD_JOB_CLASS = "jobClazz";
	public static String FIELD_JOB_DESCRIPTION = "jobDescription";
	public static String FIELD_DISPLAY_NAME = "displayName";
	public static String FIELD_FULL_DISPLAY_NAME = "fullDisplayName";
	public static String FIELD_JOB_NAME = "jobName";
	public static String FIELD_BUILDABLE="buildable";
	public static String FIELD_COLOR="color";
	public static String FIELD_IN_QUEUE="inQueue";
	public static String FIELD_KEEP_DEPENDENCIES="keepDependencies";
	public static String FIELD_LAST_BUILD="lastBuild";
	public static String FIELD_LAST_COMPLETED_BUILD="lastCompletedBuild";
	public static String FIELD_LAST_FAILED_BUILD="lastFailedBuild";
	public static String FIELD_LAST_STABLE_BUILD="lastStableBuild";
	public static String FIELD_LAST_SUCCESSFUL_BUILD="lastSuccessfullBuild";
	public static String FIELD_LAST_UNSTABLE_BUILD="lastUnstableBuild";
	public static String FIELD_LAST_UNSUCCESSFUL_BUILD="lastUnsuccessfullBuild";
	public static String FIELD_CONCURRENT_BUILD="concurrentBuild";
	
	// builds
	public static String FIELD_BUILD_URL = "buildUrl";
	public static String FIELD_BUILD_CLASS = "buildClazz";
	public static String FIELD_BUILDING = "building";
	public static String FIELD_BUILD_DESCRIPTION = "buildDescription";
	public static String FIELD_BUILD_NUMBER = "buildNumber";
	public static String FIELD_BUILD_DURATION="duration";
	public static String FIELD_BUILD_ESTIMATED_DURATION="estimatedDuration";
	public static String FIELD_KEEP_LOG="keepLog";
	public static String FIELD_BUILD_RESULT = "result";
	public static String FIELD_TIMESTAMP = "timestamp";
	// test
	public static String FIELD_TEST_FAIL = "testsFail";
	public static String FIELD_TEST_PASS = "testsPass";
	public static String FIELD_TEST_SKIP = "testsSkip";
	public static String FIELD_TEST_DURATION = "testDuration";
	
	// nokia parameters
	public static String FIELD_PARAM_BRANCH = "pBranch";
	public static String FIELD_PARAM_SUBBRANCH = "pSubBranch";
	public static String FIELD_PARAM_PRODUCT = "pProduct";
	public static String FIELD_PARAM_RANDOMSEED = "pRandomseed";
	public static String FIELD_PARAM_MAKE_CONFIGS = "pMakeConfigs";
	public static String FIELD_PARAM_REVISION = "pRevision";
	public static String FIELD_PARAM_FORCED_BUILD = "pForcedBuild";
	public static String FIELD_PARAM_UUID = "pUUID";
	public static String FIELD_PARAM_RESULT = "pResult";
	
	
	
	/**
	 * Schema Jenkins Server
	 */
	public static Schema jenkinsSchema = SchemaBuilder.struct().name("jenkins")
			.field(FIELD_CREATED, Schema.STRING_SCHEMA)
			.field(FIELD_JENKINS_URL, Schema.STRING_SCHEMA)
			.field(FIELD_JENKINS_CLASS, Schema.STRING_SCHEMA)
			.field(FIELD_MODE, Schema.STRING_SCHEMA)
			.field(FIELD_NODE_DESCRIPTION , Schema.STRING_SCHEMA)
			.field(FIELD_NODE_NAME , Schema.STRING_SCHEMA)
			.field(FIELD_NUM_EXECUTORS , Schema.INT64_SCHEMA)
			.field(FIELD_DESCRIPTION, Schema.STRING_SCHEMA)
			.field(FIELD_QUIETING_DOWN, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_USE_CRUMBS, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_USE_SECURITY, Schema.BOOLEAN_SCHEMA)
			.build();
	
	
	/**
	 * Schema Jenkins Job
	 */
	public static Schema jobSchema = SchemaBuilder.struct().name("job")
			.field(FIELD_JOB_URL, Schema.STRING_SCHEMA)
			.field(FIELD_CREATED, Schema.STRING_SCHEMA)
			.field(FIELD_JOB_CLASS, Schema.STRING_SCHEMA)
			.field(FIELD_DESCRIPTION, Schema.STRING_SCHEMA)
			.field(FIELD_DISPLAY_NAME, Schema.STRING_SCHEMA)
			.field(FIELD_JOB_NAME, Schema.STRING_SCHEMA)
			.field(FIELD_BUILDABLE, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_COLOR, Schema.STRING_SCHEMA)
			.field(FIELD_IN_QUEUE, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_KEEP_DEPENDENCIES , Schema.BOOLEAN_SCHEMA)
			.field(FIELD_LAST_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_COMPLETED_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_FAILED_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_STABLE_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_SUCCESSFUL_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_UNSTABLE_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_LAST_UNSUCCESSFUL_BUILD , Schema.INT64_SCHEMA)
			.field(FIELD_CONCURRENT_BUILD , Schema.BOOLEAN_SCHEMA)
			.build();
			
	public static Schema buildSchema = SchemaBuilder.struct().name("build")
			// jenkins
			.field(FIELD_CREATED, Schema.STRING_SCHEMA)
			.field(FIELD_JENKINS_URL, Schema.STRING_SCHEMA)
			// .field(FIELD_JENKINS_CLASS, Schema.STRING_SCHEMA)
			// .field(FIELD_MODE, Schema.STRING_SCHEMA)
			// .field(FIELD_NODE_DESCRIPTION, Schema.STRING_SCHEMA)
			// .field(FIELD_NODE_NAME, Schema.STRING_SCHEMA)
			// .field(FIELD_NUM_EXECUTORS, Schema.INT64_SCHEMA)
			// .field(FIELD_DESCRIPTION, Schema.STRING_SCHEMA)
			// .field(FIELD_QUIETING_DOWN, Schema.BOOLEAN_SCHEMA)
			// .field(FIELD_USE_CRUMBS, Schema.BOOLEAN_SCHEMA)
			// .field(FIELD_USE_SECURITY, Schema.BOOLEAN_SCHEMA)
			// job
			.field(FIELD_JOB_NAME, Schema.STRING_SCHEMA)
			.field(FIELD_JOB_URL, Schema.STRING_SCHEMA)
			.field(FIELD_JOB_CLASS, Schema.STRING_SCHEMA)
			.field(FIELD_DISPLAY_NAME, Schema.STRING_SCHEMA)
			
			// .field(FIELD_BUILDABLE, Schema.BOOLEAN_SCHEMA)
			// .field(FIELD_COLOR, Schema.STRING_SCHEMA)
			// .field(FIELD_IN_QUEUE, Schema.BOOLEAN_SCHEMA)
			// .field(FIELD_KEEP_DEPENDENCIES , Schema.BOOLEAN_SCHEMA)
			.field(FIELD_LAST_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_COMPLETED_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_FAILED_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_STABLE_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_SUCCESSFUL_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_UNSTABLE_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LAST_UNSUCCESSFUL_BUILD , Schema.OPTIONAL_INT32_SCHEMA)
			// .field(FIELD_CONCURRENT_BUILD , Schema.BOOLEAN_SCHEMA)
			// build
			.field(FIELD_BUILD_CLASS , Schema.OPTIONAL_STRING_SCHEMA )
			.field(FIELD_BUILD_NUMBER, Schema.OPTIONAL_INT32_SCHEMA )
			.field(FIELD_BUILD_URL, Schema.STRING_SCHEMA)
			.field(FIELD_BUILD_DESCRIPTION, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_BUILD_DURATION, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_BUILD_ESTIMATED_DURATION, Schema.OPTIONAL_INT64_SCHEMA)
			// .field(FIELD_KEEP_LOG, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_BUILD_RESULT, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_TIMESTAMP, Schema.STRING_SCHEMA)
			.field(FIELD_TEST_FAIL, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_TEST_PASS, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_TEST_SKIP, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_TEST_DURATION, Schema.OPTIONAL_FLOAT64_SCHEMA)
			// nokia param
			.field(FIELD_PARAM_BRANCH, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PARAM_SUBBRANCH, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PARAM_PRODUCT, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PARAM_REVISION, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PARAM_UUID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PARAM_RESULT, Schema.OPTIONAL_STRING_SCHEMA)
			
			
			
			
			.build();

}

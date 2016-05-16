package mil.nga.giat.geowave.analytic.mapreduce.operations.options;

import com.beust.jcommander.Parameter;

import mil.nga.giat.geowave.analytic.nn.NNProcessor;
import mil.nga.giat.geowave.analytic.param.ClusteringParameters;
import mil.nga.giat.geowave.analytic.param.GlobalParameters;
import mil.nga.giat.geowave.analytic.param.HullParameters;
import mil.nga.giat.geowave.analytic.param.MapReduceParameters;
import mil.nga.giat.geowave.analytic.param.OutputParameters;
import mil.nga.giat.geowave.analytic.param.PartitionParameters;
import mil.nga.giat.geowave.analytic.param.annotations.ClusteringParameter;
import mil.nga.giat.geowave.analytic.param.annotations.GlobalParameter;
import mil.nga.giat.geowave.analytic.param.annotations.HullParameter;
import mil.nga.giat.geowave.analytic.param.annotations.MapReduceParameter;
import mil.nga.giat.geowave.analytic.param.annotations.OutputParameter;
import mil.nga.giat.geowave.analytic.param.annotations.PartitionParameter;

public class DBScanOptions
{

	// Partitioning prevents N^2 operations when looking for neighbors

	@PartitionParameter(PartitionParameters.Partition.DISTANCE_THRESHOLDS)
	@Parameter(names = {
		"-pdt",
		"--partitionDistanceThresholds"
	}, description = "Comma separated list of distance thresholds, per dimension [defaults to --partitionMaxDistance]")
	private String partitioningDistanceThresholds;

	@PartitionParameter(PartitionParameters.Partition.GEOMETRIC_DISTANCE_UNIT)
	@Parameter(names = {
		"-pdu",
		"--partitionGeometricDistanceUnit"
	}, description = "Geometric distance unit (m=meters,km=kilometers, see symbols for javax.units.BaseUnit)")
	private String partitioningGeometricDistanceUnit = "m";

	@ClusteringParameter(ClusteringParameters.Clustering.MAX_ITERATIONS)
	@Parameter(names = {
		"-cmi",
		"--clusteringMaxIterations"
	}, description = "Maximum number of iterations when finding optimal clusters")
	private String clusteringMaxIterations = "15";

	@ClusteringParameter(ClusteringParameters.Clustering.MINIMUM_SIZE)
	@Parameter(names = {
		"-cms",
		"--clusteringMinimumSize"
	}, required = true, description = "Minimum points needed to create a cluster")
	private String clusteringMinimumSize;

	@GlobalParameter(GlobalParameters.Global.BATCH_ID)
	@Parameter(names = {
		"-b",
		"--globalBatchId"
	}, description = "Batch ID (defaults to UUID, can be used to distinguish results while querying across multiple runs)")
	private String globalBatchId;

	@HullParameter(HullParameters.Hull.DATA_TYPE_ID)
	@Parameter(names = {
		"-hdt",
		"--hullAdapterId"
	}, description = "Intermediate Adapter ID for concave hull (will not be automatically deleted)")
	private String hullDataTypeId = "concave_hull";

	@HullParameter(HullParameters.Hull.PROJECTION_CLASS)
	@Parameter(names = {
		"-hpe",
		"--hullProjectionClass"
	}, description = "Class to project on to 2D space. Implements mil.nga.giat.geowave.analytics.tools.Projection")
	private String hullProjectionClass;

	@MapReduceParameter(MapReduceParameters.MRConfig.CONFIG_FILE)
	@Parameter(names = {
		"-conf",
		"--mapReduceConfigFile"
	}, description = "MapReduce Configuration (Hadoop XML file configuration)")
	private String mapReduceConfigFile;

	@MapReduceParameter(MapReduceParameters.MRConfig.HDFS_BASE_DIR)
	@Parameter(names = {
		"-hdfsbase",
		"--mapReduceHdfsBaseDir"
	}, required = true, description = "Fully qualified path to the base directory in hdfs (for temporary files, automatically cleaned up)")
	private String mapReduceHdfsBaseDir;

	@MapReduceParameter(MapReduceParameters.MRConfig.HDFS_HOST_PORT)
	@Parameter(names = {
		"-hdfs",
		"--mapReduceHdfsHostPort"
	}, description = "[REQUIRED if not in config file] HDFS hostname and port in the format hostname:port")
	private String mapReduceHdfsHostPort;

	@MapReduceParameter(MapReduceParameters.MRConfig.JOBTRACKER_HOST_PORT)
	@Parameter(names = {
		"-jobtracker",
		"--mapReduceJobtrackerHostPort"
	}, description = "[REQUIRED if -resourceman is unset] Hadoop job tracker hostname and port in the format hostname:port")
	private String mapReduceJobtrackerHostPort;

	@MapReduceParameter(MapReduceParameters.MRConfig.YARN_RESOURCE_MANAGER)
	@Parameter(names = {
		"-resourceman",
		"--mapReduceYarnResourceManager"
	}, description = "[REQUIRED if -jobtracker is unset] Yarn resource manager hostname and port in the format hostname:port")
	private String mapReduceYarnResourceManager;

	@OutputParameter(OutputParameters.Output.DATA_NAMESPACE_URI)
	@Parameter(names = {
		"-ons",
		"--outputDataNamespaceUri"
	}, description = "Output feature namespace for objects that will be written to GeoWave")
	private String outputDataNamespaceUri;

	@OutputParameter(OutputParameters.Output.DATA_TYPE_ID)
	@Parameter(names = {
		"-odt",
		"--outputAdapterId"
	}, required = true, description = "Output Adapter Id assigned to objects that will be written to GeoWave")
	private String outputDataTypeId;

	// TODO: If you don't want to be named SPATIAL_IDX
	@OutputParameter(OutputParameters.Output.INDEX_ID)
	@Parameter(names = {
		"-oid",
		"--outputIndexId"
	}, description = "Output Index ID for objects that will be written to GeoWave (default to SPATIAL_IDX)")
	private String outputIndexId;

	@PartitionParameter(PartitionParameters.Partition.MAX_MEMBER_SELECTION)
	@Parameter(names = {
		"-pms",
		"--partitionMaxMemberSelection"
	}, description = "Maximum number of members selected from a partition (max num of neighbors to allow)")
	private String partitionMaxMemberSelection = String.valueOf(NNProcessor.DEFAULT_UPPER_BOUND_PARTIION_SIZE);

	@PartitionParameter(PartitionParameters.Partition.PARTITIONER_CLASS)
	@Parameter(names = {
		"-pc",
		"--partitionPartitionerClass"
	}, description = "Index Identifier for Centroids [defaults to OrthodromicDistancePartitioner]")
	private String partitionPartitionerClass;

	@PartitionParameter(PartitionParameters.Partition.PARTITION_DECREASE_RATE)
	@Parameter(names = {
		"-pdr",
		"--partitionPartitionDecreaseRate"
	}, description = "Rate of decrease for precision(within (0,1])")
	private String partitionPartitionDecreaseRate = "0.15";

	@PartitionParameter(PartitionParameters.Partition.MAX_DISTANCE)
	@Parameter(names = {
		"-pmd",
		"--partitionMaxDistance"
	}, required = true, description = "Maximum Partition Distance")
	private String partitionPartitionDistance;

	@PartitionParameter(PartitionParameters.Partition.PARTITION_PRECISION)
	@Parameter(names = {
		"-pp",
		"--partitionPartitionPrecision"
	}, description = "Partition Precision")
	private String partitionPartitionPrecision = "1.0";

	public String getPartitioningDistanceThresholds() {
		return partitioningDistanceThresholds;
	}

	public void setPartitioningDistanceThresholds(
			String clusteringDistanceThresholds ) {
		this.partitioningDistanceThresholds = clusteringDistanceThresholds;
	}

	public String getPartitioningGeometricDistanceUnit() {
		return partitioningGeometricDistanceUnit;
	}

	public void setPartitioningGeometricDistanceUnit(
			String clusteringGeometricDistanceUnit ) {
		this.partitioningGeometricDistanceUnit = clusteringGeometricDistanceUnit;
	}

	public String getClusteringMaxIterations() {
		return clusteringMaxIterations;
	}

	public void setClusteringMaxIterations(
			String clusteringMaxIterations ) {
		this.clusteringMaxIterations = clusteringMaxIterations;
	}

	public String getClusteringMinimumSize() {
		return clusteringMinimumSize;
	}

	public void setClusteringMinimumSize(
			String clusteringMinimumSize ) {
		this.clusteringMinimumSize = clusteringMinimumSize;
	}

	public String getGlobalBatchId() {
		return globalBatchId;
	}

	public void setGlobalBatchId(
			String globalBatchId ) {
		this.globalBatchId = globalBatchId;
	}

	public String getHullDataTypeId() {
		return hullDataTypeId;
	}

	public void setHullDataTypeId(
			String hullDataTypeId ) {
		this.hullDataTypeId = hullDataTypeId;
	}

	public String getHullProjectionClass() {
		return hullProjectionClass;
	}

	public void setHullProjectionClass(
			String hullProjectionClass ) {
		this.hullProjectionClass = hullProjectionClass;
	}

	public String getMapReduceConfigFile() {
		return mapReduceConfigFile;
	}

	public void setMapReduceConfigFile(
			String mapReduceConfigFile ) {
		this.mapReduceConfigFile = mapReduceConfigFile;
	}

	public String getMapReduceHdfsBaseDir() {
		return mapReduceHdfsBaseDir;
	}

	public void setMapReduceHdfsBaseDir(
			String mapReduceHdfsBaseDir ) {
		this.mapReduceHdfsBaseDir = mapReduceHdfsBaseDir;
	}

	public String getMapReduceHdfsHostPort() {
		return mapReduceHdfsHostPort;
	}

	public void setMapReduceHdfsHostPort(
			String mapReduceHdfsHostPort ) {
		this.mapReduceHdfsHostPort = mapReduceHdfsHostPort;
	}

	public String getMapReduceJobtrackerHostPort() {
		return mapReduceJobtrackerHostPort;
	}

	public void setMapReduceJobtrackerHostPort(
			String mapReduceJobtrackerHostPort ) {
		this.mapReduceJobtrackerHostPort = mapReduceJobtrackerHostPort;
	}

	public String getMapReduceYarnResourceManager() {
		return mapReduceYarnResourceManager;
	}

	public void setMapReduceYarnResourceManager(
			String mapReduceYarnResourceManager ) {
		this.mapReduceYarnResourceManager = mapReduceYarnResourceManager;
	}

	public String getOutputDataNamespaceUri() {
		return outputDataNamespaceUri;
	}

	public void setOutputDataNamespaceUri(
			String outputDataNamespaceUri ) {
		this.outputDataNamespaceUri = outputDataNamespaceUri;
	}

	public String getOutputDataTypeId() {
		return outputDataTypeId;
	}

	public void setOutputDataTypeId(
			String outputDataTypeId ) {
		this.outputDataTypeId = outputDataTypeId;
	}

	public String getOutputIndexId() {
		return outputIndexId;
	}

	public void setOutputIndexId(
			String outputIndexId ) {
		this.outputIndexId = outputIndexId;
	}

	public String getPartitionMaxMemberSelection() {
		return partitionMaxMemberSelection;
	}

	public void setPartitionMaxMemberSelection(
			String partitionMaxMemberSelection ) {
		this.partitionMaxMemberSelection = partitionMaxMemberSelection;
	}

	public String getPartitionPartitionerClass() {
		return partitionPartitionerClass;
	}

	public void setPartitionPartitionerClass(
			String partitionPartitionerClass ) {
		this.partitionPartitionerClass = partitionPartitionerClass;
	}

	public String getPartitionPartitionDecreaseRate() {
		return partitionPartitionDecreaseRate;
	}

	public void setPartitionPartitionDecreaseRate(
			String partitionPartitionDecreaseRate ) {
		this.partitionPartitionDecreaseRate = partitionPartitionDecreaseRate;
	}

	public String getPartitionPartitionDistance() {
		return partitionPartitionDistance;
	}

	public void setPartitionPartitionDistance(
			String partitionPartitionDistance ) {
		this.partitionPartitionDistance = partitionPartitionDistance;
	}

	public String getPartitionPartitionPrecision() {
		return partitionPartitionPrecision;
	}

	public void setPartitionPartitionPrecision(
			String partitionPartitionPrecision ) {
		this.partitionPartitionPrecision = partitionPartitionPrecision;
	}
}

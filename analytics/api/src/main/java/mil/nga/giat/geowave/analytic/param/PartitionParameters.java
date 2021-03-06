package mil.nga.giat.geowave.analytic.param;

import mil.nga.giat.geowave.analytic.partitioner.Partitioner;

public class PartitionParameters
{
	public enum Partition
			implements
			ParameterEnum {
		MAX_DISTANCE(
				Double.class,
				"pmd",
				"Partition Max Distance",
				true),
		PARTITION_PRECISION(
				Double.class,
				"pp",
				"Partition Precision",
				true),
		GEOMETRIC_DISTANCE_UNIT(
				String.class,
				"du",
				"Geometric distance unit (m=meters,km=kilometers, see symbols for javax.units.BaseUnit)",
				true),
		DISTANCE_THRESHOLDS(
				String.class,
				"dt",
				"Comma separated list of distance thresholds, per dimension",
				true),
		PARTITION_DECREASE_RATE(
				Double.class,
				"pdr",
				"Rate of decrease for precision(within (0,1])",
				true),
		MAX_MEMBER_SELECTION(
				Integer.class,
				"pms",
				"Maximum number of members selected from a partition",
				true),
		SECONDARY_PARTITIONER_CLASS(
				Partitioner.class,
				"psp",
				"Perform secondary partitioning with the provided class",
				false),
		PARTITIONER_CLASS(
				Partitioner.class,
				"pc",
				"Index Identifier for Centroids",
				true);

		private final ParameterHelper<?> helper;

		private Partition(
				final Class baseClass,
				final String name,
				final String description,
				final boolean hasArg ) {
			helper = new BasicParameterHelper(
					this,
					baseClass,
					name,
					description,
					hasArg);
		}

		@Override
		public Enum<?> self() {
			return this;
		}

		@Override
		public ParameterHelper<?> getHelper() {
			return helper;
		}
	}
}

package mil.nga.giat.geowave.core.geotime.store.query;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import mil.nga.giat.geowave.core.geotime.GeometryUtils;

public class SpatialConstraints {
	private LinkedList<Geometry> constraints = new LinkedList<Geometry>();
	private String name;

	public static Geometry infinity() {
		return GeometryUtils.infinity();
	}

	public SpatialConstraints() {
		constraints.add(infinity());
	}

	public String getName() {
		return name;
	}

	public void empty() {
		constraints.clear();
	}

	public SpatialConstraints(
			String name ) {
		this.name = name;
	}

	public SpatialConstraints(
			List<Geometry> ranges,
			String name ) {
		this.constraints.addAll(ranges);
		this.name = name;
	}

	public SpatialConstraints(
			Geometry range,
			String name ) {
		this.constraints.add(range);
		this.name = name;
	}

	public void replaceWithIntersections(
			final SpatialConstraints constraints ) {
		this.constraints = SpatialConstraints.findIntersections(
				this,
				constraints).constraints;
	}

	public void replaceWithMerged(
			final SpatialConstraints constraints ) {
		this.constraints = SpatialConstraints.merge(
				this,
				constraints).constraints;
	}

	public void add(
			final Geometry range ) {
		int pos = 0;
		Geometry nextNeighbor = null;
		for (final Geometry aRange : constraints) {
			nextNeighbor = aRange;
			if (nextNeighbor.getStartTime().after(
					range.getStartTime())) {
				break;
			}
			else if (nextNeighbor.getEndTime().after(
					range.getStartTime()) || nextNeighbor.getEndTime().equals(
					range.getStartTime())) {
				if (range.getEndTime().before(
						nextNeighbor.getEndTime())) {
					// subsummed
					return;
				}
				else {
					// replaced with larger range
					constraints.set(
							pos,
							new Geometry(
									nextNeighbor.getStartTime(),
									range.getEndTime()));
					return;
				}
			}
			pos++;
		}
		if ((nextNeighbor != null) && nextNeighbor.getStartTime().before(
				range.getEndTime())) {
			constraints.add(
					pos,
					new Geometry(
							range.getStartTime(),
							SpatialConstraints.max(
									nextNeighbor.getEndTime(),
									range.getEndTime())));
		}
		else {
			constraints.add(
					pos,
					range);
		}
	}

	public boolean isEmpty() {
		return constraints.isEmpty();
	}

	public static final SpatialConstraints findIntersections(
			final SpatialConstraints sideL,
			final SpatialConstraints sideR ) {

		if (sideL.constraints.isEmpty()) {
			return sideR;
		}
		if (sideR.constraints.isEmpty()) {
			return sideL;
		}

		final SpatialConstraints newSet = new SpatialConstraints(
				sideL.name);

		for (final Geometry lRange : sideL.constraints) {
			for (final Geometry rRange : sideR.constraints) {
				if (lRange.getEndTime().before(
						rRange.getStartTime()) || rRange.getEndTime().before(
						lRange.getStartTime())) {
					continue;
				}
				newSet.add(new Geometry(
						max(
								lRange.getStartTime(),
								rRange.getStartTime()),
						min(
								lRange.getEndTime(),
								rRange.getEndTime())));
			}
		}
		return newSet;
	}

	public static final SpatialConstraints merge(
			final SpatialConstraints left,
			final SpatialConstraints right ) {
		if (left.isEmpty()) {
			return right;
		}
		if (right.isEmpty()) {
			return left;
		}

		final SpatialConstraints newSetOfRanges = new SpatialConstraints(
				left.name);
		newSetOfRanges.constraints.addAll(left.constraints);
		for (final Geometry range : right.constraints) {
			newSetOfRanges.add(range);
		}
		return newSetOfRanges;
	}

//	public byte[] toBinary() {
//		final ByteBuffer buffer = ByteBuffer.allocate(4 + (constraints.size() * Geometry.getBufferSize()));
//		buffer.putInt(constraints.size());
//
//		for (final Geometry range : constraints) {
//			buffer.put(range.toBinary());
//		}
//
//		return buffer.array();
//	}
//
//	public void fromBinary(
//			final byte[] data ) {
//		final ByteBuffer buffer = ByteBuffer.wrap(data);
//
//		final int s = buffer.getInt();
//		final byte[] rangeBuf = new byte[Geometry.getBufferSize()];
//		for (int i = 0; i < s; i++) {
//			buffer.get(rangeBuf);
//			final Geometry range = new Geometry();
//			range.fromBinary(rangeBuf);
//			add(range);
//		}
//
//	}

	@Override
	public String toString() {
		return "SpatialConstraints [constraints=" + constraints + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((constraints == null) ? 0 : constraints.hashCode());
		return result;
	}

	@Override
	public boolean equals(
			final Object obj ) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SpatialConstraints other = (SpatialConstraints) obj;
		if (constraints == null) {
			if (other.constraints != null) {
				return false;
			}
		}
		else if (!constraints.equals(other.constraints)) {
			return false;
		}
		return true;
	}
}

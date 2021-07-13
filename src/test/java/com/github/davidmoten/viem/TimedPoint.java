package com.github.davidmoten.viem;

public class TimedPoint {

	final long time;
	final int position;

	public TimedPoint(long time, int position) {
		this.time = time;
		this.position = position;
	}

	@Override
	public String toString() {
		return "TimedPoint [time=" + time + ", position=" + position + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimedPoint other = (TimedPoint) obj;
		if (position != other.position)
			return false;
		if (time != other.time)
			return false;
		return true;
	}

}

package viemJava;

import java.util.Date;

import scala.collection.immutable.HashSet;
import scala.collection.immutable.Set;
import viem.Identifier;
import viem.IdentifierType;
import viem.MergeValidator;
import viem.Merger;
import viem.Data;
import viem.Entity;
import viem.TimedIdentifier;

public class Util {

	public static void test() {

		MergeValidator validator = new MergeValidator() {
			public boolean mergeIsValid(Data a, Data b) {
				return true;
			}
		};
		Merger merger = new Merger(validator, false);
		// Entity m1 = new Entity(new HashSet())
		Set<Entity> matches = new HashSet<Entity>();

		Entity a = create(1, id(1, 1, 1), id(2, 2, 1));
		Entity b = create(2, id(1, 1, 0));
		Entity c = create(3, id(2, 2, 0));
		Set<Entity> result = merger.merge(a, matches);
		System.out.println(result);
	}

	private static TimedIdentifier id(int name, int value, int time) {
		scala.math.BigDecimal d = new scala.math.BigDecimal(new java.math.BigDecimal(time));
		return new TimedIdentifier(new Identifier(
				new IdentifierType(name + ""), value + ""), d);
	}

	private static Entity create(int metaData, TimedIdentifier... identifiers) {
		HashSet<TimedIdentifier> set = new HashSet<TimedIdentifier>();
		for (TimedIdentifier id : identifiers)
			set = set.$plus(id);
		return new Entity(set, new Data() {
		});
	}

	private static class MyData implements Data {
		private final int value;

		public MyData(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value + "";
		}
	}

	public static void main(String[] args) {
		Util.test();
	}
}

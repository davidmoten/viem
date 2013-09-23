package viem

/**
 * Volatile Identifier Entity Matching (VIEM) is an algorithm for matching identities
 * based on mostly unique identifiers that change over time and incorporates a correction 
 * mechanism for conflicting identifiers.
 */
object `package` {}

import java.util.Date
import scala.collection.immutable._
import viem.Merger._

/**
 * The type of an [[viem.Identifier]]. For an example for a ship it might
 * be the MMSI number, so we might use [[viem.IdentifierType]]("MMSI"). 
 * Has strict ordering, reverse alphabetical at the moment.
 *
 * @param name the identitier type nameMergeResult(empty,Entity(z))
 */
case class IdentifierType(name: String) extends Ordered[IdentifierType] {
  def compare(that: IdentifierType): Int =
    that.name.compareTo(this.name)
}

/**
 *
 * An identifier for e.g. an [[viem.Entity]] composed of an [[viem.IdentifierType]] and a [[java.lang.String]] value.
 * Has strict ordering based on [[viem.IdentifierType]] ordering then value alphabetical
 * ordering.
 */
case class Identifier(typ: IdentifierType, value: String) extends Ordered[Identifier] {
  def compare(that: Identifier): Int =
    if (this.typ.equals(that.typ))
      this.value.compareTo(that.value)
    else
      this.typ.compare(that.typ)
}

/** 
 * An [[viem.Identifier]] with a [[scala.math.BigDecimal]] timestamp. Might be used to identify an [[viem.Entity]] at a given time.
 */
case class TimedIdentifier(id: Identifier, time: BigDecimal) extends Ordered[TimedIdentifier] {
  def compare(that: TimedIdentifier): Int =
    //compare using only type and time (not identifier value)
    if (this.id.typ.equals(that.id.typ))
      this.time.compare(that.time)
    else
      this.id.compare(that.id)
}

/**
 * Ancillary data normally for an [[viem.Entity]]. Would be used to hold an entityId
 * for example. In a position tracking system might hold entityId, time,
 * lat and long so that a [[viem.MergeValidator]] can compare two 
 * [[viem.Entity]]s based on their [[viem.Data]] and that further 
 * the Entity can be associated with some persisted object (using the entityId).
 */
trait Data

/**
 * Container for a set of [[viem.TimedIdentifier]] associated with a [[viem.Data]].
 */
case class Entity(set: Set[TimedIdentifier], data: Data) {
  //ensure that each identifier is unique by identifier type in the set
  assert(set.size == set.map(_.id.typ).size)
}

/**
 * The result of a merge. This trait is sealed so that all its 
 * implementations must come from this class.
 * @author dxm
 *
 */
sealed trait Result

/**
 * Invalid merge result.
 * 
 * @param ''data'' is the [[viem.Data]] of the set that provoked the invalid merge. 
 * For ''a'' against primary match ''b'' it will be ''b.data''. For ''a'' 
 * against secondary match ''c'' it will be ''c.data''. For ''b'' against 
 * ''c'' it will be the meta of the weaker identifier set, that is c.data. 
 *
 */
case class InvalidMerge(meta: Data) extends Result

/**
 * A set of [[viem.Entity]] returned as a Merger result.
 */
case class Entities(set: Set[Entity]) extends Result

/**
 * Companion object for [[viem.Entities]].
 */
object Entities {
  def apply(x: Entity*): Entities = new Entities(x.toSet)
}

/**
 * Validates the merging of two entities based on their [[viem.Data]].
 */
abstract trait MergeValidator {
  /**
   * Returns true if and only if ''a'' and ''b'' are ok to merge.
   * @param a
   * @param b
   * @return
   */
  def mergeIsValid(a: Data, b: Data): Boolean
}

/**
 * Merges entity with matching entities.
 *
 */
abstract trait MergerLike {
  /**
   * Returns the result of merging [[viem.Entity]] ''a'' with the entities that it matches 
   * (found by matching [[viem.Identifier]]s).
   * @param a
   * @param matches
   * @return
   */
  def merge(a: Entity, matches: Set[Entity]): Set[Entity]
}

/**
 * Utility class for performing merges of [[scala.collection.immutable.Set]] of [[viem.TimedIdentifier]].
 */
class Merger(validator: MergeValidator, onlyMergeIfStrongestIdentifierOfSecondaryIntersects: Boolean = false) extends MergerLike {

  /**
   * Implicit definition that allow a [[viem.Entity]] to be used as a [[scala.collection.immutable.Set]] of [[viem.TimedIdentifier]].
   * @param a
   * @return
   */
  private implicit def toSet(a: Entity): Set[TimedIdentifier] = a.set

  /**
   * Returns ''y'' and the member of set ''x'' that matches the [[viem.IdentifierType]] of
   * ''y'' as a two element set if and only the type matching item in ''x'' was found, or a one element 
   * set if the item in ''x'' with matching [[viem.IdentifierType]] was not found.
   * @param x
   * @param y
   * @return
   */
  private[viem] def alpha(x: Set[TimedIdentifier], y: TimedIdentifier): Set[TimedIdentifier] = {
    val set = x.filter(_.id.typ == y.id.typ)
    set.union(Set(y))
  }

  /**
   * Returns the (first) item in x that has the same [[viem.IdentifierType]] as y.
   * Throws a [[java.lang.RuntimeException]] if the identifier type of x not found in y.
   * @param x
   * @param y
   * @return
   */
  private[viem] def typeMatch(x: Set[TimedIdentifier], y: TimedIdentifier): TimedIdentifier = {
    val a = x.find(_.id.typ == y.id.typ)
    a match {
      case t: Some[TimedIdentifier] => t.get
      case None => error("matching identifier type not found:" + y + " in " + x)
    }
  }

  /**
   * Combines a set of [[viem.TimedIdentifier]] with another [[viem.TimedIdentifier]]. Ensures that
   * if the identifier type of ''y'' exists in x that the returned set only contains
   * the latest identifier from x and y of that type unioned with all other identifiers in x.
   * @param x
   * @param y
   * @return
   */
  private[viem] def z(x: Set[TimedIdentifier], y: TimedIdentifier): Set[TimedIdentifier] = {
    val a = alpha(x, y)
    x.diff(a).union(Set(a.max))
  }

  /**
   * Combines a set of [[viem.TimedIdentifier]] with another set  of [[viem.TimedIdentifier]].
   * Ensures that if the identifier types in ''y'' exist in x that the returned set only contains
   * the latest identifiers from x and y of that type unioned with all other identifiers in x 
   * that don't have an identifier type in y.
   * @param x
   * @param y
   * @return
   */
  private[viem] def z(x: Set[TimedIdentifier], y: Set[TimedIdentifier]): Set[TimedIdentifier] = {
    if (y.size == 0)
      x
    else if (y.size == 1)
      z(x, y.head)
    else
      z(z(x, y.head), y.tail)
  }

  /**
   * Returns true if and only if the time of x is greater or equal to the time of 
   * the identifier in y with the same identifier type. If the identifier type of x
   * does not exist in y then a [[java.lang.RuntimeException]] is thrown.
   * @param x
   * @param y
   * @return
   */
  private[viem] def >=(x: TimedIdentifier, y: Set[TimedIdentifier]): Boolean = {
    return x.time >= typeMatch(y, x).time
  }

  /**
   * Returns the maximum time value of a given set of [[set.TimedIdentifier]].
   * @param set
   * @return
   */
  private[viem] def maxTime(set: Set[TimedIdentifier]): BigDecimal = set.map(_.time).max

  /**
   * Returns the result of merging ''a1'' with associated metadata ''m'' with 
   * the set ''b''. ''b'' is expected to have an identifier with type and value of a1.
   * @param a1
   * @param m
   * @param b
   * @return
   */
  private[viem] def merge(a1: TimedIdentifier, m: Data, b: Entity): Entities = {
    if (b.isEmpty)
      Entities(Entity(Set(a1), m))
    else if (>=(a1, b))
      Entities(
        Entity(z(b, a1), m))
    else
      Entities(b)
  }

  /**
   * Returns the result of merging timed identifiers a1 and a2 both with associated
   * metadata m with the set b. Both a1 and a2 must be present in b (possibly with
   * different times though).
   * @param a1
   * @param a2
   * @param m
   * @param b
   * @return
   */
  private[viem] def merge(a1: TimedIdentifier, a2: TimedIdentifier, m: Data, b: Entity): Entities = {
    if (b.isEmpty)
      Entities(Entity(Set(a1, a2), m))
    else if (>=(a1, b))
      Entities(Entity(z(z(b, a1), a2), m))
    else
      Entities(Entity(z(b, a2), b.data))
  }

  /**
   * Returns true if and only if latest time from ''x'' is strictly 
   * later than the latest time from ''y''. 
   * @param x
   * @param y
   * @return
   */
  private[viem] def later(x: Set[TimedIdentifier], y: Set[TimedIdentifier]) =
    x.map(_.time).max > y.map(_.time).max

  /**
   * Returns the result of the merge of a1 and a2 with associated metadata m,
   * with the sets b and c. a1 must be in b and a2 must be in c (although 
   * possibly with different times).
   * @param a1
   * @param a2
   * @param m
   * @param b
   * @param c
   * @return
   */
  private[viem] def merge(a1: TimedIdentifier, a2: TimedIdentifier, m: Data, b: Entity, c: Entity): Result = {

    if (b == c && !b.isEmpty) return merge(a1, a2, m, b, empty)
    //do some precondition checks on the inputs
    assert(a1.time == a2.time, "a1 and a2 must have the same time because they came from the same fix set")
    assert(b.isEmpty || b.map(_.id).contains(a1.id), "a1 id must be in b if b is non-empty")
    assert(c.isEmpty || c.map(_.id).contains(a2.id), "a2 id must be in c if c is non-empty")
    assert(!(c.isEmpty && !b.isEmpty && !b.map(_.id).contains(a1.id)), "a2 id must be in b if c is empty")
    assert(b.map(_.id.typ).size == b.size, "b must not have more than one identifier of any type")
    assert(c.map(_.id.typ).size == c.size, "c must not have more than one identifier of any type")
    assert(b.map(_.id).intersect(c.map(_.id)).size == 0, "b and c cannot have an identifier in common")

    if (!b.isEmpty && !validator.mergeIsValid(m, b.data))
      return InvalidMerge(b.data)
    else if (a1.id == a2.id)
      merge(a1, m, b)
    else if (c isEmpty)
      merge(a1, a2, m, b)
    else {
      val a: Set[TimedIdentifier] = Set(a1, a2)
      if (!validator.mergeIsValid(m, c.data))
        return InvalidMerge(c.data)

      if (!(>=(a1, b)) && !(>=(a2, c)))
        if (validator.mergeIsValid(b.data, c.data)) {
          //if b and c have conflicting identifiers that both have later 
          //timestamps than a1 (or a2) then don't merge (no validity problem though)

          //calculate common identifier types with different identifier values in b
          val b2 = b.filter(t => c.map(x => x.id.typ).contains(t.id.typ) && !c.map(x => x.id).contains(t.id))
          //calculate common identifier types with different identifier values in c
          val c2 = c.filter(t => b.map(x => x.id.typ).contains(t.id.typ) && !b.map(x => x.id).contains(t.id))
          if (!b2.isEmpty && b2.map(_.time).max > a1.time)
            //don't merge
            return Entities(b, c)
          else if (!c2.isEmpty && c2.map(_.time).max > a1.time)
            return Entities(b, c)
          else
            return Entities(Entity(z(b, c), b.data))
        } else
          return InvalidMerge(c.data)
      else if (>=(a1, b) && !(>=(a2, c)) && a2.id == c.max.id)
        return Entities(Entity(z(z(b, c), a), c.data))
      else if (a2.id == c.max.id)
        return Entities(Entity(z(z(b, c), a), m))
      else {
        if (onlyMergeIfStrongestIdentifierOfSecondaryIntersects) {
          //only merge across identifiers from c that intersect with a
          val aTypes = a.map(_.id.typ)
          val cIntersection = c.set.filter(x => aTypes.contains(x.id.typ))
          val cComplement = c.set.filter(x => !aTypes.contains(x.id.typ))
          if (cComplement.isEmpty)
            return Entities(Entity(z(z(b, cIntersection), a), m))
          else
            return Entities(Entity(z(z(b, cIntersection), a), m), Entity(cComplement, c.data))
        } else if (maxTime(a) >= maxTime(c))
          return Entities(Entity(z(z(b, c), a), m))
        else
          return Entities(Entity(z(z(b, c), a), c.data))
      }
    }
  }

  /**
   *Returns the result of merging the identifiers in ''a'' (max 2) with ''b'' and ''c''.
   * @param a
   * @param b
   * @param c
   * @return
   */
  private[viem] def mergePair(a: Entity, b: Entity, c: Entity): Result = {
    assert(a.size <= 2, "a must have a size of 2 or less")
    if (a.isEmpty) Entities(b, c)
    else if (a.size == 1) merge(a.max, a.max, a.data, b, c)
    else merge(a.max, a.min, a.data, b, c)
  }

  /**
   * Returns the same [[viem.Entity]] if it has only one identifier otherwise returns a new [[viem.Entity]]
   * with the identifier ''id'' removed.
   * @param entity
   * @param id
   * @return
   */
  private[viem] def removeIdentifierIfNotOnly(entity: Entity, id: Identifier) =
    if (entity.size > 1)
      Entity(entity.set.filter(_.id != id), entity.data)
    else
      entity

  /**
   * Entity and one of its [[viem.TimedIdentifier]]s.
   */
  private[viem] case class EntityAndId(entity: Entity, id: TimedIdentifier)

  /**
   * A set of merged entities and the last entity merged with the used [[viem.TimedIdentifier]].
   */
  private[viem] case class Group(entities: Set[Entity], previous: EntityAndId)

  /**
   * Returns the [[viem.Entity]] that matches the given [[viem.Identifier]].
   * 
   * @param entities
   * @param id
   * @return
   */
  private[viem] def find(entities: Set[Entity], id: Identifier): Entity =
    entities.find(y => y.set.map(_.id).contains(id)).get

  /**
   * Returns the [[viem.Entity]]s which are the result of adding ''a'' to the ''matches''.
   * @param a
   * @param matches
   * @return
   */
  def merge(a: Entity, matches: Set[Entity]): Set[Entity] = {
    //check some preconditions
    require(a != null, "parameter 'a' cannot be null")
    require(matches != null, "matches cannot be null")
    require(a.size > 0, "'a' must have at least one identifier")
    require(matches.isEmpty || matches.filter(_.map(_.id).intersect(a.map(_.id)).isEmpty).isEmpty,
      "every Entity in matches must have an intersection with a in terms of [[viem.Identifier]]")
    require(matches.isEmpty || matches.map(_.set).flatten.map(_.id).size == matches.map(_.set).flatten.size,
      "elements of matches must be mutually non intersecting n terms of [[viem.Identifier]]")

    //if no matches the just return the set A back
    if (matches.isEmpty) return Set(a)

    //sort the list in descending strength of identifier
    val list = List.fromIterator(a.set.iterator).sortWith((x, y) => (x compare y) < 0)

    println("adding " + list)

    //obtain an iterator for the identifiers and the first element of the list
    val iterator = list.iterator

    //preconditions have checked that iterator has at least one element
    val firstId = iterator.next();

    //initialize the Group object to pass into the recursive method
    val group = Group(matches, EntityAndId(find(matches, firstId.id), firstId))

    //use recursion to run through the iterator performing merges
    val g = findGroup(a.data, group, firstId, iterator)

    //return the merged entities
    return g.entities
  }

  private def findGroup(data: Data, group: Group, x: TimedIdentifier, iterator: Iterator[TimedIdentifier]): Group = {
    println("merging " + x)

    val entity = find(group.entities, x.id)
    val prevEntity = find(group.entities, group.previous.id.id)
    val prev = EntityAndId(prevEntity, group.previous.id)
    //attempt the merge
    val result = merge(prev.id, x, data, prev.entity, entity)

    result match {
      //merge succeeded
      case r: Entities => {
        val entities = group.entities - prev.entity - entity ++ r.set
        val g = Group(entities, EntityAndId(entity, x))
        return if (iterator.hasNext)
          findGroup(data, g, iterator.next, iterator)
        else g
      }
      //merge deemed invalid
      case InvalidMerge(data) => {
        //remove problem identifiers from data which is 
        //one of entity or previous
        val entities = group.entities - prev.entity - entity +
          removeIdentifierIfNotOnly(prev.entity, x.id) +
          removeIdentifierIfNotOnly(entity, x.id)

        if (prev.entity == entity) {
          if (iterator.hasNext) {
            val y = iterator.next
            val g = Group(entities, EntityAndId(find(entities, y.id), y))
            return findGroup(data, g, y, iterator)
          } else
            // none left, return what we've got
            return Group(entities, group.previous)
        } else {
          //don't advance the iterator, throw away previous identifier
          val g = Group(entities, EntityAndId(entity, x))
          return findGroup(data, g, x, iterator)
        }
      }
    }
  }
}

/**
 * Utility methods for merging.
 * @author dxm
 *
 */
object Merger {

  //TODO move this stuff to Test class.
  case class EmptyData() extends Data

  val emptyData = EmptyData()

  val empty = Entity(Set(), emptyData)

}

/**
 * Holds the latest merged entities.
 * @author dxm
 *
 */
trait Entries[T] {
  /**
   * Returns [[Entity]] that matches the given [[Identifier]] if one exists.
   * @param id
   * @return
   */
  def find(id: Identifier): Option[Entity]

  /**
   * Adds/merges an entity report with the existing entities.
   * @param set
   * @return
   */
  def add(set: Entity): T
}

/**
 * Holds the latest merged entities in memory.
 * @author dxm
 *
 */
case class MemoryEntries(entries: Set[Entity], merger: Merger) extends Entries[MemoryEntries] {
  //make a map of identifier to entity to speed lookup
  val map = Map.empty[Identifier, Entity] ++ entries.flatMap(x => x.set.map(y => (y.id, x)))

  def find(id: Identifier) = {
    //  entries.find(x => x.set.map(_.id).contains(id))
    map.get(id)
  }

  def add(a: Entity) = {
    val matches = a.set.map(_.id).flatMap(find _)
    val mergedMatches = merger.merge(a, matches)
    val mergedEntries = entries.diff(matches) ++ mergedMatches
    MemoryEntries(mergedEntries, merger)
  }
}


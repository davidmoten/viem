package viem

import org.junit._
import java.util.Date
import Assert._
import Predef._
import viem.Merger._
import java.lang.AssertionError

@Test
class ViemTest {

  val validator = new MergeValidatorConstant(true)
  val merger = new Merger(validator)

  def create(name: String, value: String, time: Long) =
    TimedIdentifier(Identifier(IdentifierType(name), value), time)

  def pp(r: Result): String =
    r match {
      case InvalidMerge(_) => "invalid merge"
      case m: Entities =>
        m.toString()
          .replace("Entity", "\n\tEntity")
          .replace("TimedIdentifier", "\n\t\t\tTimedIdentifier")
          .replace("Data", "\n\t\tData")
    }

  def checkEquals(expected: Entities, value: Result) {
    value match {
      case InvalidMerge(_) => error("invalid merge")
      case _ => {
    	println("actual:")
        println(pp(value))
        if (!(expected equals value))
          println("expected:\n" + pp(expected))
        assertEquals(expected, value)
        println("****************")
      }
    }
  }

  def checkRejected(r: Result, meta: Data) {
    r match {
      case InvalidMerge(m) if m != meta => error("incorrect meta on invalid merge")
      case InvalidMerge(_) => Unit
      case _ => error("should be InvalidMerge but was not")
    }
  }

  case class MergeValidatorIfEqual extends MergeValidator {
    override def mergeIsValid(a: Data, b: Data): Boolean = a == b
  }

  case class MergeValidatorConstant(valid: Boolean) extends MergeValidator {
    override def mergeIsValid(a: Data, b: Data): Boolean = valid
  }

  case class MergeValidatorRejectOne(x: Data, y: Data) extends MergeValidator {
    override def mergeIsValid(a: Data, b: Data): Boolean = !(a == x && b == y || a == y && b == x)
  }

  case class MyData(name: String) extends Data

  implicit def convert(t: Tuple3[Int, Int, Int]) = create(t._1.toString, t._2.toString, t._3)
  implicit def convert(t: TimedIdentifier) = new Entity(Set(t), emptyData)

  val date1 = 100000000000L //1973
  val date0 = 0 //1970
  val date = date0 - 1000
  val m = "m"
  val g = "g"
  val a0 = create("a0", "1", date1)
  val a0old = create("a0", "1", date0)
  val a0older = create("a0", "1", date)
  val a1 = create("a1", "1", date1)
  val a1old = create("a1", "1", date0)
  val a1older = create("a1", "1", date)
  val a2 = create("a2", "2", date1)
  val a2old = create("a2", "2", date0)
  val a2olderDiff = create("a2", "4", date)
  val a3 = create("a3", "1", date1)
  val a3old = create("a3", "1", date0)
  val a3older = create("a3", "1", date)
  val mda = MyData("a")
  val mdb = MyData("b")
  val mdc = MyData("c")

  @Test
  def testSystem() = {

    println(a1)
    println(a2)
    println(Set(a2, a1).max)

    //check that max works
    assertEquals(
      a1,
      Set(a1, a2).max)
    assertEquals(
      a1,
      Set(a2, a1).max)
    assertEquals(
      a2,
      Set(a1, a2).min)

    var merger = new Merger(MergeValidatorConstant(true));

    println("testing that adding two timed identifiers to a set with identical identifiers still has size 2 (this is a test of the TimedIdentifier comparator which is not a strict ordering")
    assertEquals(2, Set(a1, a1old).size)

    println("testing alpha")
    assertEquals(Set(a2, a2old), merger.alpha(Set(a1, a2), a2old))
    assertEquals(Set(a2), merger.alpha(Set(a1), a2))

    println("testing complement")
    assertEquals(Set(a1),Set(a1, a2) diff Set(a2))
    assertEquals(Set(a2), Set(a1, a2) diff Set(a1))
    assertEquals(Set(a1, a2), Set(a1, a2) diff Set(a1old))
    assertEquals(Set(), Set(a1, a2) diff Set(a1, a2))

    println("testing typeMatch")
    assertEquals(a2, merger.typeMatch(Set(a1, a2), a2old))
    assertEquals(a2, merger.typeMatch(Set(a1, a2), a2))

    assertFalse(mda == mdb)

    println("testing merge")
    var r = merger.merge(
      a1, a2, mda,
      Entity(Set(a1old, a2old), mdb),
      empty)
    println(pp(r))

    println("add (a1) to an empty system")
    r = merger.merge(a1, a1, mda, empty, empty)
    checkEquals(Entities(Entity(Set(a1), mda)), r)

    println("add (a1,a2) to an empty system")
    r = merger.merge(a1, a2, mda, empty, empty)
    checkEquals(Entities(Entity(Set(a1, a2), mda)), r)

    println("add (a1) to a system with (a1)")
    r = merger.merge(a1, a1, mda, Entity(Set(a1), mdb), empty)
    checkEquals(Entities(Entity(Set(a1), mda)), r)

    println("add (a1) to a system with (a1,a2)")
    r = merger.merge(a1, a1, mda, Entity(Set(a1, a2), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2), mda)), r)

    println("add (a1,a2) to a system with (a1)")
    r = merger.merge(a1, a2, mda, Entity(Set(a1), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2), mda)), r)

    println("add (a1,a2) to a system with (a1,a2)")
    r = merger.merge(a1, a2, mda, Entity(Set(a1, a2), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2), mda)), r)

    println("add (a1,a2) to a system with (a1,a2,a3)")
    r = merger.merge(a1, a2, mda, Entity(Set(a1, a2, a3), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2, a3), mda)), r)

    println("add (a1) to a system with newer (a1)")
    r = merger.merge(a1old, a1old, mda, Entity(Set(a1), mdb), empty)
    checkEquals(Entities( Entity(Set(a1), mdb)), r)

    println("add (a1, a2) to a system with (newer a1)")
    r = merger.merge(a1old, a2old, mda, Entity(Set(a1), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2old), mdb)), r)

    println("add (a1, a2) to a system with (newer a1,older a2)")
    r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1, a2olderDiff), mdb), empty)
    checkEquals(Entities(Entity(Set(a1, a2old), mdb)), r)

    println("add (a1, a2) to a system with (older a1,newer a2)")
    r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1older, a2), mdb), empty)
    checkEquals(Entities(Entity(Set(a1old, a2), mda)), r)

    println("add (a1, a2) to a system with (older a1) (older a2)")
    r = merger.merge(a1, a2, mda,
      Entity(Set(a1old), mdb), Entity(Set(a2old), mdc))
    checkEquals(Entities(Entity(Set(a1, a2), mda)), r)

    println("add (a1, a2) to a system with (newer a1) (newer a2)")
    r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1), mdb), Entity(Set(a2), mdc))
    checkEquals(Entities(Entity(Set(a1, a2), mdb)), r)

    //Invalid merge tests
    merger = new Merger(new MergeValidatorIfEqual)

    println("add (a1) to a system with (a1) and same meta")
    r = merger.merge(a1, a1, mda, Entity(Set(a1), mda), empty)
    checkEquals(Entities(Entity(Set(a1), mda)), r)

    println("add (a1) to a system with (a1) and different meta")
    checkRejected(merger.merge(a1, a1, mda, Entity(Set(a1), mdb), empty), mdb)

    var validator: MergeValidator = new MergeValidatorRejectOne(mdb, mdc)
    merger = new Merger(validator)

    println("check mergeValidatorRejectOne")
    assertTrue(validator.mergeIsValid(mda, mdb))
    assertTrue(validator.mergeIsValid(mda, mdc))
    assertFalse(validator.mergeIsValid(mdb, mdc))

    println("add (a1) to a system with (a1), valid")
    r = merger.merge(a1, a1, mda, Entity(Set(a1), mdb), empty)
    checkEquals(Entities(Entity(Set(a1), mda)), r)

    println("add (a1) to a system with (a1), invalid")
    checkRejected(merger.merge(a1, a1, mdb, Entity(Set(a1), mdc), empty), mdc)

  }

  @Test
  def testMoreComplex1() {
    val merger = new Merger(new MergeValidatorRejectOne(mdb, mdc))

    println("add (a1, a2) to a system with (old a1) (a0, old a2) invalid b against c")
    val r = merger.merge(a1, a2, mda,
      Entity(Set(a1old), mdb), Entity(Set(a0, a2old), mdc))
    checkEquals(Entities(Entity(Set(a0, a1, a2), mda)), r)
  }

  @Test
  def testMoreComplex2() {
    println("add (old a1, old a2) to a system with (old a1) (a0, newer a2)")
    val r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1old), mdb), Entity(Set(a0, a2), mdc))
    checkEquals(Entities(Entity(Set(a0, a1old, a2), mdc)), r)
  }

  @Test
  def testMoreComplex3() {
    println("add (old a1, old a2) to a system with (old a1) (a0, old a2)")
    val r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1old), mdb), Entity(Set(a0, a2old), mdc))
    checkEquals(Entities(Entity(Set(a0, a1old, a2old), mdc)), r)
  }

  @Test
  def testMoreComplex4() {
    println("add (old a1, old a2) to a system with (old a1) (a0older, a2)")
    val r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1old), mdb), Entity(Set(a0older, a2), mdc))
    checkEquals(Entities(Entity(Set(a0older, a1old, a2), mdc)), r)
  }

  @Test
  def testMoreComplex5() {
    println("add (old a1, old a2) to a system with (old a1) (a0older, a2)")
    val r = merger.merge(a1old, a2old, mda,
      Entity(Set(a1old), mdb), Entity(Set(a0older, a2), mdc))
    checkEquals(Entities(Entity(Set(a0older, a1old, a2), mdc)), r)
  }

  @Test
  def testMerge1() {
    println("merge (a1) with (a1old)")
    assertEquals(Set(Entity(Set(a1), mda)),
      merger.merge(Entity(Set(a1), mda), Set(Entity(Set(a1old), mdb))))
  }

  @Test
  def testMerge2() {
    println("merge (a1old) with (a1)")
    assertEquals(Set(Entity(Set(a1), mdb)),
      merger.merge(Entity(Set(a1old), mda), Set(Entity(Set(a1), mdb))))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testMerge3() {
    println("merge (a1) with (a2)")
    merger.merge(Entity(Set(a1), mda), Set(Entity(Set(a2), mdb)))
  }
  
  @Test(expected = classOf[IllegalArgumentException])
  def testNullArgumentsProvokeException1 {
	  merger.merge(null,null)
  }
  
  @Test(expected = classOf[IllegalArgumentException])
  def testNullArgumentsProvokeException2() {
    merger.merge(null, Set(Entity(Set(a2), mdb)))
  }
  
  @Test(expected = classOf[IllegalArgumentException])
  def testNullArgumentsProvokeException3() {
    merger.merge(Entity(Set(a2), mdb),null)
  }

  @Test
  def testMerge4() {
    println("merge (a1) with (a1old,a2)")
    assertEquals(
      Set(Entity(Set(a1, a2), mda)),
      merger.merge(Entity(Set(a1), mda), Set(Entity(Set(a1old, a2), mdb))))
  }

  @Test
  def testMerge5() {
    println("merge (a1old) with (a1,a2)")
    assertEquals(
      Set(Entity(Set(a1, a2), mdb)),
      merger.merge(Entity(Set(a1old), mda), Set(Entity(Set(a1, a2), mdb))))
  }
  
  @Test
  def testEmptyMatchesReturnsSetOfA() {
	  println("emtpy matches returns set of A")
	  assertEquals(Set(Entity(Set(a1),mda)),
	 		 merger.merge(Entity(Set(a1),mda),Set()))
  }

  @Test
  def testMerge6() {
    println("merge (a1) with (a1,a2)")
    assertEquals(
      Set(Entity(Set(a1, a2), mda)),
      merger.merge(Entity(Set(a1), mda), Set(Entity(Set(a1, a2), mdb))))
  }
  
  private def checkEquals(x:Set[Entity],y:Set[Entity]) {
      println(x)
      println(y)
      assertEquals(x,y)
  }
  
  @Test
  def testMerge7() {
    println("merge (a1,a2) with (a1old),(a2old)")
    checkEquals(
      Set(Entity(Set(a1, a2), mda)),
      merger.merge(Entity(Set(a1,a2), mda), Set(Entity(Set(a1old), mdb),Entity(Set(a2old),mdc))))
  }
  
  @Test
  def testMemoryEntries {
      println("testing memory entities")
      var m = MemoryEntries(Set(Entity(Set(a1,a2),mda)), merger)
      println(m.add(Entity(Set(a3),mdb)))
  }
}


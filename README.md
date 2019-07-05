# Volatile Identifier Entity Matching 
<a href="https://travis-ci.org/davidmoten/viem"><img src="https://travis-ci.org/davidmoten/viem.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/viem/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/viem)<br/>
[![codecov](https://codecov.io/gh/davidmoten/viem/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/viem)<br/>

An algorithm and a java library for handling entity matching when entities can have multiple identifiers each of which is potentially volatile (having a non-permanent lifecycle).

Status: *pre-alpha*

## Problem statement
My workplace has multiple sources of craft (vessel, aircraft, vehicle, tracking device, beacons) information coming in as timestamped geographic positions with metadata.

We want to resolve this information into a latest positions layer. Ideally we only want to see one dot on a map for the latest position of one physical craft.

Craft positions come in with a type (vessel, aircraft, vehicle, tracking device, and others), and one or more identifiers. Identifiers are key-value pairs like ["MMSI","123456789"] (MMSI is a vessel identifier).

For a ship, the physical craft is the hull, for an aircraft the airframe, for a vehicle the chassis. 

Identifiers are often volatile in terms of their relationship with a phsyical craft. For example a car has a chassis number imprinted on the frame. This number can be seen as a permanent identifier for the car. However, a licence plate (registration number) is not so permanent. It acts as a great identifier for the car right up  to the point where the plates are handed in or moved to another car. We see that the chassis number is a more reliable identifier for the car than the licence plate and we are going to represent this reliability relationship by demanding that every craft (entity) type has a strict confidence ordering on the allowable identifier types for that craft type. 

A strict confidence ordering for large vessel identifiers might be be (in descending order):

* *IMO Number* - International Maritime Organisation Number that is associated with the hull for life
* *MMSI* - A number unique worldwide that is issued by the country of registration to a vessel. If the vessel changes country of registration then they get a new number.
* *Callsign* - A communications identifier issued by the country of registration
* *Inmarsat-C Mobile Number (primary)* - primary satellite phone number of the vessel
* *Inmarsat-C Mobile Number (secondary)* - secondary satellite phone number of the vessel

The lifecycle of the *MMSI* and the *Callsign* may be very similar but we apply the strict confidence ordering anyway so that results of processing (defined later) are deterministic.

## Abstraction
We are going to abstract away from the world of physical craft and now talk about *entities* (physical craft) and *entity states* (identifiers and metadata that attempt to represent an entity possibly at an instance of time).

An *entity state* has

* one or more key-value pairs (*identifiers*) that are unique by key for that entity
* metadata (like a timestamp or string key-value properties)

To emulate the arrival of a new timestamped position report (possibly old) that has to be resolved with a set of already resolved latest craft positions we define a *system* of *entity-states* as follows:

A *system* of entity-states has 

* zero or more entity-states
* identifier uniqueness across all entity-states (no entity-state can have the same identifier key-value as another)
* a strict ordering on entity-states (not necessarily within the system) that allows us to determine which report to have more confidence in. For the latest craft positions use case we would use the latest position timestamp (later timestamp is presumed to be more reliable).
* a strict ordering on identifier keys that indicates which identifiers to have more confidence in.

We want to define exactly how a *system* mutates when a new entity-state is resolved with it. For the latest craft position scenario this might be a new timestamped position with identifiers that we resolve against the set of existing timestamped positions. *Resolve* in this use case means to match identifiers, create new craft, merge or delete craft or transfer identifiers between craft and merge actions might only be allowed if effective speed checks are passed.

Define the current state of a system to be a set of entity-states `S = {e`<sub>1</sub>`, e`<sub>2</sub>`,.., e`<sub>n</sub>`}` and the newly arrived entity-state to be `e`<sub>new</sub>.

Each entity-state `e` has a set of identifier tuples `keyValues(e) = {[key`<sub>1</sub>`, value`<sub>1</sub>`], [key`<sub>2</sub>`, value`<sub>2</sub>`], ..}` which are unique by key for that object.

No key-value identifier appears more than once across a whole system (but the arriving entity-state may have identifiers in common with entity-states in the current system).

Define these functions:

* `keys: EntityState -> Set[Key]`

* `keyValues: EntityState -> Set[KeyValue]`

We also define

* `keyValues: Set[EntityState]-> Set[KeyValue] = union of keyValues(e) for entity-state e in set S`

For an arriving entity-state `e` we define the function `matches(e, S)` to be the set of entity-states in `S` that have one or more key-value pairs in common with `e`.

For each pair of entity-states `e`<sub>1</sub> and `e`<sub>2</sub> we define these functions 

* `common(e`<sub>1</sub>`, e`<sub>2</sub>`): EntityState x EntityState-> Set[KeyValue]` giving the set of key-value pairs that are present in both `e`<sub>1</sub> and `e`<sub>2</sub> 

* `conflicting(e`<sub>1</sub>`, e`<sub>2</sub>`): EntityState x EntityState -> Set[KeyValue]` giving the set of key-value pairs that are present by key in both `e`<sub>1</sub> and `e`<sub>2</sub> but have different values

* `different(e`<sub>1</sub>`, e`<sub>2</sub>`): EntityState x EntityState -> Set[KeyValue]` giving the set of key-value pairs whose keys are present in only one of `e`<sub>1</sub> and `e`<sub>2</sub> 

We also define comparators based on the mentioned strict orderings. The comparators are represented by `>`, `<` operators and this is their usage:

* `a > b` for entity-states
* `x > y` for identifier keys

## Algorithm
Given a *system* `S` and an *entity-state* `e` to resolve against `S` (mutation) the algorithm is as follows:

```
find M which is the set of all entity-states in S that have one or more common identifiers with e

let I = the set of all identifiers in entity-states in M

sort the members of `M` into a list `L` of descending order of identifier confidence based on the highest confidence identifier in the common identifiers with e.

Set the provisional entity-state p = e
let M2 = empty set of entity-states
while p has identifiers and L has members
  let f be the first available member of L and remove it from L
  let I1 = set of common key-values in p, f
  let I2 = set of conflicting key-values in p, f
  let I3 = set of key-values where the keys are present in only one of p, f
  let min = min(p, f)
  let max = max(p, f)
  if I1 > I2 and mergeable(p.metadata, f.metadata) 
    add I3 to max
    let I2' = I2 \ {kv: kv in min}
    add I2' to max
    max.metadata = merge(p.metadata, f.metadata)
  else 
    drop I1 from min
    if min not empty
      add min to M2
  p = max
loop
if p not empty 
  add p to M2
```
The result is a set M2 that replaces M in S.

## Testing
The above algorithm has been implemented in Java (for reuse) and has been tested over a number of scenarios visible in [SystemTest.java](src/test/java/com/github/davidmoten/viem2/SystemTest.java).

## Using this algorithm
The algorithm has been abstracted substantially. You will need to make implementations of [`KeyValue`](src/main/java/com/github/davidmoten/viem/KeyValue.java), [`EntityState`](src/main/java/com/github/davidmoten/viem/EntityState.java),[`System`](src/main/java/com/github/davidmoten/viem/System.java). The `System` class has a default method that implements the algorithm above and mutates or returns a new `System` on arrival of a new `EntityState`. The use of immutability, data structures and lookup is largely up to you (`System.merge` method may return the same System or a new one). 



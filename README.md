# detox
Java Data Transfer &amp; Value Objects made beautiful

# Introduction
Value Objects in Java are a pain. Such a simple concept — an object representing a collection of fields — yet 
getting them right is both notoriously difficult, requiring well-crafted .equals() and .hashcode() methods, and 
requiring far too much boilerplate code that must be written and maintained.

```java
public final class User {
  private final long id;
  private final String name;
  private final InternetAddress emailAddress;
  private final DateTime dateOfBirth;

  public User(
      final long id,
      final String name,
      final InternetAddress emailAddress,
      final DateTime dateOfBirth) {
    this.id = id;
    this.name = name;
    this.emailAddress = emailAddress;
    this.dateOfBirth = dateOfBirth;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public InternetAddress getEmailAddress() {
    return emailAddress;
  }

  public DateTime getDateOfBirth() {
    return dateOfBirth;
  }

  @Override public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final User user = (User) o;

    return id == user.id;

  }

  @Override public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
```

That's a lot of boilerplate for a value object consisting of just four simple members. The equals and hashcode 
methods above were generated by IntelliJ and could be simpler, but then if you are bothering to take the time 
to simplify these methods as the code evolves, you lose a lot of the benefits of getting the IDE to generate them for
you in the first place. Note that this simple class, long though it is, is far from complete: it omits a toString 
method, it omits any kind of validation or null-checking on the parameters to the constructor, it omits any kind of 
declaration as to the nullity of its fields (perhaps dateOfBirth is an optional field that some user provide), and it 
omits any kind “business” validation of the fields — e.g. requiring dateOfBirth to be in the past.

Instance construction is another problem here.

```java
User newUser = new User(nextAvailableUserId, name, emailAddress, null);
```

Invoking a constructor with four fields is manageable, but for a class with just a few more fields this approach to 
instance construction becomes an error-prone burden, especially if several fields are of the same type or may be null. 
An alternative to massive constructor parameter lists is to either introduce a hand-crafted fluent builder (even more
boilerplate), or use setters (eliminating the immutability property of the instance).
 
The same type with detox, looks like this:

```java
@GenerateBuilder
public interface User {
  @Id long getId();
  String getName();
  InternetAddress getInternetAddress();
  @Nullable DateTime getDateOfBirth();
}
```

And instance construction:

```java
User newUser = UserBuilder.newBuilder()
  .withId(nextAvailableUserId)
  .withName(name)
  .withEmailAddress(emailAddress)
  .build()
```

That's all there is to it. The generated builder will require (at compile time) that all of the mandatory fields are 
set before the build method is invoked. The generated instance will have appropriate equals, hashcode and toString 
methods. The equals and hashcode methods will be derived from any fields you have tagged with the @Id annotation. 

Detox examines the interface looking for “javabean” style methods (methods with the “get” prefix, or boolean 
return-type methods with the “is” or “should” prefix) and generates a fluent builder based on those.

# Validation
Detox has comprehensive support for validation of the value objects, throwing an IllegalArgumentException at the 
earliest possible time it detects a violation of the value object's definition.

The most obvious type of validation that Detox performs is null checking. If a field is not marked as nullable (Detox
understands any of javax.annotation.Nullable, org.jetbrains.annotations.Nullable, 
edu.umd.cs.findbugs.annotations.Nullable, or android.support.annotation.Nullable) then the _with_ method will throw 
an IllegalArgumentException if you try to pass null.

Often you want to impose constraints on the values of fields. For example, if the id field must be a positive:

```java
@GenerateBuilder
public interface User {
  @Id @Validate(PositiveLong.class) long getId();
  String getName();
  InternetAddress getInternetAddress();
  @Nullable DateTime getDateOfBirth();
}
```

Where the @Validate annotation takes a class implementing the com.moozvine.detox.FieldValidator interface. The 
com.moozvine.detox.validators package contains a bunch of useful general purpose validators you can use in your classes,
but if you want something specific, you can, of course, just implement your own FieldValidator,

```java
@GenerateBuilder
public interface User {
  @Id long getId();
  String getName();
  InternetAddress getInternetAddress();
  @Nullable @Validate(BeforeNow.class) DateTime getDateOfBirth();
}

public class BeforeNow implements FieldValidator<DateTime> {
  @Override
  public void validate(String fieldName, DateTime value) {
    if (!value.isBeforeNow()) {
      throw new IllegalArgumentException("Field " + fieldName + " must be before now, not: " + value);
    }
  }
}
```

Sometimes field validation is not enough and you want to impose some constraint across multiple fields in your object.
You can do this by declaring an InstanceValidator. This can be any class that implements the InstanceValidator<T> 
interface, but if it is only relevant to one particular value object it usually makes sense to make it a nested 
class of the value object itself,
 
```java
@GenerateBuilder(validator = User.Validator.class)
public interface User {
  @Id @Validate(PositiveLong.class) long getId();
  String getName();
  DateTime getAccountOpenedAt();
  @Nullable DateTime getAccountClosedAt();
  
  class Validator implements InstanceValidator<User> {
    @Override
    public void validate(final User user) {
      if (user.getAccountClosedAt() != null && !user.getAccountClosedAt().isAfter(user.getAccountOpenedAt())) {
        throw new IllegalArgumentException(
            "Cannot have a user with an account closing time before the account opening time");
      }
    }
  }
}
```

# Type Hierarchies

These just work as you would expect:

```java
@GenerateBuilder
public interface User {
  @Id long getId();
  String getName();
  @Nullable @Validate(BeforeNow.class) DateTime getDateOfBirth();
}

@GenerateBuilder
public interface PayingUser extends User {
  PaymentPlan getPaymentPlan();
}
```

and the generated builder will ensure that all mandatory fields are set first, presenting the fields of the supertype
before the subtype, and then presenting the optional fields,

```java
    User payingUser = PayingUserBuilder.newBuilder()
        .withId(someId)
        .withName(someName)
        .withPaymentPlan(aPaymentPlan)
        .withDateOfBirth(dateOfBirth)
        .build()
```

Note that it is impossible to add a field which forms part an object's identity to a subtype without breaking the 
contract of the equals method. Therefore it is an error to have an @Id annotation on a field of a subtype if an @Id 
field is already declared on a type higher up the hierarchy.


# Usage
To use this magic, all you need to do is:
* Ensure the detox.jar is in your classpath
* Set-up your IDE to enable annotation processing and put the generated classes in your project's generated sources 
root

# Design Choices

## Why Interfaces?
In the above example, we modelled our User value object as an interface. This might be slightly surprising if you are
use to seeing value objects as classes, but it has some significant advantages. Decoupling the definition of what 
our value object _is_ from any particular implementation of it, is not only nice from a theoretical point of view, 
but it allows other specialized implementations to exist side-by-side in your codebase. For example,

An implementation designed to support JSON serialization for passing objects back and forth to the client. The code 
responsible for deserializing the request needs only return an instance of the User interface, rather than 
being forced to return a particular implementation: 

```java
User fromClient = serializationService.fromRequest(httpServletRequest);
```

An implementation designed for persistence in a database. Whatever persistence framework your project is using, you 
will typically have to implement a class to represent the stored data; with the framework often imposing 
onerous constraints on the design of the class: e.g. a public no-arg constructor (and, consequently, non-final 
fields), a non-final class, etc. It is then common to map back and forth between these persistence classes and the 
data-transfer classes, resulting in a a proliferation of mapping code for each entity. Detox, by using interfaces to 
define its value objects, allows the persistence classes to simply _implement_ the value object interface in the 
normal way,  

```java 
@com.googlecode.objectify.annotation.Entity
@com.googlecode.objectify.annotation.Cache
public final class UserEntity implements User {
  // All the persistence ugliness...
...
}
```

and the resulting object returned from your persistence code can be used seemlessly throughout your code together 
with other implementations of User.

It's important to note that validation is also defined on the _interface_ rather than on specific implementations. 
Conceptually, this means that the validation rules defined by the interface define what is permissible for any instance
of that value object. Detox's implementations will always make sure they obey the validation rules, if you implement 
your own implementations of your value objects you should ensure that they also either invoke the validators 
(recommended), or enforce conformity in some other way. 

This freedom comes from defining a value object as _what it is_ rather than _how it is implemented_.

## Perfect Compile-Time Validated Builders
A core design goal throughout computer science is to push error detection as close to edit-time as possible. Editing 
> Compiling > Testing > Running. The earlier up that stack we can detect errors, the more efficient our whole 
development process. That's why testing is better than not testing and why strongly (statically) typed languages are 
better than weakly (or dynamically) typed ones — because they allow compilers and editors to detect errors earlier. 
(It's also why writing server-side code in JS is an extremely bad idea.)  

Detox builders follow this principle by ensuring at compile/edit time that all mandatory (non-nullable) fields on a 
value object are set. It does this by having each of the fluent setter methods return a type that does not expose the 
build method until all the mandatory fields are set. In the above example, 
* UserBuilder.newBuilder() return a type  with a single method: withId 
* The withId method returns a type with a single method: withName 
* The withName method returns a type with a single method: withEmailAddress
* Because dateOfBirth is optional (nullable), the withEmailAddress method returns a type with both withDateOfBirth 
and build methods.

This makes writing instance-creation code in a modern IDE an extremely elegant, guided process: the IDE suggests each
method to you, telling you exactly what you need to supply for each field, and ensure you don't miss any mandatory 
fields.
  
More importantly, as the code evolves, if mandatory fields are added, existing instance-creation code that doesn't 
provide the new field will fail at compile-time rather than leading to unexpected runtime behaviour.  



# JSON Serialization
(Documentation coming soon)


# Comparison With Similar Projects

## AutoValue
[AutoValue](https://github.com/google/auto/tree/master/value) is an excellent project created by some really smart 
guys with very similar goals to Detox. A key difference is that where Detox uses interfaces to define the value 
object, including its validation rules, with AutoValue you write an abstract class which it then extends with the 
boilerplate methods.

We fundamentally believe that the definition of _what is_ your value object, including it's validation, belongs in an 
interface and that the tool should be responsible for creating beautiful, easy to use implementations of that interface.
   
AutoValue, however, is much more mature than Detox and has been used in a much wider range of projects. If you don't 
object to their approach or you want something less bleeding-edge, AutoValue should be at the top of your list.
  
## Lombok
[Lombok](https://projectlombok.org) is much more long-standing than either AutoValue or Detox. It's also much more 
controversial. (TODO: the long list of reasons not to use Lombok)
  


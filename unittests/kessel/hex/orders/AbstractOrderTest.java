package kessel.hex.orders;

import static org.junit.Assert.assertEquals;

/** A base class for all order testing. */
public abstract class AbstractOrderTest<X extends Order>
{
  public void doEqualsTest( X a, X b ) throws Exception
  {
    assertEquals( a.getSubject().getId(), b._jsonSubjectId.intValue() );
  }
}

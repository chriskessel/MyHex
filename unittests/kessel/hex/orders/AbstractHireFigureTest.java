package kessel.hex.orders;

import static org.junit.Assert.assertEquals;

/** Test hiring figures. */
public class AbstractHireFigureTest<X extends AbstractHireFigure> extends AbstractOrderTest<X>
{
  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getBase().getId(), b._jsonBaseId.intValue() );
  }
}

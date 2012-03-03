package kessel.hex.orders;

import static org.junit.Assert.assertEquals;

/** Test moving figures. */
public class AbstractMoveFigureTest<X extends AbstractMoveFigure> extends AbstractOrderTest<X>
{
  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getNewBase().getId(), b._jsonNewBaseId.intValue() );
  }
}

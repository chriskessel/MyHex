package kessel.hex.orders.agent;

import kessel.hex.orders.AbstractOrderTest;

import static org.junit.Assert.assertEquals;

/** Test figure assassination. */
public class AbstractAssassinateFigureTest<X extends AbstractAssassinateFigure> extends AbstractOrderTest<X>
{
  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._target.getId(), b._jsonTargetId.intValue() );
    assertEquals( a._targetBase.getId(), b._jsonTargetBaseId.intValue() );
  }
}

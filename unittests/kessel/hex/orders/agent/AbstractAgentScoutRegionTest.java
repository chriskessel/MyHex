package kessel.hex.orders.agent;

import kessel.hex.orders.AbstractOrderTest;

import static org.junit.Assert.assertEquals;

/** Test agent regional recon. */
public class AbstractAgentScoutRegionTest<X extends AbstractAgentScoutRegion> extends AbstractOrderTest<X>
{
  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._regionalCity.getId(), b._jsonCityId.intValue() );
  }
}

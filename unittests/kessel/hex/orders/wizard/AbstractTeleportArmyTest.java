package kessel.hex.orders.wizard;

import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import static org.junit.Assert.assertEquals;

public class AbstractTeleportArmyTest<X extends AbstractTeleportArmy> extends AbstractOrderTest<X>
{
  private static final Logger LOG = Logger.getLogger( AbstractTeleportArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getDestination(), b.getDestination() );
  }
}
